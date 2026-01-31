package org.client;
import javafx.scene.Scene;
import org.client.scenes.GameScene;
import org.client.scenes.HostScene;
import org.client.scenes.MainMenu;
import org.client.scenes.ProfileMenu;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.media.*;


public class SceneManager extends Application {

    private static Stage primaryStage;

    private static MainMenu mainMenu;
    private static ProfileMenu profileMenu;
    private static HostScene hostMenu;
    private static GameScene gameMenu;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        mainMenu = new MainMenu();
        mainMenu.initializeVar();

        profileMenu = new ProfileMenu();
        profileMenu.initializeVar();

        gameMenu = new GameScene();

        setMainScene();
        primaryStage.show();

        hostMenu = new HostScene(primaryStage);

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
    public static void setGameScene(){
        primaryStage.setScene(gameMenu.createScene());
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
