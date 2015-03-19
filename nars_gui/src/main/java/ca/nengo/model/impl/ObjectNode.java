package ca.nengo.model.impl;

import automenta.vivisect.Video;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.NSource;
import ca.nengo.model.NTarget;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.icon.NodeIcon;
import ca.nengo.ui.model.icon.PolygonIcon;
import ca.nengo.ui.model.widget.UISource;
import ca.nengo.ui.model.widget.UITarget;
import ca.nengo.util.ScriptGenException;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.awt.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;

/**
 * Wraps a POJO/Bean
 */
public class ObjectNode<O> extends AbstractNode implements UIBuilder {

    private O obj;

    /**
     * stores connectivity between this node and its chidlren (sources, targets, etc..) for
     * layout and other morphological purposes
     */
    protected DefaultDirectedGraph<String, String> bodyGraph;
    protected Set<String> centroids = new HashSet(); //feature centroids to classify around

    protected PolygonIcon commonIn;
    protected PolygonIcon commonOut;
    protected UIObjectNode ui;

    public ObjectNode(String name, O object) {
        super(name);

        setObject(object);
    }

    public O getObject() {
        return obj;
    }

    protected void setObject(O object) {

        this.obj = object;
        this.ui = newUI(64,64);

        List<NTarget> inputs = new ArrayList();
        List<NSource> outputs = new ArrayList();

        commonIn = new PolygonIcon(ui,4,1f,Color.GRAY) {
            @Override
            public String name() {
                return "inputs";
            }

        };
        commonIn.setLabelVisible(false);

        commonOut = new PolygonIcon(ui,4,1f,Color.GRAY) {
            @Override
            public String name() {
                return "outputs";
            }
        };
        commonOut.setLabelVisible(false);

        bodyGraph = new DefaultDirectedGraph(String.class);
        bodyGraph.addVertex(name());
        bodyGraph.addVertex(commonIn.name());
        bodyGraph.addVertex(commonOut.name());



        for (Method m : obj.getClass().getMethods()) {
            //if (!m.isAccessible()) continue;
            if (Modifier.isPublic(m.getModifiers()))
                buildMethod(m, inputs, outputs, 2);
        }


        setInputs(inputs);
        setOutputs(outputs);

        ui.update();
    }

