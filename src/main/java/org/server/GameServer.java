package org.server;

import com.esotericsoftware.kryonet.*;
import org.shared.Network;

import java.io.IOException;
import java.util.List;

public class GameServer {

    private final Server server;
    private final Connection[] players = new Connection[2];
    private int connectedPlayers = 0;
    private int currentTurn = 0;

    public GameServer() throws IOException {
        server = new Server();
        Network.register(server.getKryo());

        server.addListener(new Listener() {

            @Override
            public void connected(Connection c) {
                if (connectedPlayers >= 2) {
                    c.close();
                    return;
                }

                players[connectedPlayers++] = c;
                System.out.println("Player connected (" + connectedPlayers + "/2)");

                if (connectedPlayers == 2) {
                    startGame();
                }
            }

            @Override
            public void received(Connection c, Object obj) {
                if (obj instanceof Network.PlayPiece move) {
                    handleMove(c, move);
                }
            }

            @Override
            public void disconnected(Connection c) {
                System.out.println("Player disconnected.");
                broadcast(new Network.GameOver());
            }
        });

        server.bind(Network.TCP_PORT, Network.UDP_PORT);
        server.start();

        System.out.println("Server started");
    }

    private void startGame() {
        System.out.println("Both players connected. Starting game.");

        for (int i = 0; i < 2; i++) {
            Network.StartGame sg = new Network.StartGame();
            sg.playerNumber = i + 1;
            players[i].sendTCP(sg);

            Network.InitialHand hand = new Network.InitialHand();
            hand.pieces = List.of(
                    new Network.Piece(1, "/cards/card1.png"),
                    new Network.Piece(2, "/cards/card2.png"),
                    new Network.Piece(3, "/cards/card3.png")
            );

            players[i].sendTCP(hand);
        }

        players[0].sendTCP(new Network.YourTurn());
    }


    private void handleMove(Connection sender, Network.PlayPiece move) {
        if (players[currentTurn] != sender) return;

        int other = (currentTurn + 1) % 2;

        Network.OpponentPlayed msg = new Network.OpponentPlayed();
        msg.pieceId = move.pieceId;
        players[other].sendTCP(msg);

        currentTurn = other;
        players[currentTurn].sendTCP(new Network.YourTurn());
    }

    private void broadcast(Object msg) {
        for (int i = 0; i < connectedPlayers; i++) {
            players[i].sendTCP(msg);
        }
    }

    public static void startServer() {
        new Thread(() -> {
            try {
                new GameServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "GameServer").start();
    }
}
