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
        drawGridBackground(sideLength, numberOfLines);
        drawHorizontalLines(sideLength, numberOfLines);
        drawVerticalLines(sideLength, numberOfLines);
    }
    
    private void setupGridStyleForLines() {
        
        gc.setStroke(Color.web("#2E8B57"));
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

    public void drawGridBackground(double sideLength, int numberOfLines) {
        
        int tilesPerSide = numberOfLines + 1;
        double tileSize = sideLength / tilesPerSide;

        Color color1 = Color.web("#c7e098"); // Verde închis
        Color color2 = Color.web("#94a86d"); // Verde deschis

        for (int row = 0; row < tilesPerSide; row++) {
            for (int col = 0; col < tilesPerSide; col++) {
                
                double x = col * tileSize;
                double y = row * tileSize;

                if ((row + col) % 2 == 0) {
                    gc.setFill(color1);
                } else {
                    gc.setFill(color2);
                }

                gc.fillRect(x, y, tileSize, tileSize);
            }
        }
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
        
        var body = snake.getBody();
        if (body.isEmpty()) return;

        Color snakeColor = Color.web("#AED751"); 

        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        // --- 1. DESENARE CORP (Linie continuă) ---
        if (body.size() > 1) {
            gc.setStroke(snakeColor);
            gc.setLineWidth(spacing * 0.85); // Grosimea corpului
            gc.beginPath();

            // Pornim de la cap
            double startX = body.getFirst().getX() * spacing + spacing / 2;
            double startY = body.getFirst().getY() * spacing + spacing / 2;
            gc.moveTo(startX, startY);

            // Trasam linii către fiecare segment al corpului
            // 'StrokeLineJoin.ROUND' se va ocupa automat de rotunjirea colțurilor
            for (int i = 1; i < body.size(); i++) {
                double nextX = body.get(i).getX() * spacing + spacing / 2;
                double nextY = body.get(i).getY() * spacing + spacing / 2;
                gc.lineTo(nextX, nextY);
            }
            gc.stroke();
        }

        // --- 2. CALCUL DIRECȚIE CAP ---
        var head = body.getFirst();
        double hX = head.getX() * spacing + spacing / 2;
        double hY = head.getY() * spacing + spacing / 2;

        double dirX = 0;
        double dirY = 0;

        if (body.size() > 1) {
            var neck = body.get(1);
            dirX = head.getX() - neck.getX();
            dirY = head.getY() - neck.getY();
        } else {
            switch (snake.getDirection()) {
                case "up" -> dirY = -1;
                case "down" -> dirY = 1;
                case "left" -> dirX = -1;
                case "right" -> dirX = 1;
            }
        }
        double angle = Math.toDegrees(Math.atan2(dirY, dirX));

        // --- 3. DESENARE CAP (Peste corp) ---
        gc.save();
        gc.translate(hX, hY);
        gc.rotate(angle);

        gc.setFill(snakeColor);
        gc.beginPath();
        
        // Formă ovală compactă (nu iese din pătrat)
        // Pornim din spatele capului
        gc.moveTo(-spacing * 0.2, 0); 

        // Contur superior
        gc.bezierCurveTo(
            -spacing * 0.2, -spacing * 0.45, 
            spacing * 0.4, -spacing * 0.45, 
            spacing * 0.4, 0
        );

        // Contur inferior
        gc.bezierCurveTo(
            spacing * 0.4, spacing * 0.45, 
            -spacing * 0.2, spacing * 0.45, 
            -spacing * 0.2, 0
        );
        
        gc.fill();

        // --- 4. OCHII ---
        gc.setFill(Color.BLACK);
        double eyeSize = spacing * 0.12;
        double eyeX = spacing * 0.15; 
        double eyeY = spacing * 0.18; 

        gc.fillOval(eyeX, -eyeY - eyeSize/2, eyeSize, eyeSize);
        gc.fillOval(eyeX, eyeY - eyeSize/2, eyeSize, eyeSize);

        gc.restore();
    }
}