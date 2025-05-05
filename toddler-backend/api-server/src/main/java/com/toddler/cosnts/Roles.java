package com.toddler.cosnts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public enum Roles {
    OWNER("OWNER"),
    MANAGER("MANAGER"),
    WORKER("WORKER");

    private final String role;

    Roles(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
