package com.example.master.controller;


import com.example.master.dtobj.*;
import com.example.master.entity.RolePermission;
import com.example.master.service.KeycloakAdminService;
import com.example.master.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2")
@Tag(name = "FMS RJ", description = "APIs to control user operations")
public class UserController {

    @Autowired
    private KeycloakAdminService admin;

    @Autowired
    private RolePermissionService service;

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_MANAGE_USERS')")
    @PostMapping("/users")
    @Operation(summary = "Create New user")
    public Mono<Map<String, String>> createUser(@RequestBody CreateUserRequest req, HttpServletRequest request) {
        extractAndPassBearerToken(request);
        return admin.createUser(req)
                .flatMap(userId -> {
                    if (req.getPassword() != null && !req.getPassword().isBlank()) {
                        return admin.setPassword(userId, req.getPassword(), false)
                                .thenReturn(userId);
                    }
                    return Mono.just(userId);
                })
                .map(id -> Map.of("id", id));
    }

    //@PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER') or hasAuthority('ROLE_MANAGE_USERS')")
    @PostMapping("/users/reset-password")
    @Operation(summary = "Reset user by passing userid and updated password")
    public Mono<Void> resetPassword(@RequestBody CreateUserRequest req, HttpServletRequest request) {
        return admin.setPassword(req.getUserid(), req.getPassword(), false);
    }

    @PostMapping("/users/forgot-password")
    public Mono<Void> forgotPassword(@RequestParam String email) {
        return admin.sendResetEmail(email);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_MANAGE_USERS')")
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user by passing userid")
    public Mono<Void> deleteUser(@PathVariable String id, HttpServletRequest req) {
        extractAndPassBearerToken(req);

        return admin.deleteUser(id);
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "Update Keycloak user's email, first name, and last name")
    public Mono<ResponseEntity<Void>> updateUser(
            @PathVariable String userId,
            @RequestBody KeycloakUserUpdateRequest request, HttpServletRequest req) {

        extractAndPassBearerToken(req);
        return admin.updateUser(userId, request.getEmail(), request.getFirstName(), request.getLastName(), request)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER') or hasAuthority('ROLE_VIEW_USERS')")
    @GetMapping("/users")
    @Operation(summary = "List users or fetch single user by passing username")
    public Mono<List<KcUserWithRole>> users(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer first,
            @RequestParam(required = false) Integer max, HttpServletRequest req) {


        extractAndPassBearerToken(req);
        return admin.searchUsers(username, first, max);
    }

    @PreAuthorize("hasAnyRole('ADMIN','AWC','NODAL','SUPPLIER') or hasAuthority('ROLE_MANAGE_ROLES')")
    @GetMapping("/users/by-role")
    @Operation(summary = "Search users by role")
    public Mono<List<KcUserWithRole>> getUsersByRole(
            @RequestParam String roleName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer first,
            @RequestParam(required = false) Integer max, HttpServletRequest req
    ) {
        extractAndPassBearerToken(req);
        return admin.searchUsersByRole(roleName, username, first, max);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_MANAGE_ROLES')")
    @PostMapping("/users/{id}/roles/{roleName}")
    @Operation(summary = "Assign role to the user")
    public Mono<Void> assignRealmRole(@PathVariable String id, @PathVariable String roleName, HttpServletRequest req) {
        extractAndPassBearerToken(req);
        return admin.assignRealmRole(id, roleName);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_MANAGE_ROLES')")
    @DeleteMapping("/users/{id}/roles/{roleName}")
    @Operation(summary = "Un-assign role to the user")
    public Mono<Void> removeRealmRole(@PathVariable String id, @PathVariable String roleName, HttpServletRequest req) {
        extractAndPassBearerToken(req);
        return admin.removeRealmRole(id, roleName);
    }

    private void extractAndPassBearerToken(HttpServletRequest req) {
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        admin.setJwtToken(auth.substring(7).trim());
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_MANAGE_USERS')")
    @GetMapping("/roles/{roleName}")
    @Operation(summary = "Get thr role by role name")
    public Mono<Map<String, Object>> getRealmRole(@PathVariable String roleName,HttpServletRequest req) {
        extractAndPassBearerToken(req);
        return admin.getRealmRole(roleName);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_MANAGE_USERS')")
    @GetMapping("/roles")
    @Operation(summary = "List the roles")
    public Mono<List<Map<String, Object>>> listRealmRoles(HttpServletRequest req) {
        extractAndPassBearerToken(req);
        return admin.listRealmRoles();
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_MANAGE_USERS')")
    @PostMapping("/roles")
    @Operation(summary = "Create the role")
    public Mono<Void> createRealmRole(@RequestBody RoleRequest request, HttpServletRequest req) {
        extractAndPassBearerToken(req);
        return admin.createRealmRole(request.getName(), request.getDescription());
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_MANAGE_USERS')")
    @DeleteMapping("/roles/{roleName}")
    @Operation(summary = "Delete the role")
    public Mono<Void> deleteRealmRole(@PathVariable String roleName, HttpServletRequest req) {
        extractAndPassBearerToken(req);
        if(roleName.equals("ADMIN")) {
            throw new IllegalArgumentException("We can't delete ADMIN Role");
        }
        return admin.deleteRealmRole(roleName);
    }


    //Permission APIs

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_MANAGE_ROLES')")
    @PostMapping("/roles/permissions/{roleName}")
    @Operation(summary = "Bulk Add/Update role permissions")
    public ResponseEntity<?> addOrUpdatePermissions(
            @PathVariable String roleName,
            @RequestBody List<PermissionRequest> requests, HttpServletRequest req) {
        extractAndPassBearerToken(req);
        service.addOrUpdatePermissions(roleName, requests);
        return ResponseEntity.ok("Permissions updated successfully");
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_MANAGE_ROLES')")
    @GetMapping("/roles/permissions")
    @Operation(summary = "All permissions irrespective of role")
    public ResponseEntity<List<String>> getAllPermissionEnums() {
        List<String> permissions = Arrays.stream(Permission.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/roles/permissions/{roleName}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_MANAGE_ROLES')")
    @Operation(summary = "Get role permissions")
    public ResponseEntity<List<RolePermission>> getPermissions(@PathVariable String roleName) {
        return ResponseEntity.ok(service.getPermissionsByRole(roleName));
    }

    //no need to single permission add update
    /*@PostMapping("/roles/permission/{roleName}")
    @Operation(summary = "Add/Update role permission")
    public ResponseEntity<?> addOrUpdatePermission(
            @PathVariable String roleName,
            @RequestBody PermissionRequest request) {

        RolePermission updated = service.addOrUpdatePermission(
                roleName,
                request.getPermission(),
                request.isCanCreate(),
                request.isCanView(),
                request.isCanUpdate(),
                request.isCanDelete()
        );

        return ResponseEntity.ok(updated);
    }*/

    //no need to delete - add/update automatocally delete if not in the add/update request
    /*@DeleteMapping("/roles/permissions/{roleName}/{permission}")
    @Operation(summary = "Delete role permissions")
    public ResponseEntity<?> deletePermission(@PathVariable String roleName,
                                              @PathVariable Permission permission) {
        service.deletePermission(roleName, permission);
        return ResponseEntity.ok("Permission deleted");
    }*/


}
