
package org.client.scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.client.GameClient;
import org.shared.Network;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameScene {

    private ImageView frameView;
    private ImageView boardView;
    String videoPath;
    Media backGroundVid;
    MediaPlayer mediaPlayer;
    MediaView mediaView;

    private final HBox handBox = new HBox(12);
    private final List<Network.Piece> hand = new ArrayList<>();

    // Game board to display played pieces
    private final HBox boardBox = new HBox(5);
    private final List<Network.Piece> playedPieces = new ArrayList<>();

    private boolean myTurn = false;
    private GameClient client;

    // Track the last piece we tried to play
    private Network.Piece lastAttemptedPiece = null;


    public GameScene() {
        System.out.println("GameScene: Constructor called");
        loadAssets();
        System.out.println("GameScene: Constructor completed");
    }

    public void setClient(GameClient client) {
        this.client = client;
        System.out.println("GameScene: Client set");
    }

    private void loadAssets() {
        try {
            // Load frame image
            frameView = new ImageView(
                    getClass().getResource("/Holder.png").toExternalForm()
            );

            boardView = new ImageView(
                    getClass().getResource("/board.png").toExternalForm()
            );
            // Get video path
            videoPath = new File("/Users/alihamdy/IdeaProjects/DominoeProject/src/main/resources/Background.mp4").toURI().toString();

            // FIX: Create Media object first
            backGroundVid = new Media(videoPath);  // This was missing!

            // Now create the player with the media
            mediaPlayer = new MediaPlayer(backGroundVid);
            mediaView = new MediaView(mediaPlayer);

            mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
            mediaPlayer.play();

            System.out.println("✓ Loaded background assets");
        } catch (Exception e) {
            System.err.println("✗ Failed to load background assets: " + e.getMessage());
            e.printStackTrace();  // Add this to see full stack trace
        }
    }

    public Scene createScene() {
        StackPane root = new StackPane();


        boardBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(boardBox, Pos.CENTER);
        boardView.setOpacity(0.3);
        StackPane.setAlignment(boardView , Pos.CENTER);
        handBox.setAlignment(Pos.BOTTOM_CENTER);
        StackPane.setAlignment(handBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(handBox, new Insets(0, 0, 20, 0));

        StackPane.setAlignment(frameView, Pos.BOTTOM_CENTER);

        root.getChildren().addAll(
                mediaView,
                boardView,
                frameView,
                boardBox,
                handBox
        );

        boardBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1); -fx-background-radius: 10;");
        boardBox.setPadding(new Insets(10));
        boardBox.setSpacing(10);
        handBox.setSpacing(15);

        renderHand();

        return new Scene(root, 1400, 800);
    }

    public void setHand(List<Network.Piece> pieces) {
        System.out.println("=== Setting hand in GameScene ===");
        System.out.println("Received " + (pieces != null ? pieces.size() : 0) + " pieces");

        hand.clear();

        if (pieces != null) {
            hand.addAll(pieces);
            for (Network.Piece piece : pieces) {
                System.out.println("Piece: ID=" + piece.id +
                        ", Values=[" + piece.leftValue + "-" + piece.rightValue +
                        "], Path=" + piece.imagePath);
            }
        }

        renderHand();
    }

    public void setMyTurn(boolean turn) {
        System.out.println("GameScene: My turn = " + turn);
        myTurn = turn;

        if (myTurn) {
            // If we get our turn back, but last attempted piece is still in hand,
            // it means the server rejected our move
            if (lastAttemptedPiece != null && hand.contains(lastAttemptedPiece)) {
                System.out.println("Server rejected our move with piece " + lastAttemptedPiece.id);
                showAlert("Invalid Move",
                        "You cannot play [" + lastAttemptedPiece.leftValue + "-" + lastAttemptedPiece.rightValue +
                                "] there. Try a different piece or placement.");
                lastAttemptedPiece = null;
            }
            enableHand();
        } else {
            disableHand();
        }
    }

    private void renderHand() {
        System.out.println("=== Rendering hand ===");
        System.out.println("Hand size: " + hand.size());

        handBox.getChildren().clear();

        for (Network.Piece piece : hand) {
            System.out.println("Loading image for piece ID " + piece.id + ": " + piece.imagePath);

            try {
                String imagePath = piece.imagePath;
                if (!imagePath.startsWith("/")) {
                    imagePath = "/" + imagePath;
                }

                System.out.println("Final image path: " + imagePath);

                Image image = new Image(getClass().getResource(imagePath).toExternalForm());

                if (image.isError()) {
                    throw new RuntimeException("Image failed to load");
                }

                ImageView view = new ImageView(image);
                view.setFitWidth(100);
                view.setFitHeight(150);
                view.setPreserveRatio(true);

                view.setOnMouseEntered(e -> {
                    if (myTurn) {
                        view.setScaleX(1.1);
                        view.setScaleY(1.1);
                    }
                });

                view.setOnMouseExited(e -> {
                    view.setScaleX(1.0);
                    view.setScaleY(1.0);
                });

                view.setOnMouseClicked(e -> {
                    if (!myTurn) {
                        System.out.println("Not your turn!");
                        return;
                    }
                    showPlacementDialog(piece);
                });

                handBox.getChildren().add(view);
                System.out.println("✓ Successfully loaded image for piece " + piece.id);

            } catch (Exception e) {
                System.err.println("✗ Failed to load image for piece " + piece.id + ": " + e.getMessage());
                createPlaceholder(piece);
            }
        }

        System.out.println("Total images in hand: " + handBox.getChildren().size());
    }

    private void createPlaceholder(Network.Piece piece) {
        StackPane placeholder = new StackPane();
        placeholder.setPrefSize(100, 150);
        placeholder.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-border-width: 2;");

        javafx.scene.control.Label label = new javafx.scene.control.Label(
                piece.leftValue + "-" + piece.rightValue
        );
        label.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        placeholder.getChildren().add(label);

        placeholder.setOnMouseClicked(event -> {
            if (!myTurn) {
                System.out.println("Not your turn!");
                return;
            }
            showPlacementDialog(piece);
        });

        handBox.getChildren().add(placeholder);
    }

    private void showPlacementDialog(Network.Piece piece) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Place Domino");
        dialog.setHeaderText("Place piece [" + piece.leftValue + "-" + piece.rightValue + "]");
        dialog.setContentText("Choose placement and orientation:");

        javafx.scene.control.ButtonType leftButton = new javafx.scene.control.ButtonType("Place on Left");
        javafx.scene.control.ButtonType rightButton = new javafx.scene.control.ButtonType("Place on Right");
        javafx.scene.control.ButtonType flipButton = new javafx.scene.control.ButtonType("Flip and Place");
        javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType("Cancel",
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getButtonTypes().setAll(leftButton, rightButton, flipButton, cancelButton);

        dialog.showAndWait().ifPresent(response -> {
            if (response == leftButton) {
                attemptPlayPiece(piece, true, false);
            } else if (response == rightButton) {
                attemptPlayPiece(piece, false, false);
            } else if (response == flipButton) {
                showFlippedPlacementDialog(piece);
            }
        });
    }

    private void showFlippedPlacementDialog(Network.Piece piece) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Place Flipped Domino");
        dialog.setHeaderText("Place flipped piece [" + piece.leftValue + "-" + piece.rightValue + "]");
        dialog.setContentText("Choose placement (piece will be horizontal):");

        javafx.scene.control.ButtonType leftButton = new javafx.scene.control.ButtonType("Place on Left");
        javafx.scene.control.ButtonType rightButton = new javafx.scene.control.ButtonType("Place on Right");
        javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType("Cancel",
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getButtonTypes().setAll(leftButton, rightButton, cancelButton);

        dialog.showAndWait().ifPresent(response -> {
            if (response == leftButton) {
                attemptPlayPiece(piece, true, true);
            } else if (response == rightButton) {
                attemptPlayPiece(piece, false, true);
            }
        });
    }

    private void attemptPlayPiece(Network.Piece piece, boolean placeOnLeft, boolean flipped) {
        System.out.println("Attempting to play piece ID: " + piece.id +
                " on " + (placeOnLeft ? "left" : "right") +
                " flipped: " + flipped);

        // Remember which piece we tried to play
        lastAttemptedPiece = piece;

        if (client == null) {
            System.err.println("ERROR: Cannot play piece - client is null!");
            showAlert("Connection Error", "Game client is not connected. Please reconnect.");
            lastAttemptedPiece = null;
            return;
        }

        // Send to server - DO NOT update UI yet
        client.playCard(piece.id, piece.leftValue, piece.rightValue, placeOnLeft, flipped);

        // Disable hand while waiting for server response
        myTurn = false;
        disableHand();

        System.out.println("Move sent to server. Waiting for validation...");
    }

    public void onOpponentPlayed(int pieceId, int leftValue, int rightValue, boolean placedOnLeft, boolean flipped) {
        System.out.println("Opponent played [" + leftValue + "-" + rightValue +
                "] on " + (placedOnLeft ? "left" : "right") + " flipped: " + flipped);

        // If opponent played, our previous move (if any) was invalid
        lastAttemptedPiece = null;

        try {
            String imagePath = getImagePathForValues(leftValue, rightValue);
            Network.Piece opponentPiece = new Network.Piece(pieceId, imagePath, leftValue, rightValue);
            addPlayedPiece(opponentPiece, false, placedOnLeft, flipped);

            // It's now our turn
            setMyTurn(true);

        } catch (Exception e) {
            System.err.println("ERROR adding opponent's piece: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addPlayedPiece(Network.Piece piece, boolean playedByMe, boolean placedOnLeft, boolean flipped) {
        System.out.println("Adding piece to board: [" + piece.leftValue + "-" + piece.rightValue +
                "] on " + (placedOnLeft ? "left" : "right") + " flipped: " + flipped +
                " playedByMe: " + playedByMe);

        try {
            String imagePath = piece.imagePath;
            if (!imagePath.startsWith("/")) {
                imagePath = "/" + imagePath;
            }

            Image image = new Image(getClass().getResource(imagePath).toExternalForm());

            if (image.isError()) {
                throw new RuntimeException("Image failed to load");
            }

            ImageView view = new ImageView(image);

            if (flipped) {
                view.setFitWidth(180);
                view.setFitHeight(120);
                view.setRotate(90);
            } else {
                view.setFitWidth(120);
                view.setFitHeight(180);
                view.setRotate(180);
            }
            view.setPreserveRatio(true);

            if (!playedByMe) {
                view.setOpacity(0.9);
                view.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.5), 10, 0, 0, 0);");
            } else {
                view.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,255,0,0.5), 10, 0, 0, 0);");
            }

            if (placedOnLeft) {
                boardBox.getChildren().add(0, view);
            } else {
                boardBox.getChildren().add(view);
            }

            // If it's our piece, remove it from hand
            if (playedByMe && lastAttemptedPiece != null) {
                hand.remove(lastAttemptedPiece);
                lastAttemptedPiece = null;
                renderHand(); // Update hand display
            }

            playedPieces.add(piece);
            System.out.println("✓ Piece added to board");

        } catch (Exception e) {
            System.err.println("Failed to add piece to board: " + e.getMessage());
            createBoardPlaceholder(piece, playedByMe, placedOnLeft, flipped);
        }
    }

    private void createBoardPlaceholder(Network.Piece piece, boolean playedByMe, boolean placedOnLeft, boolean flipped) {
        StackPane placeholder = new StackPane();

        if (flipped) {
            placeholder.setPrefSize(180, 120);
            placeholder.setRotate(90);
        } else {
            placeholder.setPrefSize(120, 180);
        }

        String bgColor = playedByMe ? "lightgreen" : "lightcoral";
        placeholder.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: black; -fx-border-width: 2;");

        javafx.scene.control.Label label = new javafx.scene.control.Label(
                piece.leftValue + "\n-\n" + piece.rightValue
        );
        label.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-alignment: center;");
        placeholder.getChildren().add(label);

        if (placedOnLeft) {
            boardBox.getChildren().add(0, placeholder);
        } else {
            boardBox.getChildren().add(placeholder);
        }

        // If it's our piece, remove it from hand
        if (playedByMe && lastAttemptedPiece != null) {
            hand.remove(lastAttemptedPiece);
            lastAttemptedPiece = null;
            renderHand();
        }

        playedPieces.add(piece);
    }

    private String getImagePathForValues(int left, int right) {
        return String.format("/Pieces/%d%d.png", left, right);
    }

    private void disableHand() {
        handBox.setDisable(true);
        handBox.setOpacity(0.6);
    }

    private void enableHand() {
        handBox.setDisable(false);
        handBox.setOpacity(1.0);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
