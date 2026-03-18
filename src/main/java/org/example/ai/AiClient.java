package org.example.ai;

import org.example.utils.GameEngine;
import java.io.*;
import java.net.Socket;

public class AiClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 65432;

    public static void main(String[] args) {
        System.out.println("[*] Pornește robotul de antrenament Java...");

        // Bucla care rulează meciuri la infinit
        int numarMeci = 1;
        while (true) {
            GameEngine engine = new GameEngine(10);

            try (Socket socket = new Socket(HOST, PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Bucla unui singur meci
                while (!engine.isGameOver() && !engine.isLevelWon()) {
                    String stateJson = String.format(
                            "{\"head_x\": %d, \"head_y\": %d, \"food_x\": %d, \"food_y\": %d, \"grid_size\": %d, \"game_over\": %b, \"ate_food\": %b}",
                            engine.getSnake().getHead().getX(),
                            engine.getSnake().getHead().getY(),
                            engine.getFood().getX(),
                            engine.getFood().getY(),
                            engine.getNumberOfLines(),
                            engine.isGameOver(),
                            engine.didJustEat()
                    );

                    out.println(stateJson);

                    String response = in.readLine();
                    if (response == null) break;

                    int action = Integer.parseInt(response.trim());
                    applyAction(engine, action);
                }

                System.out.println("Meciul " + numarMeci + " terminat. Scor: " + engine.getSnake().getBody().size());
                numarMeci++;

            } catch (IOException e) {
                System.err.println("[!] Aștept serverul Python...");
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private static void applyAction(GameEngine engine, int action) {
        switch (action) {
            case 0 -> engine.move(0, -1); // UP
            case 1 -> engine.move(0, 1);  // DOWN
            case 2 -> engine.move(-1, 0); // LEFT
            case 3 -> engine.move(1, 0);  // RIGHT
        }
    }
}