package com.proj_chatBot.backend.enums;

public enum Role {
    ADMIN("ADMIN"),
    AGENT_WILAYA("AGENT_WILAYA");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
