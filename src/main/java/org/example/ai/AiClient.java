package org.example.ai;

import org.example.utils.GameEngine;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class AiClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 65432;

    public static void main(String[] args) {
        System.out.println("[*] Pornește robotul de antrenament Java (RADAR)...");

        int numarMeci = 1;
        while (true) {
            GameEngine engine = new GameEngine(10);

            try (Socket socket = new Socket(HOST, PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                while (!engine.isGameOver() && !engine.isLevelWon()) {
                    double[] radar = getRadarDistances(engine);

                    String stateJson = String.format(
                            "{\"radar\": %s, \"game_over\": %b, \"ate_food\": %b}",
                            Arrays.toString(radar),
                            engine.isGameOver(),
                            engine.didJustEat()
                    );

                    out.println(stateJson);

                    String response = in.readLine();
                    if (response == null) break;

                    int action = Integer.parseInt(response.trim());
                    applyAction(engine, action);
                }

                numarMeci++;

            } catch (IOException e) {
                System.err.println("[!] Aștept serverul Python...");
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private static double[] getRadarDistances(GameEngine engine) {
        double[] radar = new double[24];
        int[][] directions = {
                {0, -1}, {0, 1}, {-1, 0}, {1, 0},
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };

        int headX = engine.getSnake().getHead().getX();
        int headY = engine.getSnake().getHead().getY();
        int foodX = engine.getFood().getX();
        int foodY = engine.getFood().getY();
        int gridSize = engine.getNumberOfLines();

        for (int i = 0; i < 8; i++) {
            int dx = directions[i][0];
            int dy = directions[i][1];
            int cx = headX;
            int cy = headY;
            double distance = 0;
            boolean foodFound = false;
            boolean bodyFound = false;

            while (true) {
                cx += dx;
                cy += dy;
                distance++;

                if (cx < 0 || cy < 0 || cx >= gridSize || cy >= gridSize) {
                    radar[i] = 1.0 / distance;
                    break;
                }

                if (!foodFound && cx == foodX && cy == foodY) {
                    radar[8 + i] = 1.0 / distance;
                    foodFound = true;
                }

                if (!bodyFound && isPartOfSnakeBody(engine, cx, cy)) {
                    radar[16 + i] = 1.0 / distance;
                    bodyFound = true;
                }
            }
        }
        return radar;
    }

    private static boolean isPartOfSnakeBody(GameEngine engine, int x, int y) {
        for (var segment : engine.getSnake().getBody()) {
            if (segment.getX() == x && segment.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private static void applyAction(GameEngine engine, int action) {
        switch (action) {
            case 0 -> engine.move(0, -1);
            case 1 -> engine.move(0, 1);
            case 2 -> engine.move(-1, 0);
            case 3 -> engine.move(1, 0);
        }
    }
}