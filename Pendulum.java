import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import nnet.Nnet;
import nnet.Neat;
import java.util.Scanner;
public class Pendulum{
    // region Variables
    private static double angle;
    private static double angularVelocity;
    public static final int pendulumLength = 150;
    private static final double g = 0.25;
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
    protected static double movement_cost = 0;
    private static double speedMultiplier = 100;
    //endregion
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
        }
        kb.close();
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
                if(count==1){
                    angularVelocity = 0;
                    centerVel = 0;
                }
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

    public static int scoreNnet(Nnet nnet, double startAngle){
        userScore = 0;
        x = 0;
        angle = startAngle;
        angularVelocity = 0;
        double[] input = new double[4];
        centerVel = 0;
        for(int i = 0; i<iterations; i++){
            centerVelOld = centerVel;
            updateInput(input);
            centerVel = speedMultiplier*nnet.compute_output_values(input)[0];
            x += centerVel;
            if(x>1200){
                x = 1200;
                centerVel = 0;
                userScore -= 1000;
            }
            else if(x<-1200){
                x = -1200;
                centerVel = 0;
                userScore -= 1000;
            }
            physics(centerVel - centerVelOld);
            if(i % score_frequency == 0){
                userScore += Math.max(0, -yRel);
                userScore -= 5000*Math.pow(angularVelocity, 2);
                userScore -= movement_cost*Math.abs(centerVel);
            }
        }
        return userScore;
    }

    public static int scoreNeat(Neat nnet, double startAngle){
        userScore = 0;
        x = 0;
        angle = startAngle;
        angularVelocity = 0;
        double[] input = new double[4];
        centerVel = 0;
        for(int i = 0; i<iterations; i++){
            centerVelOld = centerVel;
            updateInput(input);
            centerVel = speedMultiplier*nnet.forwardPropogation(input)[0];
            x += centerVel;
            if(x>1200){
                x = 1200;
                centerVel = 0;
                userScore -= 1000;
            }
            else if(x<-1200){
                x = -1200;
                centerVel = 0;
                userScore -= 1000;
            }
            physics(centerVel - centerVelOld);
            if(i % score_frequency == 0){
                userScore += Math.max(0, -yRel);
                userScore -= 5000*Math.pow(angularVelocity, 2);
                userScore -= movement_cost*Math.abs(centerVel);
            }
        }
        return userScore;
    }

    private static void testAI(Nnet nnet){
        System.out.println(scoreNnet(nnet, Math.PI/2) + ", velocity " + angularVelocity + ", xpos "+x+", xvel " + centerVel);
    }

    private static void visualizeAI(Nnet nnet){
        angle = Math.PI*3 / 2;
        angularVelocity = 0;
        userScore = 0;
        x = 0;
        double[] input = new double[4];
        centerVel = 0;
        frame = new JFrame("Inverted Pendulum");
        timer = new Timer(1000/fps, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e){
                centerVelOld = centerVel;
                updateInput(input);
                centerVel = speedMultiplier*nnet.compute_output_values(input)[0];
                x += centerVel;
                if(x>1200){
                    x = 1200;
                    centerVel = 0;
                    userScore -= 1000;
                }
                else if(x<-1200){
                    x = -1200;
                    centerVel = 0;
                    userScore -= 1000;
                }
                physics(centerVel-centerVelOld);
                bg.moveX((int)(centerVel));
                bg.repaint();
                if(count % score_frequency == 0){
                    userScore += Math.max(0, -yRel);
                    userScore -= 2000*Math.pow(angularVelocity, 2);
                    userScore -= movement_cost*Math.abs(centerVel);
                }
                if(count == iterations){
                    timer.stop();
                    System.out.println("Your score: "+userScore+"\nFinal angular v: "+angularVelocity+"\nFinal v: "+centerVel   );
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
        input[3] = centerVelOld/speedMultiplier;
    }
}