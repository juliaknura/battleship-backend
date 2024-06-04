package com.backend.battleship.controller.dto;

import lombok.Data;

@Data
public class BoardState {
    private int [][] board;
    private String gameID;
    private int playerType;
}
