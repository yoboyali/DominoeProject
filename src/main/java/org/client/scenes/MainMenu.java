package org.client.scenes;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.net.URL;
public class MainMenu{
    //Var
    URL hostSprite;
    URL joinHost;
    URL logoHost;
    URL creditsHost;
    URL profileHost;
    String videoPath;
    Media backGroundVid;
    MediaPlayer mediaPlayer;
    MediaView mediaView;
    ImageView hButtonView;
    ImageView jButtonView;
    ImageView logoView;
    ImageView creditView;
    ImageView profileView;
  public  void initializeVar(){

        hostSprite = getClass().getResource("/Host.png");
        joinHost = getClass().getResource("/Join.png");
        logoHost = getClass().getResource("/Logo2.png");
        creditsHost = getClass().getResource("/Credits.png");
        profileHost = getClass().getResource("/Profile.png");

        videoPath = new File("/Users/alihamdy/IdeaProjects/DominoeProject/src/main/resources/Background.mp4").toURI().toString();
        backGroundVid = new Media(videoPath);
        mediaPlayer = new MediaPlayer(backGroundVid);
        mediaView = new MediaView(mediaPlayer);
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
        mediaPlayer.play();
        hButtonView = new ImageView(hostSprite.toExternalForm());
        jButtonView = new ImageView(joinHost.toExternalForm());
        logoView = new ImageView(logoHost.toExternalForm());
        creditView = new ImageView(creditsHost.toExternalForm());
        profileView = new ImageView(profileHost.toExternalForm());
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
        hostBtn.setTranslateX(-205);
        hostBtn.setTranslateY(120);
        creditBtn.setTranslateX(65);
        creditBtn.setTranslateY(120);
        profileBtn.setTranslateX(-65);
        profileBtn.setTranslateY(120);

        logoView.setFitWidth(590);
        logoView.setFitHeight(340);
        logoView.setPreserveRatio(true);
        hostBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );
        joinBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );
        creditBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );
        profileBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );
        VBox menu = new VBox(20, logoView);
        menu.setAlignment(Pos.CENTER);
        StackPane root = new StackPane(mediaView, menu, hostBtn , joinBtn , creditBtn , profileBtn);

       // Scene scene = new Scene(root, 1280, 720);
        return new Scene(root , 1280 , 720);

    }
     //

}
