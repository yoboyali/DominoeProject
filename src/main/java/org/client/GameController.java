package org.client;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.client.scenes.GameScene;
import org.server.GameServer;
import org.shared.Network;

import java.util.List;

public class GameController {

    private static GameController instance;

    private Stage stage;
    private GameClient client;
    private GameScene gameScene;

    private boolean serverStarted = false;

    public GameController() {}

    public static GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }


    public void hostGame(Stage stage) {
        this.stage = stage;

        if (!serverStarted) {
            serverStarted = true;
            GameServer.startServer();
        }

        connect("localhost");
    }

    public void joinGame(Stage stage, String ip) {
        this.stage = stage;
        connect(ip);
    }


    private void connect(String ip) {
        client = new GameClient(ip);
        gameScene = new GameScene(client);

        client.setListener(new GameClient.GameListener() {

            @Override
            public void onStartGame(int playerNumber, List<Network.Piece> hand) {
                Platform.runLater(() -> {
                    gameScene.setHand(hand);
                    stage.setScene(gameScene.createScene());
                    stage.sizeToScene();
                    stage.centerOnScreen();
                });
            }

            @Override
            public void onYourTurn() {
                Platform.runLater(() ->
                        gameScene.setMyTurn(true)
                );
            }

            @Override
            public void onOpponentPlayed(int pieceId) {
                Platform.runLater(() -> {
                    gameScene.onOpponentPlayed(pieceId);
                    gameScene.setMyTurn(true);
                });
            }
        });
    }


    public void disconnect() {
        if (client != null) {
            client.disconnect();
        }
    }
}
