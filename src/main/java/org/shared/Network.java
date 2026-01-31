package org.shared;

import com.esotericsoftware.kryo.Kryo;
import java.util.ArrayList;
import java.util.List;

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
        public List<Piece> hand;
    }

    public static class YourTurn {}

    public static class PlayPiece {
        public int pieceId;
    }

    public static class OpponentPlayed {
        public int pieceId;
    }

    public static class GameOver {}

    /* =====================
       Data
       ===================== */

    public static class Piece {
        public int id;
        public String imagePath;

        // REQUIRED by Kryo
        public Piece() {}

        public Piece(int id, String imagePath) {
            this.id = id;
            this.imagePath = imagePath;
        }
    }

    /* =====================
       Registration
       ===================== */
    public static class InitialHand {
        public List<Piece> pieces;
    }


    public static void register(Kryo kryo) {
        kryo.register(ConnectRequest.class);
        kryo.register(StartGame.class);
        kryo.register(YourTurn.class);
        kryo.register(PlayPiece.class);
        kryo.register(OpponentPlayed.class);
        kryo.register(GameOver.class);

        kryo.register(Piece.class);
        kryo.register(ArrayList.class);
        kryo.register(List.class);
        kryo.register(String.class);
        kryo.register(int.class);
    }
}
