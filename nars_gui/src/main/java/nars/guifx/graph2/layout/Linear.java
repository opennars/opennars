package nars.guifx.graph2.layout;

import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.source.SpaceGrapher;

/**
 * for layouts which process items according to their
 * linear sequence position
 */
public abstract class Linear<V extends TermNode> implements IterativeLayout<V> {



    /** sets the position of a vertex at specified position order */
    public abstract void setPosition(TermNode vertex, int sequence, int max);

    @Override
    public void run(SpaceGrapher graph, int iterations) {
        TermNode[] verts = graph.displayed;

        int i = 0;


        for (TermNode v : verts) {
            if (v == null)  continue; //break?

            setPosition(v, i++, verts.length);
        }
    }


}
