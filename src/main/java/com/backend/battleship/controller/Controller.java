package com.backend.battleship.controller;

import com.backend.battleship.controller.dto.*;
import com.backend.battleship.model.Coord;
import com.backend.battleship.model.GameStatus;
import com.backend.battleship.model.Rank;
import com.backend.battleship.service.DatabaseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@CrossOrigin("*")
public class Controller {

    private DatabaseService dbService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping("/post/connect")
    public ResponseEntity<ConnectResponse> connect(@RequestBody ConnectRequest request)
    {
        log.info("Received connect request " + request);
        ConnectResponse response = new ConnectResponse(); //TODO filler
        response.setGameID(1234);
        response.setPlayerType(1);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/post/board")
    public ResponseEntity<String> board(@RequestBody BoardState boardState)
    {
        log.info("Received board state "+boardState);
        //TODO filler
        return ResponseEntity.ok("Board posted correctly");
    }

    @PostMapping("/post/move")
    public ResponseEntity<MoveResult> move(@RequestBody MoveRequest moveRequest)
    {
        log.info("Received move request " + moveRequest);
        MoveResult result = new MoveResult();
        result.setResult(true);
        result.setCoord(new Coord(1,2));
        result.setGameStatus(GameStatus.IN_PROGRESS);
        simpMessagingTemplate.convertAndSend("/topic/2345/1",result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get/ranking")
    public ResponseEntity<List<Rank>> ranking()
    {
        return ResponseEntity.ok(dbService.getTop10());
    }
}
