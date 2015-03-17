package nars.gui.output.graph.nengo;

import automenta.vivisect.dimensionalize.FastOrganicIterativeLayout;
import ca.nengo.model.StepListener;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.AbstractMapNetwork;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.plot.AbstractWidget;
import com.google.common.collect.Iterators;
import nars.core.NAR;
import nars.event.ConceptReaction;
import nars.logic.Terms;
import nars.logic.entity.Concept;
import nars.logic.entity.Named;
import nars.logic.entity.Task;
import nars.util.graph.DefaultGrapher;
import nars.util.graph.NARGraph;
import org.apache.commons.math3.linear.ArrayRealVector;

import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
* Created by me on 3/17/15.
*/
public class TermGraphNode extends AbstractMapNetwork<String, AbstractWidget> implements UIBuilder {

    private final NAR nar;
    private final ConceptReaction conceptReaction;
    float simTimePerCycle = 1f;
    float simTimePerLayout = 0.25f;
    long lasTLayout = System.currentTimeMillis();
    Rectangle2D layoutBounds = new Rectangle2D.Double(-500, -500, 1000, 1000);
    final FastOrganicIterativeLayout<UIVertex, UIEdge<UIVertex>> hmap =
            new FastOrganicIterativeLayout<UIVertex, UIEdge<UIVertex>>(layoutBounds) {
    /*final HyperassociativeMap<NARGraphVertex,UIEdge<NARGraphVertex>> hmap =
        new HyperassociativeMap<NARGraphVertex,UIEdge<NARGraphVertex>>(graph, 2) {*/


                @Override
                public ArrayRealVector getPosition(UIVertex node) {
                    return node.getCoordinates();
                }

                @Override
                public void pre(Collection<UIVertex> vertices) {
                    updateCoordinates(vertices);
                }


                @Override
                public double getEdgeWeight(UIEdge e) {
                    /*if (e.e instanceof TermLink) {
                        return 4.0;
                    }*/
                    return 1.0;
                }


                @Override
                public double getRadius(UIVertex narGraphVertex) {
                    double r = 1 + narGraphVertex.getRadius();
                    return r * 0.5f;
                }

                @Override
                protected Iterator<UIVertex> getVertices() {
                    return Iterators.filter(nodes().iterator(), UIVertex.class);
                }


            };
    SubCycle narCycle = new SubCycle() {
        @Override
        public double getTimePerCycle() {
            return simTimePerCycle;
        }

        @Override
        public void run(int numCycles, float endTime, long deltaMS) {

            if (!nar.isRunning())
                nar.step(numCycles);
        }

        @Override
        public String toString() {
            return "nar";
        }
    };
    SubCycle layoutCycle = new SubCycle() {
        @Override
        public double getTimePerCycle() {
            return simTimePerLayout;
        }

        @Override
        public void run(int numCycles, float endTime, long deltaMS) {

            /*
            hmap.setEquilibriumDistance(55);
            hmap.setMaxRepulsionDistance(5000);
            hmap.setAttractionStrength(16);
            hmap.setSpeedFactor(10);
            */


            double layoutRad = Math.sqrt( numVertices )* 500;
            layoutBounds.setRect(-layoutRad / 2, -layoutRad / 2, layoutRad, layoutRad);
            hmap.resetLearning();
            hmap.run(2);



        }

        @Override
        public String toString() {
            return "layout";
        }
    };
    //private UIEdge[] nextEdges;
    //private Map<String,UIEdge> edges = new FastMap<String,UIEdge>().atomic();
    private UINARGraph ui;
    private boolean edgesChanged = false;
    private int numVertices = 0;


    public TermGraphNode(NAR n) {
        super();

        this.nar = n;

        this.conceptReaction = new ConceptReaction(nar) {

            @Override
            public void onFiredConcept(Concept c) {

                refresh(c);
            }

            @Override
            public void onNewConcept(Concept c) {
                refresh(c);
                //add(c);
                //if (add(c) != null)
                //  refresh(c);
            }

            @Override
            public void onForgetConcept(Concept c) {

                remove(c);


                //int x = ui.getViewer().getGround().printTree(System.out);

                //int y = ui.getViewer().getGround().printTree(System.out);
//
//
//                    System.out.println(" forget: " +
//                        nar.memory.concepts.size() + " concepts, "
//                    + graph.vertexSet().size() + " vertices, " +
//                            synchronized(graph) {
//                    Iterators.size(Iterators.filter(graph.vertexSet().iterator(), new Predicate<UIVertex>() {
//
//                        @Override
//                        public boolean apply(UIVertex input) {
//                            if (input instanceof TermNode) {
//                                return ((TermNode) input).concept!=null;
//                            }
//                            return false;
//                        }
//                    })) + " concept ui nodes" );
//                            }

            }
        };

        addStepListener(layoutCycle);
        //addStepListener(narCycle);

    }


    void updateCoordinates(Collection<UIVertex> vertices) {
        long now = System.currentTimeMillis();
        long dt = (now - lasTLayout);
        if (dt > 0) {
            numVertices = vertices.size();
            for (UIVertex v : vertices) {
                v.getActualCoordinates(dt);
                if (v.isDependent() && v.degree()==0) {
                    //System.err.println("late removing " + v);
                    remove(v);
                }

                v.update();
            }
            lasTLayout = now;
        }
    }




    @Override
    public String name(AbstractWidget node) {
        return node.name().toString();
    }


