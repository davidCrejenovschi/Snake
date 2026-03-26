package org.example.ai;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.IntStream;

public class GeneticAlgorithm {

    private Agent[] population;
    private final int populationSize;
    private final int gridSize;

    private double currentMutationRate = 0.05;
    private double bestFitnessEver = 0;
    private int generationsWithoutImprovement = 0;

    public static class Agent {
        public NeuralNetwork brain;
        public double fitness;
        public int apples;

        public Agent(NeuralNetwork brain) {
            this.brain = brain;
            this.fitness = 0;
            this.apples = 0;
        }
    }

    public GeneticAlgorithm(int populationSize, int gridSize) {
        this.populationSize = populationSize;
        this.gridSize = gridSize;
        this.population = new Agent[populationSize];

        for (int i = 0; i < populationSize; i++) {
            population[i] = new Agent(new NeuralNetwork(24, 24, 3));
        }
    }

    public void evaluatePopulation() {
        IntStream.range(0, populationSize).parallel().forEach(i -> {
            Agent agent = population[i];

            org.example.utils.GameEngine engine = new org.example.utils.GameEngine(gridSize);

            SnakeEnvironment env = new SnakeEnvironment(engine);

            while (!env.isDone()) {
                double[] vision = env.getVision();
                int action = agent.brain.predict(vision);
                env.step(action);
            }

            agent.fitness = env.getFitness();
            agent.apples = env.getApplesEaten();
        });
    }

    public void evolve() {
        Arrays.sort(population, Comparator.comparingDouble((Agent a) -> a.fitness).reversed());

        updateAdaptiveMutation();

        Agent[] newPopulation = new Agent[populationSize];
        int elitismCount = (int) (populationSize * 0.05);

        for (int i = 0; i < elitismCount; i++) {
            newPopulation[i] = new Agent(population[i].brain);
            newPopulation[i].fitness = population[i].fitness;
            newPopulation[i].apples = population[i].apples;
        }

        Random rand = new Random();
        for (int i = elitismCount; i < populationSize; i++) {
            Agent parentA = selectParentRankBased(rand);
            Agent parentB = selectParentRankBased(rand);

            NeuralNetwork childBrain = parentA.brain.crossover(parentB.brain);
            childBrain.mutate(currentMutationRate);

            newPopulation[i] = new Agent(childBrain);
        }

        population = newPopulation;
    }

    private void updateAdaptiveMutation() {
        double currentBestFitness = population[0].fitness;

        if (currentBestFitness > bestFitnessEver) {
            bestFitnessEver = currentBestFitness;
            generationsWithoutImprovement = 0;
            currentMutationRate = 0.05;
        } else {
            generationsWithoutImprovement++;
        }

        if (generationsWithoutImprovement > 10) {
            currentMutationRate = Math.min(0.20, currentMutationRate + 0.02);
        }
    }

    private Agent selectParentRankBased(Random rand) {
        int rankSum = (populationSize * (populationSize + 1)) / 2;
        int randomValue = rand.nextInt(rankSum);

        int partialSum = 0;
        for (int i = 0; i < populationSize; i++) {
            partialSum += (populationSize - i);
            if (partialSum >= randomValue) {
                return population[i];
            }
        }
        return population[0];
    }

    public Agent getBestAgent() {
        return population[0];
    }

    public double getCurrentMutationRate() {
        return currentMutationRate;
    }
}
