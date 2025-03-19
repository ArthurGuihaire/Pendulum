import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import nnet.Nnet;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.Scanner;
public class Pendulum{
    private static double angle;
    private static double angularVelocity;
    public static final int pendulumLength = 300;
    private static final double g = 1;
    private static final double friction_factor = 0.995;
    private static final int fps = 30;
    private static final int iterations = 1800;
    private static final int score_frequency = 1;
    public static double xRel;
    public static double yRel;
    private static double centerVel;
    private static double centerVelOld;
    private static Timer timer;
    private static JFrame frame;
    private static BasicUI ui;
    private static BasicGraphics bg;
    private static int userScore;
    private static double x;
    public static void main(String[] args){
        angle = Math.PI*3/2;
        angularVelocity = 0.0;
        boolean useCustomName = false;
        Scanner kb = new Scanner(System.in);
        for(String arg:args){
            if(arg.equals("gui")){
                simulateUI();
            }
            else if(arg.equals("custom")){
                useCustomName = true;
            }
            else if(arg.equals("visualize")){
                if(useCustomName){
                    System.out.print("Filename to test: ");
                    visualizeAI(Nnet.create_from_file(kb.nextLine()));
                }
                else{
                    System.out.print("Which generation: ");
                    visualizeAI(Nnet.create_from_file("training_examples/generation_"+kb.nextLine()+".txt"));
                }
            }
            else if(arg.equals("test")){
                if(useCustomName){
                    System.out.print("Filename to test: ");
                    testAI(Nnet.create_from_file(kb.nextLine()));
                }
                else{
                    System.out.print("Which generation: ");
                    testAI(Nnet.create_from_file("training_examples/generation_"+kb.nextLine()+".txt"));
                }
            }
            else if(arg.equals("train")){
                geneticAI();
            }
        }
        kb.close();
    }

    private static void geneticAI() {
        double learning_rate = 0.01;
        int startingPopulation = 200;
        int keepHighScores = 30;
        int copiesEach = 5;
        int highestScore = 0;
        int[] shape = {4, 16, 1};
        Nnet[] population = new Nnet[startingPopulation];
        Nnet[] bestNnets = new Nnet[keepHighScores];
        int[] scores = new int[startingPopulation];
        for (int i = 0; i < startingPopulation; i++) {
            population[i] = new Nnet(shape);
        }
        for(int generation = 0; generation<16; generation++){
            for(int round = 0; round < 32; round++){
                for(int i = 0; i < startingPopulation; i++){
                    scores[i] = scoreNnet(population[i]);
                    if(scores[i] > highestScore){
                        highestScore = scores[i];
                        population[i].write_to_file("bestPendulumNetwork.txt");
                    }
                }
                Integer[] indices = IntStream.range(0, startingPopulation).boxed().toArray(Integer[]::new);
                Arrays.sort(indices, (a, b) -> Integer.compare(scores[b], scores[a]));
                System.out.print("High scores for current population:");
                for (int i = 0; i < keepHighScores; i++) {
                    int idx = indices[i];
                    int bestScore = scores[idx];
                    System.out.print(bestScore + " ");
                }
                for (int i = 0; i < keepHighScores; i++){
                    bestNnets[i] = population[indices[i]];
                }
                for (int i = 0; i < keepHighScores; i++){
                    for (int j = 0; j < copiesEach; j++){
                        population[5*i+j] = bestNnets[i].copy();
                        population[5*i+j].modify_randomly(learning_rate);
                    }
                }
                for (int i = copiesEach*keepHighScores; i<startingPopulation; i++){
                    population[i] = new Nnet(shape);
                }
                System.out.println();
            }
            learning_rate *= 0.4;
            bestNnets[0].write_to_file("training_examples/generation_"+generation+".txt");
            System.out.println("Saved to file: training_examples/generation_"+generation+".txt");
            System.out.print("Best of this gen finished with score ");
            testAI(bestNnets[0]);
        }
    }

    private static void simulateUI(){
        userScore = 0;
        frame = new JFrame("Inverted Pendulum");
        timer = new Timer(1000/fps, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e){
                doStuffUI(count);
                ui.repaint();
                if(count == iterations){
                    timer.stop();
                    System.out.println("Your score: "+userScore+"\nFinal v: "+angularVelocity);
                }
                count++;
            }
        });
        frame.setSize(2400,800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui = new BasicUI();
        frame.add(ui);
        frame.setVisible(true);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.repaint();
        // Loop
        timer.start();
    }

    //Assumes an infinite map
    private static int scoreNnet(Nnet nnet){
        userScore = 0;
        x = 0;
        angle = Math.PI/4;
        angularVelocity = 0;
        double[] input = new double[4];
        centerVel = nnet.compute_output_values(input)[0];
        for(int i = 0; i<iterations; i++){
            centerVelOld = centerVel;
            updateInput(input);
            centerVel = 1000*nnet.compute_output_values(input)[0];
            x += centerVel;
            physics(centerVel - centerVelOld);
            if(i % score_frequency == 0){
                userScore += Math.max(0, -yRel);
                userScore -= 5000*Math.pow(angularVelocity, 2);
            }
        }
        return userScore;
    }

    private static void testAI(Nnet nnet){
        System.out.println(scoreNnet(nnet) + ", velocity " + angularVelocity + ", xpos "+x);
    }

    private static void visualizeAI(Nnet nnet){
        scoreNnet(nnet);
        double addGraphicVel = (-x)/iterations;
        angle = Math.PI/4;
        angularVelocity = 0;
        userScore = 0;
        double[] input = new double[4];
        updateInput(input);
        centerVel = nnet.compute_output_values(input)[0];
        frame = new JFrame("Inverted Pendulum");
        timer = new Timer(1000/fps, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e){
                centerVelOld = centerVel;
                updateInput(input);
                centerVel = 1000*nnet.compute_output_values(input)[0];
                System.out.println(centerVel);
                physics(centerVel-centerVelOld);
                bg.moveX((int)(centerVel+addGraphicVel));
                bg.repaint();
                if(count%score_frequency == 0){
                    userScore += Math.max(0, -yRel);
                    userScore -= 5*Math.pow(angularVelocity, 2);
                }
                if(count == iterations){
                    timer.stop();
                    System.out.println("Your score: "+userScore+"\nFinal v: "+angularVelocity);
                }
                count++;
            }
        });
        frame.setSize(2400,800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        bg = new BasicGraphics();
        frame.add(bg);
        frame.setVisible(true);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.repaint();
        // Loop
        timer.start();
    }

    private static void doStuffUI(int count){
        centerVelOld = centerVel;
        centerVel = ui.getMouseMovement();
        physics(centerVel - centerVelOld);
        if(count%score_frequency == 0){
            userScore += Math.max(0, -yRel);
            userScore -= 5*Math.pow(angularVelocity, 2);
        }
    }

    private static void physics(double horizontal_acceleration){
        angularVelocity += (g/pendulumLength) * Math.cos(angle);
        angularVelocity += (horizontal_acceleration/pendulumLength) * Math.sin(angle);
        angularVelocity *= friction_factor;
        angle += angularVelocity;
        xRel = pendulumLength*Math.cos(angle);
        yRel = pendulumLength*Math.sin(angle);
    }

    private static void updateInput(double[] input){
        input[0] = Math.cos(angle);
        input[1] = Math.sin(angle);
        input[2] = angularVelocity;
        input[3] = centerVelOld;
    }

    public static void printarray(double[] array){
        for(int i=0; i<array.length; i++){
            System.out.print((array[i]) + ", ");
        }
    }
}