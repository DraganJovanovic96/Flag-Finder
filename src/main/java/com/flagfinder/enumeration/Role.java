package com.flagfinder.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.flagfinder.enumeration.Permission.*;


/**
 * Enum representing different roles for users.
 */
@RequiredArgsConstructor
public enum Role {

    /**
     * Administrator role with all permissions.
     */
    ADMIN(
            Set.of(
                    ADMIN_READ,
                    ADMIN_UPDATE,
                    ADMIN_DELETE,
                    ADMIN_CREATE,
                    USER_READ,
                    USER_UPDATE,
                    USER_DELETE,
                    USER_CREATE
            )
    ),

    /**
     * Manager role with specific permissions.
     */
    USER(
            Set.of(
                    USER_READ,
                    USER_UPDATE,
                    USER_DELETE,
                    USER_CREATE
            )
    );

    /**
     * The set of permissions associated with the role.
     */
    @Getter
    private final Set<Permission> permissions;

    /**
     * Returns the authorities associated with the role.
     *
     * @return a list of SimpleGrantedAuthority objects representing the role's authorities
     */
    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
