package nnet;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

public class Nnet {
    private double[][][] connection_weights;
    private double[][] biases;
    private int num_layers;
    private int[] num_nodes;
    public Nnet(int[] numnodes){
        this.num_layers = numnodes.length;
        this.connection_weights = new double[this.num_layers-1][][];
        this.biases = new double[num_layers-1][];

        for(int layer=0; layer<num_layers-1; layer++){
            this.biases[layer] = new double[numnodes[layer+1]];
            for(int i=0; i<numnodes[layer+1]; i++){
                this.biases[layer][i] = 0.01*Math.random();
            }
            this.connection_weights[layer] = new double[numnodes[layer]][numnodes[layer+1]];
            for(int i=0; i<numnodes[layer]; i++){
                for(int j=0; j<numnodes[layer+1]; j++){
                    this.connection_weights[layer][i][j] = Math.random()-0.5;
                }
            }
        }
        this.num_nodes = numnodes;
    }

    public Nnet copy(){
        double[][][] newWeights = new double[this.connection_weights.length][][];
        double[][] newBiases = new double[this.biases.length][];
        
        for (int layer = 0; layer < this.connection_weights.length; layer++) {
            newWeights[layer] = new double[this.connection_weights[layer].length][];
            for (int i = 0; i < this.connection_weights[layer].length; i++) {
                newWeights[layer][i] = Arrays.copyOf(this.connection_weights[layer][i], this.connection_weights[layer][i].length);
            }
            newBiases[layer] = Arrays.copyOf(this.biases[layer], this.biases[layer].length);
        }

        return new Nnet(newWeights, newBiases, Arrays.copyOf(this.num_nodes, this.num_nodes.length));
    }


    public Nnet(double[][][] weights, double[][] node_biases, int[] shape){
        this.connection_weights = weights;
        this.biases = node_biases;
        this.num_nodes = shape;
        this.num_layers = shape.length;
    }

    public double[] compute_output_values(double[] input){
        double[][] values = new double[this.num_layers][];
        for(int i=0; i<this.num_layers; i++){
            values[i] = new double[this.num_nodes[i]];
        }
        values[0] = input;
        double sum;
        for(int layer=1; layer<this.num_layers; layer++){
            for(int i=0; i<this.num_nodes[layer]; i++){
                sum = 0;
                for(int j=0; j<this.num_nodes[layer-1]; j++){
                    sum += values[layer-1][j] * this.connection_weights[layer-1][j][i];
                }
                if(layer == num_layers-1){
                    //clamp -128 to 128 values[layer][i] = Math.min(128,Math.max(-128,100*(sum+biases[layer-1][i])));
                    values[layer][i] = sum+biases[layer-1][i];
                }
                else{
                    values[layer][i] = sigmoid(sum+biases[layer-1][i]);
                }
            }
        }
        return values[num_layers-1];
    }

    public double[][] compute_all_values(double[] input){
        double[][] computed_values = new double[num_layers][];
        computed_values[0] = input.clone();
        double sum;
        for(int layer=1; layer<this.num_layers; layer++){
            computed_values[layer] = new double[num_nodes[layer]];
            for(int i=0; i<this.num_nodes[layer]; i++){
                sum = 0;
                for(int j=0; j<this.num_nodes[layer-1]; j++){
                    sum += computed_values[layer-1][j] * this.connection_weights[layer-1][j][i];
                }
                if(layer == num_layers-1){
                    computed_values[layer][i] = sum+biases[layer-1][i];
                }
                else{
                    computed_values[layer][i] = sigmoid(sum+biases[layer-1][i]);
                }
            }
        }
        return computed_values;
    }

