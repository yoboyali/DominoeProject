package org.client.scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.client.GameClient;
import org.shared.Network;

import java.util.ArrayList;
import java.util.List;

public class GameScene {

    private ImageView frameView;
    private ImageView feltView;
    private ImageView dinoFrameView;
    private ImageView piecesView;

    private final HBox handBox = new HBox(12);
    private final List<Network.Piece> hand = new ArrayList<>();

    private boolean myTurn = false;
    private final GameClient client;

    public GameScene(GameClient client) {
        this.client = client;
        loadAssets();
    }

    private void loadAssets() {
        feltView = new ImageView(
                getClass().getResource("/image.jpg").toExternalForm()
        );
        frameView = new ImageView(
                getClass().getResource("/Holder.png").toExternalForm()
        );
        piecesView = new ImageView(
                getClass().getResource("/p2.png").toExternalForm()
        );
        dinoFrameView = new ImageView(
                getClass().getResource("/Dino/frame.png").toExternalForm()
        );
    }

    public Scene createScene() {
        StackPane root = new StackPane();

        feltView.setFitWidth(1400);
        feltView.setFitHeight(800);
        feltView.setPreserveRatio(false);

        StackPane.setAlignment(frameView, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(piecesView, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(dinoFrameView, Pos.CENTER_LEFT);

        StackPane.setAlignment(handBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(handBox, new Insets(0, 0, 40, 0));

        root.getChildren().addAll(
                feltView,
                frameView,
                piecesView,
                dinoFrameView,
                handBox
        );

        return new Scene(root, 1400, 800);
    }


    public void setHand(List<Network.Piece> pieces) {
        hand.clear();

        if (pieces != null) {
            hand.addAll(pieces);
        }

        renderHand();
    }


    public void setMyTurn(boolean turn) {
        myTurn = turn;
        if (!myTurn) disableHand();
        else enableHand();
    }


    private void renderHand() {
        handBox.getChildren().clear();

        for (Network.Piece piece : hand) {
            ImageView view = new ImageView(
                    new Image(getClass().getResource(piece.imagePath).toExternalForm())
            );

            view.setFitWidth(90);
            view.setPreserveRatio(true);

            view.setOnMouseClicked(e -> {
                if (!myTurn) return;
                client.playCard(piece.id);
                myTurn = false;
                disableHand();
            });

            handBox.getChildren().add(view);
        }
    }

    private void disableHand() {
        handBox.setDisable(true);
        handBox.setOpacity(0.6);
    }

    private void enableHand() {
        handBox.setDisable(false);
        handBox.setOpacity(1.0);
    }
    public void onOpponentPlayed(int pieceId) {
        // update board / log / animation / etc.
    }

}
