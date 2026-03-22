package org.example.ai;

import java.util.ArrayList;
import java.util.List;

public class EvolutionManager {
    private final int POPULATION_SIZE = 1000;

    public void startEvolution() {
        System.out.println("[*] Se generează Populația Zero (" + POPULATION_SIZE + " de agenți)...");
        List<SnakeAgent> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new SnakeAgent());
        }

        int generation = 1;

        double globalBestFitness = 0;
        int globalBestApples = 0;

        int generationsWithoutRecord = 0;
        double currentMutationRate = 0.05; // Mutația standard

        while (true) {

            // 1. Rulăm jocurile (Multithreading)
            // L-am setat la 1 meci pe 4x4, pentru viteza și inocența de la început
            population.parallelStream().forEach(agent -> {
                agent.evaluateFitness(1, 4);
            });

            // 2. Sortăm descrescător după scor
            population.sort((a, b) -> Double.compare(b.fitness, a.fitness));

            SnakeAgent bestAgent = population.get(0);
            int mereMancate = bestAgent.engine.getSnake().getBody().size() - 1;

            boolean isNewRecord = bestAgent.fitness > globalBestFitness;

            // 3. LOGICA PENTRU ȘOC GENETIC (Evaluată la fiecare generație)
            if (isNewRecord) {
                globalBestFitness = bestAgent.fitness;
                globalBestApples = mereMancate;
                generationsWithoutRecord = 0;       // Resetăm contorul
                currentMutationRate = 0.05;         // Revenim la mutația standard

                // Salvăm la FIECARE record, ca să nu pierdem nimic
                try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(new java.io.FileOutputStream("campion_4x4.dat"))) {
                    out.writeObject(bestAgent.brain);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                generationsWithoutRecord++;
            }

            if (generationsWithoutRecord == 1000) {
                currentMutationRate = 0.15; // Triplăm mutația!
                System.out.println("⚡ [ȘOC GENETIC] Populația stagnează. Mutația a crescut la 15%!");
            }

            if (generationsWithoutRecord == 3000) {
                currentMutationRate = 0.30;
                System.out.println("☢️ [ȘOC NUCLEAR] Restart genetic parțial!");
            }

            // 4. AFIȘARE PE CONSOLĂ (Fără spam)
            if (isNewRecord || generation % 1000 == 0) {
                String recordTag = isNewRecord ? " 🌟 [NOU RECORD!]" : "";
                System.out.printf("Gen: %d | Fitness: %.0f | Mere: %d | Pași: %d%s\n",
                        generation, bestAgent.fitness, mereMancate, bestAgent.stepsSurvived, recordTag);
            }

            // 5. CONDIȚIA DE VICTORIE (Înapoi la inocență: Oprim totul!)
            if (bestAgent.engine.isLevelWon()) {
                System.out.println("🏆 VICTORIE ABSOLUTĂ în generația " + generation + "!");

                // Salvăm creierul final pe hard disk
                try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(new java.io.FileOutputStream("campion_4x4.dat"))) {
                    out.writeObject(bestAgent.brain);
                    System.out.println("[*] Creierul a fost salvat cu succes în 'campion_4x4.dat'!");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break; // Oprim evoluția definitiv
            }

            // 6. CREĂM NOUA GENERAȚIE
            population = createNextGeneration(population, currentMutationRate);
            generation++;
        }
    }

    private List<SnakeAgent> createNextGeneration(List<SnakeAgent> oldPopulation, double mutationRate) {
        List<SnakeAgent> newPopulation = new ArrayList<>();

        int eliteCount = POPULATION_SIZE / 10;     // Păstrăm top 10% (100 agenți)
        int immigrantCount = POPULATION_SIZE / 20; // Introducem 5% sânge proaspăt (50 agenți)

        // 1. ELITISM
        for (int i = 0; i < eliteCount; i++) {
            NeuralNetwork eliteBrain = oldPopulation.get(i).brain.cloneAndMutate(0.0);
            newPopulation.add(new SnakeAgent(eliteBrain));
        }

        // 2. IMIGRANȚII
        for (int i = 0; i < immigrantCount; i++) {
            newPopulation.add(new SnakeAgent());
        }

        // 3. REPRODUCERE
        for (int i = (eliteCount + immigrantCount); i < POPULATION_SIZE; i++) {
            double randomBias = Math.pow(Math.random(), 2);
            int parentIndex = (int) (randomBias * eliteCount);

            NeuralNetwork parentBrain = oldPopulation.get(parentIndex).brain;
            NeuralNetwork childBrain = parentBrain.cloneAndMutate(mutationRate);
            newPopulation.add(new SnakeAgent(childBrain));
        }

        return newPopulation;
    }
}