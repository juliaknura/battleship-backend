package com.backend.battleship.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GameStatus {
    IN_PROGRESS(0), PLAYER_1_WIN(1), PLAYER_2_WIN(2);
    private Integer value;
}
