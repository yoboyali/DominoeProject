
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


        client.getKryo().setRegistrationRequired(false);

        client.start();

        client.setKeepAliveTCP(10000);

        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
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


                if (object instanceof Network.StartGame sg) {
                    playerNumber = sg.playerNumber;
                }

                if (object instanceof Network.InitialHand hand) {
                    Platform.runLater(() -> {
                        listener.onStartGame(playerNumber, hand.pieces);
                    });
                }

                if (object instanceof Network.YourTurn) {
                    Platform.runLater(listener::onYourTurn);
                }

                if (object instanceof Network.OpponentPlayed op) {
                    Platform.runLater(() ->
                            listener.onOpponentPlayed(op.pieceId, op.leftValue, op.rightValue, op.placedOnLeft, op.flipped)
                    );
                }

                if (object instanceof Network.MoveValidated mv) {
                    Platform.runLater(() -> {
                        if (listener instanceof ExtendedGameListener) {
                            ((ExtendedGameListener) listener).onMoveValidated(
                                    mv.pieceId, mv.leftValue, mv.rightValue, mv.placedOnLeft, mv.flipped
                            );
                        }
                    });
                }

                if (object instanceof Network.MoveInvalid mi) {
                    Platform.runLater(() -> {
                        if (listener instanceof ExtendedGameListener) {
                            ((ExtendedGameListener) listener).onMoveInvalid(mi.reason);
                        }
                    });
                }
                if (object instanceof Network.PieceDrawn pd) {
                    Platform.runLater(() -> {
                        if (listener instanceof ExtendedGameListener) {
                            ((ExtendedGameListener) listener).onPieceDrawn(pd.piece, pd.successful);
                        }
                    });
                }

                if (object instanceof Network.GameOver) {
                }
            }
        });

        try {

            client.connect(10000, host, Network.TCP_PORT, Network.UDP_PORT);

            client.sendTCP(new com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive());

        } catch (IOException e) {
            System.err.println("✗ Failed to connect to " + host + ":" + Network.TCP_PORT);
            throw new IOException("Failed to connect to " + host + ":" + Network.TCP_PORT, e);
        }
    }

    public void drawPiece() {
        Network.DrawPiece draw = new Network.DrawPiece();
        client.sendTCP(draw);
    }
    public void playCard(int pieceId, int leftValue, int rightValue, boolean placedOnLeft, boolean flipped) {
        Network.PlayPiece play = new Network.PlayPiece();
        play.pieceId = pieceId;
        play.leftValue = leftValue;
        play.rightValue = rightValue;
        play.placedOnLeft = placedOnLeft;
        play.flipped = flipped;
        client.sendTCP(play);

    }

    public void disconnect() {
        if (client != null) {
            client.stop();
        }
    }
}
