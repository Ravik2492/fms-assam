package com.example.master.service;

import com.example.master.config.TokenHelper;
import com.example.master.dtobj.*;
import com.example.master.entity.RolePermission;
import com.example.master.entity.UserMetadata;
import com.example.master.repository.RolePermissionRepository;
import com.example.master.repository.UserMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import lombok.Data;

@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminService.class);

    @Autowired
    private WebClient client;
    @Value("${keycloak.auth-server-url}")
    private String baseUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${admin.user.id.for.roles}")
    private String adminUserId;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Autowired
    private RolePermissionRepository repository;

    @Autowired
    private UserMetadataRepository userMetadataRepository;

    private String jwtToken;

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

//    public KeycloakAdminService(String jwtToken) {
//        this.jwtToken = jwtToken;
//    }

    private Mono<String> getAccessToken() {
        //return Mono.fromSupplier(() -> TokenHelper.getJwt().get().getTokenValue());
        return Mono.fromSupplier(() -> {
            Jwt jwt = TokenHelper.getJwt().orElse(null);
            if (jwt != null) {
                return jwt.getTokenValue();
            } else if (this.jwtToken != null) {
                return this.jwtToken;
            } else {
                throw new IllegalStateException("No valid JWT token available");
            }
        });
    }

    // 1) Create user
    public Mono<String> createUser(CreateUserRequest req) {
        String url = baseUrl + "/admin/realms/" + realm + "/users";
        return getAccessToken().flatMap(token ->
                client.post().uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                                "username", req.getUserid(),
                                "email", req.getEmail(),
                                "enabled", Boolean.TRUE,
                                "firstName", req.getFirstName(),
                                "lastName", req.getLastName()
                        ))
                        .exchangeToMono(resp -> {
                            if (resp.statusCode().is2xxSuccessful() || resp.statusCode().value() == 201) {
                                URI loc = resp.headers().asHttpHeaders().getLocation();
                                if (loc != null) {
                                    String id = loc.getPath().substring(loc.getPath().lastIndexOf('/') + 1);
                                    return assignRealmRole(id, req.getRole().name())
                                            .then(saveMetadata(req.getUserid(), req.getUserMetadataRequest()))
                                            .thenReturn(id);
                                }
                                return Mono.empty();
                            } else if (resp.statusCode().value() == 409) {
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "User already exists"
                                ));
                            } else {
                                return resp.bodyToMono(String.class)
                                        .defaultIfEmpty("Unknown error")
                                        .flatMap(body -> Mono.error(new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Failed to create user: HTTP " + resp.statusCode().value() + " - " + body
                                        )));
                            }
                        })
        );
    }

    private Mono<Void> saveMetadata(String userId, UserMetadataRequest req) {

        if (req == null) {
            return Mono.empty(); // ✅ skip saving if request is empty
        }

        UserMetadata metadata = new UserMetadata(
                userId,
                req.getDistrictId(),
                req.getSectorId(),
                req.getProjectId(),
                req.getAwc()
        );
        return Mono.fromRunnable(() -> userMetadataRepository.save(metadata));
    }

    // 2) Set or update password
    public Mono<Void> setPassword(String userId, String password, boolean temporary) {
        String url = baseUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";
        return getAccessToken().flatMap(token ->
                client.put().uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("type", "password", "value", password, "temporary", temporary))
                        .retrieve()
                        .bodyToMono(Void.class)
        );
    }

    public Mono<Void> sendResetEmail(String email) {
        return getMasterAccessToken().flatMap(token ->
                findUserIdByEmail(email, token).flatMap(userId -> {
                    String url = "/admin/realms/" + realm + "/users/" + userId + "/execute-actions-email";
                    List<String> actions = List.of("UPDATE_PASSWORD");

                    return client.put()
                            .uri(uriBuilder -> uriBuilder.path(url).queryParam("lifespan", 300).build())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(actions)
                            .retrieve()
                            .bodyToMono(Void.class);
                })
        );
    }

    private Mono<String> getMasterAccessToken() {
        return client.post()
                .uri(baseUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials&client_id="+clientId+"&client_secret="+clientSecret)
                .retrieve()
                .bodyToMono(Map.class)
                .map(token -> token.get("access_token").toString());
    }

    private Mono<String> findUserIdByEmail(String email, String token) {
        String url = baseUrl + "/admin/realms/" + realm + "/users?email=" + email;

        return client.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .map(users -> {
                    if (users.isEmpty()) throw new RuntimeException("User not found");
                    return users.get(0).get("id").toString();
                });
    }

    // 3) Get users (search)
    public Mono<List<KcUserWithRole>> searchUsers(String username, Integer first, Integer max) {
        String url = "/admin/realms/" + realm + "/users";
        System.out.println(url);
        System.out.println(getAccessToken().block());

        return getAccessToken().flatMap(token ->
                client.get()
                        .uri(uriBuilder -> {
                            UriBuilder builder = uriBuilder.path(url);
                            if (username != null) builder.queryParam("username", username);
                            if (first != null) builder.queryParam("first", first);
                            if (max != null) builder.queryParam("max", max);
                            return builder.build();
                        })
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToFlux(KcUser.class)
                        .flatMap(user -> getSingleUserRole(token, user.getId())
                                .map(role -> {
                                    KcUserWithRole enriched = new KcUserWithRole();
                                    enriched.setUser(user);
                                    enriched.setRole(role);
                                    if(!StringUtils.isEmpty(username) && getMatchedRole(role).isPresent()) {
                                        List<RolePermission> permissions = repository.findByRoleName(role);
                                        enriched.setPermissions(permissions);
                                    }
                                    return enriched;
                                }))
                        .collectList()
        );
    }

    private Mono<String> getSingleUserRole(String token, String userId) {
        String roleUrl = baseUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        return client.get()
                .uri(roleUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                //.next() // Only take the first role
                .map(role -> (String) role.get("name"))
                .filter(roleName -> Arrays.stream(Role.values())
                        .anyMatch(r -> r.name().equalsIgnoreCase(roleName)))
                .next()
                .defaultIfEmpty("NO_ROLE"); // Optional fallback
    }

    public Mono<List<KcUserWithRole>> searchUsersByRole(String roleName, String username, Integer first, Integer max) {
        String url = "/admin/realms/" + realm + "/users";

        return getAccessToken().flatMap(token ->
                client.get()
                        .uri(uriBuilder -> {
                            UriBuilder builder = uriBuilder.path(url);
                            if (username != null) builder.queryParam("username", username);
                            if (first != null) builder.queryParam("first", first);
                            if (max != null) builder.queryParam("max", max);
                            return builder.build();
                        })
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToFlux(KcUser.class)
                        .flatMap(user -> getSingleUserRole(token, user.getId())
                                .filter(role -> role.equalsIgnoreCase(roleName))
                                .map(role -> {
                                    KcUserWithRole enriched = new KcUserWithRole();
                                    enriched.setUser(user);
                                    enriched.setRole(role);
                                    return enriched;
                                }))
                        .collectList()
        );
    }

    public Mono<Map<String, Object>> getUserById(String userId) {
        String url = baseUrl + "/admin/realms/" + realm + "/users/" + userId;

        return getAccessToken().flatMap(token ->
                client.get()
                        .uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
        );
    }

    public KcUser getUserDetails(String userNameOrId, LookupType lookupType) {
        String url = baseUrl + "/admin/realms/" + realm + "/users?username=" + userNameOrId;
        String token = getAccessToken().block();

        Map<String, Object> user = null;
        if(lookupType.equals(LookupType.ID)) {
            url = baseUrl + "/admin/realms/" + realm + "/users/" + userNameOrId;
            user = client.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            if (user == null || user.isEmpty()) {
                throw new RuntimeException("User not found: " + userNameOrId);
            }
        } else {
            List<Map<String, Object>> users = client.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
            if (users == null || users.isEmpty()) {
                throw new RuntimeException("User not found: " + userNameOrId);
            }
            user = users.get(0);
        }

        return mapToKcUser(user);
    }

    public Mono<String> getUserFullNameById(String userId) {
        String url = baseUrl + "/admin/realms/" + realm + "/users/" + userId;

        return getAccessToken().flatMap(token ->
                client.get()
                        .uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .map(user -> {
                            String firstName = (String) user.getOrDefault("firstName", "");
                            String lastName = (String) user.getOrDefault("lastName", "");
                            return "" + firstName + " " + lastName;
                        })
        );
    }


    // 4) Delete user
    public Mono<Void> deleteUser(String userId) {
        String url = baseUrl + "/admin/realms/" + realm + "/users/" + userId;
        return getAccessToken().flatMap(token ->
                client.delete().uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(Void.class)
        );
    }

    public Mono<Void> updateUser(String userId, String email, String firstName, String lastName, KeycloakUserUpdateRequest request) {
        String url = baseUrl + "/admin/realms/" + realm + "/users/" + userId;

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("firstName", firstName);
        payload.put("lastName", lastName);

        KcUser user = getUserDetails(userId, LookupType.ID);
        if(user!=null && request.getUserMetadataRequest()!=null) {
            String username = user.getUsername();
            UserMetadata metadata = userMetadataRepository.getById(username);
            if(metadata==null) {
                metadata = new UserMetadata();
                metadata.setUserId(username);
                metadata.setAwc(request.getUserMetadataRequest().getAwc());
                metadata.setSectorId(request.getUserMetadataRequest().getSectorId());
                metadata.setProjectId(request.getUserMetadataRequest().getProjectId());
                metadata.setDistrictId(request.getUserMetadataRequest().getDistrictId());
                userMetadataRepository.save(metadata);
            } else {
                metadata.setAwc(request.getUserMetadataRequest().getAwc());
                metadata.setSectorId(request.getUserMetadataRequest().getSectorId());
                metadata.setProjectId(request.getUserMetadataRequest().getProjectId());
                metadata.setDistrictId(request.getUserMetadataRequest().getDistrictId());
                userMetadataRepository.save(metadata);
            }
        }

        return getAccessToken().flatMap(token ->
                client.put()
                        .uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .retrieve()
                        .toBodilessEntity()
                        .doOnSuccess(response -> log.info("User {} updated successfully", userId))
                        .doOnError(error -> log.error("Failed to update user {}: {}", userId, error.getMessage()))
                        .then()
        );
    }

    // 5) Assign realm role
    public Mono<Void> assignRealmRole(String userId, String roleName) {
        String roleUrl = baseUrl + "/admin/realms/" + realm + "/roles/" + roleName;
        String mappingUrl = baseUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        return getAccessToken().flatMap(token ->
                client.get().uri(roleUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .flatMap(role -> client.post().uri(mappingUrl)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(List.of(Map.of("id", role.get("id"), "name", role.get("name"))))
                                .retrieve()
                                .bodyToMono(Void.class))
        );
    }

    public Mono<Void> assignRealmRoleNew(String userId, String roleName) {
        String roleUrl = baseUrl + "/admin/realms/" + realm + "/roles/" + roleName;
        String mappingUrl = baseUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        return Mono.zip(getAccessToken(), getRealmManagementRoles())
                .flatMap(tuple -> {
                    String token = tuple.getT1();
                    List<Map<String, Object>> realmMgmtRoles = tuple.getT2();

                    return client.get()
                            .uri(roleUrl)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .flatMap(role -> {
                                // Merge both payloads
                                List<Map<String, Object>> mergedPayload = new ArrayList<>(realmMgmtRoles);
                                mergedPayload.add(Map.of(
                                        "id", role.get("id"),
                                        "name", role.get("name"),
                                        "composite", role.getOrDefault("composite", false),
                                        "clientRole", false,
                                        "containerId", realm
                                ));

                                return client.post()
                                        .uri(mappingUrl)
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(mergedPayload)
                                        .retrieve()
                                        .bodyToMono(Void.class);
                            });
                });
    }



    public Mono<List<Map<String, Object>>> getRealmManagementRoles() {

        List<String> rolesToAssign = List.of("manage-users", "view-users");
        String url = baseUrl + "/admin/realms/" + realm + "/users/" + adminUserId + "/role-mappings";

        return getAccessToken().flatMap(token ->
                client.get()
                        .uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .map(response -> {
                            Map<String, Object> clientMappings = (Map<String, Object>) response.get("clientMappings");
                            if (clientMappings == null || !clientMappings.containsKey("realm-management")) {
                                return List.of(); // no realm-management roles assigned
                            }

                            Map<String, Object> realmMgmt = (Map<String, Object>) clientMappings.get("realm-management");

                            List<Map<String, Object>> allRoles = (List<Map<String, Object>>) realmMgmt.get("mappings");

                            return allRoles.stream()
                                    .filter(role -> rolesToAssign.contains(role.get("name").toString()))
                                    .toList();
                        })
        );
    }



    // 6) Remove realm role
    public Mono<Void> removeRealmRole(String userId, String roleName) {
        String roleUrl = baseUrl + "/admin/realms/" + realm + "/roles/" + roleName;
        String mappingUrl = baseUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        return getAccessToken().flatMap(token ->
                client.get()
                        .uri(roleUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .flatMap(role -> client.method(HttpMethod.DELETE)
                                .uri(mappingUrl)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(List.of(Map.of(
                                        "id", role.get("id"),
                                        "name", role.get("name")
                                ))))
                                .retrieve()
                                .bodyToMono(Void.class)
                        )
        );
    }

    public Mono<Map<String, Object>> getRealmRole(String roleName) {
        String url = baseUrl + "/admin/realms/" + realm + "/roles/" + roleName;

        return getAccessToken().flatMap(token ->
                client.get()
                        .uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
        );
    }

    public Mono<List<Map<String, Object>>> listRealmRoles() {
        String url = baseUrl + "/admin/realms/" + realm + "/roles";

        return getAccessToken().flatMap(token ->
                client.get()
                        .uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .flatMap(roleMap -> {
                            String roleName = (String) roleMap.get("name");
                            // Check if roleName matches your Role enum
                            return Mono.justOrEmpty(getMatchedRole(roleName))
                                    .flatMap(matchedRole -> {
                                        List<RolePermission> permissions = repository.findByRoleName(matchedRole.name());
                                        roleMap.put("customPermissions", permissions);
                                        return Mono.just(roleMap);
                                    });
                                    //.switchIfEmpty(Mono.just(roleMap)); // If not matched, return role as-is
                        })
                        .collectList()
        );
    }

    public Mono<Void> createRealmRole(String roleName, String description) {
        String url = baseUrl + "/admin/realms/" + realm + "/roles";

        Map<String, Object> rolePayload = Map.of(
                "name", roleName,
                "description", description,
                "composite", false,
                "clientRole", false
        );

        return getAccessToken().flatMap(token ->
                client.post()
                        .uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(rolePayload)
                        .retrieve()
                        .bodyToMono(Void.class)
        );
    }

    public Mono<Void> deleteRealmRole(String roleName) {
        String url = baseUrl + "/admin/realms/" + realm + "/roles/" + roleName;

        return getAccessToken().flatMap(token ->
                client.delete()
                        .uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(Void.class)
        );
    }

    private Optional<Role> getMatchedRole(String roleName) {
        return Arrays.stream(Role.values())
                .filter(r -> r.name().equalsIgnoreCase(roleName))
                .findFirst();
    }

    private KcUser mapToKcUser(Map<String, Object> data) {
        KcUser user = new KcUser();
        user.setId((String) data.get("id"));
        user.setUsername((String) data.get("username"));
        user.setFirstName((String) data.get("firstName"));
        user.setLastName((String) data.get("lastName"));
        user.setEmail((String) data.get("email"));
        user.setEmailVerified((Boolean) data.get("emailVerified"));
        user.setEnabled((Boolean) data.get("enabled"));
        //user.setMobile((String) data.get("attributes") != null ? extractMobile((Map<String, Object>) data.get("attributes")) : null);
        return user;
    }

    private String extractMobile(Map<String, Object> attributes) {
        Object mobileAttr = attributes.get("mobile");
        if (mobileAttr instanceof List) {
            return ((List<?>) mobileAttr).isEmpty() ? null : (String) ((List<?>) mobileAttr).get(0);
        } else if (mobileAttr instanceof String) {
            return (String) mobileAttr;
        }
        return null;
    }

}
