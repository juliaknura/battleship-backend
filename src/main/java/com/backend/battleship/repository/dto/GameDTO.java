package com.backend.battleship.repository.dto;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game")
@NoArgsConstructor
public class GameDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_player1", referencedColumnName = "id")
    private PlayerDTO player1;

    @ManyToOne
    @JoinColumn(name = "fk_player2", referencedColumnName = "id")
    private PlayerDTO player2;

    private Boolean winner;

    public GameDTO(PlayerDTO player1, PlayerDTO player2, Boolean winner) {
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
    }

    public PlayerDTO getPlayerWinner() {
        return winner ? player1 : player2;
    }
}
