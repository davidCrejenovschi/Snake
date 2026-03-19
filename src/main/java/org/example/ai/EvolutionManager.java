package org.example.ai;

import java.util.ArrayList;
import java.util.List;

public class EvolutionManager {
    private final int POPULATION_SIZE = 1000;
    private final double MUTATION_RATE = 0.05; // 5% șansă de a schimba un neuron

    public void startEvolution() {
        System.out.println("[*] Se generează Populația Zero (" + POPULATION_SIZE + " de agenți)...");
        List<SnakeAgent> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new SnakeAgent());
        }

        int generation = 1;

        while (true) {
            // 1. Rulăm jocurile (foarte rapid, fără grafică)
            for (SnakeAgent agent : population) {
                // Limităm pașii la 500 ca să nu se învârtă în cerc la infinit
                while (!agent.engine.isGameOver() && !agent.engine.isLevelWon() && agent.stepsSurvived < 500) {
                    agent.lookAndMove();
                }
                agent.calculateFitness();
            }

            // 2. Sortăm descrescător după scor
            population.sort((a, b) -> Double.compare(b.fitness, a.fitness));

            SnakeAgent bestAgent = population.get(0);
            int mereMancate = bestAgent.engine.getSnake().getBody().size() - 1;

            System.out.printf("Gen: %d | Fitness: %.0f | Mere: %d | Pași: %d\n",
                    generation, bestAgent.fitness, mereMancate, bestAgent.stepsSurvived);

            if (bestAgent.engine.isLevelWon()) {
                System.out.println("🏆 VICTORIE ABSOLUTĂ în generația " + generation + "!");

                // SALVĂM CREIERUL PE HARD DISK
                try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(new java.io.FileOutputStream("campion_4x4.dat"))) {
                    out.writeObject(bestAgent.brain);
                    System.out.println("[*] Creierul a fost salvat cu succes în 'campion_4x4.dat'!");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break; // Oprim evoluția momentan când câștigă
            }

            // 3. Creăm noua generație
            population = createNextGeneration(population);
            generation++;
        }
    }

    private List<SnakeAgent> createNextGeneration(List<SnakeAgent> oldPopulation) {
        List<SnakeAgent> newPopulation = new ArrayList<>();

        // ELITISM: Păstrăm top 10% intacți. Astfel, AI-ul nu "uită" niciodată ce a învățat bun.
        int eliteCount = POPULATION_SIZE / 10;
        for (int i = 0; i < eliteCount; i++) {
            NeuralNetwork eliteBrain = oldPopulation.get(i).brain.cloneAndMutate(0.0);
            newPopulation.add(new SnakeAgent(eliteBrain));
        }

        // REPRODUCERE: Restul de 90% sunt copii mutanți ai elitelor
        for (int i = eliteCount; i < POPULATION_SIZE; i++) {
            // Alegem un părinte din elite la întâmplare
            NeuralNetwork parentBrain = oldPopulation.get((int)(Math.random() * eliteCount)).brain;
            NeuralNetwork childBrain = parentBrain.cloneAndMutate(MUTATION_RATE);
            newPopulation.add(new SnakeAgent(childBrain));
        }

        return newPopulation;
    }
}