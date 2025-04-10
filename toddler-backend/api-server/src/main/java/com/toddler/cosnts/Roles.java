package com.toddler.cosnts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Roles {
    OWNER("OWNER");

    private final String role;
}
