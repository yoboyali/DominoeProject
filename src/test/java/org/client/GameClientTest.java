package org.client;

import org.junit.jupiter.api.Test;
import org.shared.Network;

import static org.junit.jupiter.api.Assertions.*;

class GameClientTest {

    @Test
    void playCardCreatesCorrectMessage() {
        Network.PlayPiece play = new Network.PlayPiece();
        play.pieceId = 10;
        play.leftValue = 2;
        play.rightValue = 5;
        play.placedOnLeft = true;
        play.flipped = false;

        assertEquals(10, play.pieceId);
        assertEquals(2, play.leftValue);
        assertEquals(5, play.rightValue);
        assertTrue(play.placedOnLeft);
        assertFalse(play.flipped);
    }

    @Test
    void drawPieceMessageIsValid() {
        Network.DrawPiece draw = new Network.DrawPiece();
        assertNotNull(draw);
    }
}
