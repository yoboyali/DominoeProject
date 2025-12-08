package org.client;

import com.esotericsoftware.kryonet.*;
import org.shared.Network;

import java.io.IOException;
import java.util.Scanner;

public class GameClient {

    private Client client;
    private boolean myTurn = false;

    public GameClient(String host, String playerName) throws IOException {
        client = new Client();
        client.start();

        Network.register(client.getKryo());

       client.connect(5000, host, Network.TCP_PORT, Network.UDP_PORT);
       // client.connect(5000, "localhost", 54555, 54777);

        // Send connect request
        Network.ConnectRequest req = new Network.ConnectRequest();
        req.name = playerName;
        client.sendTCP(req);

        System.out.println("Connected. Waiting for opponent...");

        client.addListener(new Listener() {
            @Override
            public void received(Connection c, Object object) {

                if (object instanceof Network.StartGame) {
                    Network.StartGame sg = (Network.StartGame) object;
                    System.out.println("You are Player " + sg.playerNumber);
                }

                if (object instanceof Network.YourTurn) {
                    myTurn = true;
                    System.out.println("\n--- Your Turn ---");
                }

                if (object instanceof Network.OpponentPlayed) {
                    Network.OpponentPlayed op = (Network.OpponentPlayed) object;
                    System.out.println("Opponent played: " + op.piece);
                }

                if (object instanceof Network.GameOver) {
                    System.out.println("Game ended.");
                    client.stop();
                }
            }
        });

        // Input Loop
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (!myTurn) continue;

            System.out.print("Place a piece: ");
            String piece = scanner.nextLine();

            Network.PlayPiece msg = new Network.PlayPiece();
            msg.piece = piece;
            client.sendTCP(msg);

            myTurn = false;
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter server IP: ");
        String ip = scan.nextLine();

        System.out.print("Enter your name: ");
        String name = scan.nextLine();

        new GameClient(ip, name);
    }
}
