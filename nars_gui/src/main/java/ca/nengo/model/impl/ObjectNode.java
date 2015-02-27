package ca.nengo.model.impl;

import automenta.vivisect.dimensionalize.HyperassociativeMap;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.Source;
import ca.nengo.model.Target;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.icon.ArrowIcon;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.icon.NodeIcon;
import ca.nengo.ui.model.widget.UIProbe;
import ca.nengo.ui.model.widget.UISource;
import ca.nengo.ui.model.widget.UITarget;
import ca.nengo.ui.model.widget.Widget;
import ca.nengo.util.ScriptGenException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Wraps a POJO/Bean
 */
public class ObjectNode<O> extends AbstractNode implements UIBuilder {

    private O obj;

    /**
     * stores connectivity between this node and its chidlren (sources, targets, etc..) for
     * layout and other morpholoigcal purposes
     */
    private DefaultDirectedGraph<String, String> bodyGraph;
    private double childlayoutTime = 0.15f; //in seconds
    private ArrowIcon commonIn;
    private ArrowIcon commonOut;
    private UIObjectNode ui;

    public ObjectNode(String name, O object) {
        super(name);

        setObject(object);
    }

    public O getObject() {
        return obj;
    }

    protected void setObject(O object) {

        this.obj = object;
        this.ui = new UIObjectNode();

        List<Target> inputs = new ArrayList();
        List<Source> outputs = new ArrayList();

        commonIn = new ArrowIcon(4);
        commonOut = new ArrowIcon(4);

        bodyGraph = new DefaultDirectedGraph(String.class);
        bodyGraph.addVertex(ui.getName());
        bodyGraph.addVertex(commonIn.getName());
        bodyGraph.addVertex(commonOut.getName());



        for (Method m : obj.getClass().getMethods()) {
            //if (!m.isAccessible()) continue;
            if (Modifier.isPublic(m.getModifiers()))
                buildMethod(m, inputs, outputs, 2);
        }
        System.out.println(inputs);
        System.out.println(outputs);

        setInputs(inputs);
        setOutputs(outputs);

        ui.update();
    }

    @Override
    public void run(float startTime, float endTime) throws SimulationException {

    }

