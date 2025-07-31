package com.flagfinder.model;

import com.flagfinder.enumeration.TokenType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing an authentication token.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token extends BaseEntity {
    /**
     * The token value.
     */
    @Column(unique = true)
    public String token;

    /**
     * The type of the token.
     */
    @Enumerated(EnumType.STRING)
    public TokenType tokenType = TokenType.BEARER;

    /**
     * Flag indicating if the token has been revoked.
     */
    private boolean revoked;

    /**
     * Flag indicating if the token has expired.
     */
    private boolean expired;

    /**
     * The user associated with the token.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public com.flagfinder.model.User user;
}
