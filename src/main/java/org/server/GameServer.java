
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
    private final List<Network.Piece>[] playerHands = new ArrayList[2];
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
        playerHands[0] = new ArrayList<>();
        playerHands[1] = new ArrayList<>();

        server.addListener(new Listener() {
            @Override
            public void connected(Connection c) {
                System.out.println("✓ Player " + c.getID() + " connected (" + (connectedPlayers + 1) + "/2)");

                if (connectedPlayers >= 2) {
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

                if (obj instanceof Network.PlayPiece move) {
                    handleMove(c, move);
                }
                if (obj instanceof Network.DrawPiece drawRequest) {
                    handleDrawPiece(c);
                }
            }

            @Override
            public void disconnected(Connection c) {
                if (connectedPlayers > 0) {
                    int other = (currentTurn + 1) % 2;
                    // broadcast(new Network.GameOver());
                   // resetGame();
                    System.out.println("Server closing");
                    players[other].sendTCP(new Network.GameOver());
                    server.stop();
                    server.close();

                }
            }

            @Override
            public void idle(Connection c) {
                c.sendTCP(new com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive());
            }
        });

        server.bind(Network.TCP_PORT, Network.UDP_PORT);

        server.start();

    }

    private int getPlayerId(Connection c) {
        for (int i = 0; i < 2; i++) {
            if (players[i] == c) return i + 1;
        }
        return 0;
    }
    private void handleDrawPiece(Connection sender) {
        int playerId = getPlayerId(sender);

        Network.PieceDrawn response = new Network.PieceDrawn();

        if (drawPile.isEmpty()) {
            response.successful = false;
        } else {
            Network.Piece drawnPiece = drawPile.remove(0);
            response.piece = drawnPiece;
            response.successful = true;
            playerHands[playerId - 1].add(drawnPiece);

        }

        sender.sendTCP(response);
    }

    private void startGame() {

        Collections.shuffle(drawPile);

        for (int i = 0; i < 2; i++) {
            playerHands[i].clear();
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
                    playerHands[i].add(piece);
                }
            }

            players[i].sendTCP(hand);
        }


        players[0].sendTCP(new Network.YourTurn());
    }
    private void handleMove(Connection sender, Network.PlayPiece move) {
        int senderId = getPlayerId(sender);
        if (players[currentTurn] != sender) {
            Network.MoveInvalid invalid = new Network.MoveInvalid();
            invalid.reason = "Not your turn!";
            sender.sendTCP(invalid);
            return;
        }

        String validationError = validateMove(move);
        if (validationError != null) {
            Network.MoveInvalid invalid = new Network.MoveInvalid();
            invalid.reason = validationError;
            sender.sendTCP(invalid);
            return;
        }

        removePieceFromPlayerHand(senderId, move.pieceId);
        Network.MoveValidated validated = new Network.MoveValidated();
        validated.pieceId = move.pieceId;
        validated.leftValue = move.leftValue;
        validated.rightValue = move.rightValue;
        validated.placedOnLeft = move.placedOnLeft;
        validated.flipped = move.flipped;
        sender.sendTCP(validated);

        applyMoveToBoard(move);
        firstMoveMade = true;
        checkForwin(senderId);
        int other = (currentTurn + 1) % 2;
        Network.OpponentPlayed msg = new Network.OpponentPlayed();
        msg.pieceId = move.pieceId;
        msg.leftValue = move.leftValue;
        msg.rightValue = move.rightValue;
        msg.placedOnLeft = move.placedOnLeft;
        msg.flipped = move.flipped;

        players[other].sendTCP(msg);

        currentTurn = other;

        players[currentTurn].sendTCP(new Network.YourTurn());

    }
    private void removePieceFromPlayerHand(int playerId, int pieceId) {
        int playerIndex = playerId - 1;

        for (int i = 0; i < playerHands[playerIndex].size(); i++) {
            if (playerHands[playerIndex].get(i).id == pieceId) {
                Network.Piece removed = playerHands[playerIndex].remove(i);
                System.out.println("Removed piece " + pieceId + " from Player " + playerId + "'s hand. Hand size: " + playerHands[playerIndex].size());
                return;
            }
        }

        System.out.println("WARNING: Could not find piece " + pieceId + " in Player " + playerId + "'s hand!");
    }

    private String validateMove(Network.PlayPiece move) {

        int pieceLeftValue, pieceRightValue;

        if (move.flipped) {
            pieceLeftValue = move.rightValue;
            pieceRightValue = move.leftValue;
        } else {
            pieceLeftValue = move.leftValue;
            pieceRightValue = move.rightValue;
        }

        if (!firstMoveMade) {
            return null;
        }


        int pieceConnectingValue;

        if (move.placedOnLeft) {

            pieceConnectingValue = pieceRightValue;
        } else {

            pieceConnectingValue = pieceLeftValue;
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

        return null;
    }

    private void applyMoveToBoard(Network.PlayPiece move) {

        int pieceLeftValue, pieceRightValue;

        if (move.flipped) {
            pieceLeftValue = move.rightValue;
            pieceRightValue = move.leftValue;
        } else {
            pieceLeftValue = move.leftValue;
            pieceRightValue = move.rightValue;
        }

        Network.Piece boardPiece = new Network.Piece(
                move.pieceId, getImagePathForValues(move.leftValue, move.rightValue),
                move.leftValue, move.rightValue
        );

        if (board.isEmpty()) {
            board.add(boardPiece);
            leftEndValue = pieceLeftValue;
            rightEndValue = pieceRightValue;

        } else if (move.placedOnLeft) {
            board.add(0, boardPiece);
            leftEndValue = pieceLeftValue;

        } else {
            board.add(boardPiece);
            rightEndValue = pieceRightValue;
        }

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

    private void checkForwin(int senderId){
        if (playerHands[senderId - 1].isEmpty()) {
            System.out.println("🎉 Player " + senderId + " has WON the game! No pieces left.");

            Network.GameWon gameWon = new Network.GameWon();
            gameWon.winnerPlayerNumber = senderId;
            gameWon.reason = "Player " + senderId + " has no pieces left!";
            broadcast(gameWon);
            return;
        }

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
