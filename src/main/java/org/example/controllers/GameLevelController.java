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

    public static boolean isAiMode = false;
    private int movesSinceLastApple = 0;

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

    private NeuralNetwork aiBrain;
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

        if (isAiMode) {
            loadAiBrain("campion_4x4.dat");
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
            if (isAiMode) return;
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
        movesSinceLastApple = 0;

        // Ticăie la fiecare 100ms
        aiLoop = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            if (engine == null || engine.isGameOver() || engine.isLevelWon()) {
                aiLoop.stop();
                return;
            }

            double[] state = getState(engine);
            int action = aiBrain.predict(state);

            int sizeBefore = engine.getSnake().getBody().size();
            applyRelativeAction(action);

            if (engine.getSnake().getBody().size() > sizeBefore) {
                movesSinceLastApple = 0;
            } else {
                movesSinceLastApple++;
            }

            // Folosim aceeași limită de înfometare ca la antrenament (GridSize * GridSize)
            int limit = engine.getNumberOfLines() * engine.getNumberOfLines();
            if (movesSinceLastApple > limit) {
                System.out.println("[!] AI-ul a intrat în Safe Loop (Înfometare). Executat pentru pierdere de timp!");
                aiLoop.stop();
                SoundManager.playGameOver();
                ScreenManager.getInstance().openOverlay(SceneFactory.getGameOverOverlay());
                return;
            }

            checkGameState();
            render();
        }));

        aiLoop.setCycleCount(Timeline.INDEFINITE);
        aiLoop.play();
    }

    private double[] getState(GameEngine e) {
        int headX = e.getSnake().getHead().getX();
        int headY = e.getSnake().getHead().getY();
        int foodX = e.getFood().getX();
        int foodY = e.getFood().getY();
        String currentDir = e.getSnake().getDirection();

        double dirUp = currentDir.equals("up") ? 1.0 : 0.0;
        double dirDown = currentDir.equals("down") ? 1.0 : 0.0;
        double dirLeft = currentDir.equals("left") ? 1.0 : 0.0;
        double dirRight = currentDir.equals("right") ? 1.0 : 0.0;

        Coordinate2D<Integer> pointUp = new Coordinate2D<>(headX, headY - 1);
        Coordinate2D<Integer> pointDown = new Coordinate2D<>(headX, headY + 1);
        Coordinate2D<Integer> pointLeft = new Coordinate2D<>(headX - 1, headY);
        Coordinate2D<Integer> pointRight = new Coordinate2D<>(headX + 1, headY);

        boolean dangerUp = isCollision(e, pointUp);
        boolean dangerDown = isCollision(e, pointDown);
        boolean dangerLeft = isCollision(e, pointLeft);
        boolean dangerRight = isCollision(e, pointRight);

        double dangerStraight = 0.0, dangerTurnLeft = 0.0, dangerTurnRight = 0.0;

        if (dirUp == 1.0) {
            dangerStraight = dangerUp ? 1.0 : 0.0;
            dangerTurnRight = dangerRight ? 1.0 : 0.0;
            dangerTurnLeft = dangerLeft ? 1.0 : 0.0;
        } else if (dirDown == 1.0) {
            dangerStraight = dangerDown ? 1.0 : 0.0;
            dangerTurnRight = dangerLeft ? 1.0 : 0.0;
            dangerTurnLeft = dangerRight ? 1.0 : 0.0;
        } else if (dirLeft == 1.0) {
            dangerStraight = dangerLeft ? 1.0 : 0.0;
            dangerTurnRight = dangerUp ? 1.0 : 0.0;
            dangerTurnLeft = dangerDown ? 1.0 : 0.0;
        } else if (dirRight == 1.0) {
            dangerStraight = dangerRight ? 1.0 : 0.0;
            dangerTurnRight = dangerDown ? 1.0 : 0.0;
            dangerTurnLeft = dangerUp ? 1.0 : 0.0;
        }

        // ORDINUL EXACT CA ÎN SNAKEAGENT
        return new double[]{
                dangerStraight, dangerTurnLeft, dangerTurnRight, // 3 senzori pericol
                dirUp, dirDown, dirLeft, dirRight,               // 4 senzori direcție curentă
                foodX < headX ? 1.0 : 0.0, // Food is Left      // 4 senzori direcție mâncare
                foodX > headX ? 1.0 : 0.0, // Food is Right
                foodY < headY ? 1.0 : 0.0, // Food is Up
                foodY > headY ? 1.0 : 0.0  // Food is Down
        };
    }

    private boolean isCollision(GameEngine e, Coordinate2D<Integer> pt) {
        if (pt.getX() < 0 || pt.getX() >= e.getNumberOfLines() || pt.getY() < 0 || pt.getY() >= e.getNumberOfLines()) {
            return true;
        }
        for (var segment : e.getSnake().getBody()) {
            if (segment.getX().equals(pt.getX()) && segment.getY().equals(pt.getY())) {
                return true;
            }
        }
        return false;
    }

    private void applyRelativeAction(int action) {
        String currentDir = engine.getSnake().getDirection();

        if (action == 0) {
            moveByDirection(currentDir);
            return;
        }

        if (action == 1) { // Viraj Dreapta
            switch (currentDir) {
                case "up" -> moveByDirection("right");
                case "down" -> moveByDirection("left");
                case "left" -> moveByDirection("up");
                case "right" -> moveByDirection("down");
            }
            return;
        }

        if (action == 2) { // Viraj Stânga
            switch (currentDir) {
                case "up" -> moveByDirection("left");
                case "down" -> moveByDirection("right");
                case "left" -> moveByDirection("down");
                case "right" -> moveByDirection("up");
            }
        }
    }

    private void moveByDirection(String dir) {
        engine.getSnake().setDirection(dir);
        switch (dir) {
            case "up" -> engine.move(0, -1);
            case "down" -> engine.move(0, 1);
            case "left" -> engine.move(-1, 0);
            case "right" -> engine.move(1, 0);
        }
    }
}