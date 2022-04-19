package chess;

import static spark.Spark.staticFiles;

import chess.controller.WebChessController;
import chess.domain.board.ChessBoard;
import chess.domain.user.User;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class WebApplication {

    private static final Gson gson = new Gson();
    private static ChessBoard chessBoard;

    public static void main(String[] args) {
        staticFiles.location("/static");

        WebChessController.index();
        WebChessController.newGame();
        WebChessController.openGame();
        WebChessController.move();
        WebChessController.currentTeam();
        final Map<String, User> inGameUser = new HashMap<>();
        WebChessController.playGame(inGameUser);
        WebChessController.users(inGameUser);
        WebChessController.board();
    }
}
