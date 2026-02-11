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
import snakeproject.utils.LevelManager;
import snakeproject.utils.LevelSettings;
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
    
    private int numberOfLines; 

    private Snake snake = new Snake();
    private Coordinate2D<Integer> food;
    private ArrayList<Coordinate2D<Integer>> freeSpots = new ArrayList<>();

    private void initFreeSpots() {
        
        freeSpots.clear(); 
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

    private Coordinate2D<Integer> getValidNeighbor(Coordinate2D<Integer> head) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int[] dir : directions) {
            int newX = head.getX() + dir[0];
            int newY = head.getY() + dir[1];
            
            if (newX >= 0 && newX <= numberOfLines && newY >= 0 && newY <= numberOfLines) {
                Coordinate2D<Integer> neighbor = new Coordinate2D<>(newX, newY);
                if (freeSpots.contains(neighbor)) {
                    return neighbor;
                }
            }
        }
        return null; 
    }

    @FXML
    public void initialize() {
        gameRenderer.init(gameCanvas.getGraphicsContext2D());

        rootPane.widthProperty().addListener(layoutHandler);
        rootPane.heightProperty().addListener(layoutHandler);

        gameCanvas.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                attachKeyEvent(newScene);
            }
        });
    }

    public void startLevel(LevelSettings settings) {
        this.numberOfLines = settings.getNumberOfLines();

        if (settings.getCssPath() != null) {
            try {
                // Curățăm stilurile vechi
                rootPane.getStylesheets().clear();
                
                // Încărcăm noul CSS
                String css = getClass().getResource(settings.getCssPath()).toExternalForm();
                rootPane.getStylesheets().add(css);
            } catch (Exception e) {
                System.err.println("Nu s-a putut încărca CSS-ul: " + settings.getCssPath());
                e.printStackTrace();
            }
        }

        gameRenderer.setTheme(
            settings.getGridColor1(),
            settings.getGridColor2(),
            settings.getGridLineColor(),
            settings.getSnakeColor()
        );

        snake = new Snake();
        initFreeSpots();

        Coordinate2D<Integer> head = chooseFreeSpot(); 
        Coordinate2D<Integer> tail = getValidNeighbor(head);
        if (tail != null) {
            freeSpots.remove(tail);
        }
        snake.init(head, tail);
        food = chooseFreeSpot();

        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        gameLoop.start();
        
        render();
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
        if (numberOfLines == 0) return; 
        
        double spacing = sideLengthOfCanvas / (numberOfLines + 1);
        gameRenderer.drawField(sideLengthOfCanvas, numberOfLines);
        gameRenderer.drawSnake(snake, spacing);
        if (food != null) {
            // LOGICA DE DESENARE ÎN FUNCȚIE DE NIVEL
            if (numberOfLines == 2) { 
                // Dacă e Nivelul 1 (Pădure - Grid mic) -> Desenăm Para
                gameRenderer.drawPear(spacing, food.getX(), food.getY());
            } else {
                // Dacă e Nivelul 2 (Apă - Grid mare) -> Desenăm Nufărul
                gameRenderer.drawLilyPad(spacing, food.getX(), food.getY());
            }
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
                render();
                gameLoop.stop();
                
                // --- LOGICA TEMPORARĂ DE TRECERE LA NIVELUL 2 ---
                // Verificăm dacă suntem la Nivelul 1 (care are numberOfLines == 2)
                if (numberOfLines == 2) { 
                    // Luăm nivelul cu indexul 1 (Nivelul 2 - Apa)
                    LevelSettings level2 = LevelManager.getLevel(1);
                    startLevel(level2);
                } else {
                    // Dacă suntem deja la nivelul 2 (sau altul), afișăm ecranul de victorie
                    showGameWonOverlay();
                }
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