///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.graph;
//
//
//import automenta.spacegraph.Surface;
//import automenta.spacegraph.control.Repeat;
//import automenta.spacegraph.math.linalg.Vec2f;
//import automenta.spacegraph.math.linalg.Vec3f;
//import automenta.spacegraph.math.linalg.Vec4f;
//import automenta.spacegraph.shape.Curve;
//import automenta.spacegraph.shape.Rect;
//import automenta.spacegraph.shape.WideIcon;
//import com.jogamp.opengl.GL2;
//import org.jgrapht.Graph;
//import org.jgrapht.event.GraphListener;
//
//import java.awt.*;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.WeakHashMap;
//import java.util.logging.Logger;
//
///**
// *
// * @author seh
// */
//public class GraphSpace<N, E extends DirectedEdge<N>> implements GraphListener<N, E>, Repeat {
//
//    private float textScaleFactor;
//    float xAng = 0;
//    float yAng = 0;
//    private final Graph<N, E> sg;
//    private final Map<N, Rect> boxes = new HashMap();
//    private Vec3f targetPos = new Vec3f(0, 0, 10);
//    private Vec3f targetTarget = new Vec3f(0, 0, 0);
//    private Vec3f downPointPos;
//    private Vec3f downPointTarget;
//    private Vec2f downPixel;
//    private TextRenderer tr;
//    private GraphDrawer<Graph<N, E>, N> layout;
//    private WeakHashMap<N, Vec3f> pos = new WeakHashMap<N, Vec3f>();
//    protected final Map<E, Curve> edgeLines = new HashMap<E, Curve>();
//    private double defaultMomentum = 0.9;
//    private final Surface surface;
//
//    @Override
//    public void onEdgeAdded(E e) {
//        Rect aBox = boxes.get(e.getSourceNode());
//        Rect bBox = boxes.get(e.getDestinationNode());
//        if ((aBox == null) || (bBox == null)) {
//            Logger.getLogger(GraphSpace.class.toString()).severe("could not find boxes for edge: " + e);
//        }
//
//        final WideIcon curveLabel = new WideIcon(e.toString(), getColor(null), getColor(null));
//
//        Curve c = new Curve(aBox, bBox, 4, 3) {
//
//            @Override
//            public void draw(GL2 gl) {
//                super.draw(gl);
//                curveLabel.move(
//                        ctrlPoints[3], ctrlPoints[4], ctrlPoints[5]);
//                curveLabel.scale(0.1f, 0.1f, 0.1f);
//                //curveLabel.draw(gl);
//            }
//        };
//
//        edgeLines.put(e, c);
//        surface.add(c);
//    }
//
//    @Override
//    public void onEdgeRemoved(E e) {
//        Curve c = edgeLines.remove(e);
//        surface.remove(c);
//    }
//
//    @Override
//    public void onNodeAdded(N s) {
//        Rect box = newNodeRect(s);
//        boxes.put(s, box);
//        surface.add(box);
//    }
//
//    @Override
//    public void onNodeRemoved(N n) {
//        Rect box = boxes.remove(n);
//        surface.remove(box);
//    }
//
//    public GraphSpace(Surface surface, NotifyingDirectedGraph<N, E> graph, GraphDrawer<Graph<N, E>, N> initialLayout) {
//        super();
//
//        this.surface = surface;
//
//        setLayout(initialLayout);
//
//        this.sg = graph;
//
//        graph.addListener(this);
//
//    }
//
//    public void setLayout(GraphDrawer<Graph<N, E>, N> newLayout) {
//        this.layout = newLayout;
//    }
//
//    public Rect newNodeRect(N n) {
//        WideIcon box = new WideIcon(n.toString(), getColor(n), getColor(n));
//        //Window box = new Window();
//        return box;
//    }
//
//    public double getMomentum(N n) {
//        return defaultMomentum;
//    }
//
//    public Vec4f getColor(N n) {
//        return new Vec4f(Color.getHSBColor((float) Math.random() * 0.1f + 0.7f, 0.75f, 1.0f));
//    }
//
////    @Override
////    public void mousePressed(MouseEvent e) {
////        super.mousePressed(e);
////        downPixel = new Vec2f(e.getX(), e.getY());
////        downPointPos = new Vec3f(targetPos);
////        downPointTarget = new Vec3f(targetTarget);
////    }
////    @Override
////    public void mouseWheelMoved(MouseWheelEvent e) {
////        super.mouseWheelMoved(e);
////
////        Vec3f delta = new Vec3f(0, 0, e.getWheelRotation() * 4.0f);
////        targetPos.add(delta);
////        targetTarget.add(delta);
////    }
////    @Override
////    public void mouseDragged(MouseEvent e) {
////        super.mouseDragged(e);
////        xAng = e.getX();
////        yAng = e.getY();
////
////        Vec3f delta = new Vec3f(-(xAng - downPixel.x()), yAng - downPixel.y(), 0);
////        delta.scale(0.01f);
////
////        targetPos.set(downPointPos);
////        targetTarget.set(downPointTarget);
////        targetPos.add(delta);
////        targetTarget.add(delta);
////    }
//    @Override
//    public synchronized void update(double dt, double t) {
//        float m = 1.0f;
//
////        for (N s : sg.getNodes()) {
////            Vector v = layout.getCoordinates().get(s);
////            Rect r = boxes.get(s);
////            float x = r.getCenter().x() , y = r.getCenter().y(), z = r.getCenter().z();
////            if (v.getCoordinate(1) != x*m) {
////                System.out.println("discrep: " + v.getCoordinate(1) + " : " + x);
////            }
////            v.setCoordinate(x/m, 1);
////            if (v.getDimensions() > 1)
////                v.setCoordinate(y/m, 2);
////            if (v.getDimensions() > 2)
////                v.setCoordinate(z/m, 3);
////        }
//
//
//        for (N s : new LinkedList<N>(sg.getNodes())) {
//            Vector v = layout.getCoordinates().get(s);
//
//            if (v == null) {
//                continue;
//            }
//
//            final Rect b = boxes.get(s);
//            if (pos.get(s) == null) {
//                pos.put(s, new Vec3f(b.getCenter().x(), b.getCenter().y(), b.getCenter().z()));
//            } else {
//                Vec3f l = pos.get(s);
//                l.set(b.getCenter().x(), b.getCenter().y(), b.getCenter().z());
//            }
//            v.setCoordinate(b.getCenter().x(), 1);
//            v.setCoordinate(b.getCenter().y(), 2);
//            //v.setCoordinate(b.getCenter().z(), 3);
//        }
//
//        if (layout.isAlignable()) {
//
//            layout.align();
//        }
//
//        if (layout.getCoordinates() != null) {
//            //TODO constructing a new linkedlist here is hackish
//            for (N s : new LinkedList<N>(sg.getNodes())) {
//                Vector v = layout.getCoordinates().get(s);
//
//                if (v == null) {
//                    continue;
//                }
//                if (pos.get(s) == null)
//                    continue;
//
//                final Rect b = boxes.get(s);
//
//                float nx, ny, nz;
//                if (v.getDimensions() == 1) {
//                    float x = (float) (v.getCoordinate(1) * m);
//                    nx = 0;
//                    ny = x;
//                    nz = 0;
//                } else if (v.getDimensions() == 2) {
//                    float x = (float) (v.getCoordinate(1) * m);
//                    float y = (float) (v.getCoordinate(2) * m);
//                    nx = x;
//                    ny = y;
//                    nz = 0;
//                } else if (v.getDimensions() == 3) {
//                    float x = (float) (v.getCoordinate(1) * m);
//                    float y = (float) (v.getCoordinate(2) * m);
//                    float z = (float) (v.getCoordinate(3) * m);
//                    nx = x;
//                    ny = y;
//                    nz = z;
//                } else {
//                    nx = ny = nz = 0;
//                }
//                float momentum = (float) getMomentum(s);
//
//                float tnx = nx * (1.0f - momentum) + pos.get(s).x() * momentum;
//                float tny = ny * (1.0f - momentum) + pos.get(s).y() * momentum;
//                float tnz = nz * (1.0f - momentum) + pos.get(s).z() * momentum;
//                b.center(tnx, tny, tnz);
//
//                updateRect(s, b);
//            }
//        }
//
////        getCamera().camPos.lerp(targetPos, 0.95f);
////        getCamera().camTarget.lerp(targetTarget, 0.95f);
//    }
//
//    protected void updateRect(N s, Rect r) {
//        r.scale(0.5f, 0.5f, 0.5f);
//    }
////
////    @Override
////    protected synchronized void handleTouch(Pointer p) {
////        super.handleTouch(p);
////        Set<Touchable> touchingNow = new HashSet();
////        final Vec2f v = new Vec2f(p.world.x(), p.world.y());
////        synchronized (getSpace().getDrawables()) {
////            for (Drawable d : getSpace().getDrawables()) {
////                if (d instanceof Touchable) {
////                    Touchable t = (Touchable) d;
////                    if (t.isTouchable()) {
////                        if (t.intersects(v)) {
////                            touchingNow.add(t);
////                        }
////                    }
////                }
////            }
////        }
////        for (Touchable t : touchingNow) {
////            if (!p.touching.contains(t)) {
////                t.onTouchChange(p, true);
////                p.touching.add(t);
////            }
////        }
////        List<Touchable> toRemove = new LinkedList();
////        for (Touchable t : p.touching) {
////            if (!touchingNow.contains(t)) {
////                t.onTouchChange(p, false);
////                toRemove.add(t);
////            } else {
////                t.onTouchChange(p, true);
////            }
////        }
////        for (Touchable t : toRemove) {
////            p.touching.remove(t);
////        }
////    }
////    public static void main(String[] args) {
////
////        ConcurrentContext.setConcurrency(Runtime.getRuntime().availableProcessors());
////
////        MemorySelf self = new MemorySelf("me", "Me");
////        new SeedSelfBuilder().build(self);
////
////        //self.addPlugin(new Twitter());
////
////        self.updateLinks(null);
////
////        MutableBidirectedGraph<Node, ValueEdge<Node, Link>> target = new MutableDirectedAdjacencyGraph<Node, ValueEdge<Node, Link>>(self.getGraph());
////        //MetadataGrapher.run(self, target, true, true, true, true);
////        MetadataGrapher.run(self, target, true, true, true, true);
////
////        new SGWindow("DemoSGCanvas", new GraphCanvas(target, 3));
////    }
//}
