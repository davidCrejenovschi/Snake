package snakeproject.utils;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameRenderer {

    private GraphicsContext gc;

    private Color gridColor1 = Color.web("#c7e098");
    private Color gridColor2 = Color.web("#94a86d");
    private Color gridLineColor = Color.web("#2E8B57");
    private Color snakeColor = Color.web("#AED751");

    public void init(GraphicsContext gc_init){
        gc = gc_init;
    }

    public void setTheme(Color gridColor1, Color gridColor2, Color gridLineColor, Color snakeColor) {
        this.gridColor1 = gridColor1;
        this.gridColor2 = gridColor2;
        this.gridLineColor = gridLineColor;
        this.snakeColor = snakeColor;
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
        gc.setStroke(gridLineColor);
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

        for (int row = 0; row < tilesPerSide; row++) {
            for (int col = 0; col < tilesPerSide; col++) {
                
                double x = col * tileSize;
                double y = row * tileSize;

                if ((row + col) % 2 == 0) {
                    gc.setFill(gridColor1);
                } else {
                    gc.setFill(gridColor2);
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
        gc.bezierCurveTo(x + w * 0.95, y + h * 0.25, x + w * 1.1, y + h * 0.95, x + w * 0.5, y + h * 0.95);
        gc.bezierCurveTo(x - w * 0.1, y + h * 0.95, x + w * 0.05, y + h * 0.25, x + w * 0.5, y + h * 0.25);
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
    
    public void drawLilyPad(double spacing, double GridX, double GridY) {
        
        double w = spacing * 0.8;
        double h = spacing * 0.8;
        
        double x = GridX * spacing + (spacing - w) / 2;
        double y = GridY * spacing + (spacing - h) / 2;
        
        double centerX = x + w / 2;
        double centerY = y + h / 2;

        gc.setFill(Color.web("#4CAF50")); 
        
        gc.fillArc(x, y, w, h, 45, 315, javafx.scene.shape.ArcType.ROUND);

        gc.save(); 
        
        gc.translate(centerX, centerY);

        int petals = 8; 
        double petalLen = w * 0.35; 
        double petalWidth = w * 0.12; 

        gc.setFill(Color.web("#E91E63")); 
        for (int i = 0; i < petals; i++) {
            gc.rotate(360.0 / petals);
            gc.fillOval(0, -petalWidth / 2, petalLen, petalWidth);
        }

        gc.rotate(360.0 / (petals * 2)); 
        gc.setFill(Color.web("#F8BBD0")); 
        double innerPetalLen = petalLen * 0.7;
        
        for (int i = 0; i < petals; i++) {
            gc.rotate(360.0 / petals);
            gc.fillOval(0, -petalWidth / 2, innerPetalLen, petalWidth);
        }

        gc.restore(); 

        gc.setFill(Color.web("#FFEB3B")); 
        double centerSize = w * 0.15;
        gc.fillOval(centerX - centerSize / 2, centerY - centerSize / 2, centerSize, centerSize);
    }
    
    public void drawSnake(Snake snake, double spacing) {
        var body = snake.getBody();
        if (body.isEmpty()) return;

        gc.setStroke(snakeColor);
        gc.setFill(snakeColor);

        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        if (body.size() > 1) {
            gc.setLineWidth(spacing * 0.85); 
            gc.beginPath();

            double startX = body.getFirst().getX() * spacing + spacing / 2;
            double startY = body.getFirst().getY() * spacing + spacing / 2;
            gc.moveTo(startX, startY);

            for (int i = 1; i < body.size(); i++) {
                double nextX = body.get(i).getX() * spacing + spacing / 2;
                double nextY = body.get(i).getY() * spacing + spacing / 2;
                gc.lineTo(nextX, nextY);
            }
            gc.stroke();
        }

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

        gc.save();
        gc.translate(hX, hY);
        gc.rotate(angle);

        gc.beginPath();
        gc.moveTo(-spacing * 0.2, 0); 
        gc.bezierCurveTo(-spacing * 0.2, -spacing * 0.45, spacing * 0.4, -spacing * 0.45, spacing * 0.4, 0);
        gc.bezierCurveTo(spacing * 0.4, spacing * 0.45, -spacing * 0.2, spacing * 0.45, -spacing * 0.2, 0);
        gc.fill();

        gc.setFill(Color.BLACK);
        double eyeSize = spacing * 0.12;
        double eyeX = spacing * 0.15; 
        double eyeY = spacing * 0.18; 

        gc.fillOval(eyeX, -eyeY - eyeSize/2, eyeSize, eyeSize);
        gc.fillOval(eyeX, eyeY - eyeSize/2, eyeSize, eyeSize);

        gc.restore();
    }
}