    @Override
    public void run(float startTime, float endTime) throws SimulationException {

    }


    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return null;
    }

    @Override
    public void reset(boolean randomize) {


    }

    @Override
    public UIObjectNode newUI(double width, double height) {
        return new UIObjectNode();
    }

    private void buildMethod(Method m, List<NTarget> inputs, List<NSource> outputs, int remainingDepth) {
        if (remainingDepth < 1) return;
        String name = m.getName();

        String object = null;
        String returnType = null;
        String firstParameterType = null;
        String declaringClass = null;


        Class declClass = m.getDeclaringClass();
        if ((declClass == Object.class) &&
                (!(name.equals("hashCode") || name.equals("equals") || name.equals("toString"))))
            return;

        if (!declClass.equals(obj.getClass())) {
            declaringClass = declClass.getName();
        }

        if (m.getParameterCount() == 0) {
            if (m.getReturnType() == Void.class) {
                //pushbutton
                ActionPushButton pb = new ActionPushButton(this, m, obj);
                //TODO add a "sub-target source" to collect the output of the method
                object = pb.getName();
                inputs.add(pb);
                bodyGraph.addVertex(object);
                bodyGraph.addEdge(object, commonIn.name(), object + ".in");
            } else {
                ProducerPushButton pb = new ProducerPushButton(this, m, obj);
                object = pb.getName();
                returnType = m.getReturnType().getName();
                bodyGraph.addVertex(object);
                bodyGraph.addEdge(commonOut.name(), object,  object + ".out");
                outputs.add(pb);
            }
        } else if (m.getParameterCount() == 1) {
            MethodTarget1 pb = new MethodTarget1(this, obj, m);
            object = pb.getName();
            firstParameterType = pb.getRequiredType().getName();
            bodyGraph.addVertex(object);
            bodyGraph.addEdge(object, commonIn.name(), object + ".in");
            inputs.add(pb);
        } else {
            //create a sub-node for this multi-arg method
        }

        if (object!=null) {
            if (declaringClass!=null) {
                addCentroid(declaringClass);
                bodyGraph.addEdge(declaringClass, object, object + ".dc");
            }
            if (returnType!=null) {
                addCentroid(returnType);
                bodyGraph.addEdge(returnType, object, object + ".rt");
            }
            if (firstParameterType!=null) {
                addCentroid(firstParameterType);
                bodyGraph.addEdge(firstParameterType, object, object + ".p0t");
            }
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

    protected void addCentroid(String n) {

        if (bodyGraph.addVertex(n)) {
            centroids.add(n);
        }

    }

    public static String methodString(Method m) {
        String s = m.toGenericString();
        return s.replace("public ", "");
    }

    /** method target with 1 parameter value */
    public static class MethodTarget1<O> extends ObjectTarget {

        private final Method method;
        private final O obj;

        public MethodTarget1(Node parent, O instance, Method m) {
            super(parent, methodString(m), m.getParameterTypes()[0]);

            this.method = m;
            this.obj = instance;
        }

        @Override
        public void apply(Object values) throws SimulationException {
            super.apply(values);
            try {
                method.invoke(obj, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static class ActionPushButton<O> extends ObjectTarget<Boolean> {

        private final Method method;
        private final O obj;

        public ActionPushButton(Node parent, Method m, O instance) {
            super(parent, methodString(m), Boolean.class);
            this.method = m;
            this.obj = instance;
        }


    }

    public static class ProducerPushButton<O> extends ObjectSource {

        public final Method method;
        public final O obj;

        public ProducerPushButton(Node parent, Method m, O instance) {
            super(parent, methodString(m));
            this.method = m;
            this.obj = instance;
        }

        @Override
        public Object get() {
            try {
                return method.invoke(obj);
            } catch (Exception e) {
                return e;
            }
        }
    }

    public class UIObjectNode extends UINeoNode<ObjectNode> {



        public UIObjectNode() {
            super(ObjectNode.this);

            setIcon(new NodeIcon(this));


            //BoundsHandle.addBoundsHandlesTo((WorldObjectImpl) getIcon());

        }

        public void update() {
            //TOOD remove children if update called > 1 time
            for (NTarget t : getTargets()) {
                UITarget u = showTarget(t.getName());
                if (t instanceof ObjectTarget) {
                    u.setColor(Video.getColorA(((ObjectTarget) t).getRequiredType().hashCode(), 0.85f, 0.85f, 0.75f));
                }
            }
            for (NSource t : getSources()) {
                UISource u = showSource(t.getName());
                if (t instanceof ProducerPushButton) {
                    ProducerPushButton pp = (ProducerPushButton)t;
                    u.setColor( Video.getColorA(pp.method.getReturnType().hashCode(), 0.85f, 0.85f, 0.75f) );
                }

            }
            for (String c : centroids) {
                if (bodyGraph.inDegreeOf(c) + bodyGraph.outDegreeOf(c) < 2) {
                    //remove a centroid if it has only one associated vertex
                    bodyGraph.removeVertex(c);
                    continue;
                }

                PolygonIcon p = new PolygonIcon(ui, 3, 1f, Color.WHITE) {
                    @Override
                    public String name() {
                        return c;
                    }
                };
                p.setLabelVisible(false);
                addChild(p);
            }

            //grow some amount by # of sources+targets
            double pct = 1 +  Math.sqrt(getSources().length + getTargets().length)/4f;
            //getBounds().setSize(getWidth() * pct, getHeight() * pct);
            setScale(getScale() * pct);
            //getIcon().setScale(getIcon().getScale() * pct);

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


        @Override
        public void layoutChildren() {
            super.layoutChildren();

        }


        @Override
        public String getTypeName() {
            return getClass().getSimpleName();
        }
    }


}
