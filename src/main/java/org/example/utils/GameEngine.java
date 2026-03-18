package org.example.utils;

import java.util.ArrayList;
import java.util.Random;

public class GameEngine {

    private final int numberOfLines;
    private final Snake snake;
    private Coordinate2D<Integer> food;
    private final ArrayList<Coordinate2D<Integer>> freeSpots;
    private boolean isGameOver = false;
    private boolean isLevelWon = false;
    private boolean justAte = false;

    public GameEngine(int numberOfLines) {
        this.numberOfLines = numberOfLines;
        this.freeSpots = new ArrayList<>();
        this.snake = new Snake();
        initFreeSpots();
        spawnInitialSnakeAndFood();
    }

    private void initFreeSpots() {
        freeSpots.clear();
        for (int i = 0; i <= numberOfLines; i++) {
            for (int j = 0; j <= numberOfLines; j++) {
                freeSpots.add(new Coordinate2D<>(i, j));
            }
        }
    }

    private Coordinate2D<Integer> chooseFreeSpot() {
        if (freeSpots.isEmpty()) {
            return null;
        }
        Random rand = new Random();
        int randomIndex = rand.nextInt(freeSpots.size());
        return freeSpots.remove(randomIndex);
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

    private void spawnInitialSnakeAndFood() {
        Coordinate2D<Integer> head = chooseFreeSpot();
        Coordinate2D<Integer> tail = getValidNeighbor(head);
        if (tail != null) {
            freeSpots.remove(tail);
        }
        snake.init(head, tail);
        food = chooseFreeSpot();
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

        handleNextStep(next);
    }

    private void handleNextStep(Coordinate2D<Integer> next) {
        if (snake.isBodyPart(next) && !snake.getTail().equals(next)) {
            isGameOver = true;
            return;
        }

        if (next.equals(food)) {
            snake.grow(next);
            justAte = true;
            food = chooseFreeSpot();
            if (food == null) {
                isLevelWon = true;
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

    public Snake getSnake() { return snake; }
    public Coordinate2D<Integer> getFood() { return food; }
    public boolean isGameOver() { return isGameOver; }
    public boolean isLevelWon() { return isLevelWon; }
    public boolean didJustEat() { return justAte; }
    public int getNumberOfLines() { return numberOfLines; }
}