    /**
     * should be called from within a synchronized(graph) { } block
     */
    public UIVertex newVertex(final Named x) {

        TermNode v = null;

        if (x instanceof Concept) {
            v = new TermNode(this, (Concept) x);
        } else if (x instanceof Task) {
            v = new TermNode(this, (Task) x);
        }

            /*
            else if (x instanceof Term) {
                v = new TermNode(this, (Term)x);
            }
            */


        return v;
    }


    public void refresh(Object x) {
        new MyGrapher().on(null, x, false).finish();
    }


    @Override
    public UINARGraph newUI(double width, double height) {
        return this.ui = new UINARGraph(this);
    }

    /** adds or gets existing edge between two vertices, directional */
    public UIEdge<UIVertex> addEdge(UIVertex<?> s, UIVertex<?> t) {
        String name = s.name().toString() + ':' + t.name();

        //this may be inefficient
        for (UIEdge e : s.getEdgesOut()) {
            if (e.name().equals(name)) {
                return e;
            }
        }

        UIEdge exist = new UIEdge(s, t);
        if (!add(exist))
            return null;
        return exist;
    }

    protected boolean add(UIEdge<UIVertex> e) {
        //if (edges.putIfAbsent(e.name(), e)==null) {
            if (e.getSource().link(e, false)) {
                e.getTarget().link(e, true);
                edgesChanged();
                return true;
            }
        //}
        return false;
    }

    private void remove(UIEdge<UIVertex> e) {
        if (e.getSource() != null) { //necessary?
            if (e.getSource().unlink(e, false)) {
                ensureValid(e.getSource());
                ensureValid(e.getTarget());
                edgesChanged();
            }
            if (e.getTarget() != null) { //necessary?
                e.getTarget().unlink(e, true);
            }
        }
    }

    private void ensureValid(UIVertex v) {
        if (v.isDependent() && v.degree() == 0)
            remove(v);
    }

    /**
     * if the UI node doesnt exist, it will add it to the graph and attempt creating one;
     * returns the existing one if already existed
     */
    protected AbstractWidget add(Named o) {

        AbstractWidget ui = getNode(o);

        if (ui == null) {

            ui = newVertex(o);
            if (ui == null) //should not be materialized
                return null;


            try {
                addNode(ui);
            } catch (StructuralException e) {
                throw new RuntimeException(e);
            }

        } else {
            //allow upgrading from Term to Concept; not downgrading
            /*if ((existing.vertex.getClass() == Term.class) && (o instanceof Concept)) {
                remove(existing.vertex);
                return add(o);
            }*/

        }

        return ui;
    }

    public AbstractWidget getNode(Named v) {
        final Named input = v;
        if ((v instanceof Concept)||(v instanceof Task))
            v = ((Terms.Termable)v).getTerm();
        AbstractWidget u = getNode(v.name().toString());
        if ((u!=null) && (u instanceof UIVertex))
            u = ((UIVertex)u).add(input);
        return u;
    }


    protected AbstractWidget remove(Named v) {

        String id = v.name().toString();
        AbstractWidget existing = removeNode(id);
        if (existing == null) return null;

        if (existing instanceof UIVertex) {
            UIVertex vv = ((UIVertex) existing);
            existing = vv = vv.remove(v);
            if (existing == null) return null;
            vv.destroy();


            List<UIEdge> toRemove = new ArrayList(vv.incoming.size() + vv.outgoing.size());
            toRemove.addAll(vv.incoming);
            toRemove.addAll(vv.outgoing);
            for (UIEdge e : toRemove) {
                remove(e);
            }

        }




        SwingUtilities.invokeLater(existing.ui::destroy);


        return existing;
    }

    protected void edgesChanged() {
        edgesChanged = true;
    }

    @Override
    public String toPostScript(HashMap scriptData) {
        return null;
    }

    public Rectangle2D getLayoutBounds() {
        return layoutBounds;
    }

    abstract public class SubCycle implements StepListener {

        float lastStep = 0;
        long lastStepReal = System.currentTimeMillis();

        @Override
        public void stepStarted(float time) {
            double interval = getTimePerCycle();
            float dt = time - lastStep;
            int numCycles = (int) (Math.floor(dt / interval));

            if (numCycles > 0) {

                long now = System.currentTimeMillis();
                run(numCycles, time, now - lastStepReal);

                lastStep = time;
                lastStepReal = now;
            }

            //System.out.println(this + " run: " + time + " waiting since " + lastStep);
        }

        abstract public double getTimePerCycle();

        abstract public void run(int count, float endTime, long deltaMS);
    }

    private class MyGrapher extends DefaultGrapher {


        public MyGrapher() {
            super(false, false, false, false, 0, true, true);
        }

        @Override
        public Object addVertex(Named o) {
            //only non-dependent objects can be added outside of an edge
            return add(o);
        }

        @Override
        public Object addEdge(NARGraph g, Named source, Named target, Object edgeContent) {

            //TODO defer creation of v1 and v2 until sure that edge can be created

            AbstractWidget v1 = add(source);
            if (v1 == null) return null;
            AbstractWidget v2 = add(target);
            if (v2 == null) return null;
            if (v2 == v1) return null; //loop
            if (!(v1 instanceof UIVertex)) return null;
            if (!(v2 instanceof UIVertex)) return null;

            UIEdge e = TermGraphNode.this.addEdge((UIVertex)v1, (UIVertex)v2);
            if (e!=null) {
                e.add((Named)edgeContent);
                return e;
            }

            return null;
        }


    }


}
