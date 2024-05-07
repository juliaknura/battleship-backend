package com.backend.battleship.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GameMode {
    PLAYER(0),
    COMPUTER(1);
    private Integer value;
}
