package com.flagfinder.service.impl;

import com.flagfinder.dto.*;
import com.flagfinder.enumeration.GameStatus;
import com.flagfinder.enumeration.RoomStatus;
import com.flagfinder.mapper.GameMapper;
import com.flagfinder.mapper.RoundMapper;
import com.flagfinder.model.*;
import com.flagfinder.repository.*;
import com.flagfinder.service.CountryService;
import com.flagfinder.service.GameService;
import com.flagfinder.service.GameTimerService;
import com.flagfinder.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements GameService {
    
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final CountryRepository countryRepository;
    private final CountryService countryService;
    private final RoundRepository roundRepository;
    private final GuessRepository guessRepository;
    
    private final GameTimerService gameTimerService;
    private final UserService userService;
    private final GameMapper gameMapper;
    private final RoundMapper roundMapper;
    private final SimpMessagingTemplate messagingTemplate;
    
    private static final int TOTAL_ROUNDS = 3;
    private static final int ROUND_DURATION_SECONDS = 8;
    private static final int TOTAL_RECENT_GAMES = 10;
    
    @Override
    public Game getGame(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }
    
    @Override
    public List<CompletedGameDto> getGamesByUser() {
        String userName = userService.getUserFromAuthentication().getGameName();

        return gameRepository.findAll().stream()
                .filter(game -> game.getUsers().stream()
                        .anyMatch(user -> userName.equals(user.getGameName())))
                .map(gameMapper::gameToCompletedGameDto)
                .toList();
    }
    
    @Override
    public List<Game> getAllCompletedGames() {
        return gameRepository.findAll().stream()
                .filter(game -> GameStatus.COMPLETED.equals(game.getStatus()))
                .toList();
    }
    
    @Override
    @Transactional
    public synchronized GameDto startGame(UUID roomId, List<com.flagfinder.enumeration.Continent> continents) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        
        if (room.getHost() == null || room.getGuest() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room must have exactly 2 players to start game");
        }
        
        Game existingGame = gameRepository.findByRoomAndStatus(room, GameStatus.IN_PROGRESS);
        if (existingGame != null) {
            return gameMapper.gameToGameDto(existingGame);
        }
        
        if (room.getStatus() != com.flagfinder.enumeration.RoomStatus.ROOM_READY_FOR_START) {
            room.setStatus(com.flagfinder.enumeration.RoomStatus.ROOM_READY_FOR_START);
            room = roomRepository.save(room);
        }
        
        Game game = new Game();
        game.setRoom(room);
        game.setUsers(Arrays.asList(room.getHost(), room.getGuest()));
        game.setHostScore(0);
        game.setGuestScore(0);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now());
        game.setContinents(continents != null ? continents : new ArrayList<>());
        game.setTotalRounds(TOTAL_ROUNDS);
        
        Game savedGame = gameRepository.save(game);
        
        room.setStatus(com.flagfinder.enumeration.RoomStatus.GAME_IN_PROGRESS);
        roomRepository.save(room);
        
        startNewRound(savedGame, 1, continents);

        GameDto gameDto = gameMapper.gameToGameDto(savedGame);

        System.out.println(gameDto.getTotalRounds());
        populateCurrentRoundData(gameDto, savedGame);
        
        try {
            messagingTemplate.convertAndSendToUser(
                    room.getHost().getGameName(),
                    "/queue/game-started",
                    gameDto
            );
        } catch (Exception e) {
            log.error("Failed to send game-started notification to host {}: {}", room.getHost().getGameName(), e.getMessage(), e);
        }
        
        try {
            messagingTemplate.convertAndSendToUser(
                    room.getGuest().getGameName(),
                    "/queue/game-started",
                    gameDto
            );
        } catch (Exception e) {
            log.error("Failed to send game-started notification to guest {}: {}", room.getGuest().getGameName(), e.getMessage(), e);
        }
        
        return gameDto;
    }
    
    @Override
    @Transactional
    public GuessResponseDto submitGuess(GuessRequestDto guessRequest) {
        Game game = gameRepository.findByIdWithRelations(guessRequest.getGameId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is not in progress");
        }
        
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        Round currentRound = game.getRounds().stream()
                .filter(round -> round.getRoundNumber().equals(guessRequest.getRoundNumber()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));
        
        boolean alreadyGuessed = currentRound.getGuesses().stream()
                .anyMatch(guess -> guess.getUser().equals(currentUser));
        
        if (alreadyGuessed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already guessed in this round");
        }
        
        Country guessedCountry = countryRepository.findByNameOfCountyIgnoreCase(guessRequest.getGuessedCountryName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid country name: " + guessRequest.getGuessedCountryName()));
        
        Guess guess = new Guess();
        guess.setUser(currentUser);
        guess.setRound(currentRound);
        guess.setGuessedCountry(guessedCountry);
        guess.setCorrect(guessedCountry.equals(currentRound.getCountry()));
        
        guessRepository.save(guess);
        
        if (guess.isCorrect()) {
            updateScore(game, currentUser);
        }

        log.info("Round {} has {} guesses", currentRound.getRoundNumber(), currentRound.getGuesses().size());
        
        if (currentRound.getGuesses().size() >= 2 || shouldEndRound(currentRound)) {
            log.info("Ending round {} - both players guessed or timeout", currentRound.getRoundNumber());
            // Explicitly initialize collections before calling endCurrentRound
            Hibernate.initialize(game.getUsers());
            Hibernate.initialize(game.getRounds());
            if (game.getRoom() != null) {
                Hibernate.initialize(game.getRoom());
            }
            endCurrentRound(game, currentRound.getRoundNumber());
        } else {
            log.info("Round {} continues - only {} guesses so far", currentRound.getRoundNumber(), currentRound.getGuesses().size());
        }
        
        GameDto gameDto = gameMapper.gameToGameDto(gameRepository.save(game));
        populateCurrentRoundData(gameDto, game);
        
        // Create guess response with feedback
        GuessResponseDto response = new GuessResponseDto();
        response.setGame(gameDto);
        response.setCorrect(guess.isCorrect());
        response.setPointsAwarded(guess.isCorrect() ? 1 : 0);
        response.setCorrectCountryName(currentRound.getCountry().getNameOfCounty());
        
        if (guess.isCorrect()) {
            response.setMessage("Correct! Well done!");
        } else {
            response.setMessage("Incorrect. The correct answer was " + currentRound.getCountry().getNameOfCounty());
        }
        
        return response;
    }
    
    @Override
    @Transactional
    public GameDto getGameState(UUID gameId) {
        Game game = gameRepository.findByIdWithRelations(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        
        // Explicitly initialize collections
        Hibernate.initialize(game.getUsers());
        Hibernate.initialize(game.getRounds());
        if (game.getRoom() != null) {
            Hibernate.initialize(game.getRoom());
        }
        
        GameDto gameDto = gameMapper.gameToGameDto(game);
        populateCurrentRoundData(gameDto, game);
        return gameDto;
    }
    
    @Override
    @Transactional
    public GameDto endGame(UUID gameId) {
        Game game = gameRepository.findByIdWithRelations(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        
        // Explicitly initialize collections
        Hibernate.initialize(game.getUsers());
        Hibernate.initialize(game.getRounds());
        if (game.getRoom() != null) {
            Hibernate.initialize(game.getRoom());
        }
        
        game.setStatus(GameStatus.COMPLETED);
        game.setEndedAt(LocalDateTime.now());
        log.info("Game {} set to COMPLETED status", gameId);

        if (game.getHostScore() > game.getGuestScore()) {
            game.setWinnerUserName(game.getUsers().get(0).getGameName());
        } else if (game.getGuestScore() > game.getHostScore()) {
            game.setWinnerUserName(game.getUsers().get(1).getGameName());
        }

        Room room = game.getRoom();
        if (room != null) {
            room.setStatus(RoomStatus.GAME_COMPLETED);
            roomRepository.save(room);
        }
        gameTimerService.cancelGameTimers(gameId);
        
        GameDto gameDto = gameMapper.gameToGameDto(gameRepository.save(game));
        populateCurrentRoundData(gameDto, game);
        
        // Send WebSocket notification for game completion
        try {
            messagingTemplate.convertAndSendToUser(
                    room.getHost().getGameName(),
                    "/queue/game-ended",
                    gameDto
            );
        } catch (Exception e) {
            log.error("Failed to send game-ended notification to host {}: {}", room.getHost().getGameName(), e.getMessage(), e);
        }
        
        try {
            messagingTemplate.convertAndSendToUser(
                    room.getGuest().getGameName(),
                    "/queue/game-ended",
                    gameDto
            );
        } catch (Exception e) {
            log.error("Failed to send game-ended notification to guest {}: {}", room.getGuest().getGameName(), e.getMessage(), e);
        }
        
        return gameDto;
    }
    
    private void startNewRound(Game game, int roundNumber, List<com.flagfinder.enumeration.Continent> continents) {
        log.info("Starting new round {} for game {} with continents: {}", roundNumber, game.getId(), continents);
        
        Country randomCountry;
        if (continents != null && !continents.isEmpty()) {
            randomCountry = countryService.getRandomCountryFromAnyContinents(continents);
        } else {
            randomCountry = countryService.getRandomCountryFromAnyContinents(null);
        }

        Round round = new Round();
        round.setGame(game);
        round.setCountry(randomCountry);
        round.setRoundNumber(roundNumber);
        
        // Cancel any existing timers for this game before starting new one
        log.info("Cancelling existing timers for game {}", game.getId());
        gameTimerService.cancelGameTimers(game.getId());
        roundRepository.save(round);
        log.info("Starting timer for game {} round {} with duration {} seconds", game.getId(), roundNumber, ROUND_DURATION_SECONDS);
        gameTimerService.startRoundTimer(game.getId(), roundNumber, ROUND_DURATION_SECONDS);
        
        // Refresh game entity to include the new round with all relations
        Game refreshedGame = gameRepository.findByIdWithRelations(game.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        
        // Initialize collections for WebSocket notification
        Hibernate.initialize(refreshedGame.getUsers());
        Hibernate.initialize(refreshedGame.getRounds());
        if (refreshedGame.getRoom() != null) {
            Hibernate.initialize(refreshedGame.getRoom());
        }
        
        GameDto gameDto = gameMapper.gameToGameDto(refreshedGame);
        populateCurrentRoundData(gameDto, refreshedGame);
        Room room = refreshedGame.getRoom();
        
        // Send WebSocket notifications for round start
        log.info("Sending WebSocket notifications for round {} start to host {} and guest {}", 
                roundNumber, room.getHost().getGameName(), room.getGuest().getGameName());
        try {
            messagingTemplate.convertAndSendToUser(
                    room.getHost().getGameName(),
                    "/queue/round-started",
                    gameDto
            );
            log.info("Successfully sent round-started notification to host {} for round {}", room.getHost().getGameName(), roundNumber);
        } catch (Exception e) {
            log.error("Failed to send round-started notification to host {}: {}", room.getHost().getGameName(), e.getMessage(), e);
        }
        
        try {
            messagingTemplate.convertAndSendToUser(
                    room.getGuest().getGameName(),
                    "/queue/round-started",
                    gameDto
            );
            log.info("Successfully sent round-started notification to guest {} for round {}", room.getGuest().getGameName(), roundNumber);
        } catch (Exception e) {
            log.error("Failed to send round-started notification to guest {}: {}", room.getGuest().getGameName(), e.getMessage(), e);
        }
    }
    
    private void endCurrentRound(Game game, int roundNumber) {
        log.info("Ending round {} for game {}, total rounds: {}", roundNumber, game.getId(), TOTAL_ROUNDS);
        
        if (roundNumber < TOTAL_ROUNDS) {
            log.info("Starting new round {} for game {}", roundNumber + 1, game.getId());
            startNewRound(game, roundNumber + 1, game.getContinents());
        } else {
            log.info("Final round completed, ending game {}", game.getId());
            endGame(game.getId());
        }
    }
    @Transactional
    public void handleRoundTimeout(UUID gameId, Integer roundNumber) {
        try {
            log.info("Handling timeout for game {} round {}", gameId, roundNumber);
            Game game = gameRepository.findByIdWithRelations(gameId).orElse(null);
            if (game != null && game.getStatus() == GameStatus.IN_PROGRESS) {
                // Explicitly initialize lazy collections to prevent LazyInitializationException
                Hibernate.initialize(game.getUsers());
                Hibernate.initialize(game.getRounds());
                if (game.getRoom() != null) {
                    Hibernate.initialize(game.getRoom());
                }
                endCurrentRound(game, roundNumber);
            }
        } catch (Exception e) {
            log.error("Error handling round timeout for game {} round {}", gameId, roundNumber, e);
        }
    }
    
    private void updateScore(Game game, User user) {
        if (game.getUsers().get(0).equals(user)) {
            game.setHostScore(game.getHostScore() + 1);
        } else {
            game.setGuestScore(game.getGuestScore() + 1);
        }
    }
    
    private boolean shouldEndRound(Round round) {
        return round.getGuesses().size() >= 2;
    }
    
    private void populateCurrentRoundData(GameDto dto, Game game) {
        if (!game.getRounds().isEmpty()) {
            // Find the round with the highest round number (most recent round)
            Round currentRound = game.getRounds().stream()
                    .max((r1, r2) -> Integer.compare(r1.getRoundNumber(), r2.getRoundNumber()))
                    .orElse(game.getRounds().get(0));
            
            dto.setCurrentRound(currentRound.getRoundNumber());
            
            com.flagfinder.dto.RoundDto roundDto = roundMapper.roundToRoundDto(currentRound);
            roundDto.setRoundActive(gameTimerService.isRoundActive(game.getId(), currentRound.getRoundNumber()));
            roundDto.setTimeRemaining(gameTimerService.getRemainingTime(game.getId(), currentRound.getRoundNumber()));
            
            dto.setCurrentRoundData(roundDto);
        }
    }
    
    @Override
    @Transactional
    public List<Round> getGameRounds(UUID gameId) {
        gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        List<Round> rounds = roundRepository.findByGameIdOrderByRoundNumber(gameId);

        for (Round round : rounds) {
            Hibernate.initialize(round.getGuesses());
            Hibernate.initialize(round.getCountry());
            for (Guess guess : round.getGuesses()) {
                Hibernate.initialize(guess.getUser());
                Hibernate.initialize(guess.getGuessedCountry());
            }
        }
        
        return rounds;
    }
    
    @Override
    @Transactional
    public List<RoundSummaryDto> getGameRoundSummaries(UUID gameId) {
        gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        List<Round> rounds = roundRepository.findByGameIdOrderByRoundNumber(gameId);

        return rounds.stream().map(round -> {
            RoundSummaryDto dto = new RoundSummaryDto();
            dto.setRoundNumber(round.getRoundNumber());

            CountryDto countryDto = new CountryDto();
            countryDto.setId(round.getCountry().getId());
            countryDto.setNameOfCounty(round.getCountry().getNameOfCounty());
            dto.setCountry(countryDto);

            List<GuessDto> guessDtos = round.getGuesses().stream().map(guess -> {
                GuessDto guessDto = new GuessDto();
                guessDto.setUserGameName(guess.getUser().getGameName());
                guessDto.setCorrect(guess.isCorrect());
                if (guess.getGuessedCountry() != null) {
                    guessDto.setGuessedCountryName(guess.getGuessedCountry().getNameOfCounty());
                }
                return guessDto;
            }).collect(Collectors.toList());
            
            dto.setGuesses(guessDtos);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Long countOfWinningGames(String userName) {

        return gameRepository.countByWinnerUserNameIgnoreCase(userName);
    }

    @Override
    public int accuracyPercentage(String userName) {
        Pageable pageable = PageRequest.of(0, TOTAL_RECENT_GAMES);

        List<Game> games = gameRepository.findRecentGamesByUser(userName, pageable);

        List<Round> rounds = games.stream()
                .flatMap(game -> roundRepository.findByGameIdOrderByRoundNumber(game.getId()).stream())
                .toList();

        long correctGuesses = rounds.stream()
                .map(round -> new AbstractMap.SimpleEntry<>(round,
                        guessRepository.findOneByRoundIdAndGameName(round.getId(), userName)))
                .filter(entry -> entry.getValue() != null) // ignore rounds without a guess
                .filter(entry -> entry.getValue().getGuessedCountry().equals(entry.getKey().getCountry()))
                .count();

        int totalGuesses = rounds.size();

        return totalGuesses > 0
                    ? (int) ((correctGuesses * 100.0) / totalGuesses)
                    : 0;
        }
}
