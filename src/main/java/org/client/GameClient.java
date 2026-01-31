
package org.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javafx.application.Platform;
import org.shared.Network;

import java.io.IOException;
import java.util.List;

public class GameClient {

    public interface GameListener {
        void onStartGame(int playerNumber, List<Network.Piece> hand);
        void onYourTurn();
        void onOpponentPlayed(int pieceId, int leftValue, int rightValue, boolean placedOnLeft, boolean flipped);
    }

    private final Client client;
    private final GameListener listener;
    private int playerNumber = 0;

    public GameClient(String host, GameListener listener) throws IOException {
        this.listener = listener;

        client = new Client();
        Network.register(client.getKryo());

        System.out.println("Client: Network classes registered");

        // Set write buffer sizes to prevent timeouts
        client.getKryo().setRegistrationRequired(false);

        // Start client with larger buffer sizes
        client.start();

        // Set TCP_NO_DELAY for faster response (optional)
        client.setKeepAliveTCP(10000); // Send keepalive every 10 seconds

        // Add connection listener for debugging
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                System.out.println("Client: Connected to server successfully");
            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("Client: Disconnected from server");
            }

            @Override
            public void idle(Connection connection) {
                // Send a small message to keep connection alive
                // This prevents timeout
                connection.sendTCP(new com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive());
            }
        });

        // Add message listener
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                // Handle KeepAlive messages but don't log them
                if (object instanceof com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive) {
                    return;
                }

                System.out.println("GameClient: Received game message: " + object.getClass().getSimpleName());

                if (object instanceof Network.StartGame sg) {
                    System.out.println("GameClient: StartGame received for player " + sg.playerNumber);
                    playerNumber = sg.playerNumber;
                    // DO NOT call onStartGame here - wait for InitialHand
                    // This StartGame has empty hand anyway
                }

                if (object instanceof Network.InitialHand hand) {
                    System.out.println("GameClient: InitialHand received with " + hand.pieces.size() + " pieces");

                    for (Network.Piece piece : hand.pieces) {
                        System.out.println(
                                "Piece ID: " + piece.id +
                                        " | Values: [" + piece.leftValue + "-" + piece.rightValue + "]" +
                                        " | Image: " + piece.imagePath
                        );
                    }

                    Platform.runLater(() -> {
                        System.out.println("GameClient: Calling listener.onStartGame with actual hand");
                        listener.onStartGame(playerNumber, hand.pieces);
                    });
                }

                if (object instanceof Network.YourTurn) {
                    System.out.println("GameClient: YourTurn received - It's MY turn now!");
                    Platform.runLater(listener::onYourTurn);
                }

                if (object instanceof Network.OpponentPlayed op) {
                    System.out.println("GameClient: OpponentPlayed received - Opponent played piece " + op.pieceId);
                    System.out.println("  Details: [" + op.leftValue + "-" + op.rightValue +
                            "] placedOnLeft=" + op.placedOnLeft + " flipped=" + op.flipped);
                    Platform.runLater(() ->
                            listener.onOpponentPlayed(op.pieceId, op.leftValue, op.rightValue, op.placedOnLeft, op.flipped)
                    );
                }

                if (object instanceof Network.GameOver) {
                    System.out.println("GameClient: GameOver received");
                }
            }
        });

        try {
            System.out.println("Client: Attempting to connect to " + host + ":" + Network.TCP_PORT);

            // Use longer timeout for connection
            client.connect(10000, host, Network.TCP_PORT, Network.UDP_PORT);

            System.out.println("✓ Connected to server at " + host);

            // Send initial keepalive
            client.sendTCP(new com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive());

        } catch (IOException e) {
            System.err.println("✗ Failed to connect to " + host + ":" + Network.TCP_PORT);
            throw new IOException("Failed to connect to " + host + ":" + Network.TCP_PORT, e);
        }
    }

    public void playCard(int pieceId, int leftValue, int rightValue, boolean placedOnLeft, boolean flipped) {
        Network.PlayPiece play = new Network.PlayPiece();
        play.pieceId = pieceId;
        play.leftValue = leftValue;
        play.rightValue = rightValue;
        play.placedOnLeft = placedOnLeft;
        play.flipped = flipped;
        client.sendTCP(play);
        System.out.println("Sent piece to server: ID=" + pieceId +
                " [" + leftValue + "-" + rightValue + "]" +
                " placedOnLeft=" + placedOnLeft + " flipped=" + flipped);
    }

    public void disconnect() {
        if (client != null) {
            System.out.println("Client: Manually disconnecting");
            client.stop();
        }
    }
}
