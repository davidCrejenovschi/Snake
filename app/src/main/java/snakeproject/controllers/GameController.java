package snakeproject.controllers;
import java.util.ArrayList;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import snakeproject.ViewFactory;
import snakeproject.utils.Coordinate2D;
import snakeproject.utils.GameRenderer;
import snakeproject.utils.Snake;

public class GameController {

    private class LayoutHandler implements ChangeListener<Number> {

        @Override
        public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
            
            double orient = Math.min(rootPane.getHeight(), rootPane.getWidth());
            double proportion = 0.80;
            double side = orient * proportion;
            gameCanvas.setWidth(side);
            gameCanvas.setHeight(side);

           javafx.application.Platform.runLater(() -> { render();});
        }
    }
    
    private final LayoutHandler layoutHandler = new LayoutHandler();

    @FXML
    private StackPane rootPane;
    @FXML
    private StackPane overlayLayer;
    @FXML
    private Canvas gameCanvas;

    private AnimationTimer gameLoop;
    private GameRenderer gameRenderer = new GameRenderer();
    private int numberOfLines = 3;
    private Snake snake = new Snake();
    private Coordinate2D<Integer> food;
    private ArrayList<Coordinate2D<Integer>> freeSpots = new ArrayList<>();

    private void initFreeSpots() {

        for (int i = 0; i <= numberOfLines; i++) {
            for (int j = 0; j <= numberOfLines; j++) {
                freeSpots.add(new Coordinate2D<>(i, j));
            }
        }
    }
    private Coordinate2D<Integer> chooseFreeSpot(){

        if(freeSpots.isEmpty()){
            return null;
        }


        Random rand = new Random();
        int randomIndex = rand.nextInt(freeSpots.size());
        Coordinate2D<Integer> randomDuo = freeSpots.get(randomIndex);
        freeSpots.remove(randomIndex);
        return randomDuo;
    }

    public void initialize() {

        initFreeSpots();
        snake.init(chooseFreeSpot());
        food = chooseFreeSpot();
        gameRenderer.init(gameCanvas.getGraphicsContext2D());
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };

        rootPane.widthProperty().addListener(layoutHandler);
        rootPane.heightProperty().addListener(layoutHandler);

        gameCanvas.sceneProperty().addListener((observable, oldScene, newScene) -> {
        if (newScene != null) {
            attachKeyEvent(newScene);
        }
        });
        
        rootPane.setStyle("-fx-background-color: radial-gradient(focus-angle 0deg, focus-distance 0%, center 50% 50%, radius 100%, #1a2e1a 0%, #000500 100%);");
        gameLoop.start();

    }

    private void attachKeyEvent(Scene scene) {

        scene.setOnKeyPressed(event -> {
            
            Coordinate2D<Integer> head = snake.getHead();

            if (event.getCode() == KeyCode.UP) {
                
                if (head.getY() > 0) {
                    Coordinate2D<Integer> next = new Coordinate2D<>(head.getX(), head.getY() - 1);
                    snake.setDirection("up");
                    handleNextStep(next);
                }

            } else if (event.getCode() == KeyCode.DOWN) {

                if (head.getY() < numberOfLines) { 
                    Coordinate2D<Integer> next = new Coordinate2D<>(head.getX(), head.getY() + 1);
                    snake.setDirection("down");
                    handleNextStep(next);
                }

            } else if (event.getCode() == KeyCode.LEFT) {

                if (head.getX() > 0) {
                    Coordinate2D<Integer> next = new Coordinate2D<>(head.getX() - 1, head.getY());
                    snake.setDirection("left");
                    handleNextStep(next);
                }

            } else if (event.getCode() == KeyCode.RIGHT) {

                if (head.getX() < numberOfLines) {
                    Coordinate2D<Integer> next = new Coordinate2D<>(head.getX() + 1, head.getY());
                    snake.setDirection("right");
                    handleNextStep(next);
                }
            }
            
        });
    }

    private void render() {

        double sideLengthOfCanvas = gameCanvas.getHeight();
        double spacing = sideLengthOfCanvas / (numberOfLines + 1);
        gameRenderer.drawField(sideLengthOfCanvas, numberOfLines);
        gameRenderer.drawSnake(snake, spacing);
        if (food != null) {
            gameRenderer.drawPear(spacing, food.getX(), food.getY());
        }
        
    }

    private void handleNextStep(Coordinate2D<Integer> next){

        if(snake.isBodyPart(next) && !(snake.getTail().equals(next))){
            gameLoop.stop();
            showGameOverOverlay();
            return;
        }


        if(next.equals(food)){

            snake.grow(next);
            render();
            food = chooseFreeSpot();
            if(food == null){
                gameLoop.stop();
                showGameWonOverlay();
                return;
            }
        

        } else {

            Coordinate2D<Integer> oldTail = snake.getTail();
            snake.move(next); 
            if (!next.equals(oldTail)) {
                freeSpots.remove(next);
                freeSpots.add(oldTail);
            }
        }
        
    }

    private void showGameWonOverlay(){

        Parent gameWonView = ViewFactory.buildGameWonView();
        
        overlayLayer.getChildren().setAll(gameWonView);
        overlayLayer.setVisible(true);
        overlayLayer.setMouseTransparent(false);

    }

    private void showGameOverOverlay() {

        Parent gameOverView = ViewFactory.buildGameOverView();
        
        overlayLayer.getChildren().setAll(gameOverView);
        overlayLayer.setVisible(true);
        overlayLayer.setMouseTransparent(false);
    }

}