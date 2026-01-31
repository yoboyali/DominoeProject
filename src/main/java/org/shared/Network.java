
package org.shared;

import com.esotericsoftware.kryo.Kryo;
import java.util.ArrayList;

public class Network {

    public static final int TCP_PORT = 54555;
    public static final int UDP_PORT = 54777;

    /* =====================
       Messages
       ===================== */

    public static class ConnectRequest {
        public String name;
    }

    public static class StartGame {
        public int playerNumber;
        public ArrayList<Piece> hand;
    }

    public static class YourTurn {}

    public static class PlayPiece {
        public int pieceId;
        public int leftValue;
        public int rightValue;
        public boolean placedOnLeft;
        public boolean flipped;
    }

    public static class OpponentPlayed {
        public int pieceId;
        public int leftValue;
        public int rightValue;
        public boolean placedOnLeft;
        public boolean flipped;
    }

    public static class GameOver {}

    /* =====================
       Data
       ===================== */

    public static class Piece {
        public int id;
        public String imagePath;
        public int leftValue;
        public int rightValue;

        // REQUIRED by Kryo
        public Piece() {}

        public Piece(int id, String imagePath, int leftValue, int rightValue) {
            this.id = id;
            this.imagePath = imagePath;
            this.leftValue = leftValue;
            this.rightValue = rightValue;
        }
    }

    /* =====================
       Registration
       ===================== */
    public static class InitialHand {
        public ArrayList<Piece> pieces;
    }

    /* =====================
       Registration
       ===================== */
    public static void register(Kryo kryo) {
        System.out.println("Registering network classes with Kryo...");

        // Register message classes
        kryo.register(ConnectRequest.class);
        kryo.register(StartGame.class);
        kryo.register(YourTurn.class);
        kryo.register(PlayPiece.class);
        kryo.register(OpponentPlayed.class);
        kryo.register(GameOver.class);
        kryo.register(InitialHand.class);

        // Register data classes
        kryo.register(Piece.class);

        // Register collections and primitives
        kryo.register(ArrayList.class);
        kryo.register(java.util.List.class);
        kryo.register(String.class);
        kryo.register(int.class);
        kryo.register(boolean.class);

        System.out.println("✓ All network classes registered");
    }
}
