package org.example.ai;

import org.example.utils.EnvLoader;
import org.example.utils.GameEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class AiBrain {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public boolean connect() {
        try {
            String host = EnvLoader.get("AI_SERVER_HOST", "127.0.0.1");
            int port = EnvLoader.getInt("AI_SERVER_PORT", 65432);

            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("[JAVA] Conectat la Creierul Python (Mod Radar)!");
            return true;
        } catch (IOException e) {
            System.err.println("[!] Nu am putut contacta Python-ul. E pornit play_expert.py?");
            return false;
        }
    }

    public int getBestMove(GameEngine engine) {
        if (socket == null || socket.isClosed()) return -1;

        try {
            // 1. Scanăm mediul din jurul șarpelui cu cele 8 "mustăți"
            double[] radar = getRadarDistances(engine);

            // 2. Construim noul JSON cerut de play_expert.py
            String stateJson = String.format(
                    "{\"radar\": %s, \"game_over\": %b, \"ate_food\": %b}",
                    Arrays.toString(radar),
                    engine.isGameOver(),
                    engine.didJustEat()
            );

            // 3. Trimitem datele spre procesare
            out.println(stateJson);

            // 4. Așteptăm răspunsul (0=Sus, 1=Jos, 2=Stânga, 3=Dreapta)
            String response = in.readLine();
            if (response != null) {
                return Integer.parseInt(response.trim());
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("[!] Eroare la comunicarea cu AI-ul: " + e.getMessage());
        }
        return -1;
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("[JAVA] AI deconectat cu succes.");
            }
        } catch (IOException e) {
            e.printStackTrace();
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

                // Lovire perete
                if (cx < 0 || cy < 0 || cx >= gridSize || cy >= gridSize) {
                    radar[i] = 1.0 / distance;
                    break;
                }

                // Găsire mâncare
                if (!foodFound && cx == foodX && cy == foodY) {
                    radar[8 + i] = 1.0 / distance;
                    foodFound = true;
                }

                // Găsire corp propriu
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
}