    public void train(double[] input, double[] target_output, double learning_rate){
        double[][] computed_values = compute_all_values(input);

        // layer_errors[][] is dError/dRaw
        double[][] layer_errors = new double[this.num_layers][];
        layer_errors[this.num_layers-1] = new double[this.num_nodes[this.num_layers-1]];
        for(int i = 0; i<this.num_nodes[this.num_layers-1]; i++){
            //layer_errors[this.num_layers-1][i] = (computed_values[this.num_layers-1][i] - target_output[i]) * sigmoid_derivative(computed_values[this.num_layers-1][i]);
            layer_errors[this.num_layers-1][i] = (computed_values[this.num_layers-1][i] - target_output[i]);
        }
        //Backpropogation
        double error; // dError/dRaw
        for(int layer = this.num_layers-2; layer > 0; layer--){
            layer_errors[layer] = new double[this.num_nodes[layer]];
            //Loop through all nodes in layer
            for(int i = 0; i<this.num_nodes[layer]; i++){
                error = 0.0;
                //Loop through all nodes in layer+1
                for(int j = 0; j<this.num_nodes[layer+1]; j++){
                    error += layer_errors[layer+1][j] * connection_weights[layer][i][j];
                }
                layer_errors[layer][i] = error * sigmoid_derivative(computed_values[layer][i]);
            }
        }
        //Update weights and biases
        //Note that "error" = dError/dRaw
        for(int layer = this.num_layers-2; layer >= 0; layer--){
            for(int j = 0; j<this.num_nodes[layer+1]; j++){
                for(int i = 0; i<this.num_nodes[layer]; i++){
                    connection_weights[layer][i][j] -= layer_errors[layer+1][j] * computed_values[layer][i] * learning_rate;
                }
                biases[layer][j] -= layer_errors[layer+1][j] * learning_rate;
            }
        }
    }

    public void write_to_file(String filename){
        try{
            FileWriter w = new FileWriter(filename);
            for(int shape_value:this.num_nodes){
                w.write(shape_value + " ");
            }
            w.write("\n");
            for(int layer=0; layer<connection_weights.length; layer++){
                for(int i=0; i<connection_weights[layer].length; i++){
                    for(int j=0; j<connection_weights[layer][i].length; j++){
                        w.write(String.valueOf(connection_weights[layer][i][j]) + " ");
                    }
                    w.write(",");
                }
                w.write("\n");
            }
            w.write("biases\n");
            for(int layer=0; layer<biases.length; layer++){
                for(int i=0; i<biases[layer].length; i++){
                    w.write(String.valueOf(biases[layer][i]) + " ");
                }
                w.write("\n");
            }
            w.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static Nnet create_from_file(String filename) {
        try{
            BufferedReader w = new BufferedReader(new FileReader(filename));
            String[] shape_string = w.readLine().split(" ");
            int[] shape = new int[shape_string.length];
            for(int i = 0; i<shape_string.length; i++){
                shape[i] = Integer.valueOf(shape_string[i]);
            }
            String line;
            int num_lines1 = 0;
            while(!((line = w.readLine()).equals("biases"))){
                num_lines1++;
            }
            int num_lines2 = 0;
            while((line = w.readLine())!=null){
                num_lines2++;
            }
            w.close();
            BufferedReader b = new BufferedReader(new FileReader(filename));
            b.readLine(); // Skip the shape line
            double[][][] weights = new double[num_lines1][][];
            String[] array;
            String[] newarray;
            for(int line_number = 0; line_number<num_lines1; line_number++){
                line = b.readLine();
                array = line.split(" ,");
                weights[line_number] = new double[array.length][];
                for(int i = 0; i < array.length; i++){
                    newarray = array[i].split(" ");
                    weights[line_number][i] = new double[newarray.length];
                    for(int j = 0; j < newarray.length; j++){
                        weights[line_number][i][j] = Double.valueOf(newarray[j]);
                    }
                }
            }
            b.readLine();
            double[][] node_biases = new double[num_lines2][];
            for(int line_number = 0; line_number < num_lines2; line_number++){
                line = b.readLine();
                array = line.split(" ");
                node_biases[line_number] = new double[array.length];
                for(int i = 0; i < array.length; i++){
                    node_biases[line_number][i] = Double.valueOf(array[i]);
                }
            }
            b.close();
            return new Nnet(weights, node_biases, shape);

        } catch(IOException e){
            e.printStackTrace();
            return new Nnet(new double[0][0][0], new double[0][0], new int[0]);
        }
    }

    public void modify_randomly(double learning_rate){
        for(int layer = 0; layer<this.num_layers-1; layer++){
            for(int j = 0; j<this.num_nodes[layer+1]; j++){
                this.biases[layer][j] += (2*Math.random()-1) * learning_rate;
                for(int i = 0; i<this.num_nodes[layer]; i++){
                    this.connection_weights[layer][i][j] += (2*Math.random()-1) * learning_rate;
                }
            }
        }
    }

    private static double sigmoid(double x){
        return 1/(1+Math.exp(-x));
    }

    // Input is a pre-sigmoid-activated value
    private static double sigmoid_derivative(double sigmoid_value){
        return sigmoid_value * (1-sigmoid_value);
    }

    public static double[] generate_random_array(int arr_length){
        double[] output = new double[arr_length];
        for(int i=0; i<arr_length; i++){
            output[i] = Math.random();
        }
        return output;
    }
}
