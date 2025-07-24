package com.flagfinder.repository;


import com.mss.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing users.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Method that returns optional of User by email.
     *
     * @param email string containing user email
     * @return Optional of {@link User}
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by their id.
     *
     * @param userId the id of the user
     * @return an Optional containing the user if found, or empty if not
     */
    Optional<User> findOneById(Long userId);

    /**
     * Find a user by their password code.
     *
     * @param passwordCode the password code of the user
     * @return an Optional containing the user if found, or empty if not
     */
    Optional <User> findOneByPasswordCode(String passwordCode);

    /**
     * Find a user by their verification code.
     *
     * @param verificationCode the verification code of the user
     * @return an Optional containing the user if found, or empty if not
     */
    Optional <User> findOneByVerificationCode(String verificationCode);
}
