import javax.swing.*;
import java.awt.*;
public class Pendulum extends JPanel{
    public static void main(String[] args){
        simulate_UI();
    }

    public static void simulate_UI(){
        JFrame frame = new JFrame("Inverted Pendulum");
        frame.setSize(2400,800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BasicGraphics bg = new BasicGraphics();
        frame.add(bg);
        frame.setVisible(true);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.repaint();
    }
}