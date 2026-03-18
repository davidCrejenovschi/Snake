package org.example.utils;

public class AiManager {
    public static boolean isAiEnabled = false;
    private static Process pythonProcess;
    public static Runnable onServerReady;

    public static void startPythonServer() {
        if (pythonProcess != null && pythonProcess.isAlive()) return;

        try {
            // 1. Aflăm calea de bază a proiectului (ex: C:\Users\...\Snake)
            String projectRoot = System.getProperty("user.dir");

            // 2. Construim căile complete (Absolute Paths)
            // Folosim File.separator pentru a fi siguri că merge și pe Windows și pe Mac/Linux
            String pythonExe = projectRoot + java.io.File.separator + "python_ai" +
                    java.io.File.separator + ".venv" +
                    java.io.File.separator + "Scripts" +
                    java.io.File.separator + "python.exe";

            String scriptPath = projectRoot + java.io.File.separator + "python_ai" +
                    java.io.File.separator + "play_expert.py";

            System.out.println("[JAVA] Încerc să pornesc: " + pythonExe);

            ProcessBuilder pb = new ProcessBuilder(pythonExe, "-u", scriptPath);

            // Setăm folderul de lucru chiar în interiorul folderului de Python
            pb.directory(new java.io.File(projectRoot + java.io.File.separator + "python_ai"));

            pb.redirectErrorStream(true);
            pythonProcess = pb.start();

            new Thread(() -> {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[PYTHON] " + line);

                        // --- NOU: Verificăm dacă Python-ul ne dă undă verde ---
                        if (line.contains("ONLINE") || line.contains("treaz")) {
                            if (onServerReady != null) {
                                // Trimitem comanda de deblocare înapoi pe thread-ul principal JavaFX
                                javafx.application.Platform.runLater(onServerReady);
                            }
                        }
                        // --------------------------------------------------------
                    }
                } catch (java.io.IOException e) {
                    System.err.println("[JAVA] Consola Python s-a închis.");
                }
            }).start();

        } catch (java.io.IOException e) {
            System.err.println("[JAVA] EROARE CRITICĂ: Nu am găsit fișierul!");
            e.printStackTrace();
        }
    }

    public static void stopPythonServer() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            System.out.println("[JAVA] Opresc forțat serverul AI...");
            pythonProcess.destroyForcibly();
        }
    }
}