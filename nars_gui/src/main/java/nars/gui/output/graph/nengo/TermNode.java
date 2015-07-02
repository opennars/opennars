package nars.gui.output.graph.nengo;

import automenta.vivisect.swing.ColorArray;
import ca.nengo.model.SimulationException;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.model.icon.ModelIcon;
import ca.nengo.ui.model.icon.NodeIcon;
import nars.concept.Concept;
import nars.util.data.id.Named;
import nars.task.Task;
import nars.term.Term;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

/** a node which can represent a pre-Term, a Term, or a Concept */
public class TermNode extends UIVertex {

    private final TermGraphNode graphnode;
    String text;

    final Term term; //required

    Concept concept = null; //if non-null, this represents Concept nature
    Set<Task> task = new HashSet(); //if non-empty, this represents Task nature

    private NodeIcon icon;
    private float priority = 0;
    private float lastUIUpdate;
    private float minUpdateTime = 0.1f;
    protected float time;
    private double currentScale = -1;
    private double radius = 0;

    @Override
    public double getRadius() {
        return radius;
    }

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

    public TermNode(TermGraphNode graphnode, Concept c) {
        this(graphnode, c.getTerm());
        setConcept(c);
    }

    public TermNode(TermGraphNode graphnode, Task t) {
        this(graphnode, t.getTerm());
        addTask(t);
    }

    public TermNode(TermGraphNode graphnode, Term term) {
        super(term);
        this.term = term;
        this.graphnode = graphnode;
        ui.setChildrenPickable(true);
    }

    public boolean addTask(Task t) {
        if (this.task!=t) {
            if (this.task.add(t)) {
                updateUI();
            }
            return true;
        }
        return false;
    }
    public boolean removeTask(Task t) {
        if (this.task!=t) {
            if (this.task.remove(t)) {
                updateUI();
            }
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
        float r = 0.20f;
        float g = 0.50f;
        float b = 0.50f;

        if (concept != null) {
            priority += concept.getPriority();
            g += concept.getPriority() / 2f;
            scale += 0.5f;
        }
        if (!task.isEmpty()) {
            float p = 0;
            int numTasks = task.size();

            for (Task t : task) {
                float tp = t.getPriority() / numTasks;
                b += tp * 0.5f;
                /*if (t.isInput()) {
                    scale += 0.25f;
                }*/
                p += tp;
            }
            angle = time * 0.1f * (p);
            priority += p * 0.5f;

        }

        if (priority==0) {
            priority = 0;
            scale = 0.5f;
            //r = g = b = 0.5f;
        }

        color = new Color(r, g, b, alpha);
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

        ui.dragTo(x, y, bounds.getWidth()*1 /* speed */, 0.01);
        //ui.animateToPositionScaleRotation(x, y, targetScale, 0, 0);

        ui.getIcon().getBody().setRotation(angle);

        //System.out.println(x + " " + y);
        layoutPeriod = -1;


        //radius = ui.getFullBoundsReference().getWidth()/2.0; //hegith?
        radius = ui.getWidth()*2;


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
            addTask((Task) v);
        else if (concept == null & v instanceof Concept)
            setConcept((Concept)v);
        return this;
    }

    @Override
    public UIVertex remove(Named v) {
        if (task != null && v instanceof Task)
            removeTask(null);
        if (concept != null & v instanceof Concept)
            setConcept(null);

        if ((this.task.isEmpty()) && (this.concept == null))
            return this; //just a term, remove by default

        return null; //keep alive
    }


}
