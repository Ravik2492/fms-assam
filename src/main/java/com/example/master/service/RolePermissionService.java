package com.example.master.service;

import com.example.master.dtobj.Permission;
import com.example.master.dtobj.PermissionRequest;
import com.example.master.entity.RolePermission;
import com.example.master.repository.RolePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RolePermissionService {

    @Autowired
    private RolePermissionRepository repository;

    public void addOrUpdatePermissions(String roleName, List<PermissionRequest> requests) {

        List<RolePermission> existingPermissions = repository.findByRoleName(roleName);
        Set<Permission> incomingFunctions = requests.stream()
                .map(PermissionRequest::getPermission)
                .collect(Collectors.toSet());

        // Delete permissions not in the incoming list
        existingPermissions.stream()
                .filter(p -> !incomingFunctions.contains(p.getPermission()))
                .forEach(p -> repository.deleteById(p.getId()));

        // Add or update incoming permissions
        for (PermissionRequest req : requests) {
            RolePermission permission = repository
                    .findByRoleNameAndPermission(roleName, req.getPermission())
                    .orElse(new RolePermission());

            permission.setRoleName(roleName);
            permission.setPermission(req.getPermission());
            permission.setCanCreate(req.isCanCreate());
            permission.setCanView(req.isCanView());
            permission.setCanUpdate(req.isCanUpdate());
            permission.setCanDelete(req.isCanDelete());

            repository.save(permission);
        }
    }

    public RolePermission addOrUpdatePermission(String roleName, Permission permission,
                                                boolean create, boolean view, boolean update, boolean delete) {
        RolePermission rolePermission = repository
                .findByRoleNameAndPermission(roleName, permission)
                .orElse(new RolePermission());

        rolePermission.setRoleName(roleName);
        rolePermission.setPermission(permission);
        rolePermission.setCanCreate(create);
        rolePermission.setCanView(view);
        rolePermission.setCanUpdate(update);
        rolePermission.setCanDelete(delete);

        return repository.save(rolePermission);
    }

    public List<RolePermission> getPermissionsByRole(String roleName) {
        return repository.findByRoleName(roleName);
    }

    public void deletePermission(String roleName, Permission permission) {
        repository.deleteByRoleNameAndPermission(roleName, permission);
    }

    public List<GrantedAuthority> buildAuthorities(List<RolePermission> rolePermissions) {
        return rolePermissions.stream()
                .flatMap(rp -> {
                    List<String> authorities = new ArrayList<>();

                    String base = rp.getRoleName() + "_" + rp.getPermission().name(); // e.g. ADMIN_USER

                    if (rp.isCanCreate()) authorities.add(base + "_CREATE");
                    if (rp.isCanView())   authorities.add(base + "_VIEW");
                    if (rp.isCanUpdate()) authorities.add(base + "_UPDATE");
                    if (rp.isCanDelete()) authorities.add(base + "_DELETE");

                    return authorities.stream()
                            .map(SimpleGrantedAuthority::new)
                            .map(a -> (GrantedAuthority) a);
                })
                .toList();
    }

}

