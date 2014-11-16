package automenta.vivisect.face;

import java.awt.Graphics;
import java.awt.Panel;


public class DrawGraphAppPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private final GraphApp outer;

    public DrawGraphAppPanel(GraphApp graphanim, int i, final GraphApp outer) {
        this.outer = outer;
        rent = graphanim;
        type = i;
    }

    public void paint(Graphics g) {
        outer.draw(type);
    }
    private int type;
    private GraphApp rent;
}
