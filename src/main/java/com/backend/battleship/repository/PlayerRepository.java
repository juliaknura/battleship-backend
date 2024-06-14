package com.backend.battleship.repository;

import com.backend.battleship.repository.dto.PlayerDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerDTO, Long> {

    List<PlayerDTO> findTop10ByOrderByWinCountDesc();

    Optional<PlayerDTO> findByNick(String player1Nick);
}
