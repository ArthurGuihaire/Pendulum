import javax.swing.*;
import java.awt.*;
public class Pendulum extends JPanel{
    private static double angle;
    private static double angular_velocity;
    private static double g = 2;
    private static double friction_factor = 0.995;
    public static int pendulum_length = 300;
    public static void main(String[] args){
        angle = Math.PI/4;
        angular_velocity = 0.0;
        // Later change to if args.contains("gui"){}
        simulateUI();
    }

    public static void simulateUI(){
        JFrame frame = new JFrame("Inverted Pendulum");
        frame.setSize(2400,800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BasicGraphics bg = new BasicGraphics();
        frame.add(bg);
        frame.setVisible(true);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.repaint();

        double centerVel = bg.getMouseMovement();
        double centerVelOld = centerVel;
        // Loop
        for(int i = 0; i<1000; i++){
            centerVelOld = centerVel;
            centerVel = bg.getMouseMovement();
            physics(centerVel - centerVelOld);
        }
    }

    public static void physics(double horizontal_acceleration){
        angular_velocity += (g/pendulum_length) * Math.cos(angle);
        angular_velocity += (horizontal_acceleration/pendulum_length) * Math.sin(angle);
        angular_velocity *= friction_factor;
        angle += angular_velocity;
    }
}