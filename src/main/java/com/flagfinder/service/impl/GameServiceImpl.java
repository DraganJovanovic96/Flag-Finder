package com.flagfinder.service.impl;

import com.flagfinder.dto.*;
import com.flagfinder.enumeration.Continent;
import com.flagfinder.enumeration.GameStatus;
import com.flagfinder.enumeration.RoomStatus;
import com.flagfinder.mapper.GameMapper;
import com.flagfinder.mapper.RoundMapper;
import com.flagfinder.mapper.SinglePlayerGameMapper;
import com.flagfinder.mapper.SinglePlayerRoundMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final SinglePlayerGameRepository singlePlayerGameRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final SinglePlayerRoomRepository singlePlayerRoomRepository;
    private final CountryRepository countryRepository;
    private final CountryService countryService;
    private final RoundRepository roundRepository;
    private final SinglePlayerRoundRepository singlePlayerRoundRepository;
    private final GuessRepository guessRepository;
    
    private final GameTimerService gameTimerService;
    private final UserService userService;
    private final GameMapper gameMapper;
    private final SinglePlayerGameMapper singlePlayerGameMapper;
    private final RoundMapper roundMapper;
    private final SinglePlayerRoundMapper singlePlayerRoundMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    private static final int TOTAL_ROUNDS = 3;
    private static final int ROUND_DURATION_SECONDS = 12;
    private static final int TOTAL_RECENT_GAMES = 10;
    private static final String QUEUE_ROUND_STARTED = "/queue/round-started";
    private static final String QUEUE_GAME_STARTED = "/queue/game-started";
    private static final String QUEUE_GAME_ENDED = "/queue/game-ended";
    private static final String GAME_NOT_FOUND = "Game not found";

    
    @Override
    public Game getGame(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }
    
    @Override
    public List<CompletedGameDto> getGamesByUser() {
        String userName = userService.getUserFromAuthentication().getGameName();

        return gameRepository.findAllMultiplayerByUser(userName).stream()
                .map(gameMapper::gameToCompletedGameDto)
                .toList();
    }
    
    @Override
    public Page<CompletedGameDto> getGamesByUser(Integer page, Integer pageSize) {
        String userName = userService.getUserFromAuthentication().getGameName();
        Page<Game> resultPage = gameRepository.findAllMultiplayerByUser(userName, PageRequest.of(page, pageSize));
        List<Game> games = resultPage.getContent();
        
        List<CompletedGameDto> completedGameDtos = games.stream()
                .map(gameMapper::gameToCompletedGameDto)
                .toList();
        
        return new PageImpl<>(completedGameDtos, resultPage.getPageable(), resultPage.getTotalElements());
    }
    
    @Override
    public Long getWonGamesCount() {
        String userName = userService.getUserFromAuthentication().getGameName();
        return gameRepository.countWonGamesByUser(userName);
    }
    
    @Override
    public Long getDrawGamesCount() {
        String userName = userService.getUserFromAuthentication().getGameName();
        return gameRepository.countDrawGamesByUser(userName);
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
        game.setTotalRounds(room.getNumberOfRounds());
        
        Game savedGame = gameRepository.save(game);
        
        room.setStatus(com.flagfinder.enumeration.RoomStatus.GAME_IN_PROGRESS);
        roomRepository.save(room);
        
        startNewRound(savedGame, 1, continents);

        GameDto gameDto = gameMapper.gameToGameDto(savedGame);

        populateCurrentRoundData(gameDto, savedGame);
        
        try {
            messagingTemplate.convertAndSendToUser(
                    room.getHost().getGameName(),
                    QUEUE_GAME_STARTED,
                    gameDto
            );
        } catch (Exception e) {
            log.error("Failed to send game-started notification to host {}: {}", room.getHost().getGameName(), e.getMessage(), e);
        }
        
        try {
            messagingTemplate.convertAndSendToUser(
                    room.getGuest().getGameName(),
                    QUEUE_GAME_STARTED,
                    gameDto
            );
        } catch (Exception e) {
            log.error("Failed to send game-started notification to guest {}: {}", room.getGuest().getGameName(), e.getMessage(), e);
        }
        
        return gameDto;
    }

    @Override
    public SinglePlayerGameDto startSinglePlayerGame(UUID roomId, List<Continent> continents) {
        SinglePlayerRoom singlePlayerRoom = singlePlayerRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (singlePlayerRoom.getHost() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room doesn't have a host");
        }

        if (singlePlayerRoom.getStatus() != com.flagfinder.enumeration.RoomStatus.ROOM_READY_FOR_START) {
            singlePlayerRoom.setStatus(com.flagfinder.enumeration.RoomStatus.ROOM_READY_FOR_START);
            singlePlayerRoomRepository.save(singlePlayerRoom);
        }

        SinglePlayerGame singlePlayerGame = new SinglePlayerGame();
        singlePlayerGame.setSinglePlayerRoom(singlePlayerRoom);
        singlePlayerGame.setUser(singlePlayerRoom.getHost());
        singlePlayerGame.setHostScore(0);
        singlePlayerGame.setStatus(GameStatus.IN_PROGRESS);
        singlePlayerGame.setStartedAt(LocalDateTime.now());
        singlePlayerGame.setContinents(continents != null ? continents : new ArrayList<>());
        singlePlayerGame.setTotalRounds(singlePlayerRoom.getNumberOfRounds());

        singlePlayerGameRepository.save(singlePlayerGame);

        singlePlayerRoom.setStatus(com.flagfinder.enumeration.RoomStatus.GAME_IN_PROGRESS);
        singlePlayerRoomRepository.save(singlePlayerRoom);

        startNewSinglePlayerRound(singlePlayerGame, 1, continents);

        SinglePlayerGameDto singlePlayerGameDto = singlePlayerGameMapper.singlePlayerGameToSinglePlayerGameDto(singlePlayerGame);

        populateCurrentSinglePlayerRoundData(singlePlayerGameDto, singlePlayerGame);

        try {
            messagingTemplate.convertAndSendToUser(
                    singlePlayerRoom.getHost().getGameName(),
                    QUEUE_GAME_STARTED,
                    singlePlayerGameDto
            );
        } catch (Exception e) {
            log.error("Failed to send game-started notification to host {}: {}", singlePlayerRoom.getHost().getGameName(), e.getMessage(), e);
        }

        return singlePlayerGameDto;
    }

    @Override
    @Transactional
    public GuessResponseDto submitGuess(GuessRequestDto guessRequest) {
        Game game = null;
        SinglePlayerGame singlePlayerGame = null;
        
        try {
            game = gameRepository.findByIdWithRelations(guessRequest.getGameId())
                    .orElse(null);
        } catch (Exception e) {
        }
        
        if (game == null) {
            singlePlayerGame = singlePlayerGameRepository.findByIdWithRelations(guessRequest.getGameId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, GAME_NOT_FOUND));
            
            if (singlePlayerGame.getStatus() != GameStatus.IN_PROGRESS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is not in progress");
            }
            
            return submitSinglePlayerGuess(guessRequest, singlePlayerGame);
        }
        
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, GAME_NOT_FOUND));
        
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, GAME_NOT_FOUND));
        
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

        try {
            messagingTemplate.convertAndSendToUser(
                    room.getHost().getGameName(),
                    QUEUE_GAME_ENDED,
                    gameDto
            );
        } catch (Exception e) {
            log.error("Failed to send game-ended notification to host {}: {}", room.getHost().getGameName(), e.getMessage(), e);
        }
        
        try {
            messagingTemplate.convertAndSendToUser(
                    room.getGuest().getGameName(),
                    QUEUE_GAME_ENDED,
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
        
        log.info("Cancelling existing timers for game {}", game.getId());
        gameTimerService.cancelGameTimers(game.getId());
        roundRepository.save(round);
        log.info("Starting timer for game {} round {} with duration {} seconds", game.getId(), roundNumber, ROUND_DURATION_SECONDS);
        gameTimerService.startRoundTimer(game.getId(), roundNumber, ROUND_DURATION_SECONDS);
        
        Game refreshedGame = gameRepository.findByIdWithRelations(game.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, GAME_NOT_FOUND));
        
        Hibernate.initialize(refreshedGame.getUsers());
        Hibernate.initialize(refreshedGame.getRounds());
        if (refreshedGame.getRoom() != null) {
            Hibernate.initialize(refreshedGame.getRoom());
        }
        
        GameDto gameDto = gameMapper.gameToGameDto(refreshedGame);
        populateCurrentRoundData(gameDto, refreshedGame);
        Room room = refreshedGame.getRoom();

        log.info("Sending WebSocket notifications for round {} start to host {} and guest {}",
                roundNumber, room.getHost().getGameName(), room.getGuest().getGameName());
        try {
            messagingTemplate.convertAndSendToUser(
                    room.getHost().getGameName(),
                    QUEUE_ROUND_STARTED,
                    gameDto
            );
            log.info("Successfully sent round-started notification to host {} for round {}", room.getHost().getGameName(), roundNumber);
        } catch (Exception e) {
            log.error("Failed to send round-started notification to host {}: {}", room.getHost().getGameName(), e.getMessage(), e);
        }
        
        try {
            messagingTemplate.convertAndSendToUser(
                    room.getGuest().getGameName(),
                    QUEUE_ROUND_STARTED,
                    gameDto
            );
            log.info("Successfully sent round-started notification to guest {} for round {}", room.getGuest().getGameName(), roundNumber);
        } catch (Exception e) {
            log.error("Failed to send round-started notification to guest {}: {}", room.getGuest().getGameName(), e.getMessage(), e);
        }
    }

    private void startNewSinglePlayerRound(SinglePlayerGame singlePlayerGame, int roundNumber, List<com.flagfinder.enumeration.Continent> continents) {

        Country randomCountry;
        if (continents != null && !continents.isEmpty()) {
            randomCountry = countryService.getRandomCountryFromAnyContinents(continents);
        } else {
            randomCountry = countryService.getRandomCountryFromAnyContinents(null);
        }

        SinglePlayerRound singlePlayerRound = new SinglePlayerRound();
        singlePlayerRound.setSinglePlayerGame(singlePlayerGame);
        singlePlayerRound.setCountry(randomCountry);
        singlePlayerRound.setRoundNumber(roundNumber);

        gameTimerService.cancelGameTimers(singlePlayerGame.getId());
        singlePlayerRoundRepository.save(singlePlayerRound);
        gameTimerService.startRoundTimer(singlePlayerGame.getId(), roundNumber, ROUND_DURATION_SECONDS);

        SinglePlayerGame refreshedGame = singlePlayerGameRepository.findByIdWithRelations(singlePlayerGame.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, GAME_NOT_FOUND));

        Hibernate.initialize(refreshedGame.getUser());
        Hibernate.initialize(refreshedGame.getRounds());
        if (refreshedGame.getSinglePlayerRoom() != null) {
            Hibernate.initialize(refreshedGame.getSinglePlayerRoom());
        }

        SinglePlayerGameDto singlePlayerGameDto = singlePlayerGameMapper.singlePlayerGameToSinglePlayerGameDto(refreshedGame);
        populateCurrentSinglePlayerRoundData(singlePlayerGameDto, refreshedGame);
        SinglePlayerRoom singlePlayerRoom = refreshedGame.getSinglePlayerRoom();

        try {
            messagingTemplate.convertAndSendToUser(
                    singlePlayerRoom.getHost().getGameName(),
                    QUEUE_ROUND_STARTED,
                    singlePlayerGameDto
            );
        } catch (Exception e) {
            log.error("Failed to send round-started notification to host {}: {}", singlePlayerRoom.getHost().getGameName(), e.getMessage(), e);
        }
    }
    
    private void endCurrentRound(Game game, int roundNumber) {
        log.info("Ending round {} for game {}, total rounds: {}", roundNumber, game.getId(), game.getTotalRounds());
        
        if (roundNumber < game.getTotalRounds()) {
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
                Hibernate.initialize(game.getUsers());
                Hibernate.initialize(game.getRounds());
                if (game.getRoom() != null) {
                    Hibernate.initialize(game.getRoom());
                }
                endCurrentRound(game, roundNumber);
                return;
            }

            SinglePlayerGame singlePlayerGame = singlePlayerGameRepository.findByIdWithRelations(gameId).orElse(null);
            if (singlePlayerGame != null && singlePlayerGame.getStatus() == GameStatus.IN_PROGRESS) {
                Hibernate.initialize(singlePlayerGame.getUser());
                Hibernate.initialize(singlePlayerGame.getRounds());
                if (singlePlayerGame.getSinglePlayerRoom() != null) {
                    Hibernate.initialize(singlePlayerGame.getSinglePlayerRoom());
                }
                endCurrentSinglePlayerRound(singlePlayerGame, roundNumber);
            }
        } catch (Exception e) {
            log.error("Error handling round timeout for game {} round {}", gameId, roundNumber, e);
        }
    }
    
    private void endCurrentSinglePlayerRound(SinglePlayerGame singlePlayerGame, int roundNumber) {
        log.info("Ending single player round {} for game {}, total rounds: {}", roundNumber, singlePlayerGame.getId(), singlePlayerGame.getTotalRounds());
        
        if (roundNumber < singlePlayerGame.getTotalRounds()) {
            log.info("Starting new single player round {} for game {}", roundNumber + 1, singlePlayerGame.getId());
            startNewSinglePlayerRound(singlePlayerGame, roundNumber + 1, singlePlayerGame.getContinents());
        } else {
            log.info("Final single player round completed, ending game {}", singlePlayerGame.getId());
            endSinglePlayerGame(singlePlayerGame.getId());
        }
    }
    
    private void endSinglePlayerGame(UUID gameId) {
        log.info("Ending single player game {}", gameId);
        
        SinglePlayerGame singlePlayerGame = singlePlayerGameRepository.findByIdWithRelations(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Single player game not found"));
        
        Hibernate.initialize(singlePlayerGame.getUser());
        Hibernate.initialize(singlePlayerGame.getRounds());
        if (singlePlayerGame.getSinglePlayerRoom() != null) {
            Hibernate.initialize(singlePlayerGame.getSinglePlayerRoom());
        }
        
        singlePlayerGame.setStatus(GameStatus.COMPLETED);
        singlePlayerGame.setEndedAt(LocalDateTime.now());
        log.info("Single player game {} set to COMPLETED status", gameId);

        SinglePlayerRoom singlePlayerRoom = singlePlayerGame.getSinglePlayerRoom();
        if (singlePlayerRoom != null) {
            singlePlayerRoom.setStatus(RoomStatus.GAME_COMPLETED);
            singlePlayerRoomRepository.save(singlePlayerRoom);
        }
        gameTimerService.cancelGameTimers(gameId);
        
        singlePlayerGameRepository.save(singlePlayerGame);
        
        SinglePlayerGameDto singlePlayerGameDto = singlePlayerGameMapper.singlePlayerGameToSinglePlayerGameDto(singlePlayerGame);
        
        try {
            messagingTemplate.convertAndSendToUser(
                    singlePlayerRoom.getHost().getGameName(),
                    QUEUE_GAME_ENDED,
                    singlePlayerGameDto
            );
            log.info("Successfully sent game-ended notification to host {} for single player game {}", 
                    singlePlayerRoom.getHost().getGameName(), gameId);
        } catch (Exception e) {
            log.error("Failed to send game-ended notification to host {}: {}", 
                    singlePlayerRoom.getHost().getGameName(), e.getMessage(), e);
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

    private void populateCurrentSinglePlayerRoundData(SinglePlayerGameDto dto, SinglePlayerGame game) {
        if (!game.getRounds().isEmpty()) {
            SinglePlayerRound currentSinglePlayerRound = null;

            for (SinglePlayerRound round : game.getRounds()) {
                if (gameTimerService.isRoundActive(game.getId(), round.getRoundNumber())) {
                    currentSinglePlayerRound = round;
                    break;
                }
            }
            
            if (currentSinglePlayerRound == null) {
                currentSinglePlayerRound = game.getRounds().stream()
                        .max((r1, r2) -> Integer.compare(r1.getRoundNumber(), r2.getRoundNumber()))
                        .orElse(game.getRounds().get(0));
            }

            dto.setCurrentRound(currentSinglePlayerRound.getRoundNumber());

            SinglePlayerRoundDto roundDto = singlePlayerRoundMapper.singlePlayerRoundToSinglePlayerRoundDto(currentSinglePlayerRound);
            boolean isRoundActive = gameTimerService.isRoundActive(game.getId(), currentSinglePlayerRound.getRoundNumber());
            Long timeRemaining = gameTimerService.getRemainingTime(game.getId(), currentSinglePlayerRound.getRoundNumber());
            
            roundDto.setRoundActive(isRoundActive);
            roundDto.setTimeRemaining(timeRemaining);

            dto.setCurrentSinglePlayerRoundData(roundDto);
        }
    }
    
    @Override
    @Transactional
    public List<Round> getGameRounds(UUID gameId) {
        gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, GAME_NOT_FOUND));

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
        Optional<Game> multiplayerGame = gameRepository.findById(gameId);
        if (multiplayerGame.isPresent()) {
            List<Round> rounds = roundRepository.findByGameIdOrderByRoundNumber(gameId);
            return mapMultiplayerRoundsToSummary(rounds);
        }
        
        Optional<SinglePlayerGame> singlePlayerGame = singlePlayerGameRepository.findById(gameId);
        if (singlePlayerGame.isPresent()) {
            List<SinglePlayerRound> rounds = singlePlayerRoundRepository.findByGameIdOrderByRoundNumber(gameId);
            return mapSinglePlayerRoundsToSummary(rounds);
        }
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, GAME_NOT_FOUND);
    }
    
    private List<RoundSummaryDto> mapMultiplayerRoundsToSummary(List<Round> rounds) {
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
            }).toList();
            
            dto.setGuesses(guessDtos);
            return dto;
        }).toList();
    }
    
    private List<RoundSummaryDto> mapSinglePlayerRoundsToSummary(List<SinglePlayerRound> rounds) {
        return rounds.stream().map(round -> {
            RoundSummaryDto dto = new RoundSummaryDto();
            dto.setRoundNumber(round.getRoundNumber());

            CountryDto countryDto = new CountryDto();
            countryDto.setId(round.getCountry().getId());
            countryDto.setNameOfCounty(round.getCountry().getNameOfCounty());
            dto.setCountry(countryDto);

            List<GuessDto> guessDtos = new ArrayList<>();
            if (round.getGuess() != null) {
                GuessDto guessDto = new GuessDto();
                guessDto.setUserGameName(round.getGuess().getUser().getGameName());
                guessDto.setCorrect(round.getGuess().isCorrect());
                if (round.getGuess().getGuessedCountry() != null) {
                    guessDto.setGuessedCountryName(round.getGuess().getGuessedCountry().getNameOfCounty());
                }
                guessDtos.add(guessDto);
            }
            
            dto.setGuesses(guessDtos);
            return dto;
        }).toList();
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
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getValue().getGuessedCountry().equals(entry.getKey().getCountry()))
                .count();

        int totalGuesses = rounds.size();

        return totalGuesses > 0
                    ? (int) ((correctGuesses * 100.0) / totalGuesses)
                    : 0;
    }

    @Override
    public int getBestWinningStreak(String userName) {
        List<Game> allGames = gameRepository.findByUserAndStatusOrderByGameEndedAtAsc(userName, GameStatus.COMPLETED);
        
        if (allGames.isEmpty()) {
            return 0;
        }
        
        int maxStreak = 0;
        int currentStreak = 0;
        
        for (Game game : allGames) {
            if (userName.equalsIgnoreCase(game.getWinnerUserName())) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }
        
        return maxStreak;
    }

    private GuessResponseDto submitSinglePlayerGuess(GuessRequestDto guessRequest, SinglePlayerGame singlePlayerGame) {
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        SinglePlayerRound currentRound = singlePlayerGame.getRounds().stream()
                .filter(round -> round.getRoundNumber().equals(guessRequest.getRoundNumber()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));
        
        if (currentRound.getGuess() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already guessed in this round");
        }
        Country guessedCountry = countryRepository.findByNameOfCountyIgnoreCase(guessRequest.getGuessedCountryName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid country name: " + guessRequest.getGuessedCountryName()));

        Guess guess = new Guess();
        guess.setUser(currentUser);
        guess.setGuessedCountry(guessedCountry);
        guess.setSinglePlayerRound(currentRound);

        boolean isCorrect = guessedCountry.equals(currentRound.getCountry());
        guess.setCorrect(isCorrect);

        guessRepository.save(guess);
        currentRound.setGuess(guess);
        singlePlayerRoundRepository.save(currentRound);
        
        if (isCorrect) {
            singlePlayerGame.setHostScore(singlePlayerGame.getHostScore() + 1);
        }

        int currentRoundNumber = guessRequest.getRoundNumber();
        UUID gameId = singlePlayerGame.getId();
        List<Continent> continents = new ArrayList<>(singlePlayerGame.getContinents());

        java.util.concurrent.Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            transactionTemplate.execute(status -> {
                try {
                    if (currentRoundNumber < singlePlayerGame.getTotalRounds()) {
                        SinglePlayerGame game = singlePlayerGameRepository.findByIdWithRelations(gameId)
                                .orElse(null);
                        if (game != null && game.getStatus() == GameStatus.IN_PROGRESS) {
                            startNewSinglePlayerRound(game, currentRoundNumber + 1, continents);
                        }
                    } else {
                        SinglePlayerGame game = singlePlayerGameRepository.findById(gameId).orElse(null);
                        if (game != null) {
                            game.setStatus(GameStatus.COMPLETED);
                            game.setEndedAt(LocalDateTime.now());
                            singlePlayerGameRepository.save(game);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error in delayed single player game progression for game {}", gameId, e);
                }
                return null;
            });
        }, 1, java.util.concurrent.TimeUnit.SECONDS);

        singlePlayerGameRepository.save(singlePlayerGame);
        
        SinglePlayerGame refreshedGame = singlePlayerGameRepository.findByIdWithRelations(singlePlayerGame.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, GAME_NOT_FOUND));

        SinglePlayerGameDto gameDto = singlePlayerGameMapper.singlePlayerGameToSinglePlayerGameDto(refreshedGame);
        populateCurrentSinglePlayerRoundData(gameDto, refreshedGame);

        GuessResponseDto response = new GuessResponseDto();
        response.setCorrect(isCorrect);
        response.setMessage(isCorrect ? "Correct!" : "Incorrect. The correct answer was " + currentRound.getCountry().getNameOfCounty());
        response.setPointsAwarded(isCorrect ? 1 : 0);
        response.setGame(gameDto);

        return response;
    }

    @Override
    public SinglePlayerGameDto getSinglePlayerGameByRoom(UUID roomId) {
        SinglePlayerRoom singlePlayerRoom = singlePlayerRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Single player room not found"));
        
        SinglePlayerGame singlePlayerGame = singlePlayerGameRepository.findBySinglePlayerRoom(singlePlayerRoom)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Single player game not found for room"));
        
        Hibernate.initialize(singlePlayerGame.getUser());
        Hibernate.initialize(singlePlayerGame.getRounds());
        if (singlePlayerGame.getSinglePlayerRoom() != null) {
            Hibernate.initialize(singlePlayerGame.getSinglePlayerRoom());
        }
        
        SinglePlayerGameDto singlePlayerGameDto = singlePlayerGameMapper.singlePlayerGameToSinglePlayerGameDto(singlePlayerGame);
        populateCurrentSinglePlayerRoundData(singlePlayerGameDto, singlePlayerGame);
        
        return singlePlayerGameDto;
    }

    @Override
    public SinglePlayerGameDto getSinglePlayerGameById(UUID gameId) {
        SinglePlayerGame singlePlayerGame = singlePlayerGameRepository.findByIdWithRelations(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Single player game not found"));
        
        Hibernate.initialize(singlePlayerGame.getUser());
        Hibernate.initialize(singlePlayerGame.getRounds());
        if (singlePlayerGame.getSinglePlayerRoom() != null) {
            Hibernate.initialize(singlePlayerGame.getSinglePlayerRoom());
        }
        
        SinglePlayerGameDto singlePlayerGameDto = singlePlayerGameMapper.singlePlayerGameToSinglePlayerGameDto(singlePlayerGame);
        populateCurrentSinglePlayerRoundData(singlePlayerGameDto, singlePlayerGame);
        
        return singlePlayerGameDto;
    }
}
