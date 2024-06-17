package com.backend.battleship.config;

import com.backend.battleship.repository.GameRepository;
import com.backend.battleship.repository.PlayerRepository;
import com.backend.battleship.repository.dto.GameDTO;
import com.backend.battleship.repository.dto.PlayerDTO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PlayerTableConfig {

    @Bean
    CommandLineRunner commandLineRunner(PlayerRepository playerRepository, GameRepository gameRepository) {
        return args -> {
            var player1 = new PlayerDTO("fillerNick1",10);
            var player2 = new PlayerDTO("fillerNick5",10);

            playerRepository.saveAll(
                    List.of(
                            player1,
                            player2,
                            new PlayerDTO("fillerNick2",9),
                            new PlayerDTO("fillerNick4",8),
                            new PlayerDTO("fillerNick3",7),
                            new PlayerDTO("fillerNick6",6),
                            new PlayerDTO("fillerNick8",5),
                            new PlayerDTO("fillerNick7",4),
                            new PlayerDTO("fillerNick9",3),
                            new PlayerDTO("fillerNick10",3),
                            new PlayerDTO("fillerNick11",2)
                    )
            );

            gameRepository.save(
                    new GameDTO(player1, player2, Boolean.TRUE)
            );
        };
    }
}
