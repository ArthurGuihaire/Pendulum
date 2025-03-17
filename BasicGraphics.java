import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BasicGraphics extends JPanel implements MouseMotionListener {
    private int x = 1200;
    private int x_old = 1200;
    private int y = 400;
    public BasicGraphics(){
        setBackground(Color.BLACK);
        addMouseMotionListener(this);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawLine(this.x, this.y, (int) (this.x+Pendulum.xRel), (int) (this.y + Pendulum.yRel));
        g.fillOval(this.x+(int)Pendulum.xRel-25, this.y+(int)Pendulum.yRel-25, 50, 50);
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
}