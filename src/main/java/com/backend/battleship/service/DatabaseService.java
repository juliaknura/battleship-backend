package com.backend.battleship.service;

import com.backend.battleship.model.Rank;
import com.backend.battleship.repository.GameRepository;
import com.backend.battleship.repository.PlayerRepository;
import com.backend.battleship.repository.dto.GameDTO;
import com.backend.battleship.repository.dto.PlayerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class DatabaseService {

    private static final List<Rank> ranking = Arrays.asList(new Rank("fillerNick1",10),new Rank("fillerNick5",10), new Rank("fillerNick2",9), new Rank("fillerNick4",8), new Rank("fillerNick3",7),new Rank("fillerNick6",6),new Rank("fillerNick8",5), new Rank("fillerNick7",4), new Rank("fillerNick9",3), new Rank("fillerNick10",3),new Rank("fillerNick11",2));

    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;

    @Autowired
    public DatabaseService(PlayerRepository playerRepository, GameRepository gameRepository) {
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
    }

    public List<Rank> getTop10() {
        return ranking.stream().sorted(Comparator.reverseOrder()).limit(10).toList();
    }

    public List<Rank> getTop10Players() {
        return playerRepository.findTop10ByOrderByWinCountDesc()
                .stream()
                .map(playerDTO -> new Rank(playerDTO.getNick(), playerDTO.getWinCount()))
                .toList();
    }

    private PlayerDTO getOrCreatePlayer(String playerNick) {
        return playerRepository.findByNick(playerNick)
                .orElseGet(() -> playerRepository.save(new PlayerDTO(playerNick, 0)));
    }

    private void updateWinCount(PlayerDTO playerDTO) {
        playerDTO.setWinCount(playerDTO.getWinCount() + 1);
        playerRepository.save(playerDTO);
    }

    @Transactional
    public void saveGameResult(String player1Nick, String player2Nick, Boolean winner) {
        var game = new GameDTO(
                getOrCreatePlayer(player1Nick),
                getOrCreatePlayer(player2Nick),
                winner
        );

        gameRepository.save(game);
        updateWinCount(game.getPlayerWinner());
    }

}
