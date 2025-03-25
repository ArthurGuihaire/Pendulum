package nnet;

public class Main {
    public static void main(String[] args) {
        // Define the XOR dataset: inputs and expected output.
        double[][] inputs = {
            {0, 0},
            {0, 1},
            {1, 0},
            {1, 1}
        };
        double[] expectedOutputs = {0, 1, 1, 0};

        // Create a NEAT network with 2 inputs and 1 output.
        double learningRate = 0.1;
        Neat neat = new Neat(2, 1, learningRate);

        // Define mutation probabilities.
        // Order: [add neuron, remove neuron, add connection, remove connection, change weights]
        double[] mutationChances = {0.1, 0.1, 0.4, 0.1, 0.3};

        int maxIterations = 10000;
        double errorThreshold = 0.01;
        int iteration = 0;

        // Training loop: measure error on the XOR dataset and apply mutations.
        while (iteration < maxIterations) {
            double totalError = 0.0;
            for (int i = 0; i < inputs.length; i++) {
                double[] output = neat.forwardPropogation(inputs[i]);
                double error = expectedOutputs[i] - output[0];
                totalError += error * error;
            }
            totalError /= inputs.length;

            // Print progress every 1000 iterations.
            if (iteration % 1000 == 0) {
                System.out.println("Iteration: " + iteration + " Error: " + totalError);
            }

            // If error is below our threshold, finish training.
            if (totalError < errorThreshold) {
                break;
            }

            // Apply a random mutation.
            neat.randomMutation(mutationChances);
            iteration++;
        }

        System.out.println("Training completed in " + iteration + " iterations.");

        // Test the network on the XOR dataset.
        for (int i = 0; i < inputs.length; i++) {
            double[] output = neat.forwardPropogation(inputs[i]);
            System.out.println("Input: (" + inputs[i][0] + ", " + inputs[i][1] + ") " +
                               "Expected: " + expectedOutputs[i] +
                               " Got: " + output[0]);
        }
    }
}
