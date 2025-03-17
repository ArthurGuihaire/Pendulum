import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BasicGraphics extends JPanel implements MouseMotionListener {
    private int x = 1200;
    private int x_old = 1200;
    private int y = 400;
    private double angular_velocity = 0.0;
    private double angle = Math.PI/4;
    public BasicGraphics(){
        setBackground(Color.BLACK);
        
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawLine(this.x, this.y, (int) (this.x+Pendulum.pendulum_length * Math.cos(angle)), (int) (this.y + Pendulum.pendulum_length * Math.sin(angle)));
        g.fillOval(this.x2-25, this.y2-25, 50, 50);
    }
    @Override
    public void mouseMoved(MouseEvent e){
        this.x_old = this.x;
        this.x = e.getX();
    }
    @Override
    public void mouseDragged(MouseEvent e){
        this.x_old = this.x;
        this.x = e.getX();
    }
    public int getMouseMovement(){
        return(this.x - this.x_old);
    }
    public int getMouseX(){
        return(this.x);
    }
    public void physics(){

    }
}