import javax.swing.*;
import java.awt.*;

public class BasicGraphics extends JPanel {
    private int x1 = 1200;
    private int x2 = 1200;
    private int y1 = 400;
    private int y2 = 700;
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawLine(this.x1, this.y1, this.x2, this.y2);
        g.fillOval(this.x2, this.y2, 50, 50);
    }
    protected void update(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
}
