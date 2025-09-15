package com.flagfinder.service;


import com.flagfinder.dto.*;
import com.flagfinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for user operations.
 * Provides methods for user management, authentication, profile updates, and user queries.
 * Contains methods that correlate to User entity operations.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
public interface UserService {

    /**
     * Sets the online status for a user.
     *
     * @param gameName The game name of the user
     * @param isOnline The online status to set
     */
    void setUserOnlineStatus(String gameName, boolean isOnline);


    /**
     * Finds a user by their unique identifier.
     *
     * @param userId the unique identifier of the user to retrieve
     * @return a {@link UserDto} representing the found user
     */
    UserDto findUserById(UUID userId);

    /**
     * A method for retrieving all users implemented in UserServiceImpl class.
     *
     * @param isDeleted parameter that checks if object is soft deleted
     * @return a list of all UserDtos
     */
    List<UserDto> getAllUsers(boolean isDeleted);

    /**
     * Retrieves the user associated with the current authentication context.
     *
     * @return The User object representing the authenticated user.
     */
    User getUserFromAuthentication();

    /**
     * Retrieves the user associated with the current authentication context.
     *
     *
     * @return The LocalStorageUserDto object representing the authenticated user with fewer details.
     */
    LocalStorageUserDto getLocalStorageUserDtoFromAuthentication();

    /**
     * Retrieves the user associated with the current authentication context.
     *
     *
     * @return The UserDto object representing the authenticated user with fewer details.
     */
    UserUpdateDto getUserDtoFromAuthentication();

    /**
     * Update the user associated with the current authentication context.
     *
     *
     * @return The UserUpdateDto object representing the authenticated user details.
     */
    UserUpdateDto updateUser(UserUpdateDto userUpdateDto);

    /**
     * Update the user by admin from id.
     *
     *
     * @return The UserUpdateDto object representing the authenticated user details.
     */
    UserUpdateDto updateUserByAdmin(UserUpdateDto userUpdateDto);

    /**
     * Update the user associated with the current authentication context.
     *
     */
    void changePassword(PasswordChangeDto passwordChangeDto);
    
    /**
     * A method for deleting user. It is implemented in UserServiceImpl class.
     *
     * @param userId parameter that is unique to entity
     */
    void deleteUser(UUID userId);

    /**
     * This method first calls the userRepository's findFilteredUsers method
     * to retrieve a Page of User objects that match the query.
     *
     * @param userFiltersQueryDto {@link UserFiltersQueryDto} object which contains query parameters
     * @param isDeleted               boolean representing deleted objects
     * @param page                    int number of wanted page
     * @param pageSize                number of results per page
     * @return a Page of UsersDto objects that match the specified query
     */
    Page<UserDto> findFilteredUsers(boolean isDeleted, UserFiltersQueryDto userFiltersQueryDto, Integer page, Integer pageSize);

    /**
     * Retrieves user profile information (email and gameName) by email address.
     *
     * @param email The email address of the user to retrieve profile for
     * @return UserProfileDto containing email and gameName
     * @throws ResponseStatusException If a user with the specified email address is not found
     */
    UserProfileDto getUserProfileByEmail(String email);
}
