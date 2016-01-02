package automenta.vivisect.face;

import java.awt.*;


public class DrawGraphAppPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private final GraphApp outer;

    public DrawGraphAppPanel(GraphApp graphanim, int i, GraphApp outer) {
        this.outer = outer;
        rent = graphanim;
        type = i;
    }

    @Override
    public void paint(Graphics g) {
        outer.draw(type);
    }
    private final int type;
    private final GraphApp rent;
}
