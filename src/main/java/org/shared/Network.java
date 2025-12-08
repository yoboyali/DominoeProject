package org.shared;

import com.esotericsoftware.kryo.Kryo;

public class Network {

    public static final int TCP_PORT = 54555;
    public static final int UDP_PORT = 54777;

    // Messages shared between client and server
    public static class ConnectRequest { public String name; }
    public static class StartGame { public int playerNumber; }
    public static class YourTurn {}
    public static class OpponentPlayed { public String piece; }
    public static class PlayPiece { public String piece; }
    public static class GameOver {}

    public static void register(Kryo kryo) {
        kryo.register(ConnectRequest.class);
        kryo.register(StartGame.class);
        kryo.register(YourTurn.class);
        kryo.register(OpponentPlayed.class);
        kryo.register(PlayPiece.class);
        kryo.register(GameOver.class);
        kryo.register(String.class);
    }
}
