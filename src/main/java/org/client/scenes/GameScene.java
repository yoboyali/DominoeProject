package org.client.scenes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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

    private final HBox boardBox = new HBox(5);
    private final List<Network.Piece> playedPieces = new ArrayList<>();

    private final StackPane drawButton = new StackPane();

    private boolean myTurn = false;
    private GameClient client;

    private Network.Piece lastAttemptedPiece = null;

    public GameScene() {
        loadAssets();
    }

    public void setClient(GameClient client) {
        this.client = client;
        System.out.println("GameScene: Client set");
    }

    private void loadAssets() {
        try {

            frameView = new ImageView(
                    getClass().getResource("/Holder.png").toExternalForm()
            );

            boardView = new ImageView(
                    getClass().getResource("/board.png").toExternalForm()
            );
            videoPath = new File("/Users/alihamdy/IdeaProjects/DominoeProject/src/main/resources/Background.mp4").toURI().toString();

            backGroundVid = new Media(videoPath);

            mediaPlayer = new MediaPlayer(backGroundVid);
            mediaView = new MediaView(mediaPlayer);

            mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
            mediaPlayer.play();

            System.out.println("✓ Loaded background assets");
        } catch (Exception e) {
            System.err.println("✗ Failed to load background assets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Scene createScene() {
        StackPane root = new StackPane();

        setupDrawButton();

        boardBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(boardBox, Pos.CENTER);
        boardView.setOpacity(0.3);
        StackPane.setAlignment(boardView, Pos.CENTER);
        handBox.setAlignment(Pos.BOTTOM_CENTER);
        StackPane.setAlignment(handBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(handBox, new Insets(0, 0, 20, 0));
        StackPane.setAlignment(frameView, Pos.BOTTOM_CENTER);

        StackPane.setAlignment(drawButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(drawButton, new Insets(0, 50, 50, 0));

        root.getChildren().addAll(
                mediaView,
                boardView,
                frameView,
                boardBox,
                handBox,
                drawButton
        );


        boardBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1); -fx-background-radius: 10;");
        boardBox.setPadding(new Insets(10));
        boardBox.setSpacing(40);
        handBox.setSpacing(15);

        renderHand();

        return new Scene(root, 1400, 800);
    }

    private void setupDrawButton() {
        try {
            Image drawImage = new Image(getClass().getResource("/Pieces/?.png").toExternalForm());
            ImageView drawImageView = new ImageView(drawImage);
            drawImageView.setPreserveRatio(true);
            drawImageView.setFitWidth(40);
            drawImageView.setFitHeight(80);

            drawButton.getChildren().clear();
            drawButton.getChildren().add(drawImageView);


        } catch (Exception e) {
            System.err.println("✗ Failed to load draw button texture: " + e.getMessage());
        }


        drawButton.setPrefSize(40, 80);
        drawButton.setMinSize(40, 80);
        drawButton.setMaxSize(40, 80);

        drawButton.setOnMouseEntered(e -> {
            if (myTurn) {
                drawButton.setScaleX(1.1);
                drawButton.setScaleY(1.1);
                drawButton.setStyle("-fx-background-color: rgba(76, 175, 80, 0.8); " +
                        "-fx-border-color: #2E7D32; " +
                        "-fx-border-width: 2; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-radius: 5;");
            }
        });

        drawButton.setOnMouseExited(e -> {
            drawButton.setScaleX(1.0);
            drawButton.setScaleY(1.0);
            drawButton.setStyle("-fx-background-color: transparent; " +
                    "-fx-border-color: #2E7D32; " +
                    "-fx-border-width: 2; " +
                    "-fx-background-radius: 5; " +
                    "-fx-border-radius: 5;");
        });

        drawButton.setOnMouseClicked(e -> handleDrawRequest());

        drawButton.setDisable(!myTurn);
        drawButton.setOpacity(myTurn ? 1.0 : 0.5);

        drawButton.setStyle("-fx-background-color: transparent; " +
                "-fx-border-color: #2E7D32; " +
                "-fx-border-width: 2; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5;");
    }


    private void handleDrawRequest() {
        if (!myTurn) {
            System.out.println("Cannot draw now - not your turn");
            return;
        }

        System.out.println("Requesting to draw a piece...");

        if (client == null) {
            System.err.println("ERROR: Cannot draw - client is null!");
            showAlert("Connection Error", "Game client is not connected. Please reconnect.");
            return;
        }

        drawButton.setDisable(true);
        drawButton.setOpacity(0.5);

        client.drawPiece();
        System.out.println("Draw request sent to server");
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

    public void onPieceDrawn(Network.Piece piece, boolean successful) {
        System.out.println("Piece drawn response - successful: " + successful);

        if (myTurn) {
            drawButton.setDisable(false);
            drawButton.setOpacity(1.0);
        }

        if (successful && piece != null) {
            System.out.println("Successfully drew piece: [" + piece.leftValue + "-" + piece.rightValue + "]");

    
            hand.add(piece);
            renderHand();


            showAlert("Piece Drawn", "You drew [" + piece.leftValue + "-" + piece.rightValue + "]");
        } else {
            System.out.println("Failed to draw piece - draw pile might be empty");
            showAlert("Cannot Draw", "Draw pile is empty!");
        }
    }

    public void setMyTurn(boolean turn) {
        System.out.println("GameScene: My turn = " + turn);
        myTurn = turn;

        if (myTurn) {
            drawButton.setDisable(false);
            drawButton.setOpacity(1.0);


            if (lastAttemptedPiece != null) {
                System.out.println("Server rejected our move with piece " + lastAttemptedPiece.id);
                showAlert("Invalid Move",
                        "You cannot play [" + lastAttemptedPiece.leftValue + "-" + lastAttemptedPiece.rightValue +
                                "] there. Try a different piece or placement.");

                removeLastPieceFromBoard();
                lastAttemptedPiece = null;
            }
            enableHand();
        } else {
            disableHand();
        }
    }

    private void removeLastPieceFromBoard() {
        if (!boardBox.getChildren().isEmpty()) {
            boardBox.getChildren().remove(boardBox.getChildren().size() - 1);
            System.out.println("Removed rejected piece from board");
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

        lastAttemptedPiece = piece;

        if (client == null) {
            System.err.println("ERROR: Cannot play piece - client is null!");
            showAlert("Connection Error", "Game client is not connected. Please reconnect.");
            lastAttemptedPiece = null;

            if (myTurn) {
                drawButton.setDisable(false);
                drawButton.setOpacity(1.0);
            }
            return;
        }

        client.playCard(piece.id, piece.leftValue, piece.rightValue, placeOnLeft, flipped);

        myTurn = false;
        disableHand();

        System.out.println("Move sent to server. Waiting for validation...");
    }
    public void onValidMovePlayed(Network.Piece piece, boolean placedOnLeft, boolean flipped) {
        System.out.println("Server validated our move: [" + piece.leftValue + "-" + piece.rightValue + "]");

        lastAttemptedPiece = null;
        addPlayedPiece(piece, true, placedOnLeft, flipped);
    }

    public void onOpponentPlayed(int pieceId, int leftValue, int rightValue, boolean placedOnLeft, boolean flipped) {
        System.out.println("Opponent played [" + leftValue + "-" + rightValue +
                "] on " + (placedOnLeft ? "left" : "right") + " flipped: " + flipped);

        lastAttemptedPiece = null;

        try {
            String imagePath = getImagePathForValues(leftValue, rightValue);
            Network.Piece opponentPiece = new Network.Piece(pieceId, imagePath, leftValue, rightValue);
            addPlayedPiece(opponentPiece, false, placedOnLeft, flipped);

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
            view.setPreserveRatio(true);

            if (flipped) {
                view.setRotate(90);
            } else {
                view.setRotate(-90);
            }

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

            if (playedByMe) {
                boolean removed = false;
                for (int i = 0; i < hand.size(); i++) {
                    if (hand.get(i).id == piece.id) {
                        hand.remove(i);
                        removed = true;
                        break;
                    }
                }
                lastAttemptedPiece = null;
                renderHand();
            }

            playedPieces.add(piece);

        } catch (Exception e) {
            System.err.println("Failed to add piece to board: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onMoveInvalid(String reason) {
        showAlert("Invalid Move", reason);
        lastAttemptedPiece = null;
        setMyTurn(true);
    }

    private String getImagePathForValues(int left, int right) {
        return String.format("/Pieces/%d%d.png", left, right);
    }

    private void disableHand() {
        handBox.setDisable(true);
        handBox.setOpacity(0.6);
        drawButton.setDisable(true);
        drawButton.setOpacity(0.6);
    }

    private void enableHand() {
        handBox.setDisable(false);
        handBox.setOpacity(1.0);
        drawButton.setDisable(false);
        drawButton.setOpacity(1.0);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}