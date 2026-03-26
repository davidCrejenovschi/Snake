package org.example.ai;
import java.util.Random;

public class NeuralNetwork {

    private final double[][] weightsInputHidden;
    private final double[][] weightsHiddenOutput;
    private final double[] biasHidden;
    private final double[] biasOutput;

    private final int inputSize;
    private final int hiddenSize;
    private final int outputSize;

    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;

        weightsInputHidden = new double[inputSize][hiddenSize];
        weightsHiddenOutput = new double[hiddenSize][outputSize];
        biasHidden = new double[hiddenSize];
        biasOutput = new double[outputSize];

        initializeRandomWeights();
    }

    private void initializeRandomWeights() {
        Random rand = new Random();

        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weightsInputHidden[i][j] = rand.nextDouble() * 2 - 1;
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            biasHidden[i] = rand.nextDouble() * 2 - 1;
            for (int j = 0; j < outputSize; j++) {
                weightsHiddenOutput[i][j] = rand.nextDouble() * 2 - 1;
            }
        }
        for (int i = 0; i < outputSize; i++) {
            biasOutput[i] = rand.nextDouble() * 2 - 1;
        }
    }

    private double relu(double x) {
        return Math.max(0, x);
    }

    public int predict(double[] inputs) {
        double[] hidden = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = biasHidden[j];
            for (int i = 0; i < inputSize; i++) {
                sum += inputs[i] * weightsInputHidden[i][j];
            }
            hidden[j] = relu(sum);
        }

        double[] output = new double[outputSize];
        for (int j = 0; j < outputSize; j++) {
            double sum = biasOutput[j];
            for (int i = 0; i < hiddenSize; i++) {
                sum += hidden[i] * weightsHiddenOutput[i][j];
            }
            output[j] = sum;
        }

        int bestAction = 0;
        double maxScore = output[0];
        for (int i = 1; i < outputSize; i++) {
            if (output[i] > maxScore) {
                maxScore = output[i];
                bestAction = i;
            }
        }

        return bestAction;
    }

    public NeuralNetwork crossover(NeuralNetwork partner) {
        NeuralNetwork child = new NeuralNetwork(inputSize, hiddenSize, outputSize);
        Random r = new Random();

        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                child.weightsInputHidden[i][j] = r.nextBoolean() ? this.weightsInputHidden[i][j] : partner.weightsInputHidden[i][j];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            child.biasHidden[i] = r.nextBoolean() ? this.biasHidden[i] : partner.biasHidden[i];
            for (int j = 0; j < outputSize; j++) {
                child.weightsHiddenOutput[i][j] = r.nextBoolean() ? this.weightsHiddenOutput[i][j] : partner.weightsHiddenOutput[i][j];
            }
        }
        for (int i = 0; i < outputSize; i++) {
            child.biasOutput[i] = r.nextBoolean() ? this.biasOutput[i] : partner.biasOutput[i];
        }
        return child;
    }

    public void mutate(double mutationRate) {
        Random r = new Random();
        mutateMatrix(weightsInputHidden, mutationRate, r);
        mutateMatrix(weightsHiddenOutput, mutationRate, r);
        mutateArray(biasHidden, mutationRate, r);
        mutateArray(biasOutput, mutationRate, r);
    }

    private void mutateMatrix(double[][] matrix, double rate, Random r) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (r.nextDouble() < rate) {
                    matrix[i][j] += r.nextGaussian() * 0.5;
                }
            }
        }
    }

    private void mutateArray(double[] array, double rate, Random r) {
        for (int i = 0; i < array.length; i++) {
            if (r.nextDouble() < rate) {
                array[i] += r.nextGaussian() * 0.5;
            }
        }
    }

    public void saveToFile(String filepath) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filepath)) {
            writer.println(inputSize + "," + hiddenSize + "," + outputSize);

            for (double[] row : weightsInputHidden) {
                for (double val : row) writer.println(val);
            }
            for (double[] row : weightsHiddenOutput) {
                for (double val : row) writer.println(val);
            }
            for (double val : biasHidden) writer.println(val);
            for (double val : biasOutput) writer.println(val);

        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static NeuralNetwork loadFromStream(java.io.InputStream is) {
        if (is == null) return null;

        try (java.util.Scanner sc = new java.util.Scanner(is).useLocale(java.util.Locale.US)) {
            sc.useDelimiter("[,\\s\\r\\n]+");

            if (!sc.hasNextInt()) return null;

            int in = sc.nextInt();
            int hid = sc.nextInt();
            int out = sc.nextInt();

            NeuralNetwork nn = new NeuralNetwork(in, hid, out);

            for (int i = 0; i < in; i++) {
                for (int j = 0; j < hid; j++) {
                    if (sc.hasNextDouble()) {
                        nn.weightsInputHidden[i][j] = sc.nextDouble();
                    }
                }
            }

            for (int i = 0; i < hid; i++) {
                for (int j = 0; j < out; j++) {
                    if (sc.hasNextDouble()) {
                        nn.weightsHiddenOutput[i][j] = sc.nextDouble();
                    }
                }
            }

            for (int i = 0; i < hid; i++) {
                if (sc.hasNextDouble()) {
                    nn.biasHidden[i] = sc.nextDouble();
                }
            }

            for (int i = 0; i < out; i++) {
                if (sc.hasNextDouble()) {
                    nn.biasOutput[i] = sc.nextDouble();
                }
            }

            return nn;
        } catch (Exception e) {
            System.err.println("AI Brain Load Error: " + e.getMessage());
            return null;
        }
    }
}
