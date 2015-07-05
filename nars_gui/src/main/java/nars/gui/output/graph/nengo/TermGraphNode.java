package nars.gui.output.graph.nengo;

import automenta.vivisect.dimensionalize.FastOrganicIterativeLayout;
import automenta.vivisect.dimensionalize.HyperassociativeMap;
import automenta.vivisect.dimensionalize.IterativeLayout;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.AbstractMapNetwork;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.plot.AbstractWidget;
import com.google.common.collect.Iterators;
import nars.Events;
import nars.Memory;
import nars.event.ConceptReaction;
import nars.process.ConceptProcess;
import nars.util.data.id.Named;
import nars.task.Task;
import nars.concept.Concept;
import nars.term.Term;
import nars.term.Termed;
import nars.util.event.AbstractReaction;
import nars.util.graph.DefaultGrapher;
import nars.util.graph.NARGraph;
import org.apache.commons.math3.linear.ArrayRealVector;

import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.util.*;


public class TermGraphNode extends AbstractMapNetwork<String, AbstractWidget> implements UIBuilder {



    private ConceptReaction conceptReaction;
    private final Memory memory;

    //float simTimePerCycle = 1f;
    boolean allowTermNodes = true;

    float simTimePerLayout = 0.25f;
    long lasTLayout = System.currentTimeMillis();

    Rectangle2D layoutBounds = new Rectangle2D.Double(-500, -500, 1000, 1000);

    private DefaultUINetwork ui;
    private int numVertices = 0;
    final FastOrganicIterativeLayout<UIVertex, UIEdge<UIVertex>> organicLayout =
            new FastOrganicIterativeLayout<UIVertex, UIEdge<UIVertex>>(layoutBounds) {


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
                    return 0.5 + 0.5 * e.getTermlinkPriority();
                    //return 1;
                }


                @Override
                public double getRadius(UIVertex narGraphVertex) {
                    double r = 1 + narGraphVertex.getRadius();
                    return r * 2000;
                }

                @Override
                protected Iterator<UIVertex> getVertices() {
                    return Iterators.filter(nodes().iterator(), UIVertex.class);
                }


            };
    final HyperassociativeMap<UIVertex,UIEdge<UIVertex>> haLayout =
            new HyperassociativeMap<UIVertex,UIEdge<UIVertex>>(2) {


                @Override
                public ArrayRealVector getPosition(UIVertex node) {

                    return node.getCoordinates();
                }

                @Override
                public void pre(Collection<UIVertex> vertices) {

                    setScale(96);
                    setRepulsiveWeakness(3);
                    setAttractionStrength(2.2);
                    setEquilibriumDistance(4);
                    setMaxRepulsionDistance(1000);
                    updateCoordinates(vertices);
                }


                @Override
                public double getSpeedFactor() {
                    return 0.02;
                }

                @Override
                public double getEdgeWeight(UIEdge e) {
                    /*if (e.e instanceof TermLink) {
                        return 4.0;
                    }*/
                    return 0.25 + 0.75 * e.getTermlinkPriority();
                    //return 1;
                }


                @Override
                public double getRadius(UIVertex narGraphVertex) {
                    double r = 1 + narGraphVertex.getRadius();
                    return r * 3;
                }

                @Override
                protected Iterator<UIVertex> getVertices() {
                    return Iterators.filter(nodes().iterator(), UIVertex.class);
                }


            };



    IterativeLayout<UIVertex, UIEdge<UIVertex>> hmap = haLayout;

