package org.client;
import javafx.scene.Scene;
import org.client.scenes.*;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.media.*;

import java.io.IOException;
import java.net.URL;


public class SceneManager extends Application {

    private static Stage primaryStage;

    private static MainMenu mainMenu;
    private static ProfileMenu profileMenu;
    private static HostScene hostMenu;
    private static GameScene gameMenu;
    private static JoinScene joinMenu;
    private static CreditsScene creditsMenu;
    private MediaPlayer backGroundTrack;
    @Override
    public void start(Stage stage) {

        URL url = getClass().getResource("/Sfx/Track.mp3");

        if (url == null) {
            System.err.println("Background music not found");
            return;
        }

        Media media = new Media(url.toExternalForm());
        backGroundTrack = new MediaPlayer(media);
        backGroundTrack.setCycleCount(MediaPlayer.INDEFINITE);
        backGroundTrack.setVolume(0.1);
        backGroundTrack.play();

        primaryStage = stage;
        mainMenu = new MainMenu();
        mainMenu.initializeVar();

        profileMenu = new ProfileMenu();
        profileMenu.initializeVar();


        setMainScene();
        primaryStage.show();

        joinMenu = new JoinScene(primaryStage);
        hostMenu = new HostScene(primaryStage);

        creditsMenu = new CreditsScene();
        creditsMenu.loadAssets();

    }

    public static void setMainScene() {
        primaryStage.setScene(mainMenu.initalizeWindow());
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    public static void setProfileScene() {
        primaryStage.setScene(profileMenu.initializeWindow());
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    public static void setHostScene(){
        primaryStage.setScene(hostMenu.createScene());
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }
    public static void setGameScene(Scene gameScene) {
        primaryStage.setScene(gameScene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
   public static void setJoinScene() {
       primaryStage.setScene(joinMenu.createScene());
       primaryStage.sizeToScene();
       primaryStage.centerOnScreen();
   }
   public static void setCreditsScene(){
        primaryStage.setScene(creditsMenu.createScene());
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();

   }
    public static void main(String[] args) {
        launch(args);
    }
}
