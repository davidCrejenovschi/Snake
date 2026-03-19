package org.example.ai;

import java.io.Serializable;
import java.util.Random;

public class NeuralNetwork implements Serializable {
    private int inputNodes = 16;   // 4x4 grid
    private int hiddenNodes = 12;  // Un strat intermediar
    private int outputNodes = 4;   // Sus, Jos, Stânga, Dreapta

    private double[][] weightsIH; // Input to Hidden
    private double[][] weightsHO; // Hidden to Output
    private double[] biasH;
    private double[] biasO;

    public NeuralNetwork() {
        weightsIH = new double[hiddenNodes][inputNodes];
        weightsHO = new double[outputNodes][hiddenNodes];
        biasH = new double[hiddenNodes];
        biasO = new double[outputNodes];
        randomize();
    }


    public NeuralNetwork cloneAndMutate(double mutationRate) {
        NeuralNetwork child = new NeuralNetwork();
        java.util.Random r = new java.util.Random();

        for (int i = 0; i < hiddenNodes; i++) {
            for (int j = 0; j < inputNodes; j++) {
                child.weightsIH[i][j] = this.weightsIH[i][j];
                if (r.nextDouble() < mutationRate) child.weightsIH[i][j] += r.nextGaussian() * 0.1;
            }
            child.biasH[i] = this.biasH[i];
            if (r.nextDouble() < mutationRate) child.biasH[i] += r.nextGaussian() * 0.1;
        }

        for (int i = 0; i < outputNodes; i++) {
            for (int j = 0; j < hiddenNodes; j++) {
                child.weightsHO[i][j] = this.weightsHO[i][j];
                if (r.nextDouble() < mutationRate) child.weightsHO[i][j] += r.nextGaussian() * 0.1;
            }
            child.biasO[i] = this.biasO[i];
            if (r.nextDouble() < mutationRate) child.biasO[i] += r.nextGaussian() * 0.1;
        }
        return child;
    }

    private void randomize() {
        Random r = new Random();
        for (int i = 0; i < hiddenNodes; i++) {
            for (int j = 0; j < inputNodes; j++) weightsIH[i][j] = r.nextGaussian();
            biasH[i] = r.nextGaussian();
        }
        for (int i = 0; i < outputNodes; i++) {
            for (int j = 0; j < hiddenNodes; j++) weightsHO[i][j] = r.nextGaussian();
            biasO[i] = r.nextGaussian();
        }
    }

    public int predict(double[] inputs) {
        double[] hidden = activate(matrixMultiply(weightsIH, inputs, biasH));
        double[] output = activate(matrixMultiply(weightsHO, hidden, biasO));

        // Returnează indexul celei mai mari valori (Softmax simplificat)
        int maxIdx = 0;
        for (int i = 1; i < output.length; i++) {
            if (output[i] > output[maxIdx]) maxIdx = i;
        }
        return maxIdx;
    }

    private double[] activate(double[] x) {
        for (int i = 0; i < x.length; i++) x[i] = Math.tanh(x[i]); // Funcție de activare
        return x;
    }

    private double[] matrixMultiply(double[][] w, double[] inputs, double[] b) {
        double[] result = new double[w.length];
        for (int i = 0; i < w.length; i++) {
            for (int j = 0; j < inputs.length; j++) {
                result[i] += w[i][j] * inputs[j];
            }
            result[i] += b[i];
        }
        return result;
    }
}