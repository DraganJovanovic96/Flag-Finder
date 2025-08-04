package com.flagfinder.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * This class represents status information for entities in finance system.
 * It contains fields to track the creation date and time, the last update date and time,
 * and whether the entity has been deleted.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@MappedSuperclass
@Data
public abstract class BaseEntity {
    /**
     * The unique identifier for entities.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    /**
     * The date and time when the entity was created.
     */
    @Column
    private final Instant createdAt = Instant.now();
}
