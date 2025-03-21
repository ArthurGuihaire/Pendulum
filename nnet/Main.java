package nnet;

public class Main{
    public static void main(String[] args) {
        int[] shape = {2, 1};
        double[][] inputs = {{0,0}, {0,1}, {1,0}, {1,1}};
        double[][] outputs = {{0},{2},{1},{4}};
        Nnet nnet = new Nnet(shape);
        for(int i = 0; i<100000; i++){
            int value = (int) (Math.random()*4);
            nnet.train(inputs[value], outputs[value], 0.1);
        }
        for(int i = 0; i<4; i++){
            System.out.println(nnet.compute_output_values(inputs[i])[0]);
        }
    }
}