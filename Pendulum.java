import javax.swing.*;
import java.awt.*;
public class Pendulum extends JPanel{
    public static void main(String[] args){
        if(args[0].contains("g")){
            simulate_UI();
        }
        else{
            //simulate_nnet();
        }
    }

    public static void simulate_UI(){
        JFrame frame = new JFrame("Inverted Pendulum");
        frame.setSize(2400,800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new BasicGraphics());
        frame.setVisible(true);
        
    }

    /*public static void simulate_nnet(){
        int*/
}