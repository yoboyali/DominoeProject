
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
    public interface ExtendedGameListener extends GameListener {
        void onMoveValidated(int pieceId, int leftValue, int rightValue, boolean placedOnLeft, boolean flipped);
        void onMoveInvalid(String reason);
        void onPieceDrawn(Network.Piece piece, boolean successful);
    }

    private final Client client;
    private final GameListener listener;
    private int playerNumber = 0;

    public GameClient(String host, GameListener listener) throws IOException {
        this.listener = listener;

        client = new Client();
        Network.register(client.getKryo());

        System.out.println("Client: Network classes registered");

        client.getKryo().setRegistrationRequired(false);

        client.start();

        client.setKeepAliveTCP(10000);

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

                connection.sendTCP(new com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive());
            }
        });

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive) {
                    return;
                }

                System.out.println("GameClient: Received game message: " + object.getClass().getSimpleName());

                if (object instanceof Network.StartGame sg) {
                    System.out.println("GameClient: StartGame received for player " + sg.playerNumber);
                    playerNumber = sg.playerNumber;
                }

                if (object instanceof Network.InitialHand hand) {
                    System.out.println("GameClient: InitialHand received with " + hand.pieces.size() + " pieces");
                    Platform.runLater(() -> {
                        listener.onStartGame(playerNumber, hand.pieces);
                    });
                }

                if (object instanceof Network.YourTurn) {
                    System.out.println("GameClient: YourTurn received - It's MY turn now!");
                    Platform.runLater(listener::onYourTurn);
                }

                if (object instanceof Network.OpponentPlayed op) {
                    System.out.println("GameClient: OpponentPlayed received - Opponent played piece " + op.pieceId);
                    Platform.runLater(() ->
                            listener.onOpponentPlayed(op.pieceId, op.leftValue, op.rightValue, op.placedOnLeft, op.flipped)
                    );
                }

                if (object instanceof Network.MoveValidated mv) {
                    System.out.println("GameClient: MoveValidated for piece " + mv.pieceId);
                    Platform.runLater(() -> {
                        if (listener instanceof ExtendedGameListener) {
                            ((ExtendedGameListener) listener).onMoveValidated(
                                    mv.pieceId, mv.leftValue, mv.rightValue, mv.placedOnLeft, mv.flipped
                            );
                        }
                    });
                }

                if (object instanceof Network.MoveInvalid mi) {
                    System.out.println("GameClient: MoveInvalid - Reason: " + mi.reason);
                    Platform.runLater(() -> {
                        if (listener instanceof ExtendedGameListener) {
                            ((ExtendedGameListener) listener).onMoveInvalid(mi.reason);
                        }
                    });
                }
                if (object instanceof Network.PieceDrawn pd) {
                    System.out.println("GameClient: Received PieceDrawn response");
                    Platform.runLater(() -> {
                        if (listener instanceof ExtendedGameListener) {
                            ((ExtendedGameListener) listener).onPieceDrawn(pd.piece, pd.successful);
                        }
                    });
                }

                if (object instanceof Network.GameOver) {
                    System.out.println("GameClient: GameOver received");
                }
            }
        });

        try {
            System.out.println("Client: Attempting to connect to " + host + ":" + Network.TCP_PORT);

            client.connect(10000, host, Network.TCP_PORT, Network.UDP_PORT);

            System.out.println("✓ Connected to server at " + host);

            client.sendTCP(new com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive());

        } catch (IOException e) {
            System.err.println("✗ Failed to connect to " + host + ":" + Network.TCP_PORT);
            throw new IOException("Failed to connect to " + host + ":" + Network.TCP_PORT, e);
        }
    }
    public void drawPiece() {
        Network.DrawPiece draw = new Network.DrawPiece();
        client.sendTCP(draw);
        System.out.println("Sent draw request to server");
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
