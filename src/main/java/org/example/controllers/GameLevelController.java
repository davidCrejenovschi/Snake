package org.example.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.example.SceneFactory;
import org.example.ScreenManager;
import org.example.utils.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Objects;
import org.example.ai.NeuralNetwork;

public class GameLevelController {

    // Am adăugat un flag static simplu pe care îl poți schimba din HomeMenuController
    public static boolean isAiMode = false;

    private class LayoutHandler implements ChangeListener<Number> {
        @Override
        public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
            double orient = Math.min(rootPane.getHeight(), rootPane.getWidth());
            double proportion = 0.80;
            double side = orient * proportion;
            gameCanvas.setWidth(side);
            gameCanvas.setHeight(side);
            javafx.application.Platform.runLater(GameLevelController.this::render);
        }
    }

    private final LayoutHandler layoutHandler = new LayoutHandler();

    @FXML
    private StackPane rootPane;
    @FXML
    private StackPane overlayLayer;
    @FXML
    private Canvas gameCanvas;

    private final GameRenderer gameRenderer = new GameRenderer();
    private GameEngine engine;

    private double dragStartX;
    private double dragStartY;

    private NeuralNetwork aiBrain; // Acum folosim rețeaua noastră pură în Java
    private Timeline aiLoop;

    private int level;

    @FXML
    public void initialize() {
        gameRenderer.init(gameCanvas.getGraphicsContext2D());

        rootPane.widthProperty().addListener(layoutHandler);
        rootPane.heightProperty().addListener(layoutHandler);

        gameCanvas.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win") || os.contains("mac") || os.contains("nix") || os.contains("nux")) {
                    attachKeyEvent(newScene);
                } else {
                    attachSwipeEvent(newScene);
                }
            }
        });
    }

    public void startLevel(LevelSettings settings) {
        if (aiLoop != null) {
            aiLoop.stop();
        }

        level = settings.getLevelNumber();

        if (settings.getCssPath() != null) {
            try {
                rootPane.getStylesheets().clear();
                String css = Objects.requireNonNull(getClass().getResource(settings.getCssPath())).toExternalForm();
                rootPane.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        gameRenderer.setTheme(
                settings.getGridColor1(),
                settings.getGridColor2(),
                settings.getGridLineColor(),
                settings.getSnakeColor()
        );

        engine = new GameEngine(settings.getNumberOfLines());
        render();

        // Dacă e modul AI, încărcăm campionul de pe disc
        if (isAiMode) {
            loadAiBrain("campion_4x4.dat"); // Numele fișierului salvat la evoluție
            if (aiBrain != null) {
                startAiLoop();
            } else {
                System.err.println("[!] Nu s-a putut porni AI-ul. Lipseste fisierul campionului.");
            }
        }
    }

    private void loadAiBrain(String filePath) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            aiBrain = (NeuralNetwork) in.readObject();
            System.out.println("[*] Creierul AI a fost incarcat cu succes!");
        } catch (Exception e) {
            aiBrain = null;
            System.err.println("[!] Eroare la incarcarea fisierului .dat: " + e.getMessage());
        }
    }

    private void attachKeyEvent(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (isAiMode) return; // Dacă joacă AI-ul, blocăm tastatura
            if (engine == null || engine.isGameOver() || engine.isLevelWon()) return;

            boolean moved = false;
            if (event.getCode() == KeyCode.UP) {
                engine.getSnake().setDirection("up");
                engine.move(0, -1);
                moved = true;
            } else if (event.getCode() == KeyCode.DOWN) {
                engine.getSnake().setDirection("down");
                engine.move(0, 1);
                moved = true;
            } else if (event.getCode() == KeyCode.LEFT) {
                engine.getSnake().setDirection("left");
                engine.move(-1, 0);
                moved = true;
            } else if (event.getCode() == KeyCode.RIGHT) {
                engine.getSnake().setDirection("right");
                engine.move(1, 0);
                moved = true;
            }

            if (moved) {
                checkGameState();
                render();
            }
        });
    }

    private void attachSwipeEvent(Scene scene) {
        scene.setOnMousePressed(event -> {
            if (isAiMode) return;
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
        });

        scene.setOnMouseReleased(event -> {
            if (isAiMode || engine == null || engine.isGameOver() || engine.isLevelWon()) return;

            double dragEndX = event.getSceneX();
            double dragEndY = event.getSceneY();
            double deltaX = dragEndX - dragStartX;
            double deltaY = dragEndY - dragStartY;

            boolean moved = false;

            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                if (Math.abs(deltaX) > 30) {
                    if (deltaX > 0) {
                        engine.getSnake().setDirection("right");
                        engine.move(1, 0);
                    } else {
                        engine.getSnake().setDirection("left");
                        engine.move(-1, 0);
                    }
                    moved = true;
                }
            } else {
                if (Math.abs(deltaY) > 30) {
                    if (deltaY > 0) {
                        engine.getSnake().setDirection("down");
                        engine.move(0, 1);
                    } else {
                        engine.getSnake().setDirection("up");
                        engine.move(0, -1);
                    }
                    moved = true;
                }
            }

            if (moved) {
                checkGameState();
                render();
            }
        });
    }

    private void checkGameState() {
        if (engine.isGameOver() || engine.isLevelWon()) {
            if (aiLoop != null) aiLoop.stop();

            if (engine.isGameOver()) {
                SoundManager.playGameOver();
                ScreenManager.getInstance().openOverlay(SceneFactory.getGameOverOverlay());
            } else {
                SoundManager.playWin();
                ScreenManager.getInstance().openOverlay(SceneFactory.getGameNextOverlay());
            }
        } else if (engine.didJustEat()) {
            SoundManager.playEat();
        }
    }

    private void render() {
        if (engine == null) return;

        double sideLength = gameCanvas.getHeight();
        int lines = engine.getNumberOfLines();
        if (lines == 0) return;

        double spacing = sideLength / (lines + 1);

        gameRenderer.drawField(sideLength, lines);
        gameRenderer.drawSnake(engine.getSnake(), spacing);

        Coordinate2D<Integer> food = engine.getFood();
        if (food != null) {
            if (level == 1) {
                gameRenderer.drawPear(spacing, food.getX(), food.getY());
            } else {
                gameRenderer.drawLilyPad(spacing, food.getX(), food.getY());
            }
        }
    }

    private void startAiLoop() {
        // Am pus 100ms pentru a fi ușor vizibil cum gândește. Poți modifica pentru viteză!
        aiLoop = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            if (engine == null || engine.isGameOver() || engine.isLevelWon()) {
                aiLoop.stop();
                return;
            }

            // 1. Extragem matricea jocului curent
            double[] vision = getFlattenedGrid(engine);

            // 2. Creierul prezice mutarea (0=Sus, 1=Jos, 2=Stânga, 3=Dreapta)
            int action = aiBrain.predict(vision);

            boolean moved = false;
            switch (action) {
                case 0 -> { engine.getSnake().setDirection("up"); engine.move(0, -1); moved = true; }
                case 1 -> { engine.getSnake().setDirection("down"); engine.move(0, 1); moved = true; }
                case 2 -> { engine.getSnake().setDirection("left"); engine.move(-1, 0); moved = true; }
                case 3 -> { engine.getSnake().setDirection("right"); engine.move(1, 0); moved = true; }
            }

            if (moved) {
                checkGameState();
                render();
            }
        }));

        aiLoop.setCycleCount(Timeline.INDEFINITE);
        aiLoop.play();
    }

    // --- FUNCTIILE DE VIZIUNE PENTRU REȚEAUA NEURONALĂ ---

    private double[] getFlattenedGrid(GameEngine e) {
        int gridSize = e.getNumberOfLines();
        double[] grid = new double[gridSize * gridSize];
        int headX = e.getSnake().getHead().getX();
        int headY = e.getSnake().getHead().getY();
        int foodX = e.getFood().getX();
        int foodY = e.getFood().getY();

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                int index = y * gridSize + x;
                if (x == headX && y == headY) grid[index] = 0.5;
                else if (x == foodX && y == foodY) grid[index] = 1.0;
                else if (isPartOfSnakeBody(e, x, y)) grid[index] = -1.0;
                else grid[index] = 0.0;
            }
        }
        return grid;
    }

    private boolean isPartOfSnakeBody(GameEngine e, int x, int y) {
        for (var segment : e.getSnake().getBody()) {
            if (segment.getX() == x && segment.getY() == y) return true;
        }
        return false;
    }
}