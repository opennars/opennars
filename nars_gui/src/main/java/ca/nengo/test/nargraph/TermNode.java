package ca.nengo.test.nargraph;

import automenta.vivisect.swing.ColorArray;
import ca.nengo.model.SimulationException;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.model.icon.ModelIcon;
import ca.nengo.ui.model.icon.NodeIcon;
import nars.logic.Terms;
import nars.logic.entity.Concept;
import nars.logic.entity.Task;
import nars.logic.entity.Term;

import java.awt.*;

/** a node which can represent a pre-Term, a Term, or a Concept */
public class TermNode extends UIVertex {

    private final TestNARGraph.NARGraphNode graphnode;
    String text;
    Terms.Termable term = null;
    Concept concept = null;
    private Task task = null;

    private NodeIcon icon;
    private float priority = 0;
    private float lastUIUpdate;
    private float minUpdateTime = 0.1f;

    final Color inputTaskColor = new Color(0.4f, 0.3f, 0.85f, 1.0f);

//    public TermNode(TestNARGraph.NARGraphNode graphnode, String text) {
//        super(UUID.randomUUID().toString());
//        this.graphnode = graphnode;
//        setTerm(null);
//        this.text = text;
//    }

    @Override
    public String toString() {
        if (concept!=null) return "TermNode[Concept=" + concept +"]";
        if (task!=null) return "TermNode[Task=" + task +"]";
        if (term!=null) return "TermNode[Term=" + term +"]";
        return "TermNode[\""  + text + "\"]";
    }

    public TermNode(TestNARGraph.NARGraphNode graphnode, Concept c) {
        super(c.getTerm());
        this.graphnode = graphnode;
        setConcept(c);
    }

    public TermNode(TestNARGraph.NARGraphNode graphnode, Task t) {
        super(t.sentence);
        this.graphnode = graphnode;
        setTask(t);
    }

    public TermNode(TestNARGraph.NARGraphNode graphnode, Term term) {
        super(term);
        this.graphnode = graphnode;
        setTerm(term.getTerm());
    }

    public boolean setTask(Task t) {
        if (this.task == t) return false;
        this.priority = 0; //will be updated
        this.concept = null;
        this.task = t;
        if (t == null) {
            this.term = null;
        }
        else {
            this.term = t.getTerm();
        }
        updateUI();
        return true;
    }
    public boolean setTerm(Term t) {
        if (this.term == t) return false;
        this.priority = 0; //will be updated
        this.concept = null;
        this.task = null;
        this.term = t;
        updateUI();
        return true;
    }
    public boolean setConcept(Concept c) {
        if (this.concept == c) return false;
        this.priority = 0; //will be updated
        this.concept = c;
        this.task = null;
        if (c == null) {
            this.term = null;
        }
        else {
            this.term = c.getTerm();
        }
        updateUI();
        return true;
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
        if (concept != null) {
            priority = concept.getPriority();
            color = green.get(priority);
            alpha = 0.5f + (0.5f * color.getAlpha()) / 256f;
            scale = 1.5f;
        }
        else if (task != null) {
            priority = task.getPriority();

            alpha = 0.5f + (0.5f * color.getAlpha()) / 256f;

            if (task.isInput()) {
                color = inputTaskColor;
                scale = 1.25f;
            }
            else {
                color = purple.get(priority); //to match the blue of TaskLinks
                scale = 0.75f;
            }
        }
        else {
            priority = 0;
            scale = 0.5f;
        }
        icon.getBody().setPaint(color);
        icon.setTransparency(alpha);

        if (priority < 0) priority = 0;

        double[] d = getCoordinates().getDataRef();

        double x = d[0];
        double y = d[1];
        if (Double.isFinite(x) && Double.isFinite(y)) {
            long animTime = (long) (layoutPeriod * 1f);

            //TODO combine these into one Transform update
            ui.dragTo(x, y, 0.02);
            ui.scaleTo(scale * (0.75f + priority), 0.05);
        }
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
}
