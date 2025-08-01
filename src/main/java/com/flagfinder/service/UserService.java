package com.flagfinder.service;


import com.flagfinder.dto.*;
import com.flagfinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * The UserService interface contains methods that will be implemented is UserServiceImpl and methods correlate
 * to User entity.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
public interface UserService {

    /**
     * Retrieves a user entity by their email address.
     *
     * @param email The email address of the user.
     * @return The User entity with the specified email address.
     * @throws ResponseStatusException If a user with the specified email address is not found.
     */
    User findOneByEmail(String email);

    /**
     * Finds a user by their unique identifier.
     *
     * @param userId the unique identifier of the user to retrieve
     * @return a {@link CustomerDto} representing the found user
     */
    UserDto findUserById(Long userId);

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
    void deleteUser(Long userId);

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
}
