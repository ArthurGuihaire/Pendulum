package nnet;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        // Define the XOR dataset.
        double[][] inputs = {
            {0, 0},
            {0, 1},
            {1, 0},
            {1, 1}
        };
        double[] expectedOutputs = {0, 1, 1, 0};

        // Parameters.
        int popSize = 50;
        int maxGenerations = 10000;
        double errorThreshold = 0.01;
        double learningRate = 1;
        // Mutation chances: [add neuron, remove neuron, add connection, remove connection, change weights]
        double[] mutationChances = {0.05, 0.05, 0.2, 0.1, 0.6};

        // Create an initial population of NEAT networks.
        Neat[] population = new Neat[popSize];
        for (int i = 0; i < popSize; i++) {
            population[i] = new Neat(2, 1, learningRate);
        }
        
        int generation = 0;
        Neat bestNetwork = null;
        double bestError = Double.MAX_VALUE;
        Random rand = new Random();

        // Main evolutionary loop.
        while (generation < maxGenerations) {
            // Evaluate fitness (mean squared error) for each network.
            double[] errors = new double[popSize];
            for (int i = 0; i < popSize; i++) {
                double totalError = 0.0;
                for (int j = 0; j < inputs.length; j++) {
                    double[] output = population[i].forwardPropogation(inputs[j]);
                    double error = expectedOutputs[j] - output[0];
                    totalError += error * error;
                }
                totalError /= inputs.length;
                errors[i] = totalError;
                
                if (totalError < bestError) {
                    bestError = totalError;
                    bestNetwork = population[i];
                }
            }
            
            // Print progress every 100 generations.
            if (generation % 100 == 0) {
                System.out.println("Generation: " + generation + " Best Error: " + bestError);
                if (generation == 0){
                    for (int i = 0; i < inputs.length; i++) {
                        double[] output = bestNetwork.forwardPropogation(inputs[i]);
                        System.out.println("Input: (" + inputs[i][0] + ", " + inputs[i][1] + ") " +
                                           "Expected: " + expectedOutputs[i] +
                                           " Got: " + output[0]);
                    }
                }
            }
            
            // Stop if error threshold is reached.
            if (bestError < errorThreshold) {
                break;
            }
            
            // Create an index array to sort networks by error.
            int[] indices = new int[popSize];
            for (int i = 0; i < popSize; i++) {
                indices[i] = i;
            }
            // Simple bubble sort on indices based on errors.
            for (int i = 0; i < popSize - 1; i++) {
                for (int j = i + 1; j < popSize; j++) {
                    if (errors[indices[j]] < errors[indices[i]]) {
                        int temp = indices[i];
                        indices[i] = indices[j];
                        indices[j] = temp;
                    }
                }
            }
            
            // Elitism: retain the top 10% as elites.
            int numElites = Math.max(1, popSize / 10);
            Neat[] elites = new Neat[numElites];
            for (int i = 0; i < numElites; i++) {
                // Clone the elite networks.
                elites[i] = population[indices[i]].copy();
            }
            
            // Create new population.
            Neat[] newPopulation = new Neat[popSize];
            // Copy elites directly.
            for (int i = 0; i < numElites; i++) {
                newPopulation[i] = elites[i];
            }
            // Fill the rest of the population with mutated copies of randomly chosen elites.
            for (int i = numElites; i < popSize; i++) {
                int eliteIndex = rand.nextInt(numElites);
                newPopulation[i] = elites[eliteIndex].copy();
                newPopulation[i].randomMutation(mutationChances);
            }
            
            population = newPopulation;
            generation++;
        }
        
        System.out.println("Training completed in " + generation + " generations with best error: " + bestError);
        System.out.println("Testing best network on XOR:");
        for (int i = 0; i < inputs.length; i++) {
            double[] output = bestNetwork.forwardPropogation(inputs[i]);
            System.out.println("Input: (" + inputs[i][0] + ", " + inputs[i][1] + ") " +
                               "Expected: " + expectedOutputs[i] +
                               " Got: " + output[0]);
        }
    }
}
