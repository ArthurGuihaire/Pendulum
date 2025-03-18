package nnet;

public class Main{
    public static void main(String[] args) {
        int[] shape = {2,4,6,1};
        Nnet nnet = new Nnet(shape);
        double learning_rate = 2;
        double[][] inputs = {{0,0},{1,1},{1,0},{0,1}};
        double[][] expected_outputs = {{0.1},{0.1},{0.9},{0.9}};
        int value;
        for(int i = 0; i<10000; i++){
            value = (int) (Math.random() * inputs.length);
            nnet.train(inputs[value], expected_outputs[value], learning_rate);
        }
        learning_rate = 0.1;
        for(int i = 0; i<90000; i++){
            value = (int) (Math.random() * inputs.length);
            nnet.train(inputs[value], expected_outputs[value], learning_rate);
        }
        double[] output;
        for(int i = 0; i<inputs.length; i++){
            output = nnet.compute_output_values(inputs[i]);
            System.out.print("Input: ");
            printarray(inputs[i]);
            System.out.print("Output: ");
            printarray(output);
        }
        nnet.write_to_file("wob.txt");
        nnet = Nnet.create_from_file("wob.txt");
        for(int i = 0; i<inputs.length; i++){
            output = nnet.compute_output_values(inputs[i]);
            System.out.print("Input: ");
            printarray(inputs[i]);
            System.out.print("Output: ");
            printarray(output);
        }
    }
    public static void printarray(double[] array){
        for(int i=0; i<array.length; i++){
            System.out.print((array[i]) + ", ");
        }
        System.out.println();
    }
}