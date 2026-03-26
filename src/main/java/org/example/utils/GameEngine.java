package org.example.utils;
import java.util.Random;


public class GameEngine {

    private final int numberOfLines;
    private final Snake snake;
    private Coordinate2D<Integer> food;
    private boolean isGameOver = false;
    private boolean isLevelWon = false;
    private boolean justAte = false;
    private final Random rand = new Random();

    public GameEngine(int numberOfLines) {
        this.numberOfLines = numberOfLines;
        this.snake = new Snake();
        spawnInitialSnakeAndFood();
    }

    private Coordinate2D<Integer> spawnRandomFood() {

        int totalTiles = (numberOfLines + 1) * (numberOfLines + 1);

        if (snake.getBody().size() >= totalTiles) {
            isLevelWon = true;
            return null;
        }

        int x, y;
        Coordinate2D<Integer> newFood;
        do {
            x = rand.nextInt(numberOfLines + 1);
            y = rand.nextInt(numberOfLines + 1);
            newFood = new Coordinate2D<>(x, y);
        } while (snake.isBodyPart(newFood));

        return newFood;
    }

    private void spawnInitialSnakeAndFood() {
        int headX = numberOfLines / 2;
        int headY = numberOfLines / 2;
        Coordinate2D<Integer> head = new Coordinate2D<>(headX, headY);
        Coordinate2D<Integer> tail = new Coordinate2D<>(headX + 1, headY);

        snake.init(head, tail);
        snake.setDirection("left");
        food = spawnRandomFood();
    }

    public void move(int dx, int dy) {
        if (isGameOver || isLevelWon) return;
        justAte = false;

        Coordinate2D<Integer> head = snake.getHead();
        Coordinate2D<Integer> next = new Coordinate2D<>(head.getX() + dx, head.getY() + dy);

        if (next.getX() < 0 || next.getX() > numberOfLines || next.getY() < 0 || next.getY() > numberOfLines) {
            isGameOver = true;
            return;
        }

        if (snake.isBodyPart(next) && !snake.getTail().equals(next)) {
            isGameOver = true;
            return;
        }

        if (next.equals(food)) {
            snake.grow(next);
            justAte = true;
            food = spawnRandomFood();
            if (food == null) {
                isLevelWon = true;
            }
        }else {
            snake.move(next);
        }
    }

    public Snake getSnake() { return snake; }
    public Coordinate2D<Integer> getFood() { return food; }
    public boolean isGameOver() { return isGameOver; }
    public boolean isLevelWon() { return isLevelWon; }
    public boolean didJustEat() { return justAte; }
    public int getNumberOfLines() { return numberOfLines; }
}