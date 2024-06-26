package com.backend.battleship.controller.dto;

import com.backend.battleship.model.Coord;
import com.backend.battleship.model.GameStatus;
import lombok.Data;

@Data
public class MoveResult {
    private Coord coord;
    private boolean result;
    private boolean sunk;
    private int gameStatus;
    private int[][] boardView;
}
