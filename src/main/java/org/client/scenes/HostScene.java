package org.client.scenes;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import org.client.SceneManager;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import org.client.GameController;

public class HostScene {

    Stage primaryStage;
    GameController controller;

  public  HostScene(Stage stage){
        primaryStage = stage;
    }
    public Scene createScene() {

        Label titleLabel = new Label("Host Game");
        titleLabel.setFont(Font.font("Arial", 24));

        controller = new GameController();
        Button hostBtn = new Button("HOST");
        hostBtn.setPrefWidth(150);

        hostBtn.setOnAction(e -> {
            hostBtn.setDisable(true);
            controller.hostGame(primaryStage);
        });
        Button backButton = new Button("Back to Menu");
        backButton.setPrefWidth(150);
        backButton.setOnAction(e -> SceneManager.setMainScene());


;

        VBox root = new VBox(20);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(
                titleLabel,
                hostBtn,
                backButton
        );


        return new Scene(root, 400, 300);
    }
}

