package com.flagfinder.service.impl;

import com.flagfinder.dto.*;
import com.flagfinder.enumeration.Role;
import com.flagfinder.mapper.UserMapper;
import com.flagfinder.model.User;
import com.flagfinder.repository.FriendshipRepository;
import com.flagfinder.repository.TokenRepository;
import com.flagfinder.repository.UserCustomRepository;
import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.UserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the User interface.
 * <p>
 * Provides methods to manage user-related operations.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    /**
     * The repository used to retrieve user data.
     */
    private final UserRepository userRepository;

    private final ExtractAuthenticatedUserService extractAuthenticatedUserService;

    /**
     * The repository used to retrieve token data.
     */
    private final TokenRepository tokenRepository;

    /**
     * The repository used to retrieve token data.
     */
    private final UserCustomRepository userCustomRepository;

    private final FriendshipRepository friendshipRepository;
    /**
     * The mapper used to map user data.
     */
    private final UserMapper userMapper;

    /**
     * Service interface for encoding passwords. The preferred implementation is BCryptPasswordEncoder.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Created USER_FILTER attribute, so we can change Filter easily if needed.
     */
    private static final String USER_FILTER = "deletedUserFilter";

    /**
     * An EntityManager instance is associated with a persistence context.
     * A persistence context is a set of entity instances in which for any
     * persistent entity identity there is a unique entity instance.
     */
    private final EntityManager entityManager;

    /**
     * Retrieves a user entity by their email address.
     *
     * @param email The email address of the user.
     * @return The User entity with the specified email address.
     * @throws ResponseStatusException If a user with the specified email address is not found.
     */
    public User findOneByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User doesn't exist");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with that email doesn't exist"));
    }

    /**
     * Finds a user by their unique identifier.
     *
     * @param userId the unique identifier of the user to retrieve
     * @return a {@link UserDto} representing the found user
     */
    @Override
    public UserDto findUserById(UUID userId) {
        User user = userRepository.findOneById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this id doesn't exist"));

        return userMapper.userToUserDto(user);
    }

    /**
     * A method for retrieving all users implemented in UserServiceImpl class.
     *
     * @param isDeleted parameter that checks if object is soft deleted
     * @return a list of all UserDto's
     */
    @Override
    public List<UserDto> getAllUsers(boolean isDeleted) {
        List<User> users = userRepository.findAll();

        return userMapper.usersToUserDtos(users);
    }

    /**
     * Retrieves the username of the currently authenticated user from the Spring Security context.
     *
     * @return The username of the currently authenticated user.
     * @throws RuntimeException If the authentication object does not contain user details.
     */
    @Override
    public User getUserFromAuthentication() {
        String email = extractAuthenticatedUserService.getAuthenticatedUser();
        return findOneByEmail(email);
    }

    /**
     * Retrieves the username of the currently authenticated user from the Spring Security context.
     *
     * @return The username of the currently authenticated user.
     * @throws RuntimeException If the authentication object does not contain user details.
     */
    @Override
    public LocalStorageUserDto getLocalStorageUserDtoFromAuthentication() {
        User user = getUserFromAuthentication();
        return userMapper.userToLocalStorageUserDto(user);
    }

    /**
     * Retrieves the user associated with the current authentication context.
     *
     * @return The UserDto object representing the authenticated user.
     */
    @Override
    public UserUpdateDto getUserDtoFromAuthentication() {
        User user = getUserFromAuthentication();
        return userMapper.userToUserUpdateDto(user);
    }

    /**
     * Update the user associated with the current authentication context.
     *
     * @return The UserUpdateDto object representing the authenticated user with fewer details.
     */
    @Override
    public UserUpdateDto updateUser(UserUpdateDto userUpdateDto) {
        User user = getUserFromAuthentication();
        user.setFirstname(userUpdateDto.getFirstname());
        user.setLastname(userUpdateDto.getLastname());
        user.setEmail(userUpdateDto.getEmail());
        user.setImageUrl(userUpdateDto.getImageUrl());
        user.setMobileNumber(userUpdateDto.getMobileNumber());
        user.setDateOfBirth(userUpdateDto.getDateOfBirth());
        user.setAddress(userUpdateDto.getAddress());
        userRepository.save(user);

        return userMapper.userToUserUpdateDto(user);
    }

    /**
     * Update the user by admin from id.
     *
     * @param userUpdateDto dto for updating user
     * @return The UserUpdateDto object representing the authenticated user details.
     */
    @Override
    public UserUpdateDto updateUserByAdmin(UserUpdateDto userUpdateDto) {
        User user = userRepository.findOneById(userUpdateDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this id doesn't exist"));

        user.setFirstname(userUpdateDto.getFirstname());
        user.setLastname(userUpdateDto.getLastname());
        user.setEmail(userUpdateDto.getEmail());
        user.setImageUrl(userUpdateDto.getImageUrl());
        user.setMobileNumber(userUpdateDto.getMobileNumber());
        user.setDateOfBirth(userUpdateDto.getDateOfBirth());
        user.setAddress(userUpdateDto.getAddress());
        userRepository.save(user);

        return userMapper.userToUserUpdateDto(user);
    }

    /**
     * Update the user associated with the current authentication context.
     *
     * @param passwordChangeDto dto for changing password
     */
    @Override
    public void changePassword(PasswordChangeDto passwordChangeDto) {
        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getRepeatNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New passwords don't match");
        }

        User user = getUserFromAuthentication();
        if (!passwordEncoder.matches(passwordChangeDto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect password");
        }
        user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * A method for performing soft delete of User entity. It is implemented in UserController class.
     *
     * @param userId parameter that is unique to entity
     */
    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        userRepository.findById(userId)
                .map(user -> {
                    if (user.getRole() == Role.ADMIN) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin cannot be deleted");
                    }

                    user.getTokens().forEach(token -> tokenRepository.permanentlyDeleteTokenById(token.getId()));

                    userRepository.save(user);
                    userRepository.flush();

                    return user;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found."));

        userRepository.deleteById(userId);
    }

    /**
     * This method first calls the userRepository's findFilteredUsers method
     * to retrieve a Page of User objects that match the query.
     *
     * @param isDeleted           boolean representing deleted objects
     * @param userFiltersQueryDto {@link UserFiltersQueryDto} object which contains query parameters
     * @param page                int number of wanted page
     * @param pageSize            number of results per page
     * @return a Page of UsersDto objects that match the specified query
     */
    @Override
    public Page<UserDto> findFilteredUsers(boolean isDeleted, UserFiltersQueryDto userFiltersQueryDto, Integer page, Integer pageSize) {
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter(USER_FILTER);
        filter.setParameter("isDeleted", isDeleted);

        Page<User> resultPage = userCustomRepository.findFilteredUsers(userFiltersQueryDto, PageRequest.of(page, pageSize));
        List<User> users = resultPage.getContent();

        session.disableFilter(USER_FILTER);

        List<UserDto> userDtos = userMapper.usersToUserDtos(users);

        return new PageImpl<>(userDtos, resultPage.getPageable(), resultPage.getTotalElements());
    }

    @Override
    public UserProfileDto getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));

        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setGameName(user.getGameName());

        return userProfileDto;
    }

    @Override
    @Transactional
    public void setUserOnlineStatus(String gameName, boolean isOnline) {
        User user = userRepository.findOneByGameNameIgnoreCase(gameName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with game name: " + gameName));

        user.setIsOnline(isOnline);
        userRepository.save(user);
    }
}
