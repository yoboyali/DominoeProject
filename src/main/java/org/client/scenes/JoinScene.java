package org.client.scenes;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.client.GameController;
import org.client.SceneManager;

public class JoinScene {

    private Stage stage;
    private TextField ipField;

    public JoinScene(Stage stage) {
        this.stage = stage;
    }

    public Scene createScene() {

        Label titleLabel = new Label("Join Game");
        titleLabel.setFont(Font.font("Arial", 24));

        Label ipLabel = new Label("Enter Server IP:");
        ipField = new TextField();
        ipField.setPromptText("e.g., localhost or 192.168.1.100");
        ipField.setPrefWidth(300);

        Button joinButton = new Button("Join Game");
        joinButton.setPrefWidth(150);
        joinButton.setOnAction(e -> joinGame());

        Button backButton = new Button("Back to Menu");
        backButton.setPrefWidth(150);
        backButton.setOnAction(e -> SceneManager.setMainScene());

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(50));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                titleLabel,
                ipLabel,
                ipField,
                joinButton,
                backButton
        );

        return new Scene(layout, 400, 400);
    }

    private void joinGame() {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) {
            ip = "localhost";
        }
        GameController.getInstance().joinGame(stage, ip);
    }
}