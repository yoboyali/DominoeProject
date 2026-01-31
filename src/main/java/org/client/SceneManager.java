package org.client;
import javafx.scene.Scene;
import org.client.scenes.*;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.media.*;


public class SceneManager extends Application {

    private static Stage primaryStage;

    private static MainMenu mainMenu;
    private static ProfileMenu profileMenu;
    private static HostScene hostMenu;
    private static GameScene gameMenu;
    private static JoinScene joinMenu;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        mainMenu = new MainMenu();
        mainMenu.initializeVar();

        profileMenu = new ProfileMenu();
        profileMenu.initializeVar();


        setMainScene();
        primaryStage.show();

        joinMenu = new JoinScene(primaryStage);

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
    public static void setGameScene(Scene gameScene) {
        System.out.println("SceneManager: Setting game scene");
        if (primaryStage == null) {
            System.err.println("SceneManager: primaryStage is null!");
            return;
        }

        if (gameScene == null) {
            System.err.println("SceneManager: gameScene is null!");
            return;
        }

        try {
            primaryStage.setScene(gameScene);
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
            primaryStage.show(); // IMPORTANT: Make sure stage is shown

            System.out.println("SceneManager: Game scene set successfully!");
        } catch (Exception e) {
            System.err.println("SceneManager: Error setting game scene: " + e.getMessage());
            e.printStackTrace();
        }
    }
   public static void setJoinScene() {  // NEW METHOD
       primaryStage.setScene(joinMenu.createScene());
       primaryStage.sizeToScene();
       primaryStage.centerOnScreen();
   }
    public static void main(String[] args) {
        launch(args);
    }
}
