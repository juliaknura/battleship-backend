package com.backend.battleship.controller.dto;

import com.backend.battleship.model.Coord;
import lombok.Data;

@Data
public class MoveRequest {
    private Coord coord;
    private int playerType;
    private int gameID;
}
