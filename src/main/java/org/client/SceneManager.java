package org.client;
import com.esotericsoftware.minlog.Log;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.media.*;
import javafx.util.Duration;

import javax.print.DocFlavor;
import java.io.File;
import java.net.URL;

public class SceneManager extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DOMINOES!");
        URL bgurl = getClass().getResource("/background.jpg");
        URL logourl = getClass().getResource("/Logo.png");
        String path = new File("/Users/alihamdy/IdeaProjects/DominoeProject/src/main/resources/Background.mp4").toURI().toString();
        Media Backgroundvid = new Media(path);
        MediaPlayer mediaplayer = new MediaPlayer(Backgroundvid);
        MediaView mediaView = new MediaView(mediaplayer);
        //mediaplayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaplayer.setOnEndOfMedia(() -> mediaplayer.seek(Duration.ZERO));
        mediaplayer.play();
        // Image background = new Image("/Users/alihamdy/IdeaProjects/DominoeProject/src/background.jpg");
        if(bgurl == null){throw new RuntimeException("Cannot find image");}
        ImageView backgroundView = new ImageView(bgurl.toExternalForm());
        ImageView LogoView = new ImageView(logourl.toExternalForm());
        backgroundView.setFitWidth(1280);
        backgroundView.setFitHeight(720);
        backgroundView.setPreserveRatio(false);
        Rectangle rect = new Rectangle();
        rect.setHeight(100);
        rect.setWidth(600);
        rect.setX(200);
        rect.setY(200);
        rect.setFill(Color.DARKGRAY);
        
        Button joinBtn = new Button("Join");
        Button hostBtn = new Button("Host");

        joinBtn.setOnAction(e -> System.out.println("Join clicked"));
        hostBtn.setOnAction(e -> System.out.println("Host clicked"));
        VBox menu = new VBox(20, LogoView, rect );
        menu.setAlignment(Pos.CENTER);
        StackPane root = new StackPane(mediaView, menu);

        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
