package org.example.ai;

import org.example.utils.Coordinate2D;
import org.example.utils.GameEngine;

public class SnakeAgent {
    public GameEngine engine;
    public NeuralNetwork brain;
    public double fitness;

    // Variabile temporare pentru fiecare meci
    public int stepsSurvived;
    public int stepsSinceLastMeal;

    public SnakeAgent() {
        this.brain = new NeuralNetwork();
        this.fitness = 0;
    }

    public SnakeAgent(NeuralNetwork inheritedBrain) {
        this.brain = inheritedBrain;
        this.fitness = 0;
    }

    public void evaluateFitness(int numberOfGames, int gridSize) {
        double totalScore = 0;

        for (int i = 0; i < numberOfGames; i++) {
            this.engine = new GameEngine(gridSize);
            this.stepsSurvived = 0;
            this.stepsSinceLastMeal = 0;

            // Limită strictă: dacă nu mănâncă în (gridSize * gridSize) pași, e mort de foame
            int starvationLimit = gridSize * gridSize;

            while (!engine.isGameOver() && !engine.isLevelWon() && stepsSinceLastMeal < starvationLimit) {
                lookAndMove();
            }

            int applesEaten = engine.getSnake().getBody().size() - 1;

            // SCORING NOU:
            // Puncte masive pentru mere. Pedepsit ușor pentru fiecare pas făcut,
            // ca să aleagă ruta cea mai scurtă către măr.
            double gameScore = (Math.pow(2, applesEaten) + (applesEaten * 500)) - (stepsSurvived * 0.1);

            if (gameScore < 0) gameScore = 0.1; // Evităm scoruri negative

            if (engine.isLevelWon()) {
                gameScore += 10000;
            }

            totalScore += gameScore;
        }

        this.fitness = totalScore / numberOfGames;
    }

    public void lookAndMove() {
        if (engine.isGameOver() || engine.isLevelWon()) return;

        // 1. Obținem cele 11 date de stare (State Space)
        double[] state = getState(engine);

        // 2. Creierul alege din 3 opțiuni: 0 (Înainte), 1 (Dreapta), 2 (Stânga)
        int action = brain.predict(state);

        int sizeBefore = engine.getSnake().getBody().size();

        // 3. Executăm virajul relativ
        applyRelativeAction(action);
        stepsSurvived++;

        if (engine.getSnake().getBody().size() > sizeBefore) {
            stepsSinceLastMeal = 0;
        } else {
            stepsSinceLastMeal++;
        }
    }

    private double[] getState(GameEngine e) {
        int headX = e.getSnake().getHead().getX();
        int headY = e.getSnake().getHead().getY();
        int foodX = e.getFood().getX();
        int foodY = e.getFood().getY();
        String currentDir = e.getSnake().getDirection();

        // Variabile pentru direcția curentă (boolean ca 1.0 sau 0.0)
        double dirUp = currentDir.equals("up") ? 1.0 : 0.0;
        double dirDown = currentDir.equals("down") ? 1.0 : 0.0;
        double dirLeft = currentDir.equals("left") ? 1.0 : 0.0;
        double dirRight = currentDir.equals("right") ? 1.0 : 0.0;

        // Puncte direct adiacente capului
        Coordinate2D<Integer> pointUp = new Coordinate2D<>(headX, headY - 1);
        Coordinate2D<Integer> pointDown = new Coordinate2D<>(headX, headY + 1);
        Coordinate2D<Integer> pointLeft = new Coordinate2D<>(headX - 1, headY);
        Coordinate2D<Integer> pointRight = new Coordinate2D<>(headX + 1, headY);

        // Pericol Absolut (Zid sau Corp)
        boolean dangerUp = isCollision(e, pointUp);
        boolean dangerDown = isCollision(e, pointDown);
        boolean dangerLeft = isCollision(e, pointLeft);
        boolean dangerRight = isCollision(e, pointRight);

        // Senzorii ceruți de text: Pericol Față, Stânga, Dreapta (raportat la direcția curentă)
        double dangerStraight = 0.0, dangerTurnLeft = 0.0, dangerTurnRight = 0.0;

        if (dirUp == 1.0) {
            dangerStraight = dangerUp ? 1.0 : 0.0;
            dangerTurnRight = dangerRight ? 1.0 : 0.0;
            dangerTurnLeft = dangerLeft ? 1.0 : 0.0;
        } else if (dirDown == 1.0) {
            dangerStraight = dangerDown ? 1.0 : 0.0;
            dangerTurnRight = dangerLeft ? 1.0 : 0.0;
            dangerTurnLeft = dangerRight ? 1.0 : 0.0;
        } else if (dirLeft == 1.0) {
            dangerStraight = dangerLeft ? 1.0 : 0.0;
            dangerTurnRight = dangerUp ? 1.0 : 0.0;
            dangerTurnLeft = dangerDown ? 1.0 : 0.0;
        } else if (dirRight == 1.0) {
            dangerStraight = dangerRight ? 1.0 : 0.0;
            dangerTurnRight = dangerDown ? 1.0 : 0.0;
            dangerTurnLeft = dangerUp ? 1.0 : 0.0;
        }

        // Returnăm fix vectorul de 11 valori (Pericole, Direcții, Mâncare relativă)
        return new double[]{
                dangerStraight, dangerTurnLeft, dangerTurnRight, // 3 senzori pericol
                dirUp, dirDown, dirLeft, dirRight,               // 4 senzori direcție curentă
                foodX < headX ? 1.0 : 0.0, // Food is Left      // 4 senzori direcție mâncare
                foodX > headX ? 1.0 : 0.0, // Food is Right
                foodY < headY ? 1.0 : 0.0, // Food is Up
                foodY > headY ? 1.0 : 0.0  // Food is Down
        };
    }

    private boolean isCollision(GameEngine e, Coordinate2D<Integer> pt) {
        // Lovește peretele?
        if (pt.getX() < 0 || pt.getX() >= e.getNumberOfLines() || pt.getY() < 0 || pt.getY() >= e.getNumberOfLines()) {
            return true;
        }
        // Lovește propriul corp?
        for (var segment : e.getSnake().getBody()) {
            if (segment.getX().equals(pt.getX()) && segment.getY().equals(pt.getY())) {
                return true;
            }
        }
        return false;
    }

    private void applyRelativeAction(int action) {
        String currentDir = engine.getSnake().getDirection();

        // Acțiunea 0: Merge drept înainte (nu schimbă direcția)
        if (action == 0) {
            moveByDirection(currentDir);
            return;
        }

        // Acțiunea 1: Viraj Dreapta
        if (action == 1) {
            switch (currentDir) {
                case "up" -> moveByDirection("right");
                case "down" -> moveByDirection("left");
                case "left" -> moveByDirection("up");
                case "right" -> moveByDirection("down");
            }
            return;
        }

        // Acțiunea 2: Viraj Stânga
        if (action == 2) {
            switch (currentDir) {
                case "up" -> moveByDirection("left");
                case "down" -> moveByDirection("right");
                case "left" -> moveByDirection("down");
                case "right" -> moveByDirection("up");
            }
        }
    }

    private void moveByDirection(String dir) {
        engine.getSnake().setDirection(dir);
        switch (dir) {
            case "up" -> engine.move(0, -1);
            case "down" -> engine.move(0, 1);
            case "left" -> engine.move(-1, 0);
            case "right" -> engine.move(1, 0);
        }
    }
}