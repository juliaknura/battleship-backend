package com.backend.battleship.controller.dto;

import lombok.Data;

@Data
public class BoardState {
    private int [][] board;
    private int gameID;
    private int playerType; //TODO
}
