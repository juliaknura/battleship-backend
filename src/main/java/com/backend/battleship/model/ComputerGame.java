package com.backend.battleship.model;

import lombok.Data;

@Data
public class ComputerGame extends Game {
    private Player player;
    private int[][] playerBoard;
    private int[][] computerBoard;
    private int[][] computerViewBoard;
    private int playerSunk;
    private int computerSunk;
    private int[][] playerViewBoard;
}
