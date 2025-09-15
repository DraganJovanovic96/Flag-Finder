package com.flagfinder.mapper;


import com.flagfinder.dto.LocalStorageUserDto;
import com.flagfinder.dto.UserDto;
import com.flagfinder.dto.UserUpdateDto;
import com.flagfinder.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * UserMapper is a mapper interface that defines mapping methods between {@link User} and{@link UserDto}
 * classes using MapStruct library. It also enables list to list mapping.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Mapper
public interface UserMapper {
    /**
     * Maps a User object to a UserDto object.
     *
     * @param user the User object to be mapped to a UserDto object
     * @return a UserDto object containing the user's information
     */
    UserDto userToUserDto(User user);

    /**
     * Maps a User object to a UserDto object.
     *
     * @param user the User object to be mapped to a UserDto object
     * @return a UserDto object containing the user's information
     */
    UserUpdateDto userToUserUpdateDto(User user);

    /**
     * Maps a UserDto object to a User object.
     *
     * @param userDto the UserDto object to be mapped to a User object
     * @return a User object containing the UserDto's information
     */
    User userDtoToUser(UserDto userDto);

    /**
     * Maps a list of User objects to a list of UserDto objects.
     *
     * @param users the List<User> to be mapped to a List<UserDto>
     * @return a List<UserDto> containing the users information
     */
    List<UserDto> usersToUserDtos(List<User> users);

    /**
     * Maps a list of UserDto objects to a list of User objects.
     *
     * @param userDtos the List<UserDto> to be mapped to a List<User>
     * @return a List<User> containing the UserDtos information
     */
    List<User> userDtosToUsers(List<UserDto> userDtos);

    /**
     * Maps a User object to a LocalStorageUserDto object.
     *
     * @param user the User object to be mapped to a LocalStorageUserDto object
     * @return a LocalStorageUserDto object containing the user's information
     */
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    LocalStorageUserDto userToLocalStorageUserDto(User user);

}
