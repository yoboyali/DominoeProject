package org.shared;

import com.esotericsoftware.kryo.Kryo;
import java.util.ArrayList;

public class Network {

    public static final int TCP_PORT = 54555;
    public static final int UDP_PORT = 54777;



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

    public static class GameWon{
        public int winnerPlayerNumber;  // 1 or 2
        public String reason;
    }
    public static class MoveValidated {
        public int pieceId;
        public int leftValue;
        public int rightValue;
        public boolean placedOnLeft;
        public boolean flipped;
    }

    public static class MoveInvalid {
        public String reason;
    }



    public static class Piece {
        public int id;
        public String imagePath;
        public int leftValue;
        public int rightValue;

        public Piece() {}

        public Piece(int id, String imagePath, int leftValue, int rightValue) {
            this.id = id;
            this.imagePath = imagePath;
            this.leftValue = leftValue;
            this.rightValue = rightValue;
        }
    }


    public static class InitialHand {
        public ArrayList<Piece> pieces;
    }
    public static class DrawPiece {
        public int playerNumber;
    }

    public static class PieceDrawn {
        public Network.Piece piece;
        public boolean successful;
    }

    public static void register(Kryo kryo) {
        kryo.register(ConnectRequest.class);
        kryo.register(StartGame.class);
        kryo.register(YourTurn.class);
        kryo.register(PlayPiece.class);
        kryo.register(OpponentPlayed.class);
        kryo.register(GameOver.class);
        kryo.register(InitialHand.class);
        kryo.register(DrawPiece.class);
        kryo.register(PieceDrawn.class);
        kryo.register(GameWon.class);
        kryo.register(MoveValidated.class);
        kryo.register(MoveInvalid.class);
        kryo.register(Piece.class);
        kryo.register(ArrayList.class);
        kryo.register(java.util.List.class);
        kryo.register(String.class);
        kryo.register(int.class);
        kryo.register(boolean.class);

    }
}