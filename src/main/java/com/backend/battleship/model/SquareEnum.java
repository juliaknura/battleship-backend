package com.backend.battleship.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SquareEnum {
    EMPTY(0),SHIP(1),SUNK(2), HIDDEN(-1);
    private Integer value;
}
