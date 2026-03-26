package org.example.ai;

import org.example.utils.Coordinate2D;
import org.example.utils.GameEngine;
import org.example.utils.Snake;

public class SnakeEnvironment {

    private final GameEngine engine;
    private final int maxStepsWithoutFood;

    private int stepsSinceLastMeal = 0;
    private int totalSteps = 0;
    private int applesEaten = 0;
    private double fitness = 0.0;

    public SnakeEnvironment(GameEngine engine) {
        this.engine = engine;
        this.maxStepsWithoutFood = ( engine.getNumberOfLines() + 1 ) * ( engine.getNumberOfLines() + 1);
    }

    public void step(int action) {

        if (isDone()) return;

        Snake snake = engine.getSnake();
        String newDir = getString(action, snake);

        int dx = switch (newDir) {
            case "left" -> -1;
            case "right" -> 1;
            default -> 0;
        };

        int dy = switch (newDir) {
            case "up" -> -1;
            case "down" -> 1;
            default -> 0;
        };

        snake.setDirection(newDir);
        engine.move(dx, dy);

        totalSteps++;
        stepsSinceLastMeal++;

        if (engine.didJustEat()) {
            applesEaten++;
            stepsSinceLastMeal = 0;
        }

        calculateFitness();
    }

    private static String getString(int action, Snake snake) {
        String currentDir = snake.getDirection();
        String newDir = currentDir;

        if (action == 1) {
            newDir = switch (currentDir) {
                case "up" -> "left";
                case "down" -> "right";
                case "left" -> "down";
                case "right" -> "up";
                default -> currentDir;
            };
        } else if (action == 2) {
            newDir = switch (currentDir) {
                case "up" -> "right";
                case "down" -> "left";
                case "left" -> "up";
                case "right" -> "down";
                default -> currentDir;
            };
        }
        return newDir;
    }

    private void calculateFitness() {

        fitness = totalSteps + (Math.pow(2, applesEaten) + (applesEaten * 500));
    }

    public double[] getVision() {

        double[] vision = new double[24];
        Snake snake = engine.getSnake();

        int[][] directions = getRelativeDirections(snake.getDirection());

        for (int i = 0; i < 8; i++) {
            double[] look = lookInDirection(directions[i][0], directions[i][1]);
            System.arraycopy(look, 0, vision, i * 3, 3);
        }

        return vision;
    }

    private int[][] getRelativeDirections(String currentDirection) {

        return switch (currentDirection) {
            case "up" -> new int[][]{{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
            case "right" -> new int[][]{{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
            case "down" -> new int[][]{{0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}};
            case "left" -> new int[][]{{-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}};
            default -> new int[8][2];
        };
    }

    private double[] lookInDirection(int dx, int dy) {
        double[] result = new double[3];
        Coordinate2D<Integer> head = engine.getSnake().getHead();
        Coordinate2D<Integer> food = engine.getFood();
        int cx = head.getX();
        int cy = head.getY();
        double distance = 0.0;

        boolean foundFood = false;
        boolean foundBody = false;

        while (true) {
            cx += dx;
            cy += dy;
            distance += 1.0;

            if (cx < 0 || cx > engine.getNumberOfLines() || cy < 0 || cy > engine.getNumberOfLines()) {
                result[0] = 1.0 / distance;
                break;
            }

            if (!foundFood && food.getX() == cx && food.getY() == cy) {
                result[1] = 1.0 / distance;
                foundFood = true;
            }

            if (!foundBody && engine.getSnake().isBodyPart(new Coordinate2D<>(cx, cy))) {
                result[2] = 1.0 / distance;
                foundBody = true;
            }
        }
        return result;
    }

    public boolean isDone() {
        return engine.isGameOver() || engine.isLevelWon() || stepsSinceLastMeal >= maxStepsWithoutFood;
    }

    public double getFitness() { return fitness; }
    public int getApplesEaten() { return applesEaten; }
}