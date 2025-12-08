package org.server;

import com.esotericsoftware.kryonet.*;
import org.shared.Network;

import java.io.IOException;

public class GameServer {

    private Server server;
    private Connection[] players = new Connection[2];
    private int connectedPlayers = 0;
    private int currentTurn = 0;  // 0 = Player1, 1 = Player2

    public GameServer() throws IOException {
        server = new Server();
        server.start();
        server.bind(Network.TCP_PORT, Network.UDP_PORT);
        //server.bind(54555, 54777);  // TCP, UDP ports
        Network.register(server.getKryo());

        System.out.println("Server started. Waiting for players...");

        server.addListener(new Listener() {
            @Override
            public void received(Connection c, Object object) {

                // RECIEVE MOVE
                if (object instanceof Network.PlayPiece) {
                    Network.PlayPiece move = (Network.PlayPiece) object;

                    System.out.println("Player " + (currentTurn+1) + " played: " + move.piece);

                    // Send to other player
                    int other = (currentTurn == 0 ? 1 : 0);
                    players[other].sendTCP(new Network.OpponentPlayed() {{
                        piece = move.piece;
                    }});

                    // Switch Turns
                    currentTurn = other;
                    players[currentTurn].sendTCP(new Network.YourTurn());
                }
            }

            @Override
            public void connected(Connection c) {
                if (connectedPlayers >= 2) {
                    System.out.println("A third player tried to join. Rejecting.");
                    c.close();
                    return;
                }

                players[connectedPlayers++] = c;
                System.out.println("A player connected! (" + connectedPlayers + "/2)");

                if (connectedPlayers == 2) startGame();
            }

            @Override
            public void disconnected(Connection c) {
                System.out.println("A player disconnected. Game over.");
                broadcast(new Network.GameOver());
            }
        });
    }

    private void startGame() {
        System.out.println("Both players connected. Starting game.");

        // Tell each player their number
        for (int i = 0; i < 2; i++) {
            Network.StartGame sg = new Network.StartGame();
            sg.playerNumber = i + 1;
            players[i].sendTCP(sg);
        }

        // Player 1 begins
        players[0].sendTCP(new Network.YourTurn());
    }

    private void broadcast(Object message) {
        for (int i = 0; i < connectedPlayers; i++) {
            players[i].sendTCP(message);
        }
    }

    public static void main(String[] args) {
        try {
            new GameServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
