package snakeproject.controllers;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class GameController {


    @FXML
    private StackPane rootPane;

    @FXML
    private Canvas gameCanvas;

    private int nb_lines = 2;

    public void initialize() {

        gameCanvas.widthProperty().bind(rootPane.widthProperty().multiply(0.60));
        gameCanvas.heightProperty().bind(rootPane.heightProperty().multiply(0.60));

        render();

        gameCanvas.widthProperty().addListener((obs, oldVal, newVal) -> render());
        gameCanvas.heightProperty().addListener((obs, oldVal, newVal) -> render());


    }

    private void render() {

        clearGameCanvas();
        drawVLines();
        drawHLines();
       
    }

    private void clearGameCanvas(){

        if (getWidth() == 0 || getHeight() == 0) {
                    return;
                }

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
    }

    private void drawHLines(){
        
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(4);
        int spacing = (int) getHeight() / (nb_lines+1);

        gc.strokeLine(0, 0+2, getWidth(), 0+2);

        for(int i=0; i<nb_lines; i++){
            int aux = (i+1)*spacing;
            gc.strokeLine(0, aux, getWidth(), aux);
        }

    
        gc.strokeLine(0, getHeight()-2, getWidth(), getHeight()-2);


    }

    private void drawVLines(){

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(4);
        int spacing = (int) getWidth()/ (nb_lines+1);

        gc.strokeLine(0+2, 0, 0+2, getHeight());

        for(int i=0; i<nb_lines; i++){
            int aux = (i+1)*spacing;
            gc.strokeLine(aux, 0, aux, getHeight());
        }

        gc.strokeLine(getWidth()-2, 0, getWidth()-2, getHeight());

    }

    public double getWidth() {
        return gameCanvas.getWidth();
    }

    public double getHeight() {
        return gameCanvas.getHeight();
    }
}