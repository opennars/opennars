package nars.gui.output.face;

import java.awt.Event;
import java.awt.Frame;
import java.awt.Insets;

public class GraphAnimFrame extends Frame {

    GraphAnim g;

    public GraphAnimFrame(String title, Face2bApplet fa, String cb, String su) {
	this(title, fa, cb, su, false);
    }

    
    public GraphAnimFrame(String title, Face2bApplet fa, String cb, String su, boolean sa) {
	super(title);
	g = new GraphAnim(fa, cb, su, sa);
	this.add("Center", g);

	g.init();
	show();
	g.start();

	Insets in = insets();
	this.resize(600 + in.left + in.right, 400 + in.top + in.bottom);
    }

    public GraphAnim getGraphanim() {
	return g;
    }

    public boolean handleEvent(Event e) {
	if(e.id == Event.WINDOW_DESTROY) {
	    dispose();
	    return true;
	}

	return super.handleEvent(e);
    }
}
