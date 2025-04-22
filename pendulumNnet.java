import java.util.Arrays;
import java.util.stream.IntStream;

import java_nnet.Nnet;

public class pendulumNnet {
    public static void main(String[] args){
        for(String arg : args){
            if(arg.equals("gen")){
                geneticNnet();
            }
        }
    }
    private static void geneticNnet() {
        double learning_rate = 0.001;
        int startingPopulation = 200;
        int keepHighScores = 15;
        int copiesEach = 10;
        int[] shape = {4, 16, 1};
        Nnet[] population = new Nnet[startingPopulation];
        Nnet[] bestNnets = new Nnet[keepHighScores];
        int[] scores = new int[startingPopulation];
        for (int i = 0; i < startingPopulation; i++) {
            population[i] = new Nnet(shape);
        }
        for(int generation = 0; generation<16; generation++){
            for(int round = 0; round < 64; round++){
                for(int i = 0; i < startingPopulation; i++){
                    scores[i] = Pendulum.scoreNnet(population[i], Math.PI/2);
                    scores[i] += Pendulum.scoreNnet(population[i], Math.PI*3/2);
                }
                Integer[] indices = IntStream.range(0, startingPopulation).boxed().toArray(Integer[]::new);
                Arrays.sort(indices, (a, b) -> Integer.compare(scores[b], scores[a]));
                System.out.print("High score for current population: "+scores[indices[0]]);
                for (int i = 0; i < keepHighScores; i++){
                    bestNnets[i] = population[indices[i]];
                }
                for (int i = 0; i < keepHighScores; i++){
                    for (int j = 0; j < copiesEach-1; j++){
                        population[copiesEach*i+j] = bestNnets[i].copy();
                        population[copiesEach*i+j].modify_randomly(learning_rate);
                    }
                    population[copiesEach*i+copiesEach-1] = bestNnets[i].copy();
                }
                for (int i = copiesEach*keepHighScores; i<startingPopulation; i++){
                    population[i] = new Nnet(shape);
                }
                System.out.println();
            }

            learning_rate *= 0.5;
            Pendulum.movement_cost = Math.min(Pendulum.movement_cost+1, 5);
            bestNnets[0].write_to_file("training_examples/generation_"+generation+".txt");
            System.out.println("Saved to file: training_examples/generation_"+generation+".txt");
            System.out.print("Best of this gen finished with score ");
        }
    }
}