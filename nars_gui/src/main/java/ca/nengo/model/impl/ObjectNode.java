package ca.nengo.model.impl;

import automenta.vivisect.dimensionalize.AbstractFastOrganicLayout;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.Source;
import ca.nengo.model.Target;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.icon.ArrowIcon;
import ca.nengo.ui.lib.world.piccolo.primitive.PXEdge;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.icon.NodeIcon;
import ca.nengo.ui.model.widget.UIProbe;
import ca.nengo.ui.model.widget.UISource;
import ca.nengo.ui.model.widget.UITarget;
import ca.nengo.ui.model.widget.Widget;
import ca.nengo.util.ScriptGenException;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps a POJO/Bean
 */
public class ObjectNode<O> extends AbstractNode implements UIBuilder {

    private O obj;

    /** stores connectivity between this node and its chidlren (sources, targets, etc..) for
     * layout and other morpholoigcal purposes */
    private DefaultDirectedGraph<WorldObject,String> bodyGraph;
    private AbstractFastOrganicLayout<WorldObject,String,WorldObject> bodyLayout;
    private double childlayoutTime = 1.25f; //in seconds
    private ArrowIcon commonIn;
    private ArrowIcon commonOut;
    Map<WorldObject, double[]> pos = new HashMap();

    public ObjectNode(String name, O object) {
        super(name);

        setObject(object);
    }

    protected void setObject(O object) {

        this.obj = object;

        List<Target> inputs = new ArrayList();
        List<Source> outputs = new ArrayList();


        for (Method m : obj.getClass().getMethods()) {
            //if (!m.isAccessible()) continue;
            System.out.println(m);
            buildMethod(m, inputs, outputs, 2);
        }
        System.out.println(inputs);
        System.out.println(outputs);

        setInputs(inputs);
        setOutputs(outputs);
    }

