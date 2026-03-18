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
import org.example.ai.AiBrain;
import org.example.utils.*;
import org.example.utils.GameEngine;
import org.example.utils.Coordinate2D;
import org.example.utils.LevelSettings;
import org.example.utils.SoundManager;

import java.util.Objects;

public class GameLevelController {

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

    private AiBrain aiBrain;
    private Timeline aiLoop;

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
        if (aiBrain != null) {
            aiBrain.disconnect();
        }

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

        engine = new GameEngine(settings.getNumberOfLines());
        render();

        if (org.example.utils.AiManager.isAiEnabled) {
            aiBrain = new AiBrain();
            if (aiBrain.connect()) {
                startAiLoop();
            }
        }
    }

    private void attachKeyEvent(Scene scene) {
        scene.setOnKeyPressed(event -> {

            if (org.example.utils.AiManager.isAiEnabled) return;

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
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
        });

        scene.setOnMouseReleased(event -> {
            if (engine == null || engine.isGameOver() || engine.isLevelWon()) return;

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
            // OPRIM AI-ul IMEDIAT
            if (aiLoop != null) aiLoop.stop();
            if (aiBrain != null) aiBrain.disconnect();

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
            if (lines == 2) {
                gameRenderer.drawPear(spacing, food.getX(), food.getY());
            } else {
                gameRenderer.drawLilyPad(spacing, food.getX(), food.getY());
            }
        }
    }

    private void startAiLoop() {
        // Acest ceas va ticăi la fiecare 150 de milisecunde (viteza șarpelui)
        aiLoop = new Timeline(new KeyFrame(Duration.millis(1000), event -> {
            if (engine == null || engine.isGameOver() || engine.isLevelWon()) {
                aiLoop.stop(); // Oprim bucla dacă s-a terminat jocul
                return;
            }

            int action = aiBrain.getBestMove(engine);
            boolean moved = false;

            // Mapăm deciziile AI-ului pe logica ta de joc
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

        aiLoop.setCycleCount(Timeline.INDEFINITE); // Rulează la infinit
        aiLoop.play(); // Pornește animația
    }
}