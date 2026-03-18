package org.example.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class EnvLoader {
    // Folosim clasa Properties din Java care e perfectă pentru sistemul cheie=valoare
    private static final Properties properties = new Properties();

    // Bloc static - se execută o singură dată la pornirea aplicației
    static {
        loadEnvironmentVariables();
    }

    private static void loadEnvironmentVariables() {
        String projectRoot = System.getProperty("user.dir");
        File envFile = new File(projectRoot + File.separator + "python_ai" + File.separator + ".env");

        if (!envFile.exists()) {
            System.err.println("[WARN] Fișierul .env nu a fost găsit la: " + envFile.getAbsolutePath());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Ignorăm comentariile și liniile goale
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Spargem linia în două la primul semn de egal
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    properties.setProperty(parts[0].trim(), parts[1].trim());
                }
            }
            System.out.println("[JAVA] Configurări încărcate cu succes din .env!");
        } catch (IOException e) {
            System.err.println("[ERROR] Eroare la citirea fișierului .env: " + e.getMessage());
        }
    }

    // Metodă pentru a scoate text (String)
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // Metodă pentru a scoate numere (Int)
    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue; // Dacă nu găsește portul, folosește valoarea de rezervă (ex: 65432)
        }
    }
}