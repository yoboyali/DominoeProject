package org.client.scenes;

import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.client.ImageViewSprite;
import org.client.SceneManager;

import java.io.InputStream;
import java.net.URL;

public class ProfileMenu {
    URL backBtn;
    URL frame;
    ImageView backView;
    ImageView frameView;
    InputStream fontStream;
    Font fontHost;
    Text Logo;

    public void initializeVar(){

        backBtn = getClass().getResource("/Back.png");
        frame = getClass().getResource("/Frame.png");
        frameView = new ImageView(frame.toExternalForm());
        backView = new ImageView(backBtn.toExternalForm());
        fontStream = getClass().getResourceAsStream("/Moldie.otf");
        fontHost = Font.loadFont(fontStream , 40);
        Logo = new Text( 10 , 20 ,"BACK");
        Logo.setFont(fontHost);
        Logo.setFill(Color.WHITE);
    }
    public   Scene initializeWindow() {

        Label title = new Label("Player Profile");

        Button backBtn = new Button();
        backBtn.setGraphic(backView);
        backBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );
        backBtn.setOnAction(e -> {
            SceneManager.setMainScene();
        });
        Logo.setX(150);
        Logo.setY(42);
        frameView.setX(-150);
        frameView.setFitWidth(200);
        frameView.setPreserveRatio(true);

        ImageView image = new ImageView();
        ImageViewSprite anim = new ImageViewSprite(image, new Image("/Dino/Dinos.png"), 3, 1, 3, 120, 120, 6);
        anim.start();
        Group buttons = new Group( Logo, backBtn);
        VBox root = new VBox(buttons , frameView , image);
        root.setAlignment(Pos.BOTTOM_CENTER);
        root.setStyle("-fx-background-color: black;");

        return new Scene(root, 400, 420);


    }
}

