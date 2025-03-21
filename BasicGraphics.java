import javax.swing.*;
import java.awt.*;

public class BasicGraphics extends JPanel {
    protected int x;
    protected int y;
    public BasicGraphics(){
        setBackground(Color.BLACK);
        this.x = 1200;
        this.y = 400;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawLine(this.x, this.y, (int) (this.x+Pendulum.xRel), (int) (this.y + Pendulum.yRel));
        g.fillOval(this.x+(int)Pendulum.xRel-25, this.y+(int)Pendulum.yRel-25, 50, 50);
    
        Toolkit.getDefaultToolkit().sync();
    }

    public void moveX(int x) {
        this.x += x;
    }
}