package com.flagfinder.dto;

import lombok.Data;

import java.time.Instant;

/**
 * Represents a base entity data transfer object with common attributes such as ID,
 * creation date and modification date.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Data
public class BaseEntityDto {
    /**
     * The unique identifier for entities.
     */
    private Long id;

    /**
     * The date and time when the entity was created.
     */
    private Instant createdAt;

    /**
     * The date and time when the entity was updated.
     */
    private Instant updatedAt;

    /**
     * Indicates whether the entity has been deleted.
     */
    private Boolean deleted = false;
}