    public O getObject() {
        return obj;
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
        return new UIObjectNode();
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

    public static class ProducerPushButton<O> extends ObjectSource  {

        private final Method method;
        private final O obj;

        public ProducerPushButton(Node parent, Method m, O instance) {
            super(parent, m.toGenericString());
            this.method = m;
            this.obj = instance;
        }



    }

    private void buildMethod(Method m, List<Target> inputs, List<Source> outputs, int remainingDepth) {
        if (remainingDepth < 1) return;

        if (m.getParameterCount() == 0) {
            if (m.getReturnType() == Void.class) {
                //pushbutton
                ActionPushButton pb = new ActionPushButton(this, m, obj);
                //TODO add a "sub-target source" to collect the output of the method
                inputs.add(pb);
            }
            else {
                ProducerPushButton pb = new ProducerPushButton(this, m, obj);
                outputs.add(pb);
            }
        }
        else if (m.getParameterCount() == 1) {
            ObjectTarget o = new ObjectTarget(this, getName() + "_" + m.toGenericString(), m.getReturnType());
            inputs.add(o);
        }
        else {
            //create a sub-node for this multi-arg method
        }


    }

    public class UIObjectNode extends UINeoNode<ObjectNode> {


        public UIObjectNode() {
            super(ObjectNode.this);
            setIcon(new NodeIcon(this));

        }

        @Override
        public void layoutChildren() {
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


            if (getChildrenCount() == 0) return;

            if (bodyGraph == null) {

                commonIn = new ArrowIcon(4);
                addChild(commonIn);
                commonIn.animateToPosition(-getWidth()/2, 0, 0);
                commonOut = new ArrowIcon(4);
                addChild(commonOut);
                commonOut.animateToPosition(+getWidth()/2, 0, 0);

                bodyGraph = new DefaultDirectedGraph(String.class);
                bodyGraph.addVertex(this);
                bodyGraph.addVertex(commonIn);
                bodyGraph.addVertex(commonOut);

            /*
            bodyLayout = new HyperassociativeMap(bodyGraph, HyperassociativeMap.Manhattan, 2) {

                @Override
                public double getSpeedFactor(Object o) {
                    if (o == UINeoNode.this) return 0; //do not all this UINeoNode to move
                    return super.getSpeedFactor(o);
                }

                @Override
                public double getRadius(Object o) {
                    if (o == UINeoNode.this)
                        return 1;
                    return super.getRadius(o);
                }
            };
            bodyLayout.getPosition(this).set(0);
            bodyLayout.setEquilibriumDistance(0.01);
            */

                bodyLayout = new AbstractFastOrganicLayout<WorldObject,String,WorldObject>() {


                    @Override
                    public WorldObject getDisplay(Graph<WorldObject, String> graph, WorldObject vertex) {
                        return vertex;
                    }

                    @Override
                    public boolean isVertexMovable(WorldObject vd) {
                        if (vd==UIObjectNode.this) return false;
                        if (vd==commonIn) return false;
                        if (vd==commonOut) return false;
                        return true;
                    }

                    @Override
                    public void setPosition(WorldObject vd, float x, float y) {
                        if (!isVertexMovable(vd)) return;

                        double[] p = pos.get(vd);
                        if (p == null) { p = new double[2]; pos.put(vd, p); }
                        p[0] = x; p[1] = y;


                        pos(vd, p);
                    }

                    @Override
                    public void movePosition(WorldObject vd, final float dx, final float dy) {
                        if (!isVertexMovable(vd)) return;

                        double[] p = pos.get(vd);
                        if (p == null) return;

                        p[0] += dx; p[1] += dy;
                        pos(vd, p);
                    }

                    protected void pos(WorldObject w, double[]p) {
                        double x = p[0];
                        double y = p[1];
                        double ww = getWidth()/1.5f;
                        if (w instanceof UISource && x < ww) x = ww;
                        else if (w instanceof UITarget && x > -ww) x = -ww;
                        w.animateToPosition(x, y, 1.25);
                        p[0] = x;
                        p[1] = y;
                    }

                    @Override
                    public WorldObject getVertex(WorldObject vd) {
                        return vd;
                    }

                    @Override
                    public double getX(WorldObject vd) {

                        double[] x = pos.get(vd);
                        if (x == null) return vd.getX();
                        return x[0];
                        //return vd.getX();
                    }

                    @Override
                    public double getY(WorldObject vd) {
                        double[] x = pos.get(vd);
                        if (x == null) return vd.getY();
                        return x[1];
                        //return vd.getY();
                    }

                    @Override
                    public float getRadius(WorldObject vd) {
                        final float w = (float)vd.getWidth()*4f;
                        final float h = (float)vd.getHeight()*4f;
                        if (w > h) return w;
                        return h;
                    }
                };
                //bodyLayout.setForceConstant(2180);
                //bodyLayout.setInitialTemp(4f);
                bodyLayout.setIterationsRemain(1);

                bodyLayout.setMinDistanceLimit(0.1f);
                bodyLayout.setMaxDistanceLimit(520f);
                //bodyLayout.setMaxDistanceLimit(bodyLayout.getRadius(this) * 16f);*/

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

                            if (!bodyGraph.containsVertex(widget)) {
                                bodyGraph.addVertex(widget);

                                //TODO draw inter-node edges in paint function, less expensive

                                if (widget instanceof UISource) {
                                    //originY -= scale * widget.getHeight() + 8;
                                    //widget.setOffset(originX, originY);
                                    bodyGraph.addEdge(commonOut, widget, widget.getName() + " out");
                                    widget.getPiccolo().addChild(new PXEdge(commonOut, widget));
                                } else if (widget instanceof UITarget) {
                                    //termY -= scale * widget.getHeight() + 8;
                                    //widget.setOffset(termX, termY);
                                    bodyGraph.addEdge(widget, commonIn, widget.getName() + " in");
                                    widget.getPiccolo().addChild(new PXEdge(widget, commonIn));
                                }

                                //TODO add additional contextual edges to cluster the items by:
                                //  argument type
                                //  return type
                                //  annotations
                                //  which superclass or interface it is declared
                                //  equivalent but overloaded method name
                            }
                            //TODO remove removed widgets

                        }
                    }

                }
            }



            if (bodyLayout.getIterationsRemain() > 0) {
                for (Map.Entry<WorldObject,double[]> x : pos.entrySet()) {
                    //copy current positoin to p[]
                    double[] p = x.getValue();
                    WorldObject w = x.getKey();
                    p[0] = w.getX();
                    p[1] = w.getY();
                }
                bodyLayout.update(bodyGraph);
            }

        /*
        final double w = getWidth();
        final double h = getHeight();
        final double s = Math.max(w, h);

        for (WorldObject wo : bodyGraph.vertexSet()) {
            if (wo == this) continue;
            //w.setOffset
            ArrayRealVector p = bodyLayout.getPosition(wo);

            wo.animateToPosition(p.getEntry(0)*s-w/2, p.getEntry(1)*s-h/2, childlayoutTime);
            //w.setOffset(p.getEntry(0), p.getEntry(1));
        }*/

        }

            @Override
        public String getTypeName() {
            return getClass().getSimpleName();
        }
    }
}
