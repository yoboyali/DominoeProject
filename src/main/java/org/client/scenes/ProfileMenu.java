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
    URL change;
    URL backGround;
    ImageView backView;
    ImageView frameView;
    ImageView changeView;
    ImageView backGroundView;
    InputStream fontStream;
    Font fontHost ;
    Font fontHost2;
    Text Logo , changeText , Profile;

    public void initializeVar(){

        backBtn = getClass().getResource("/Back.png");
        frame = getClass().getResource("/Frame.png");
        change = getClass().getResource("/Change.png");
        backGround = getClass().getResource("/background.jpg");
        changeView = new ImageView(change.toExternalForm());
        frameView = new ImageView(frame.toExternalForm());
        backView = new ImageView(backBtn.toExternalForm());
        backGroundView = new ImageView(backGround.toExternalForm());
        fontStream = getClass().getResourceAsStream("/Moldie.otf");
        fontHost = Font.loadFont(fontStream , 40);
        fontStream = getClass().getResourceAsStream("/Moldie.otf");
        fontHost2 = Font.loadFont(fontStream , 20);
        Logo = new Text( 10 , 20 ,"BACK");
        Logo.setFont(fontHost);
        Logo.setFill(Color.WHITE);
        Profile = new Text(10 , 20 ,"PROFILE");
        fontStream = getClass().getResourceAsStream("/Moldie.otf");
        fontHost = Font.loadFont(fontStream , 60);
        Profile.setFont(fontHost);
        Profile.setFill(Color.WHITE);
        changeText = new Text(10 , 20 ,"CHANGE");
        changeText.setFont(fontHost2);
        changeText.setFill(Color.WHITE);
    }
    public   Scene initializeWindow() {

        Button backBtn = new Button();
        Button changebtn = new Button();
        backBtn.setGraphic(backView);
        changebtn.setGraphic(changeView);
        changebtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );
        backBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );
        backBtn.setOnAction(e -> {
            SceneManager.setMainScene();
        });
        Logo.setX(150);
        Logo.setY(42);
        frameView.setX(3);
        frameView.setY(-150);
        changebtn.setTranslateX(9);
        changebtn.setTranslateY(20);
        changeText.setX(68);
        changeText.setY(37);
        Profile.setX(-1);
        Profile.setY(-180);

        ImageView image = new ImageView();
        ImageViewSprite anim = new ImageViewSprite(image, new Image("/Dino/Dinos.png"), 3, 1, 3, 120, 120, 6);
        anim.start();
        Group buttons = new Group( Logo, backBtn);
        Group Frame = new Group(frameView , image , changeText , changebtn , Profile);

        image.setX(40);
        image.setY(-100);

        VBox menu = new VBox(60 ,backGroundView ,Frame, buttons);
        menu.setAlignment(Pos.BOTTOM_CENTER);
        //menu.setStyle("-fx-background-color: black;");
        StackPane root = new StackPane(backGroundView , menu);

        return new Scene(root, 400, 420);


    }
}

