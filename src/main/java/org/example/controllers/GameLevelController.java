package org.example.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import org.example.SceneFactory;
import org.example.ScreenManager;
import org.example.utils.*;
import java.util.Objects;


public class GameLevelController {

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
    private javafx.animation.Timeline aiTimeline;
    private org.example.ai.SnakeEnvironment aiEnv;
    private org.example.ai.NeuralNetwork aiBrain;

    @FXML
    private StackPane rootPane;
    @FXML
    private Canvas gameCanvas;

    private final GameRenderer gameRenderer = new GameRenderer();
    private GameEngine engine;

    private double dragStartX;
    private double dragStartY;

    private int lastDx = 0;
    private int lastDy = 0;

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

        if (isAiMode) {
            aiBrain = org.example.ai.NeuralNetwork.loadFromFile("champion.txt");
            if (aiBrain != null) {
                startAiLoop();
            }
        } else {
            Coordinate2D<Integer> head = engine.getSnake().getHead();
            Coordinate2D<Integer> tail = engine.getSnake().getTail();
            if (head != null && tail != null) {
                lastDx = head.getX() - tail.getX();
                lastDy = head.getY() - tail.getY();
            }
        }

        render();

    }

    private void startAiLoop() {
        aiEnv = new org.example.ai.SnakeEnvironment(engine);

        aiTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), e -> {

                    double[] vision = aiEnv.getVision();
                    int action = aiBrain.predict(vision);

                    aiEnv.step(action);

                    render();

                    if (aiEnv.isDone()) {
                        aiTimeline.stop();

                        handleAiEnd();
                    }
                })
        );
        aiTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        aiTimeline.play();
    }

    private void handleAiEnd() {
        if (engine.isLevelWon()) {
            SoundManager.playWin();
            ScreenManager.getInstance().openOverlay(SceneFactory.getGameNextOverlay());
        } else {
            SoundManager.playGameOver();
            ScreenManager.getInstance().openOverlay(SceneFactory.getGameOverOverlay());
        }
    }

    private void attachKeyEvent(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (isAiMode) return;
            if (engine == null || engine.isGameOver() || engine.isLevelWon()) return;

            boolean moved = false;
            if (event.getCode() == KeyCode.UP) {
                if (lastDx == 0 && lastDy == 1) return;
                engine.getSnake().setDirection("up");
                engine.move(0, -1);
                lastDx = 0; lastDy = -1;
                moved = true;
            } else if (event.getCode() == KeyCode.DOWN) {
                if (lastDx == 0 && lastDy == -1) return;
                engine.getSnake().setDirection("down");
                engine.move(0, 1);
                lastDx = 0; lastDy = 1;
                moved = true;
            } else if (event.getCode() == KeyCode.LEFT) {
                if (lastDx == 1 && lastDy == 0) return;
                engine.getSnake().setDirection("left");
                engine.move(-1, 0);
                lastDx = -1; lastDy = 0;
                moved = true;
            } else if (event.getCode() == KeyCode.RIGHT) {
                if (lastDx == -1 && lastDy == 0) return;
                engine.getSnake().setDirection("right");
                engine.move(1, 0);
                lastDx = 1; lastDy = 0;
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
                        if (lastDx == -1 && lastDy == 0) return;
                        engine.getSnake().setDirection("right");
                        engine.move(1, 0);
                        lastDx = 1;
                    } else {
                        if (lastDx == 1 && lastDy == 0) return;
                        engine.getSnake().setDirection("left");
                        engine.move(-1, 0);
                        lastDx = -1;
                    }
                    lastDy = 0;
                    moved = true;
                }
            } else {
                if (Math.abs(deltaY) > 30) {
                    if (deltaY > 0) {
                        if (lastDx == 0 && lastDy == -1) return;
                        engine.getSnake().setDirection("down");
                        engine.move(0, 1);
                        lastDx = 0; lastDy = 1;
                    } else { // SUS
                        if (lastDx == 0 && lastDy == 1) return;
                        engine.getSnake().setDirection("up");
                        engine.move(0, -1);
                        lastDx = 0; lastDy = -1;
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

}