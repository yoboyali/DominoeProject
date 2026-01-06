package org.client;
import org.client.scenes.MainMenu;
import org.client.scenes.ProfileMenu;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.media.*;


public class SceneManager extends Application {
        MainMenu menu = new MainMenu();
        ProfileMenu profileMenu = new ProfileMenu();
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage Primary){
        menu.initializeVar();
        Primary.setScene(menu.initalizeWindow());
        Primary.show();
        
    }

}
