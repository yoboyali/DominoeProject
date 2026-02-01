
package org.server;

import com.esotericsoftware.kryonet.*;
import org.shared.Network;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameServer {

    private final Server server;
    private final Connection[] players = new Connection[2];
    private int connectedPlayers = 0;
    private int currentTurn = 0;

    private final List<Network.Piece> board = new ArrayList<>();
    private final List<Network.Piece> drawPile = new ArrayList<>();
    private int leftEndValue = -1;
    private int rightEndValue = -1;
    private boolean firstMoveMade = false;

    private static GameServer instance;
    private final List<Network.Piece> allPieces = createDominoSet();

    private List<Network.Piece> createDominoSet() {
        List<Network.Piece> pieces = new ArrayList<>();
        pieces.add(new Network.Piece(1, "/Pieces/00.png", 0, 0));
        pieces.add(new Network.Piece(2, "/Pieces/10.png", 1, 0));
        pieces.add(new Network.Piece(3, "/Pieces/20.png", 2, 0));
        pieces.add(new Network.Piece(4, "/Pieces/30.png", 3, 0));
        pieces.add(new Network.Piece(5, "/Pieces/40.png", 4, 0));
        pieces.add(new Network.Piece(6, "/Pieces/50.png", 5, 0));
        pieces.add(new Network.Piece(28, "/Pieces/60.png", 6, 0));

        pieces.add(new Network.Piece(7, "/Pieces/11.png", 1, 1));
        pieces.add(new Network.Piece(8, "/Pieces/12.png", 1, 2));
        pieces.add(new Network.Piece(9, "/Pieces/13.png", 1, 3));
        pieces.add(new Network.Piece(10, "/Pieces/14.png", 1, 4));
        pieces.add(new Network.Piece(11, "/Pieces/15.png", 1, 5));
        pieces.add(new Network.Piece(12, "/Pieces/16.png", 1, 6));

        pieces.add(new Network.Piece(13, "/Pieces/22.png", 2, 2));
        pieces.add(new Network.Piece(14, "/Pieces/23.png", 2, 3));
        pieces.add(new Network.Piece(15, "/Pieces/24.png", 2, 4));
        pieces.add(new Network.Piece(16, "/Pieces/25.png", 2, 5));
        pieces.add(new Network.Piece(17, "/Pieces/26.png", 2, 6));
        pieces.add(new Network.Piece(18, "/Pieces/33.png", 3, 3));
        pieces.add(new Network.Piece(19, "/Pieces/34.png", 3, 4));
        pieces.add(new Network.Piece(20, "/Pieces/35.png", 3, 5));
        pieces.add(new Network.Piece(21, "/Pieces/36.png", 3, 6));

        pieces.add(new Network.Piece(22, "/Pieces/44.png", 4, 4));
        pieces.add(new Network.Piece(23, "/Pieces/45.png", 4, 5));
        pieces.add(new Network.Piece(24, "/Pieces/46.png", 4, 6));

        pieces.add(new Network.Piece(25, "/Pieces/55.png", 5, 5));
        pieces.add(new Network.Piece(26, "/Pieces/56.png", 5, 6));
        pieces.add(new Network.Piece(27, "/Pieces/66.png", 6, 6));

        return pieces;
    }

    public GameServer() throws IOException {
        server = new Server();
        Network.register(server.getKryo());

        server.getKryo().setRegistrationRequired(false);
        drawPile.addAll(allPieces);
        server.addListener(new Listener() {
            @Override
            public void connected(Connection c) {
                System.out.println("✓ Player " + c.getID() + " connected (" + (connectedPlayers + 1) + "/2)");

                if (connectedPlayers >= 2) {
                    System.out.println("✗ Server full, rejecting connection");
                    c.close();
                    return;
                }

                players[connectedPlayers] = c;
                connectedPlayers++;

                if (connectedPlayers == 2) {
                    startGame();
                }
            }

            @Override
            public void received(Connection c, Object obj) {
                if (obj instanceof com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive) {
                    c.sendTCP(new com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive());
                    return;
                }

                System.out.println("Server: Received from connection " + c.getID() + ": " + obj.getClass().getSimpleName());

                if (obj instanceof Network.PlayPiece move) {
                    handleMove(c, move);
                }
                if (obj instanceof Network.DrawPiece drawRequest) {
                    handleDrawPiece(c);
                }
            }

            @Override
            public void disconnected(Connection c) {
                System.out.println("Player " + getPlayerId(c) + " disconnected");
                if (connectedPlayers > 0) {
                    broadcast(new Network.GameOver());
                    resetGame();
                }
            }

            @Override
            public void idle(Connection c) {
                c.sendTCP(new com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive());
            }
        });

        server.bind(Network.TCP_PORT, Network.UDP_PORT);

        server.start();

        System.out.println("✓ Server started on port " + Network.TCP_PORT);
        System.out.println("✓ Waiting for players to connect...");
        System.out.println("✓ KeepAlive system enabled to prevent timeouts");
    }

    private int getPlayerId(Connection c) {
        for (int i = 0; i < 2; i++) {
            if (players[i] == c) return i + 1;
        }
        return 0;
    }
    private void handleDrawPiece(Connection sender) {
        int playerId = getPlayerId(sender);
        System.out.println("Player " + playerId + " wants to draw a piece");

        Network.PieceDrawn response = new Network.PieceDrawn();

        if (drawPile.isEmpty()) {
            response.successful = false;
            System.out.println("Draw pile is empty!");
        } else {
            Network.Piece drawnPiece = drawPile.remove(0);
            response.piece = drawnPiece;
            response.successful = true;
            System.out.println("Player " + playerId + " drew piece " + drawnPiece.id +
                    " [" + drawnPiece.leftValue + "-" + drawnPiece.rightValue + "]");
            System.out.println("Draw pile now has " + drawPile.size() + " pieces remaining");
        }

        sender.sendTCP(response);
    }

    private void startGame() {
        System.out.println("✓ Both players connected. Starting game.");

        Collections.shuffle(drawPile);
        System.out.println("Draw pile shuffled. " + drawPile.size() + " pieces total.");

        for (int i = 0; i < 2; i++) {
            Network.StartGame sg = new Network.StartGame();
            sg.playerNumber = i + 1;
            sg.hand = new ArrayList<>();
            players[i].sendTCP(sg);

            Network.InitialHand hand = new Network.InitialHand();
            hand.pieces = new ArrayList<>();

            for (int j = 0; j < 7; j++) {
                if (!drawPile.isEmpty()) {
                    Network.Piece piece = drawPile.remove(0);
                    hand.pieces.add(piece);
                    System.out.println("Gave piece " + piece.id + " to player " + (i + 1));
                }
            }

            players[i].sendTCP(hand);
            System.out.println("Player " + (i + 1) + " received " + hand.pieces.size() + " pieces");
        }

        System.out.println("Draw pile now has " + drawPile.size() + " pieces remaining");

        players[0].sendTCP(new Network.YourTurn());
        System.out.println("Game started. Player 1's turn.");
    }
    private void handleMove(Connection sender, Network.PlayPiece move) {
        int senderId = getPlayerId(sender);
        if (players[currentTurn] != sender) {
            System.out.println("Invalid: Not player's turn! Expected player " + (currentTurn + 1) +
                    ", but player " + senderId + " tried to move");

            Network.MoveInvalid invalid = new Network.MoveInvalid();
            invalid.reason = "Not your turn!";
            sender.sendTCP(invalid);
            return;
        }

        String validationError = validateMove(move);
        if (validationError != null) {
            System.out.println("Invalid move by player " + senderId + ": " + validationError);

            Network.MoveInvalid invalid = new Network.MoveInvalid();
            invalid.reason = validationError;
            sender.sendTCP(invalid);
            return;
        }

        System.out.println("✓ Player " + senderId + " made valid move");

        Network.MoveValidated validated = new Network.MoveValidated();
        validated.pieceId = move.pieceId;
        validated.leftValue = move.leftValue;
        validated.rightValue = move.rightValue;
        validated.placedOnLeft = move.placedOnLeft;
        validated.flipped = move.flipped;
        sender.sendTCP(validated);

        applyMoveToBoard(move);
        firstMoveMade = true;

        int other = (currentTurn + 1) % 2;
        Network.OpponentPlayed msg = new Network.OpponentPlayed();
        msg.pieceId = move.pieceId;
        msg.leftValue = move.leftValue;
        msg.rightValue = move.rightValue;
        msg.placedOnLeft = move.placedOnLeft;
        msg.flipped = move.flipped;

        System.out.println("Sending OpponentPlayed to player " + (other + 1));
        players[other].sendTCP(msg);

        currentTurn = other;
        System.out.println("Now it's Player " + (currentTurn + 1) + "'s turn");

        players[currentTurn].sendTCP(new Network.YourTurn());
        System.out.println("Sent YourTurn to Player " + (currentTurn + 1));

        printBoardState();
    }
    private void printBoardState() {
        System.out.println("=== BOARD STATE ===");
        System.out.println("Current turn: Player " + (currentTurn + 1));
        System.out.println("Left end: " + leftEndValue + " | Right end: " + rightEndValue);
        System.out.print("Board pieces: ");
        for (Network.Piece p : board) {
            System.out.print("[" + p.leftValue + "-" + p.rightValue + "] ");
        }
        System.out.println("\n==================");
    }

    private String validateMove(Network.PlayPiece move) {
        System.out.println("Validating move: [" + move.leftValue + "-" + move.rightValue +
                "] placedOnLeft=" + move.placedOnLeft + " flipped=" + move.flipped);

        int pieceLeftValue, pieceRightValue;

        if (move.flipped) {
            pieceLeftValue = move.rightValue;
            pieceRightValue = move.leftValue;
            System.out.println("  Piece flipped: Values swapped to [" + pieceLeftValue + "-" + pieceRightValue + "]");
        } else {
            pieceLeftValue = move.leftValue;
            pieceRightValue = move.rightValue;
            System.out.println("  Piece not flipped: Using [" + pieceLeftValue + "-" + pieceRightValue + "]");
        }

        if (!firstMoveMade) {
            System.out.println("  First move: Any piece is allowed");
            return null;
        }


        int pieceConnectingValue;

        if (move.placedOnLeft) {

            pieceConnectingValue = pieceRightValue;
            System.out.println("  Left placement: Right value " + pieceRightValue + " connects to board left end " + leftEndValue);
        } else {

            pieceConnectingValue = pieceLeftValue;
            System.out.println("  Right placement: Left value " + pieceLeftValue + " connects to board right end " + rightEndValue);
        }


        if (move.placedOnLeft) {
            if (pieceConnectingValue != leftEndValue) {
                return "Piece value " + pieceConnectingValue + " doesn't match left end value " + leftEndValue;
            }
        } else {
            if (pieceConnectingValue != rightEndValue) {
                return "Piece value " + pieceConnectingValue + " doesn't match right end value " + rightEndValue;
            }
        }

        System.out.println("  ✓ Valid: " + pieceConnectingValue + " matches board end");
        return null;
    }

    private void applyMoveToBoard(Network.PlayPiece move) {
        System.out.println("Applying move to board: [" + move.leftValue + "-" + move.rightValue +
                "] placedOnLeft=" + move.placedOnLeft + " flipped=" + move.flipped);


        int pieceLeftValue, pieceRightValue;

        if (move.flipped) {
            pieceLeftValue = move.rightValue;
            pieceRightValue = move.leftValue;
            System.out.println("  Piece flipped: Values swapped to [" + pieceLeftValue + "-" + pieceRightValue + "]");
        } else {
            pieceLeftValue = move.leftValue;
            pieceRightValue = move.rightValue;
            System.out.println("  Piece not flipped: Using [" + pieceLeftValue + "-" + pieceRightValue + "]");
        }

        Network.Piece boardPiece = new Network.Piece(
                move.pieceId, getImagePathForValues(move.leftValue, move.rightValue),
                move.leftValue, move.rightValue
        );

        if (board.isEmpty()) {
            board.add(boardPiece);
            leftEndValue = pieceLeftValue;
            rightEndValue = pieceRightValue;
            System.out.println("  First piece: Left end = " + leftEndValue + ", Right end = " + rightEndValue);

        } else if (move.placedOnLeft) {
            board.add(0, boardPiece);
            leftEndValue = pieceLeftValue;
            System.out.println("  Added to left: Connected " + pieceRightValue + " to board, new left end = " + leftEndValue);

        } else {
            board.add(boardPiece);
            rightEndValue = pieceRightValue;
            System.out.println("  Added to right: Connected " + pieceLeftValue + " to board, new right end = " + rightEndValue);
        }

        System.out.println("  Board ends: Left=" + leftEndValue + ", Right=" + rightEndValue);
    }

    private String getImagePathForValues(int left, int right) {
        if (left == 0 && right == 0) return "/Pieces/00.png";
        if (left == 1 && right == 0) return "/Pieces/10.png";
        if (left == 1 && right == 1) return "/Pieces/11.png";
        if (left == 1 && right == 2) return "/Pieces/12.png";
        if (left == 1 && right == 3) return "/Pieces/13.png";
        if (left == 1 && right == 4) return "/Pieces/14.png";
        if (left == 1 && right == 5) return "/Pieces/15.png";
        if (left == 1 && right == 6) return "/Pieces/16.png";

        if (left == 2 && right == 0) return "/Pieces/20.png";
        if (left == 2 && right == 2) return "/Pieces/22.png";
        if (left == 2 && right == 3) return "/Pieces/23.png";
        if (left == 2 && right == 4) return "/Pieces/24.png";
        if (left == 2 && right == 5) return "/Pieces/25.png";
        if (left == 2 && right == 6) return "/Pieces/26.png";

        if (left == 3 && right == 0) return "/Pieces/30.png";
        if (left == 3 && right == 3) return "/Pieces/33.png";
        if (left == 3 && right == 4) return "/Pieces/34.png";
        if (left == 3 && right == 5) return "/Pieces/35.png";
        if (left == 3 && right == 6) return "/Pieces/36.png";

        if (left == 4 && right == 0) return "/Pieces/40.png";
        if (left == 4 && right == 4) return "/Pieces/44.png";
        if (left == 4 && right == 5) return "/Pieces/45.png";
        if (left == 4 && right == 6) return "/Pieces/46.png";

        if (left == 5 && right == 0) return "/Pieces/50.png";
        if (left == 5 && right == 5) return "/Pieces/55.png";
        if (left == 5 && right == 6) return "/Pieces/56.png";

        if (left == 6 && right == 0) return "/Pieces/60.png";
        if (left == 6 && right == 6) return "/Pieces/66.png";

        return "/Pieces/00.png";
    }


    private void broadcast(Object msg) {
        for (int i = 0; i < connectedPlayers; i++) {
            if (players[i] != null) {
                players[i].sendTCP(msg);
            }
        }
    }

    private void resetGame() {
        players[0] = null;
        players[1] = null;
        connectedPlayers = 0;
        currentTurn = 0;
        board.clear();
        leftEndValue = -1;
        rightEndValue = -1;
        firstMoveMade = false;
        System.out.println("✓ Game reset. Waiting for new players...");
    }

    public static void startServer() {
        new Thread(() -> {
            try {
                if (instance == null) {
                    instance = new GameServer();
                }
            } catch (IOException e) {
                System.err.println("✗ Failed to start server: " + e.getMessage());
                e.printStackTrace();
            }
        }, "GameServer").start();
    }
}
