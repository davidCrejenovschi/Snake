package org.example.ai;

public class Trainer {

    public static void main(String[] args) {
        int numGenerations = 1000;
        int populationSize = 1000;
        int gridSize = 5;

        System.out.println("START ANTRENAMENT...");
        GeneticAlgorithm ga = new GeneticAlgorithm(populationSize, gridSize);

        double absoluteBestFitness = 0;
        GeneticAlgorithm.Agent absoluteBestAgent = null;

        for (int gen = 1; gen <= numGenerations; gen++) {
            ga.evaluatePopulation();

            GeneticAlgorithm.Agent bestOfGen = ga.getBestAgent();

            System.out.printf("Generatia %d | Cel mai bun fitness: %.2f | Mere mancate: %d | Rata mutatie: %.2f\n",
                    gen, bestOfGen.fitness, bestOfGen.apples, ga.getCurrentMutationRate());

            if (bestOfGen.fitness > absoluteBestFitness) {
                absoluteBestFitness = bestOfGen.fitness;
                absoluteBestAgent = new GeneticAlgorithm.Agent(bestOfGen.brain);
                absoluteBestAgent.fitness = bestOfGen.fitness;
            }

            if (gen < numGenerations) {
                ga.evolve();
            }
        }

        System.out.println("ANTRENAMENT FINALIZAT!");
        if (absoluteBestAgent != null) {
            String fileName = "champion.txt";
            absoluteBestAgent.brain.saveToFile(fileName);
            System.out.println("Agentul suprem a fost salvat in fisierul: " + fileName);
        }
    }
}
