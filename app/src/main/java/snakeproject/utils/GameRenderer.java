package snakeproject.utils;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameRenderer {

    private GraphicsContext gc;

    public void init(GraphicsContext gc_init){
        gc = gc_init;
    }

    private void clear(double sideLength) {

        if (sideLength <= 0) {
            return;
        }

        gc.clearRect(0, 0, sideLength, sideLength);
    }

    public void drawField(double sideLength, int numberOfLines) {
        
        clear(sideLength); 
        drawHorizontalLines(sideLength, numberOfLines);
        drawVerticalLines(sideLength, numberOfLines);
    }
    
    private void setupGridStyleForLines() {
        
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(4);
    }
   
    private void drawHorizontalLines(double sideLength, int numberOfLines) {
        
        setupGridStyleForLines();
        
        double spacing = sideLength / (numberOfLines + 1);
        double lineOffset = 2.0; 

        gc.strokeLine(0, lineOffset, sideLength, lineOffset);

        for (int i = 0; i < numberOfLines; i++) {
            double position = (i + 1) * spacing;
            gc.strokeLine(0, position, sideLength, position);
        }

        gc.strokeLine(0, sideLength - lineOffset, sideLength, sideLength - lineOffset);
    }

    private void drawVerticalLines(double sideLength, int numberOfLines) {
        
        setupGridStyleForLines();
        
        double spacing = sideLength / (numberOfLines + 1);
        double lineOffset = 2.0; 
        
        gc.strokeLine(lineOffset, 0, lineOffset, sideLength);

        for (int i = 0; i < numberOfLines; i++) {
            double position = (i + 1) * spacing;
            gc.strokeLine(position, 0, position, sideLength);
        }

        gc.strokeLine(sideLength - lineOffset, 0, sideLength - lineOffset, sideLength);
    }

    public void drawPear(double spacing, double GridX, double GridY) {


        int x = (int) (GridX * spacing + spacing * 0.27);
        int y = (int) (GridY * spacing + spacing * 0.15);
        double w = spacing*0.45;
        double h = spacing*0.6;

        gc.setFill(Color.web("#FFD54F")); 
        gc.beginPath();
        gc.moveTo(x + w * 0.5, y + h * 0.25);
        gc.bezierCurveTo(
            x + w * 0.95, y + h * 0.25, 
            x + w * 1.1, y + h * 0.95,   
            x + w * 0.5, y + h * 0.95    
        );
        gc.bezierCurveTo(
            x - w * 0.1, y + h * 0.95,   
            x + w * 0.05, y + h * 0.25,  
            x + w * 0.5, y + h * 0.25    
        );
        gc.fill();

        gc.setStroke(Color.web("#5D4037"));
        gc.setLineWidth(2);
        gc.beginPath();
        gc.moveTo(x + w * 0.5, y + h * 0.25);
        gc.quadraticCurveTo(x + w * 0.6, y + h * 0.1, x + w * 0.55, y + h * 0.05);
        gc.stroke();

        gc.setFill(Color.web("#FFFFFF66"));
        gc.fillOval(x + w * 0.6, y + h * 0.45, w * 0.15, h * 0.2);
    }
    
    public void drawSnake(Snake snake, double spacing) {

        for (Coordinate2D<Integer> segment : snake.getBody()) {

            double x = segment.getX() * spacing + 2;
            double y = segment.getY() * spacing + 2;

            gc.setFill(Color.BLUE);
            gc.fillRect(x, y, spacing - 4, spacing - 4);
        }
    }

}