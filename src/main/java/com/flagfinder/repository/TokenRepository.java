package com.flagfinder.repository;


import com.flagfinder.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing tokens.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    /**
     * Retrieves a list of valid tokens associated with a user.
     *
     * @param id the ID of the user
     * @return a list of valid tokens
     */
    @Query(value = """
            select t from Token t inner join User u\s
            on t.user.id = u.id\s
            where u.id = :id and (t.expired = false or t.revoked = false)\s
            """)
    List<Token> findAllValidTokenByUser(UUID id);

    /**
     * Retrieves a token by its value.
     *
     * @param token the token value
     * @return an Optional containing the token, or an empty Optional if not found
     */
    Optional<Token> findByToken(String token);

    /**
     * Permanently deletes a Token entity from the database by its ID.
     *
     * <p>This method executes a DELETE operation on the Token entity,
     * removing the record with the specified ID from the database. This operation
     * is not reversible and will permanently remove the entity.</p>
     *
     * @param tokenId the ID of the Token entity to be deleted
     */
    @Modifying
    @Query("DELETE FROM Token t WHERE t.id = :tokenId")
    void permanentlyDeleteTokenById(UUID tokenId);

    /**
     * Finds all tokens where the createdAt field is older than one week.
     *
     * @param oneWeekAgo the timestamp representing one week ago
     * @return a list of tokens created more than a week ago
     */
    @Query("SELECT t FROM Token t WHERE t.createdAt < :oneWeekAgo")
    List<Token> findTokensOlderThanOneWeek(@Param("oneWeekAgo") Instant oneWeekAgo);

    /**
     * Permanently deletes a list of tokens by their IDs.
     *
     * @param tokenIds the list of Token IDs to be deleted.
     */
    @Modifying
    @Query("DELETE FROM Token t WHERE t.id IN :tokenIds")
    void deleteByIds(List<UUID> tokenIds);
}
