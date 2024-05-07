package com.backend.battleship.controller.dto;

import lombok.Data;

@Data
public class ConnectResponse {
    private int playerType;
    private int gameID;
}
