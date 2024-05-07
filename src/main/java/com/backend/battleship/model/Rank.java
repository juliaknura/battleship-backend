package com.backend.battleship.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Rank implements Comparable<Rank> {
    private String player;
    private int score; //number of games won

    @Override
    public int compareTo(Rank o) {
        return Integer.compare(getScore(),o.getScore());
    }
}
