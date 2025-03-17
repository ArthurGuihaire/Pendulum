import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class Pendulum{
    private static double angle;
    private static double angularVelocity;
    private static final double g = 1;
    private static final double friction_factor = 0.995;
    private static final int fps = 60;
    public static final int pendulumLength = 300;
    public static double xRel;
    public static double yRel;
    private static double centerVel;
    private static double centerVelOld;
    private static Timer timer;
    private static JFrame frame;
    private static BasicGraphics bg;
    public static void main(String[] args){
        angle = Math.PI/4;
        angularVelocity = 0.0;
        // Later change to if args.contains("gui"){}
        simulateUI();
    }

    public static void simulateUI(){
        frame = new JFrame("Inverted Pendulum");
        timer = new Timer(1000/fps, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                doStuff();
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

    public static void doStuff(){
        centerVelOld = centerVel;
        centerVel = bg.getMouseMovement();
        physics(centerVel - centerVelOld);
        bg.repaint();
    }

    public static void physics(double horizontal_acceleration){
        angularVelocity += (g/pendulumLength) * Math.cos(angle);
        angularVelocity += (horizontal_acceleration/pendulumLength) * Math.sin(angle);
        angularVelocity *= friction_factor;
        angle += angularVelocity;
        xRel = pendulumLength*Math.cos(angle);
        yRel = pendulumLength*Math.sin(angle);
    }
}