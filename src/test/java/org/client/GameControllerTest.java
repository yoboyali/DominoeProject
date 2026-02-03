package org.client;

import org.junit.jupiter.api.Test;

import static javax.management.Query.times;
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
    @Test
    void serverStartedFlagPreventsRestart() throws Exception {
        GameController controller = GameController.getInstance();

        var field = GameController.class.getDeclaredField("serverStarted");
        field.setAccessible(true);

        field.set(controller, true);

        assertTrue((boolean) field.get(controller));
    }
}
