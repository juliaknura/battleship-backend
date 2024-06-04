package com.backend.battleship.model;

import lombok.Data;

@Data
public class PVPGame extends Game{
    private Player player1;
    private Player player2;
    private int[][] p1board;
    private int[][] p2board;
    private int p1sunk;
    private int p2sunk;
}
