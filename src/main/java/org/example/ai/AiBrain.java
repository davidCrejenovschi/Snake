package org.example.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.example.utils.EnvLoader;
import org.example.utils.GameEngine;

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
            System.out.println("Conectat la Creierul Python!");
            return true;
        } catch (IOException e) {
            System.err.println("Nu am putut contacta Python-ul. E pornit play_expert.py?");
            return false;
        }
    }

    public int getBestMove(GameEngine engine) {
        if (socket == null || socket.isClosed()) return -1;

        try {
            // Construim fotografia jocului
            String stateJson = String.format(
                    "{\"head_x\": %d, \"head_y\": %d, \"food_x\": %d, \"food_y\": %d}",
                    engine.getSnake().getHead().getX(),
                    engine.getSnake().getHead().getY(),
                    engine.getFood().getX(),
                    engine.getFood().getY()
            );

            // Trimitem poza
            out.println(stateJson);

            // Așteptăm răspunsul
            String response = in.readLine();
            if (response != null) {
                return Integer.parseInt(response.trim());
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return -1; // Eroare
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}