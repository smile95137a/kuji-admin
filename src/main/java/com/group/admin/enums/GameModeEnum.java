package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameModeEnum implements DisplayableEnum {
    TICKET("TICKET", "籤位制"),
    RANDOM("RANDOM", "隨機"),
    SCRATCH_STORE("SCRATCH_STORE", "店家指定"),
    SCRATCH_PLAYER("SCRATCH_PLAYER", "玩家指定");

    private final String code;
    private final String displayName;

    public static GameModeEnum fromCode(String code) {
        for (GameModeEnum mode : values()) {
            if (mode.code.equalsIgnoreCase(code)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown game mode: " + code);
    }
}
