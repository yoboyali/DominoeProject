package org.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    @Test
    void singletonReturnsSameInstance() {
        GameController a = GameController.getInstance();
        GameController b = GameController.getInstance();

        assertSame(a, b);
    }

    @Test
    void controllerConstructsSafely() {
        assertDoesNotThrow(GameController::new);
    }
}
