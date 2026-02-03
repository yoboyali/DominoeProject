package org.client.scenes;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import org.client.SceneManager;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
public class MainMenu{
    URL hostSprite;
    URL joinHost;
    URL creditsHost;
    URL profileHost;
    Font fontHost;
    InputStream fontStream;
    String videoPath;
    Media backGroundVid;
    MediaPlayer mediaPlayer;
    MediaView mediaView;
    ImageView hButtonView;
    ImageView jButtonView;
    ImageView creditView;
    ImageView profileView;
    Text Logo;
    ProfileMenu profileMenu = new ProfileMenu();
    public  void initializeVar(){

        hostSprite = getClass().getResource("/Host.png");
        joinHost = getClass().getResource("/Join.png");
        creditsHost = getClass().getResource("/Credits.png");
        profileHost = getClass().getResource("/Profile.png");
        fontStream = getClass().getResourceAsStream("/Moldie.otf");

        videoPath = new File("/Users/alihamdy/IdeaProjects/DominoeProject/src/main/resources/Background.mp4").toURI().toString();
        backGroundVid = new Media(videoPath);
        mediaPlayer = new MediaPlayer(backGroundVid);
        mediaView = new MediaView(mediaPlayer);
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
        mediaPlayer.play();
        hButtonView = new ImageView(hostSprite.toExternalForm());
        jButtonView = new ImageView(joinHost.toExternalForm());
        creditView = new ImageView(creditsHost.toExternalForm());
        profileView = new ImageView(profileHost.toExternalForm());
        fontHost = Font.loadFont(fontStream , 140);
        Logo = new Text( 10 , 20 ,"Dominoes");
        Logo.setFont(fontHost);
        Logo.setFill(Color.WHITE);


    }
    public Scene initalizeWindow(){
        Button joinBtn = new Button();
        Button hostBtn = new Button();
        Button creditBtn = new Button();
        Button profileBtn = new Button();
        hostBtn.setGraphic(hButtonView);
        joinBtn.setGraphic(jButtonView);
        creditBtn.setGraphic(creditView);
        profileBtn.setGraphic(profileView);
        joinBtn.setTranslateX(205);
        joinBtn.setTranslateY(120);
        hostBtn.setTranslateX(-200);
        hostBtn.setTranslateY(120);
        creditBtn.setTranslateX(80);
        creditBtn.setTranslateY(127);
        profileBtn.setTranslateX(-45);
        profileBtn.setTranslateY(127);

        hostBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );
        hostBtn.setOnAction(e -> {
            SceneManager.setHostScene();
        });
        joinBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );
        joinBtn.setOnAction( e -> SceneManager.setJoinScene());

        creditBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );
        profileBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );
        profileBtn.setOnAction(e -> {
            SceneManager.setProfileScene();

        });
        Group gb = new Group(hostBtn , joinBtn , creditBtn , profileBtn);
        VBox menu = new VBox(20, Logo , gb);
        menu.setAlignment(Pos.CENTER);
        StackPane root = new StackPane(mediaView, menu);

        // Scene scene = new Scene(root, 1280, 720);
        return new Scene(root , 1280 , 720);

    }


     //

}
