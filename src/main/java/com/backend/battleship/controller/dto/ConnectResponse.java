package com.backend.battleship.controller.dto;

import com.backend.battleship.model.Player;
import lombok.Data;

@Data
public class ConnectResponse {
    private int playerType;
    private String gameID;
    private Player opponent;
}
