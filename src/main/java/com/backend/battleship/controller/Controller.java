package com.backend.battleship.controller;

import com.backend.battleship.controller.dto.*;
import com.backend.battleship.model.*;
import com.backend.battleship.service.DatabaseService;
import com.backend.battleship.service.GameService;
import com.backend.battleship.storage.GameStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@CrossOrigin("*")
public class Controller {

    private DatabaseService dbService;
    private GameService gameService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping("/post/connect")
    public ResponseEntity<ConnectResponse> connect(@RequestBody ConnectRequest request)
    {
        log.info("Received connect request: " + request);
        var response = gameService.connectToGame(request);
        log.info("Sending connect response: " + response);
        if(response.getPlayerType() == 2)
        {
            String topic = "/topic/"+response.getGameID()+"/opponent/1";
            simpMessagingTemplate.convertAndSend(topic,request.getPlayer());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/post/board")
    public ResponseEntity<BoardResponse> board(@RequestBody BoardState boardState)
    {
        log.info("Received board state "+boardState);
        try{
            var result = gameService.initBoard(boardState);
            if(result == null)
            {
                log.info("Returning board response: true, "+ Arrays.deepToString(boardState.getBoard()));
                return ResponseEntity.ok(new BoardResponse(true, boardState.getBoard()));
            }
            else if(result)
            {
                String topic = "/topic/"+boardState.getGameID()+"/board/";
                var game = (PVPGame)GameStorage.getInstance().getGames().get(boardState.getGameID());
                int[][] otherPlayerBoard;
                if(boardState.getPlayerType()==1)
                {
                    topic += "2";
                    otherPlayerBoard = game.getP2board();
                }
                else
                {
                    topic += "1";
                    otherPlayerBoard = game.getP1board();
                }

                simpMessagingTemplate.convertAndSend(topic,new BoardResponse(true,otherPlayerBoard)); //sending to the other player their own board
                log.info("Returning board response: true" + Arrays.deepToString(boardState.getBoard()));
                return ResponseEntity.ok(new BoardResponse(true, boardState.getBoard()));
            }
            else
            {
                log.info("Returning board response: false" + Arrays.deepToString(boardState.getBoard()));
                return ResponseEntity.ok(new BoardResponse(false, boardState.getBoard()));
            }

        }
        catch(IllegalArgumentException e)
        {
            log.error(e.getMessage());
            return new ResponseEntity<>(new BoardResponse(false, new int[10][10]), HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping("/post/move")
    public ResponseEntity<MoveResult> move(@RequestBody MoveRequest moveRequest)
    {
        log.info("Received move request " + moveRequest);
        try{
            MoveResult result = gameService.checkMove(moveRequest);

            var game = GameStorage.getInstance().getGames().get(moveRequest.getGameID());
            if(game instanceof PVPGame)
            {
                String topic = "/topic/"+moveRequest.getGameID()+"/";
                if(moveRequest.getPlayerType() == 1)
                    topic += "2";
                else
                    topic += "1";

                simpMessagingTemplate.convertAndSend(topic,result);
                log.info("Message by websocket sent: "+topic);
            }
            else if(game instanceof ComputerGame)
            {
                if(result.getGameStatus() == GameStatus.IN_PROGRESS.getValue())
                {
                    var computerMoveResult = gameService.makeMove((ComputerGame) game);
                    log.info("Computer move result: "+computerMoveResult);
                    log.info("Message by websocket sent: "+"/topic/"+moveRequest.getGameID()+"/1");
                    simpMessagingTemplate.convertAndSend("/topic/"+moveRequest.getGameID()+"/1",computerMoveResult);
                }
            }
            log.info("Sending move result: "+result);
            return ResponseEntity.ok(result);
        }
        catch(IllegalArgumentException e)
        {
            log.error(e.getMessage());
            MoveResult result = new MoveResult();
            result.setGameStatus(GameStatus.IN_PROGRESS.getValue());
            result.setCoord(moveRequest.getCoord());
            result.setResult(false);
            result.setSunk(false);
            return new ResponseEntity<>(new MoveResult(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/get/ranking")
    public ResponseEntity<List<Rank>> ranking()
    {
        return ResponseEntity.ok(dbService.getTop10());
    }
}
