package com.backend.battleship.model;

import lombok.Data;

@Data
public class Game {
    private String gameID;
    private GameStatus status;
    private int currentTurn;
}
