package com.example.master.config;

import com.example.master.dtobj.Role;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;

public class TokenHelper {

    public static String getUsername() {
        return getJwt().map(jwt -> jwt.getClaimAsString("preferred_username")).orElse(null);
    }

    public static String getEmail() {
        return getJwt().map(jwt -> jwt.getClaimAsString("email")).orElse(null);
    }

    public static List<String> getRealmRoles() {
        return getJwt()
                .map(jwt -> (Map<String, Object>) jwt.getClaim("realm_access"))
                .map(map -> (List<String>) map.getOrDefault("roles", List.of()))
                .orElse(List.of());
    }

    public static Optional<Role> getRole() {
        return getJwt()
                .map(jwt -> (Map<String, Object>) jwt.getClaim("realm_access"))
                .map(map -> (List<String>) map.getOrDefault("roles", List.of()))
                .orElse(List.of()).stream()
                .map(roleName -> Arrays.stream(Role.values())
                        .filter(r -> r.name().equalsIgnoreCase(roleName))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .findFirst();
    }

    public static List<String> getClientRoles(String clientId) {
        return getJwt()
                .map(jwt -> (Map<String, Object>) jwt.getClaim("resource_access"))
                .map(map -> (Map<String, Object>) map.getOrDefault(clientId, Map.of()))
                .map(map -> (List<String>) map.getOrDefault("roles", List.of()))
                .orElse(List.of());
    }

    public static Optional<Jwt> getJwt() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(jwtAuth.getToken());
        }
        return Optional.empty();
    }
}


