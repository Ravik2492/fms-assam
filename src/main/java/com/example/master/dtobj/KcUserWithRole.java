package com.example.master.dtobj;

import com.example.master.entity.RolePermission;

import java.util.List;

public class KcUserWithRole {

    private KcUser user;
    private String role;

    private List<RolePermission> permissions;

    public List<RolePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<RolePermission> permissions) {
        this.permissions = permissions;
    }

    // Getters and setters
    public KcUser getUser() { return user; }
    public void setUser(KcUser user) { this.user = user; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

}
