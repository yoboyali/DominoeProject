package org.client.scenes;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import org.client.SceneManager;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import javafx.scene.Scene;

public class CreditsScene {
   private InputStream fontStream;
   private Font fontHost;
   private URL backbtnUrl;
    ImageView backbtn;
   private Text text;




  public  void loadAssets() {
        fontStream = getClass().getResourceAsStream("/Moldie.otf");
        fontHost = Font.loadFont(fontStream, 25);

        backbtnUrl = getClass().getResource("/Back.png");
        backbtn = new ImageView(backbtnUrl.toExternalForm());
    }

  public  Scene createScene() {

        text = new Text("Starry BackGrond Video -\n https://www.vecteezy.com/members/ezstudio\n" +
                "Domino Pieces -\n https://dani-maccari.itch.io/cute-domino\n" +
                "Logo Font -\n https://www.1001fonts.com/moldie-demo-font.html\n" +
                "Background Track -\n FNX_Sound\n" +
                "Piece Placement sfx -\n https://pixabay.com/users/freesound_community-46691455/\n" +
                "Buttons were custom made using -\n https://www.pixilart.com/\n" +
                "Thank you All for your generosity!");
      text.setTextAlignment(TextAlignment.CENTER);
      text.setWrappingWidth(800);
      text.setLineSpacing(12);
      text.setFont(fontHost);
      text.setFill(Color.WHITE);
      Button backButton = new Button("Back");
      backButton.setPrefWidth(150);
      backButton.setFont(fontHost);
      backButton.setTextFill(Color.WHITE);
      backButton.setOnAction(e -> SceneManager.setMainScene());

      backButton.setStyle(
              "-fx-background-color: grey;" +
                      "-fx-padding: 10;"
      );

        StackPane root = new StackPane(text , backButton);
        StackPane.setAlignment(text, Pos.CENTER);
        StackPane.setAlignment(backButton , Pos.BOTTOM_CENTER);
        root.setStyle("-fx-background-color: black;");
        return new Scene(root, 900, 900);
    }
}

