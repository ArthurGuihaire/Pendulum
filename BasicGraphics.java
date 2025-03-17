import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BasicGraphics extends JPanel implements MouseMotionListener {
    private int x1 = 1200;
    private int x1_old = 1200;
    private int x2 = 1200;
    private int y1 = 400;
    private int y2 = 700;
    public BasicGraphics(){
        setBackground(Color.BLACK);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawLine(this.x1, this.y1, this.x2, this.y2);
        g.fillOval(this.x2-25, this.y2-25, 50, 50);
    }
    public void update(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    @Override
    public void mouseMoved(MouseEvent e){
        this.x1_old = this.x1;
        this.x1 = e.getX();
    }
    @Override
    public void mouseDragged(MouseEvent e){
        this.x1_old = this.x1;
        this.x1 = e.getX();
    }
    public int getMouseMovement(){
        return(this.x1 - this.x1_old);
    }
    public int getMouseX(){
        return(this.x1);
    }
}