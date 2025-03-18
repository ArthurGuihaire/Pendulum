import java.awt.event.*;

public class BasicUI extends BasicGraphics implements MouseMotionListener {
    private int x_old;
    public BasicUI(){
        super();
        addMouseMotionListener(this);
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