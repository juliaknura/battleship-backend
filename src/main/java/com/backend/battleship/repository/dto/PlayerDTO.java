package com.backend.battleship.repository.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "player")
@NoArgsConstructor
public class PlayerDTO {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Setter
    @Getter
    private String nick;

    @Setter
    @Getter
    private Integer winCount;

    public PlayerDTO(String nick, Integer winCount) {
        this.nick = nick;
        this.winCount = winCount;
    }
}
