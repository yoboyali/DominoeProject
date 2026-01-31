
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

    // Game board state
    private final List<Network.Piece> board = new ArrayList<>();
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
        return pieces;
    }

    public GameServer() throws IOException {
        server = new Server();
        Network.register(server.getKryo());

        // Allow unregistered classes for FrameworkMessages
        server.getKryo().setRegistrationRequired(false);

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
                // Handle KeepAlive messages silently
                if (obj instanceof com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive) {
                    // Echo back to keep connection alive
                    c.sendTCP(new com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive());
                    return;
                }

                System.out.println("Server: Received from connection " + c.getID() + ": " + obj.getClass().getSimpleName());

                if (obj instanceof Network.PlayPiece move) {
                    handleMove(c, move);
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
                // Send keepalive to prevent timeout
                c.sendTCP(new com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive());
            }
        });

        // Bind with larger buffer sizes
        server.bind(Network.TCP_PORT, Network.UDP_PORT);

        // Start the server
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

    private void startGame() {
        System.out.println("✓ Both players connected. Starting game.");

        List<Network.Piece> shuffledPieces = new ArrayList<>(allPieces);
        Collections.shuffle(shuffledPieces);

        for (int i = 0; i < 2; i++) {
            Network.StartGame sg = new Network.StartGame();
            sg.playerNumber = i + 1;
            sg.hand = new ArrayList<>();
            players[i].sendTCP(sg);

            Network.InitialHand hand = new Network.InitialHand();
            hand.pieces = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                int pieceIndex = i * 3 + j;
                if (pieceIndex < shuffledPieces.size()) {
                    hand.pieces.add(shuffledPieces.get(pieceIndex));
                }
            }
            players[i].sendTCP(hand);
        }

        players[0].sendTCP(new Network.YourTurn());
        System.out.println("Game started. Player 1's turn.");
    }

    private void handleMove(Connection sender, Network.PlayPiece move) {
        int senderId = getPlayerId(sender);
        if (players[currentTurn] != sender) {
            System.out.println("Invalid: Not player's turn! Expected player " + (currentTurn + 1) +
                    ", but player " + senderId + " tried to move");
            return;
        }

        String validationError = validateMove(move);
        if (validationError != null) {
            System.out.println("Invalid move by player " + senderId + ": " + validationError);
            sender.sendTCP(new Network.YourTurn()); // Invalid move, same player tries again
            return;
        }

        System.out.println("✓ Player " + senderId + " made valid move");
        applyMoveToBoard(move);
        firstMoveMade = true;

        // Send move to opponent
        int other = (currentTurn + 1) % 2;
        Network.OpponentPlayed msg = new Network.OpponentPlayed();
        msg.pieceId = move.pieceId;
        msg.leftValue = move.leftValue;
        msg.rightValue = move.rightValue;
        msg.placedOnLeft = move.placedOnLeft;
        msg.flipped = move.flipped;

        System.out.println("Sending OpponentPlayed to player " + (other + 1));
        players[other].sendTCP(msg);

        // Switch turns
        currentTurn = other;
        System.out.println("Now it's Player " + (currentTurn + 1) + "'s turn");

        // Send YourTurn to the next player
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

        // FIRST MOVE: Allow ANY piece (not just doubles)
        if (!firstMoveMade) {
            System.out.println("  First move: Any piece is allowed");
            return null; // Any piece can start the game
        }

        // SUBSEQUENT MOVES: Must match board ends
        // Determine which value connects to the board
        int pieceConnectingValue;

        if (move.placedOnLeft) {
            // Placing on LEFT end of board
            if (move.flipped) {
                // Piece is horizontal: When flipped and placed on left, the RIGHT value connects
                pieceConnectingValue = move.rightValue;
                System.out.println("  Left placement, flipped: Right value " + move.rightValue + " connects");
            } else {
                // Piece is vertical: When not flipped and placed on left, the LEFT value connects
                pieceConnectingValue = move.leftValue;
                System.out.println("  Left placement, not flipped: Left value " + move.leftValue + " connects");
            }

            if (pieceConnectingValue != leftEndValue) {
                return "Piece value " + pieceConnectingValue + " doesn't match left end value " + leftEndValue;
            }
        } else {
            // Placing on RIGHT end of board
            if (move.flipped) {
                // Piece is horizontal: When flipped and placed on right, the LEFT value connects
                pieceConnectingValue = move.leftValue;
                System.out.println("  Right placement, flipped: Left value " + move.leftValue + " connects");
            } else {
                // Piece is vertical: When not flipped and placed on right, the RIGHT value connects
                pieceConnectingValue = move.rightValue;
                System.out.println("  Right placement, not flipped: Right value " + move.rightValue + " connects");
            }

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

        Network.Piece boardPiece = new Network.Piece(
                move.pieceId, getImagePathForValues(move.leftValue, move.rightValue),
                move.leftValue, move.rightValue
        );

        if (board.isEmpty()) {
            // FIRST MOVE: Place the piece
            board.add(boardPiece);

            // Set both ends based on the piece's orientation
            if (move.flipped) {
                // When flipped, piece is horizontal
                // For first move, we need to decide which end is left/right
                // Let's say leftValue goes to left end, rightValue to right end
                leftEndValue = move.leftValue;
                rightEndValue = move.rightValue;
                System.out.println("  First piece (flipped): Left end = " + leftEndValue + ", Right end = " + rightEndValue);
            } else {
                // When not flipped, piece is vertical
                // For dominoes, we treat top as left, bottom as right
                leftEndValue = move.leftValue;
                rightEndValue = move.rightValue;
                System.out.println("  First piece (not flipped): Left end = " + leftEndValue + ", Right end = " + rightEndValue);
            }

        } else if (move.placedOnLeft) {
            board.add(0, boardPiece);

            // Update left end: The NEW left end is the OTHER value of the piece
            if (move.flipped) {
                // When flipped on left: rightValue connected, so leftValue becomes new end
                leftEndValue = move.leftValue;
                System.out.println("  Added to left (flipped): New left end = " + leftEndValue);
            } else {
                // When not flipped on left: leftValue connected, so rightValue becomes new end
                leftEndValue = move.rightValue;
                System.out.println("  Added to left (not flipped): New left end = " + leftEndValue);
            }
        } else {
            board.add(boardPiece);

            // Update right end: The NEW right end is the OTHER value of the piece
            if (move.flipped) {
                // When flipped on right: leftValue connected, so rightValue becomes new end
                rightEndValue = move.rightValue;
                System.out.println("  Added to right (flipped): New right end = " + rightEndValue);
            } else {
                // When not flipped on right: rightValue connected, so leftValue becomes new end
                rightEndValue = move.leftValue;
                System.out.println("  Added to right (not flipped): New right end = " + rightEndValue);
            }
        }

        System.out.println("  Board ends: Left=" + leftEndValue + ", Right=" + rightEndValue);
    }

    private String getImagePathForValues(int left, int right) {
        if (left == 0 && right == 0) return "/Pieces/00.png";
        if (left == 1 && right == 0) return "/Pieces/10.png";
        if (left == 2 && right == 0) return "/Pieces/20.png";
        if (left == 3 && right == 0) return "/Pieces/30.png";
        if (left == 4 && right == 0) return "/Pieces/40.png";
        if (left == 5 && right == 0) return "/Pieces/50.png";
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
