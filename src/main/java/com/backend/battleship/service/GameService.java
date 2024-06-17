package com.backend.battleship.service;

import com.backend.battleship.controller.dto.*;
import com.backend.battleship.model.*;
import com.backend.battleship.storage.GameStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class GameService {

    private final int BOARD_SIZE = 10;
    private final int SHIP_COUNT = 5;
    public ConnectResponse connectToGame(ConnectRequest request){

        if(request.getMode() == GameMode.COMPUTER) // if game with algorithm than join immediately
        {
            ComputerGame newGame = new ComputerGame();
            newGame.setGameID(UUID.randomUUID().toString());
            newGame.setPlayer(request.getPlayer());
            newGame.setStatus(GameStatus.IN_PROGRESS);
            newGame.setCurrentTurn(1);
            GameStorage.getInstance().addGame(newGame);
            log.info("Creating new game for player: "+ request.getPlayer().getNickname() + " with computer of ID: " + newGame.getGameID());

            ConnectResponse response = new ConnectResponse();
            response.setGameID(newGame.getGameID());
            response.setPlayerType(1);
            return response;
        }
        else // if no new game then create game, if new game then join that game
        {
            var games = GameStorage.getInstance().getGames();
            var gameToJoinOpt = games.values().stream()
                    .filter(game -> game.getStatus().equals(GameStatus.NEW))
                    .filter(game -> game instanceof PVPGame && !((PVPGame) game).getPlayer1().equals(request.getPlayer()))
                    .findFirst();
            if(gameToJoinOpt.isEmpty())
            {
                PVPGame newGame = new PVPGame();
                newGame.setGameID(UUID.randomUUID().toString());
                newGame.setStatus(GameStatus.NEW);
                newGame.setPlayer1(request.getPlayer());
                newGame.setCurrentTurn(1);
                GameStorage.getInstance().addGame(newGame);
                log.info("Creating new PVP game for player: "+ request.getPlayer().getNickname() + " of ID: " + newGame.getGameID());

                ConnectResponse response = new ConnectResponse();
                response.setGameID(newGame.getGameID());
                response.setPlayerType(1);
                return response;
            }
            else
            {
                PVPGame gameToJoin = (PVPGame)gameToJoinOpt.get();
                gameToJoin.setPlayer2(request.getPlayer());
                gameToJoin.setStatus(GameStatus.IN_PROGRESS);
                GameStorage.getInstance().addGame(gameToJoin);
                log.info("Joining existing PVP game for player: "+ request.getPlayer().getNickname() + "  of ID: " + gameToJoin.getGameID());

                ConnectResponse response = new ConnectResponse();
                response.setGameID(gameToJoin.getGameID());
                response.setPlayerType(2);
                response.setOpponent(gameToJoin.getPlayer1());
                return response;
            }
        }
    }

    public Boolean initBoard(BoardState boardState) throws IllegalArgumentException{

        if(!GameStorage.getInstance().getGames().containsKey(boardState.getGameID()))
            throw new IllegalArgumentException("Game of ID: "+boardState.getGameID()+" not found");

        if(boardState.getBoard().length != BOARD_SIZE)
            throw new IllegalArgumentException("Invalid board vertical size: "+boardState.getBoard().length);

        for(var row: boardState.getBoard())
        {
            if(row.length != BOARD_SIZE)
                throw new IllegalArgumentException("Invalid board horizontal size: "+row.length);
        }

        var game = GameStorage.getInstance().getGames().get(boardState.getGameID());
        if(game instanceof ComputerGame)
        {
            ((ComputerGame) game).setPlayerBoard(boardState.getBoard());
            log.info("Creating random board for computer for gameID: "+game.getGameID());
            ((ComputerGame) game).setComputerBoard(createRandomBoard());

            ((ComputerGame) game).setComputerViewBoard(createEmptyView());
            ((ComputerGame) game).setPlayerViewBoard(createEmptyView());

            return null;
        }
        else
        {
            if(boardState.getPlayerType() == 1)
            {
                log.info("Setting board for player 1");
                ((PVPGame) game).setP1board(boardState.getBoard());
                ((PVPGame) game).setP1ViewBoard(createEmptyView());
                return ((PVPGame) game).getP2board() != null;
            }
            else if(boardState.getPlayerType() == 2)
            {
                log.info("Setting board for player 2");
                ((PVPGame) game).setP2board(boardState.getBoard());
                ((PVPGame) game).setP2ViewBoard(createEmptyView());
                return ((PVPGame) game).getP1board() != null;
            }
            else
                throw new IllegalArgumentException("Invalid player type: "+boardState.getPlayerType());

        }
    }

    public MoveResult checkMove(MoveRequest moveRequest) throws IllegalArgumentException{

        if(!GameStorage.getInstance().getGames().containsKey(moveRequest.getGameID()))
            throw new IllegalArgumentException("Game of ID: "+moveRequest.getGameID()+" not found");

        var game = GameStorage.getInstance().getGames().get(moveRequest.getGameID());

        MoveResult result = new MoveResult();
        result.setCoord(moveRequest.getCoord());
        GameStatus newStatus = GameStatus.IN_PROGRESS;

        var x = moveRequest.getCoord().getY();
        var y = moveRequest.getCoord().getX();

        if(!inBoard(x) || !inBoard(y))
            throw  new IllegalArgumentException("Invalid coordinates: ("+x+","+y+")");

        if(!game.getStatus().equals(GameStatus.IN_PROGRESS))
        {
            result.setResult(false);
            result.setGameStatus(game.getStatus().getValue());
        }
        else if(game instanceof ComputerGame)
        {
            if(((ComputerGame) game).getComputerBoard()[x][y] == SquareEnum.SHIP.getValue())
            {
                result.setResult(true);
                ((ComputerGame) game).getComputerBoard()[x][y] = SquareEnum.SUNK.getValue();
                if(((ComputerGame) game).getPlayerViewBoard()[x][y] == SquareEnum.HIDDEN.getValue())
                    ((ComputerGame) game).getPlayerViewBoard()[x][y] = SquareEnum.SHIP.getValue();
                if(isShipSunk(((ComputerGame) game).getComputerBoard(),new Coord(x,y)))
                {
                    result.setSunk(true);
                    ((ComputerGame) game).setComputerSunk(((ComputerGame) game).getComputerSunk()+1);
                }
                else
                    result.setSunk(false);
                if(((ComputerGame) game).getComputerSunk() == SHIP_COUNT)
                    newStatus = GameStatus.PLAYER_1_WIN;
            }
            else
            {
                if(((ComputerGame) game).getPlayerViewBoard()[x][y] == SquareEnum.HIDDEN.getValue())
                    ((ComputerGame) game).getPlayerViewBoard()[x][y] = SquareEnum.EMPTY.getValue();
                result.setResult(false);
            }


            log.info("Setting game status as: "+newStatus);
            game.setStatus(newStatus);
            GameStorage.getInstance().addGame(game);
            result.setGameStatus(newStatus.getValue());
            result.setBoardView(((ComputerGame) game).getPlayerViewBoard());

        }
        else if(game instanceof PVPGame)
        {
            if(moveRequest.getPlayerType()==1)
            {
                if(((PVPGame)game).getP2board()[x][y] == SquareEnum.SHIP.getValue())
                {
                    result.setResult(true);
                    ((PVPGame) game).getP2board()[x][y] = SquareEnum.SUNK.getValue();
                    if(((PVPGame)game).getP1ViewBoard()[x][y] == SquareEnum.HIDDEN.getValue())
                        ((PVPGame)game).getP1ViewBoard()[x][y] = SquareEnum.SHIP.getValue();
                    if(isShipSunk(((PVPGame) game).getP2board(),new Coord(x,y)))
                    {
                        result.setSunk(true);
                        ((PVPGame) game).setP2sunk(((PVPGame) game).getP2sunk()+1);
                    }
                    else
                        result.setSunk(false);
                    if(((PVPGame) game).getP2sunk() == SHIP_COUNT)
                        newStatus = GameStatus.PLAYER_1_WIN;
                }
                else
                {
                    if(((PVPGame)game).getP1ViewBoard()[x][y] == SquareEnum.HIDDEN.getValue())
                        ((PVPGame)game).getP1ViewBoard()[x][y] = SquareEnum.HIDDEN.getValue();
                    result.setResult(false);
                }

                result.setBoardView(((PVPGame)game).getP1ViewBoard());
            }
            else if(moveRequest.getPlayerType()==2)
            {
                if(((PVPGame)game).getP1board()[x][y] == SquareEnum.SHIP.getValue())
                {
                    result.setResult(true);
                    ((PVPGame)game).getP1board()[x][y] = SquareEnum.SUNK.getValue();
                    if(((PVPGame)game).getP2ViewBoard()[x][y] == SquareEnum.HIDDEN.getValue())
                        ((PVPGame)game).getP2ViewBoard()[x][y] = SquareEnum.SHIP.getValue();
                    if(isShipSunk(((PVPGame) game).getP1board(),new Coord(x,y)))
                    {
                        result.setSunk(true);
                        ((PVPGame) game).setP1sunk(((PVPGame) game).getP1sunk()+1);
                    }
                    else
                        result.setSunk(false);
                    if(((PVPGame) game).getP1sunk() == SHIP_COUNT)
                        newStatus = GameStatus.PLAYER_2_WIN;
                }
                else
                {
                    if(((PVPGame)game).getP2ViewBoard()[x][y] == SquareEnum.HIDDEN.getValue())
                        ((PVPGame)game).getP2ViewBoard()[x][y] = SquareEnum.HIDDEN.getValue();
                    result.setResult(false);
                }

                result.setBoardView(((PVPGame)game).getP2ViewBoard());
            }
            else
                throw new IllegalArgumentException("Invalid player type: "+moveRequest.getPlayerType());

            log.info("Setting game status as: "+newStatus);
            game.setStatus(newStatus);
            GameStorage.getInstance().addGame(game);
            result.setGameStatus(newStatus.getValue());
        }

        return result;
    }

    public MoveResult makeMove(ComputerGame game){

        MoveResult result = new MoveResult();
        GameStatus newStatus = GameStatus.IN_PROGRESS;

        var coord = chooseNextComputerMove(game.getComputerViewBoard());
        result.setCoord(new Coord(coord.getY(), coord.getX()));
        if(game.getPlayerBoard()[coord.getX()][coord.getY()] == SquareEnum.SHIP.getValue())
        {
            result.setResult(true);
            game.getPlayerBoard()[coord.getX()][coord.getY()] = SquareEnum.SUNK.getValue();
            if(isShipSunk(game.getPlayerBoard(),coord))
            {
                result.setSunk(true);
                game.setPlayerSunk(game.getPlayerSunk()+1);
            }
            else
                result.setSunk(false);
            if(result.isResult() && !result.isSunk())
                game.getComputerViewBoard()[coord.getX()][coord.getY()] = SquareEnum.SHIP.getValue();
            else if(result.isResult() && result.isSunk()) //clearing fields around the ship
            {
                game.getComputerViewBoard()[coord.getX()][coord.getY()] = SquareEnum.SHIP.getValue();
                for(int i=0;i<BOARD_SIZE;i++)
                    for(int j=0;j<BOARD_SIZE;j++)
                    {
                        if(game.getComputerViewBoard()[i][j] == SquareEnum.SHIP.getValue())
                        {
                            Coord[] dirs = {new Coord(-1,0),new Coord(1,0),new Coord(0,-1),new Coord(0,1),new Coord(-1,-1), new Coord(1,1), new Coord(-1,1), new Coord(1,-1)};

                            for(var dir: dirs)
                                if(inBoard(i+dir.getX()) && inBoard(j+dir.getY()) && game.getComputerViewBoard()[i+dir.getX()][j+dir.getY()] == SquareEnum.HIDDEN.getValue())
                                    game.getComputerViewBoard()[i+dir.getX()][j+dir.getY()] = SquareEnum.EMPTY.getValue();

                            game.getComputerViewBoard()[i][j] = SquareEnum.SUNK.getValue();
                        }
                    }
            }

            if(game.getPlayerSunk() == SHIP_COUNT)
                newStatus = GameStatus.PLAYER_2_WIN;
        }
        else
        {
            result.setResult(false);
            game.getComputerViewBoard()[coord.getX()][coord.getY()] = SquareEnum.EMPTY.getValue();
        }

        log.info("Setting game status: "+newStatus);
        result.setGameStatus(newStatus.getValue());
        game.setStatus(newStatus);
        GameStorage.getInstance().addGame(game);
        log.info("Viewboard after move: "+ Arrays.deepToString(game.getComputerViewBoard()));

        result.setCoord(new Coord(result.getCoord().getX(), result.getCoord().getY()));
        return result;
    }

    private Coord chooseNextComputerMove(int[][] boardView){

        Coord chosen = null;
        for(int i=0;i<BOARD_SIZE;i++) //search for existing hits
        {
            for(int j=0;j<BOARD_SIZE;j++)
            {
                if(boardView[i][j]==SquareEnum.SHIP.getValue())
                {
                    chosen=new Coord(i,j);
                    break;
                }
            }
            if(chosen != null)
                break;
        }

        if(chosen == null) //choose next random move
        {
            ArrayList<Coord> hidden = new ArrayList<>();
            for(int i=0;i<BOARD_SIZE;i++)
                for(int j=0;j<BOARD_SIZE;j++)
                {
                    if(boardView[i][j]==SquareEnum.HIDDEN.getValue())
                        hidden.add(new Coord(i,j));
                }

            return hidden.get((int)(Math.random()*hidden.size()));
        }
        else //choose next move next to an existing hit
        {
            Coord[] dirs = {new Coord(-1,0),new Coord(1,0),new Coord(0,-1),new Coord(0,1)};
            Coord chosenDir = null;
            for(var dir:dirs)
            {
                if(inBoard(chosen, dir) && boardView[chosen.getX()+dir.getX()][chosen.getY()+dir.getY()]==SquareEnum.SHIP.getValue())
                    chosenDir = dir;
            }
            if(chosenDir == null) //if no neighboring hits, just chose first hidden
            {
                Coord chosenNeighbor = null;
                for(var dir:dirs)
                {
                    if(inBoard(chosen, dir) && boardView[chosen.getX()+dir.getX()][chosen.getY()+dir.getY()]==SquareEnum.HIDDEN.getValue())
                        chosenNeighbor = new Coord(chosen.getX()+dir.getX(),chosen.getY()+dir.getY());
                }
                return chosenNeighbor;
            }
            else //choose next hit along the exitsting hits line
            {
                Coord chosen1 = new Coord(chosen.getX() + chosenDir.getX(), chosen.getY() + chosenDir.getY());
                Coord chosen2 = new Coord(chosen.getX() - chosenDir.getX(), chosen.getY() - chosenDir.getY());
                while(inBoard(chosen1) || inBoard(chosen2))
                {
                    if(inBoard(chosen1) && boardView[chosen1.getX()][chosen1.getY()] == SquareEnum.HIDDEN.getValue())
                    {
                        chosen = chosen1;
                        break;
                    }
                    if(inBoard(chosen2) && boardView[chosen2.getX()][chosen2.getY()] == SquareEnum.HIDDEN.getValue())
                    {
                        chosen = chosen2;
                        break;
                    }

                    if(inBoard(chosen1) && boardView[chosen1.getX()][chosen1.getY()] == SquareEnum.SHIP.getValue())
                        chosen1 = new Coord(chosen1.getX() + chosenDir.getX(), chosen1.getY() + chosenDir.getY());
                    if(inBoard(chosen2) && boardView[chosen2.getX()][chosen2.getY()] == SquareEnum.SHIP.getValue())
                        chosen2 = new Coord(chosen2.getX() - chosenDir.getX(), chosen2.getY() - chosenDir.getY());
                }

                return chosen;
            }
        }
    }
    boolean isShipSunk(int[][] board, Coord coord)
    {
        Coord[] dirs = {new Coord(-1,0),new Coord(1,0),new Coord(0,-1),new Coord(0,1)};
        Coord current;
        for(var dir : dirs)
        {
            current = coord;
            while(inBoard(current) && board[current.getX()][current.getY()] == SquareEnum.SUNK.getValue())
                current = new Coord(current.getX()+dir.getX(), current.getY()+dir.getY());
            if(inBoard(current) && board[current.getX()][current.getY()] == SquareEnum.SHIP.getValue())
                return false;
        }

        return true;
    }

    private int[][] createRandomBoard()
    {
        int[] shipSizes = {5,4,3,3,2};
        int[][] board = new int[BOARD_SIZE][BOARD_SIZE];

        for(int shipSize: shipSizes)
        {
            boolean placed = false;
            while(!placed)
            {
                boolean horizontal = Math.random() < 0.5;

                int col = horizontal ? (int) (Math.random() * (BOARD_SIZE - shipSize)) : (int) (Math.random() * BOARD_SIZE);
                int row = horizontal ? (int) (Math.random() * BOARD_SIZE) : (int) (Math.random() * (BOARD_SIZE - shipSize));

                if(isPlacementValid(board,row,col,shipSize,horizontal))
                {
                    placeShip(board,row,col,shipSize,horizontal);
                    placed = true;
                }
            }
        }
        return board;
    }

    private boolean isPlacementValid(int[][] board, int row, int col, int shipSize, boolean horizontal)
    {
        if(!inBoard(row) || !inBoard(row+shipSize) || !inBoard(col) || !inBoard(col+shipSize))
            return false;

        if(horizontal)
        {
            for(int i = row-1;i<=row+1;i++)
            {
                for(int j = col-1;j<=col+shipSize+1;j++)
                {
                    if(inBoard(i) && inBoard(j))
                    {
                        if(board[i][j]==SquareEnum.SHIP.getValue())
                            return false;
                    }
                }
            }
        }
        else
        {
            for(int i = row-1;i<=row+shipSize+1;i++)
            {
                for(int j = col-1;j<=col+1;j++)
                {
                    if(inBoard(i) && inBoard(j))
                    {
                        if(board[i][j]==SquareEnum.SHIP.getValue())
                            return false;
                    }
                }
            }
        }


        return true;
    }

    private void placeShip(int[][] board, int row, int col, int shipSize, boolean horizontal)
    {
        if(horizontal)
        {
            for(int i=col; i<col+shipSize;i++)
                board[row][i] = SquareEnum.SHIP.getValue();
        }
        else
        {
            for(int i=row; i<row+shipSize;i++)
                board[i][col] = SquareEnum.SHIP.getValue();
        }
    }

    private boolean inBoard(int a)
    {
        return a>=0 && a<BOARD_SIZE;
    }

    private boolean inBoard(Coord c)
    {
        return inBoard(c.getX()) && inBoard(c.getY());
    }

    private boolean inBoard(Coord a, Coord b)
    {
        return inBoard(new Coord(a.getX()+b.getX(),a.getY()+b.getY()));
    }

    private int[][] createEmptyView()
    {
        int[][] view = new int[BOARD_SIZE][BOARD_SIZE];
        for(int[] row: view)
            Arrays.fill(row,SquareEnum.HIDDEN.getValue());
        return view;
    }

}
