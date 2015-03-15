package ca.nengo.test.nargraph;

import automenta.vivisect.swing.ColorArray;
import ca.nengo.model.SimulationException;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.model.icon.ModelIcon;
import ca.nengo.ui.model.icon.NodeIcon;
import nars.logic.entity.Concept;
import nars.logic.entity.Named;
import nars.logic.entity.Task;
import nars.logic.entity.Term;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/** a node which can represent a pre-Term, a Term, or a Concept */
public class TermNode extends UIVertex {

    private final TestNARGraph.NARGraphNode graphnode;
    String text;

    final Term term; //required

    Concept concept = null; //if non-null, this represents Concept nature
    Task task = null; //if non-null, this represents Task nature

    private NodeIcon icon;
    private float priority = 0;
    private float lastUIUpdate;
    private float minUpdateTime = 0.1f;
    protected float time;


//    public TermNode(TestNARGraph.NARGraphNode graphnode, String text) {
//        super(UUID.randomUUID().toString());
//        this.graphnode = graphnode;
//        setTerm(null);
//        this.text = text;
//    }

    @Override
    public String toString() {
        return "TermNode[Term=" + name().toString() +",C=" + concept +", T=" + task + "]";
        //return "TermNode[\""  + text + "\"]";
    }

    public TermNode(TestNARGraph.NARGraphNode graphnode, Concept c) {
        this(graphnode, c.getTerm());
        setConcept(c);
    }

    public TermNode(TestNARGraph.NARGraphNode graphnode, Task t) {
        this(graphnode, t.getTerm());
        setTask(t);
    }

    public TermNode(TestNARGraph.NARGraphNode graphnode, Term term) {
        super(term);
        this.term = term;
        this.graphnode = graphnode;
    }

    public boolean setTask(Task t) {
        if (this.task!=t) {
            this.task = t;
            updateUI();
            return true;
        }
        return false;
    }

    public boolean setConcept(Concept c) {
        if (this.concept!=c) {
            this.concept = c;
            updateUI();
            return true;
        }
        return false;
    }

    @Override
    public boolean isDependent() {
        return concept==null; //concepts are allowed non-dependent
    }

    public float getPriority() {
        return priority;
    }

    @Override
    public ModelIcon newIcon(ModelObject UI) {
        return icon = new NodeIcon(UI);
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    final static ColorArray purple = new ColorArray(64, new Color(0.1f, 0f, 0.6f, 0.35f), new Color(0.2f, 0.0f, 0.87f, 0.93f));
    final static ColorArray green = new ColorArray(64, new Color(0.2f, 0.6f, 0.2f, 0.55f), new Color(0.25f, 0.8f, 0.05f, 0.9f));

    protected void updateUI() {
        float alpha = 0.75f;
        Color color = Color.DARK_GRAY;
        float scale = 1f;
        float angle = 0;

        priority = 0;
        float r = 0.5f;
        float g = 0;
        float b = 0.5f;

        if (concept != null) {
            priority += concept.getPriority();
            g += concept.getPriority() / 2f;
            scale += 0.5f;
        }
        if (task != null) {
            priority += task.getPriority();
            b += task.getPriority() / 2f;
            if (task.isInput()) {
                scale += 0.25f;
            }
            angle = time * 0.5f * (0.5f * task.getPriority());
        }

        if (priority==0) {
            priority = 0;
            scale = 0.5f;
            r = g = b = 0.5f;
        }

        color = new Color(r, 1-g, 1-b, alpha);
        alpha = 0.5f + (0.5f * color.getAlpha()) / 256f;

        icon.getBody().setPaint(color);
        icon.getBody().setTransparency(alpha);

        if (priority < 0) priority = 0;

        double[] d = getCoordinates().getDataRef();

        double x = d[0];
        double y = d[1];

        //bounds
        Rectangle2D bounds = graphnode.getLayoutBounds();
        if (x > bounds.getMaxX()) x= bounds.getMaxX();
        if (x < bounds.getMinX()) x = bounds.getMinX();
        if (y > bounds.getMaxY()) y= bounds.getMaxY();
        if (y < bounds.getMinY()) y = bounds.getMinY();

        //TODO combine these into one Transform update
        float targetScale = scale * (0.75f + priority);
        ui.scaleTo(targetScale, 0.05);
        ui.dragTo(x, y, bounds.getWidth() /* speed */, 0.005);
        //ui.animateToPositionScaleRotation(x, y, targetScale, 0, 0);

        ui.getIcon().getBody().setRotation(angle);

        //System.out.println(x + " " + y);
        layoutPeriod = -1;



    }


    @Override
    public void draggedTo(double x, double y) {
        //set input dragged mouse coordinates to override layout
        double[] d =getCoordinates().getDataRef();
        d[0] = x;
        d[1] = y;
    }

    @Override
    public void run(float startTime, float endTime) throws SimulationException {

        if (endTime - lastUIUpdate > minUpdateTime) {
            //updateUI();
            lastUIUpdate = endTime;
        }
        time = endTime;
    }


    @Override
    protected void paint(ca.nengo.ui.lib.world.PaintContext paintContext, double width, double height) {
        super.paint(paintContext, width, height);
        updateUI();
    }

    @Override
    public void reset(boolean randomize) {

        lastUIUpdate = -1;
    }

    @Override
    public UIVertex add(Named v) {
        if (task==null && v instanceof Task)
            setTask((Task)v);
        else if (concept == null & v instanceof Concept)
            setConcept((Concept)v);
        return this;
    }

    @Override
    public UIVertex remove(Named v) {
        if (task != null && v instanceof Task)
            setTask(null);
        if (concept != null & v instanceof Concept)
            setConcept(null);

        if ((this.task ==null) && (this.concept == null))
            return this; //just a term, remove by default

        return null; //keep alive
    }


}