    @Override
    public Node[] getChildren() {
        return new Node[0];
    }

    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return null;
    }

    @Override
    public void reset(boolean randomize) {


    }

    @Override
    public UINeoNode newUI() {
        return ui;
    }

    private void buildMethod(Method m, List<Target> inputs, List<Source> outputs, int remainingDepth) {
        if (remainingDepth < 1) return;
        String name = m.getName();
        if ((m.getDeclaringClass() == Object.class) &&
                (!(name.equals("hashCode") || name.equals("equals") || name.equals("toString"))))
            return;

        if (m.getParameterCount() == 0) {
            if (m.getReturnType() == Void.class) {
                //pushbutton
                ActionPushButton pb = new ActionPushButton(this, m, obj);
                //TODO add a "sub-target source" to collect the output of the method
                inputs.add(pb);
                bodyGraph.addVertex(pb.getName());
                bodyGraph.addEdge(pb.getName(), commonIn.getName(), pb.getName() + ".in");
            } else {
                ProducerPushButton pb = new ProducerPushButton(this, m, obj);
                bodyGraph.addVertex(pb.getName());
                bodyGraph.addEdge(commonOut.getName(), pb.getName(),  pb.getName() + ".out");
                outputs.add(pb);
            }
        } else if (m.getParameterCount() == 1) {
            ObjectTarget pb = new ObjectTarget(this, getName() + "_" + m.toGenericString(), m.getReturnType());
            bodyGraph.addVertex(pb.getName());
            bodyGraph.addEdge(pb.getName(), commonIn.getName(), pb.getName() + ".in");
            inputs.add(pb);
        } else {
            //create a sub-node for this multi-arg method
        }




//            //TODO draw inter-node edges in paint function, less expensive
//
//            if (widget instanceof UISource) {
//                //originY -= scale * widget.getHeight() + 8;
//                //widget.setOffset(originX, originY);
//                widget.getPiccolo().addChild(new PXEdge(commonOut, widget));
//            } else if (widget instanceof UITarget) {
//                //termY -= scale * widget.getHeight() + 8;
//                //widget.setOffset(termX, termY);
//                widget.getPiccolo().addChild(new PXEdge(widget, commonIn));
//            }

            //TODO add additional contextual edges to cluster the items by:
            //  argument type
            //  return type
            //  annotations
            //  which superclass or interface it is declared
            //  equivalent but overloaded method name


    }

    public static class ActionPushButton<O> extends ObjectTarget<Boolean> {

        private final Method method;
        private final O obj;

        public ActionPushButton(Node parent, Method m, O instance) {
            super(parent, m.toGenericString(), Boolean.class);
            this.method = m;
            this.obj = instance;
        }


    }

    public static class ProducerPushButton<O> extends ObjectSource {

        private final Method method;
        private final O obj;

        public ProducerPushButton(Node parent, Method m, O instance) {
            super(parent, m.toGenericString());
            this.method = m;
            this.obj = instance;
        }


    }

    public class UIObjectNode extends UINeoNode<ObjectNode> {


        private HyperassociativeMap<String,String> bodyLayout;

        public UIObjectNode() {
            super(ObjectNode.this);
            setIcon(new NodeIcon(this));

        }

        public void update() {
            //TOOD remove children if update called > 1 time
            for (Target t : getTargets()) {
                showSource(t.getName());
            }
            for (Source t : getSources()) {
                showSource(t.getName());
            }

        }
//
//        private void attemptAdd(Object t) {
//            if (t instanceof Node)
//                addChild(UINeoNode.createNodeUI((Node)t));
//            else if (t instanceof UIBuilder)
//                addChild(((UIBuilder)t).newUI());
//            else if (t instanceof WorldObject)
//                addChild((UINeoNode)t);
//            else
//                System.err.println("unknown: " + t);
//        }
//
//        @Override
//        public void addChild(WorldObject wo) {
//            super.addChild(wo);
//            wo.setVisible(true);
//            wo.setPickable(true);
//            wo.setChildrenPickable(true);
//            wo.setBounds(Math.random(), Math.random(), 10, 10);
//        }

        //@Override
        public void layoutChildren2() {
            //super.layoutChildren();


		/*
         * layout widgets such as Origins and Terminations
		 */
            Rectangle2D bounds = getIcon().localToParent(getIcon().getBounds());

            double offsetX = bounds.getX();
            double offsetY = bounds.getY();

            double centerX = offsetX + bounds.getWidth() / 2f;
            double centerY = offsetY + bounds.getHeight() / 2f;

            double termX = -20 + bounds.getX();
            double termY = getIcon().getHeight() + offsetY;

            double originX = getIcon().getWidth() + 5 + offsetX;
            double originY = termY;

            double probeY = 0;


            if (getChildrenCount() == 0) {

                return;
            }

            if (bodyLayout == null) {

                commonIn.setOffset(-getWidth() / 2, 0);
                commonOut.setOffset(+getWidth() / 2, 0);

                addChild(commonIn);
                addChild(commonOut);


                bodyLayout = new HyperassociativeMap<String,String>(bodyGraph, HyperassociativeMap.Euclidean, 2) {

                    @Override
                    public double getSpeedFactor() {
                        return 1000f;
                    }

                    @Override
                    public double getSpeedFactor(String o) {
                        if (o.equals(UIObjectNode.this.getName())) return 0;
                        if (o.equals(commonIn.getName())) return 0;
                        if (o.equals(commonOut.getName())) return 0;
                        return super.getSpeedFactor(o);
                    }

                    @Override
                    public double getRadius(String o) {

//                        if (o instanceof WorldObject) {
//                            WorldObject w = (WorldObject)o;
//                            return Math.max(w.getWidth(), w.getHeight()) * 1f;
//                        }
                        return 1d;
                    }

                };
                bodyLayout.getPosition(getName()).set(0);
                bodyLayout.setEquilibriumDistance(0.1);
                bodyLayout.setMaxRepulsionDistance(100);


            }


		/*
		 * Lays out origin objects
		 */
            for (WorldObject wo : getChildren()) {

                if (wo instanceof UIProbe) {
                    UIProbe probe = (UIProbe) wo;

                    probe.setOffset(getWidth() * (1f / 4f), probeY + getHeight() * (1f / 4f));
                    probeY += probe.getHeight() + 5;

                } else if (wo instanceof Widget) {
                    Widget widget = (Widget) wo;
                    if (widget.getParent() == null) {
					/*
					 * Check to see that the origin has not been removed from
					 * the world
					 */

                    } else {

                        double scale = widget.getScale();

                        if (!(widget).isWidgetVisible()) {
                            double x = centerX - widget.getWidth() * scale / 2f;
                            double y = centerY - widget.getHeight() * scale / 2f;

                            widget.setOffset(x, y);

                            widget.setVisible(false);
                            widget.setPickable(false);
                            widget.setChildrenPickable(false);

                        } else {
                            widget.setVisible(true);
                            widget.setPickable(true);
                            widget.setChildrenPickable(true);


                            //TODO remove removed widgets

                        }
                    }

                }
            }



            /*if (bodyLayout.isAlignable())*/ {
                layoutGraph();

            }

        }

        protected void layoutGraph() {
            //bodyLayout.resetLearning();
            bodyLayout.run(16);

            final double w = getWidth();
            final double h = getHeight();
            //final double s = Math.max(w, h);

            for (WorldObject wo : getChildren()) {

                ArrayRealVector p = bodyLayout.getPosition(wo.getName());
                //System.out.println(wo + " " + p);

                if (wo == this || wo == commonIn || wo == commonOut) {
                    //force set the map's value to the fixed position
                    p.setEntry(0, wo.getX());
                    p.setEntry(1, wo.getY());
                }
                else {
                    double x = p.getEntry(0);
                    double ww = w/2 * 1.25f;
                    if (wo instanceof UITarget && x > -ww) x = -ww;
                    else if (wo instanceof UISource && x < ww) x = ww;
                    wo.animateToPosition(x, p.getEntry(1), childlayoutTime);
                }
            }


        }

        @Override
        public void paint(PaintContext paintContext) {

            super.paint(paintContext);

            Graphics2D g = paintContext.getGraphics();

            g.setStroke(new BasicStroke(1));
            g.setColor(Color.ORANGE);

            //draw edges
            for (String a : bodyGraph.edgeSet()) {
                String src = bodyGraph.getEdgeSource(a);
                WorldObject sw = getChild(src, null); //this function can be accelerated if children are stored as a map
                if (sw == null) continue;
                String tgt = bodyGraph.getEdgeTarget(a);
                WorldObject tw = getChild(tgt, null); //this function can be accelerated if children are stored as a map
                if (tw == null) continue;

                System.out.println(src + " " + sw.getX() + " " + sw.getY() + " " + tgt + " " + tw.getX());
                g.drawLine((int) sw.getX(), (int) sw.getY(), (int) tw.getX(), (int) tw.getY());
            }

        }

        @Override
        public String getTypeName() {
            return getClass().getSimpleName();
        }
    }


}
