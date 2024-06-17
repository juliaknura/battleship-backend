package com.backend.battleship.repository;

import com.backend.battleship.repository.dto.GameDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<GameDTO, Long> {
}
