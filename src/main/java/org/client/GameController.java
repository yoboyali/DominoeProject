
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
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
        }

        connect("localhost");
    }

    public void joinGame(Stage stage, String ip) {
        this.stage = stage;
        connect(ip);
    }

    private void connect(String ip) {
        new Thread(() -> {
            try {
                System.out.println("GameController: Connecting to " + ip);

                // Create GameScene with empty constructor
                gameScene = new GameScene();
                System.out.println("GameController: GameScene created");

                // Create GameClient with listener
                client = new GameClient(ip, new GameClient.GameListener() {
                    @Override
                    public void onStartGame(int playerNumber, List<Network.Piece> hand) {
                        Platform.runLater(() -> {
                            System.out.println("GameController: Setting hand and scene");
                            gameScene.setHand(hand);
                            gameScene.setClient(client); // SET CLIENT HERE!
                            SceneManager.setGameScene(gameScene.createScene());
                            stage.show();
                        });
                    }

                    @Override
                    public void onYourTurn() {
                        Platform.runLater(() -> {
                            gameScene.setMyTurn(true);
                        });
                    }

                    @Override
                    public void onOpponentPlayed(int pieceId, int leftValue, int rightValue, boolean placedOnLeft, boolean flipped) {
                        Platform.runLater(() -> {
                            gameScene.onOpponentPlayed(pieceId, leftValue, rightValue, placedOnLeft, flipped);
                            gameScene.setMyTurn(true);
                        });
                    }
                });

                System.out.println("GameController: Connection successful!");

            } catch (Exception e) {
                System.err.println("GameController: Connection failed: " + e.getMessage());
                Platform.runLater(() -> {
                    // Show error and go back to join scene
                    SceneManager.setJoinScene();
                });
            }
        }, "GameClient-Connector").start();
    }

    public void disconnect() {
        if (client != null) {
            client.disconnect();
        }
    }
}
