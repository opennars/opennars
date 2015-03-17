package automenta.vivisect.dimensionalize;

import ca.nengo.model.SimulationException;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.util.ScriptGenException;
import javolution.util.FastSet;
import nars.logic.entity.Named;
import nars.util.graph.NARGraph;

import java.awt.*;
import java.util.HashMap;
import java.util.Set;

/**
* Created by me on 3/12/15.
*/
public class UIEdge<V extends Named> extends AbstractWidget {

    final V s, t;

    /** items contained in this edge */
    public final Set<Named> e = new FastSet<Named>().atomic();

    float termlinkPriority, tasklinkPriority, priority;


    public Shape shape;

    public UIEdge(V s, V t) {
        super(s.name().toString() + ':' + t.name());
        this.s = s;
        this.t = t;

    }

    public void update() {
        tasklinkPriority = termlinkPriority = priority = 0;
        int ntask = 0, nterm = 0, np = 0;
        for (Named n : e) {
            if (n.getClass() == NARGraph.TaskLinkEdge.class) {
                float bp = ((NARGraph.TaskLinkEdge) n).getBudget().getPriority();
                priority += bp;
                tasklinkPriority += bp;
                ntask++;
                np++;
            }
            else if (n.getClass() == NARGraph.TermLinkEdge.class) {
                float bp = ((NARGraph.TermLinkEdge) n).getBudget().getPriority();
                priority += bp;
                termlinkPriority += bp;
                nterm++;
                np++;
            }
            else {

            }
        }
        if (np > 0) priority /= np;
        if (ntask > 0) tasklinkPriority /= ntask;
        if (nterm > 0) termlinkPriority /= nterm;

    }


    public float getPriorityMean() {
        return priority;
    }

    public float getTermlinkPriority() {
        return termlinkPriority;
    }

    public float getTasklinkPriority() {
        return tasklinkPriority;
    }

    public UIEdge<V> add(Named item) {
        e.add(item);
        return this;
    }
    public UIEdge<V> remove(Named item) {
        e.remove(item);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return name().equals(((UIEdge)obj).name());
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public String toString() {
        return name();
    }

    public V getSource() {
        return s;
    }

    public V getTarget() {
        return t;
    }

    @Override
    protected void paint(PaintContext paintContext, double width, double height) {

    }

    @Override
    public void run(float startTime, float endTime) throws SimulationException {

    }

    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return null;
    }
}
