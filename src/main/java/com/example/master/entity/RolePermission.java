package com.example.master.entity;

import com.example.master.dtobj.Permission;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "role_permissions")
@Data
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // optional if column name matches field name
    private Long id;

    @Column(name = "role_name")
    private String roleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Permission permission;

    @Column(name = "can_create")
    private boolean canCreate;

    @Column(name = "can_view")
    private boolean canView;

    @Column(name = "can_update")
    private boolean canUpdate;

    @Column(name = "can_delete")
    private boolean canDelete;
}


