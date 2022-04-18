package chess;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import chess.dao.DbUserDao;
import chess.dao.UserDao;
import chess.domain.board.ChessBoard;
import chess.domain.board.factory.BoardFactory;
import chess.domain.board.factory.RegularBoardFactory;
import chess.domain.board.position.Position;
import chess.domain.piece.Piece;
import chess.domain.user.User;
import chess.dto.response.BoardResponse;
import chess.service.UserService;
import chess.turndecider.AlternatingGameFlow;
import chess.turndecider.GameFlow;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

public class WebApplication {

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        staticFiles.location("/static");

        final BoardFactory boardFactory = RegularBoardFactory.getInstance();
        GameFlow gameFlow = new AlternatingGameFlow();
        ChessBoard chessBoard = new ChessBoard(boardFactory.create(), gameFlow);
        UserDao userDao = new DbUserDao();
        UserService userService = new UserService(userDao);

        index();

        newGame();

        openGame();

        final Map<String, User> inGameUser = new HashMap<>();

        playGame(userService, inGameUser);

        final Map<Position, Piece> initBoard = RegularBoardFactory.getInstance().create();
        final BoardResponse initBoardResponse = BoardResponse.from(initBoard);

        board(initBoardResponse);

        users(inGameUser);

        move(chessBoard);

        currentTeam(chessBoard);
    }

    private static void currentTeam(ChessBoard chessBoard) {
        post("/current-team", (req, res) -> chessBoard.currentState().getName(), gson::toJson);
    }

    private static void move(ChessBoard chessBoard) {
        post("/move", (request, response) -> {
            Position from = Position.of(request.queryParams("from"));
            Position to = Position.of(request.queryParams("to"));
            chessBoard.movePiece(from, to);

            Map<Position, Piece> movedBoard = chessBoard.getBoard();
            return BoardResponse.from(movedBoard);

        }, gson::toJson);
    }

    private static void users(Map<String, User> inGameUser) {
        get("/users", (request, response) -> inGameUser, gson::toJson);
    }

    private static void board(BoardResponse initBoardResponse) {
        get("/board", "application/json", (request, response) -> initBoardResponse, gson::toJson);
    }

    private static void playGame(UserService userService, Map<String, User> inGameUser) {
        post("/play-game", (request, response) -> {
            final Map<String, Object> model = new HashMap<>();

            User whitePlayer = new User(request.queryParams("white-player-name"));
            User blackPlayer = new User(request.queryParams("black-player-name"));

            userService.save(whitePlayer);
            userService.save(blackPlayer);

            inGameUser.clear();
            inGameUser.put("whiteUser", whitePlayer);
            inGameUser.put("blackUser", blackPlayer);

            return render(model, "play_game.html");
        });
    }

    private static void openGame() {
        get("/open-game", (request, response) -> {
            final Map<String, Object> model = new HashMap<>();
            return render(model, "open_game.html");
        });
    }

    private static void newGame() {
        get("/new-game", (request, response) -> {
            final Map<String, Object> model = new HashMap<>();
            return render(model, "new_game.html");
        });
    }

    private static void index() {
        get("/", (request, response) -> {
            final Map<String, Object> model = new HashMap<>();
            return render(model, "index.html");
        });
    }

    private static String render(Map<String, Object> model, String templatePath) {
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, templatePath));
    }
}
