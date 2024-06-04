package com.backend.battleship.model;

import lombok.Data;

@Data
public class ComputerGame extends Game {
    private Player player;
    private int[][] playerBoard;
    private int[][] computerBoard;
    private int[][] computerViewBoard;
    //dodać pole które będzie zbiorem wszystkich hidden pól? zainicjalizować, systematycznie usuwać
    private int playerSunk;
    private int computerSunk;
}
