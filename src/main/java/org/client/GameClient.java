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
        void onOpponentPlayed(int pieceId);
    }

    private final Client client;
    private GameListener listener;

    public GameClient(String host) {
        client = new Client();
        Network.register(client.getKryo());

        client.start();

        try {
            client.connect(5000, host, Network.TCP_PORT, Network.UDP_PORT);
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to server", e);
        }

        client.addListener(new Listener() {

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Network.StartGame sg) {
                    Platform.runLater(() ->
                            listener.onStartGame(sg.playerNumber, sg.hand)
                    );
                }

                if (object instanceof Network.YourTurn) {
                    Platform.runLater(() ->
                            listener.onYourTurn()
                    );
                }

                if (object instanceof Network.OpponentPlayed op) {
                    Platform.runLater(() ->
                            listener.onOpponentPlayed(op.pieceId)
                    );
                }

            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("Disconnected from server.");
            }
        });
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }



    public void playCard(int pieceId) {
        Network.PlayPiece play = new Network.PlayPiece();
        play.pieceId = pieceId;
        client.sendTCP(play);
    }

    public void disconnect() {
        client.stop();
    }
}
