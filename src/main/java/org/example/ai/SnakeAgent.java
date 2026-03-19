package org.example.ai;
import org.example.utils.GameEngine;

public class SnakeAgent {
    public GameEngine engine;
    public NeuralNetwork brain;
    public double fitness;
    public int stepsSurvived;

    // Când creăm un agent complet nou (Generația 1)
    public SnakeAgent() {
        this.engine = new GameEngine(4); // Antrenăm pe 4x4
        this.brain = new NeuralNetwork();
        this.fitness = 0;
        this.stepsSurvived = 0;
    }

    // Când un agent "face pui" (Generațiile următoare)
    public SnakeAgent(NeuralNetwork inheritedBrain) {
        this.engine = new GameEngine(4);
        this.brain = inheritedBrain;
        this.fitness = 0;
        this.stepsSurvived = 0;
    }

    // Aici AI-ul "privește" tabla și decide mutarea
    public void lookAndMove() {
        if (engine.isGameOver() || engine.isLevelWon()) return;

        double[] vision = getFlattenedGrid(engine);
        int action = brain.predict(vision);

        applyAction(action);
        stepsSurvived++;
    }

    // Calculăm cât de bun a fost la finalul vieții
    public void calculateFitness() {
        int applesEaten = engine.getSnake().getBody().size() - 1; // Mărimea șarpelui minus capul

        // Regula de aur pe 4x4: Merele sunt sfinte, dar și supraviețuirea contează
        this.fitness = (applesEaten * 100) + stepsSurvived;

        if (engine.isLevelWon()) {
            this.fitness += 5000; // Jackpot-ul suprem
        }
    }

    // --- Funcțiile tale clasice de viziune și mișcare ---
    private double[] getFlattenedGrid(GameEngine e) {
        int gridSize = e.getNumberOfLines();
        double[] grid = new double[gridSize * gridSize];
        int headX = e.getSnake().getHead().getX();
        int headY = e.getSnake().getHead().getY();
        int foodX = e.getFood().getX();
        int foodY = e.getFood().getY();

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                int index = y * gridSize + x;
                if (x == headX && y == headY) grid[index] = 0.5;
                else if (x == foodX && y == foodY) grid[index] = 1.0;
                else if (isPartOfSnakeBody(e, x, y)) grid[index] = -1.0;
                else grid[index] = 0.0;
            }
        }
        return grid;
    }

    private boolean isPartOfSnakeBody(GameEngine e, int x, int y) {
        for (var segment : e.getSnake().getBody()) {
            if (segment.getX() == x && segment.getY() == y) return true;
        }
        return false;
    }

    private void applyAction(int action) {
        switch (action) {
            case 0 -> engine.move(0, -1); // Sus
            case 1 -> engine.move(0, 1);  // Jos
            case 2 -> engine.move(-1, 0); // Stânga
            case 3 -> engine.move(1, 0);  // Dreapta
        }
    }
}
