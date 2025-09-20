package com.example.master.repository;

import com.example.master.dtobj.Permission;
import com.example.master.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleName(String roleName);
    Optional<RolePermission> findByRoleNameAndPermission(String roleName, Permission permission);
    void deleteByRoleNameAndPermission(String roleName, Permission permission);
}

