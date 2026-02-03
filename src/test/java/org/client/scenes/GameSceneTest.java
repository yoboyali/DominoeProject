package org.client.scenes;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.shared.Network;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameSceneTest {

    @Test
    void setHandReplacesExistingHand() {
        GameScene scene = new GameScene();

        Network.Piece p1 = new Network.Piece(1, "/Pieces/11.png", 1, 1);
        Network.Piece p2 = new Network.Piece(2, "/Pieces/22.png", 2, 2);

        scene.setHand(List.of(p1, p2));

        assertEquals(2, getHandSize(scene));
    }

    // reflection helper
    private int getHandSize(GameScene scene) {
        try {
            var field = GameScene.class.getDeclaredField("hand");
            field.setAccessible(true);
            return ((List<?>) field.get(scene)).size();
        } catch (Exception e) {
            fail("Reflection failed");
            return -1;
        }
    }

    @Test
    void setMyTurnUpdatesTurnState() {
        GameScene scene = new GameScene();

        scene.setMyTurn(true);
        assertTrue(getMyTurn(scene));

        scene.setMyTurn(false);
        assertFalse(getMyTurn(scene));
    }

    private boolean getMyTurn(GameScene scene) {
        try {
            var field = GameScene.class.getDeclaredField("myTurn");
            field.setAccessible(true);
            return field.getBoolean(scene);
        } catch (Exception e) {
            fail("Reflection failed");
            return false;
        }
    }

    private Object getLastAttempt(GameScene scene) {
        try {
            var field = GameScene.class.getDeclaredField("lastAttemptedPiece");
            field.setAccessible(true);
            return field.get(scene);
        } catch (Exception e) {
            fail("Reflection failed");
            return null;
        }
    }

}