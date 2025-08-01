package com.flagfinder.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration representing permissions in the system.
 */
@RequiredArgsConstructor
public enum Permission {
    /**
     * Permission to read.
     */
    ADMIN_READ("admin:read"),

    /**
     * Permission to update.
     */
    ADMIN_UPDATE("admin:update"),

    /**
     * Permission to create.
     */
    ADMIN_CREATE("admin:create"),

    /**
     * Permission to delete.
     */
    ADMIN_DELETE("admin:delete"),

    /**
     * Permission to read.
     */
    USER_READ("user:read"),

    /**
     * Permission to update.
     */
    USER_UPDATE("user:update"),

    /**
     * Permission to create.
     */
    USER_CREATE("user:create"),

    /**
     * Permission to delete.
     */
    USER_DELETE("user:delete");

    /**
     * The string representation of the permission.
     */
    @Getter
    private final String permission;
}