//    SubCycle narCycle = new SubCycle() {
//        @Override
//        public double getTimePerCycle() {
//            return simTimePerCycle;
//        }
//
//        @Override
//        public void run(int numCycles, float endTime, long deltaMS) {
//
//            if (!nar.isRunning())
//                nar.step(numCycles);
//        }
//
//        @Override
//        public String toString() {
//            return "nar";
//        }
//    };

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


            double layoutRad = Math.sqrt(numVertices) * 800;
            layoutBounds.setRect(-layoutRad / 2, -layoutRad / 2, layoutRad, layoutRad);
            //hmap.setInitialTemp(200, 0.5f);
            //hmap.setForceConstant(100);
            hmap.resetLearning();
            hmap.run(1);


        }

        @Override
        public String toString() {
            return "layout";
        }
    };
    private AbstractReaction conceptReaction2;

    public boolean filter() {
        return false;
    }
    public boolean includeTerm(Term t) {
        return true;
    }


    public TermGraphNode(Memory m) {
        super();

        this.memory = m;

        addStepListener(layoutCycle);
    }

    public void start() {

        stop();

        this.conceptReaction = new ConceptReaction(memory) {


            @Override
            public void onConceptActive(Concept c) {
                refresh(c);
            }

            @Override
            public void onConceptForget(Concept c) {
                remove(c);
            }
        };
        this.conceptReaction2 = new AbstractReaction(memory.event, Events.ConceptProcessed.class) {

            @Override
            public void event(Class event, Object... args) {
                refresh(((ConceptProcess)args[0]).getConcept());
            }
        };

    }

    public void stop() {
        if (conceptReaction!=null) {
            conceptReaction.off();
            conceptReaction = null;
        }
    }

    void updateCoordinates(Collection<UIVertex> vertices) {
        long now = System.currentTimeMillis();
        long dt = (now - lasTLayout);
        if (dt > 0) {
            numVertices = vertices.size();
            for (UIVertex v : vertices) {
                v.getActualCoordinates(dt);
                if (v.isDependent() && v.degree() == 0) {
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
            v = new TermNode(this, (Concept) x) {
                @Override
                public boolean isDependent() {
                    if (filter())
                        return super.isDependent() || !includeTerm(term);
                    return super.isDependent();
                }
            };
        } else if (x instanceof Task) {
            v = new TermNode(this, (Task) x) {
                @Override
                public boolean isDependent() {
                    if (filter())
                        return super.isDependent() || !includeTerm(term);
                    return super.isDependent();
                }
            };
        }
        else if ((x instanceof Term) && allowTermNodes) {
            v = new TermNode(this, (Term)x);
        }



        return v;
    }


    public void refresh(Object x) {
        if (x instanceof Termed)
            if (!includeTerm(((Termed) x).getTerm()))
                return;
        new MyGrapher().on(null, x, false).finish();
    }


    @Override
    public DefaultUINetwork newUI(double width, double height) {
        return this.ui = new DefaultUINetwork(this);
    }

    /**
     * adds or gets existing edge between two vertices, directional
     */
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
    protected synchronized AbstractWidget add(Named o) {

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
        if ((v instanceof Concept) || (v instanceof Task))
            v = ((Termed) v).getTerm();
        AbstractWidget u = getNode(v.name().toString());
        if ((u != null) && (u instanceof UIVertex))
            u = ((UIVertex) u).add(input);
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
            SwingUtilities.invokeLater(vv::destroy);


            List<UIEdge> toRemove = new ArrayList(vv.incoming.size() + vv.outgoing.size());
            toRemove.addAll(vv.incoming);
            toRemove.addAll(vv.outgoing);
            for (UIEdge e : toRemove) {
                remove(e);
            }

            ArrayList a;
        }


        SwingUtilities.invokeLater(existing.ui::destroy);


        return existing;
    }


    @Override
    public String toPostScript(HashMap scriptData) {
        return null;
    }

    public Rectangle2D getLayoutBounds() {
        return layoutBounds;
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
            AbstractWidget v2 = add(target);
            //allow both source and target opportunity to combine with existing vertex
            if ((v1 == null || v2 == null)) return null;
            if (v2 == v1) return null; //loop
            if (!(v1 instanceof UIVertex)) return null;
            if (!(v2 instanceof UIVertex)) return null;

            UIEdge e = TermGraphNode.this.addEdge((UIVertex) v1, (UIVertex) v2);
            if (e != null) {
                e.add((Named) edgeContent);
                return e;
            }

            return null;
        }


    }


}
