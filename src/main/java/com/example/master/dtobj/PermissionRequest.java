package com.example.master.dtobj;

import lombok.Data;

@Data
public class PermissionRequest {
    private Permission permission;
    private boolean canCreate;
    private boolean canView;
    private boolean canUpdate;
    private boolean canDelete;

    // Getters and setters
}

