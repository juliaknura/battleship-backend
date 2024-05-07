package com.backend.battleship.controller.dto;

import com.backend.battleship.model.GameMode;
import com.backend.battleship.model.Player;
import lombok.Data;

@Data
public class ConnectRequest {
    private Player player; //TODO
    private GameMode mode;
}
