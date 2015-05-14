///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.demo.swing;
//
//import automenta.netention.Link;
//import automenta.netention.Node;
//import automenta.netention.Node.StringNode;
//import automenta.netention.demo.Demo;
//import automenta.netention.graph.NotifyingDirectedGraph;
//import automenta.netention.graph.SeHHyperassociativeMap;
//import automenta.netention.graph.ValueEdge;
//import automenta.netention.link.Next;
//import automenta.netention.node.TimePoint;
//import automenta.netention.plugin.finance.PublicBusiness.BusinessPerformance;
//import automenta.spacegraph.control.FractalControl;
//import automenta.spacegraph.graph.GraphSpace;
//import automenta.spacegraph.math.linalg.Vec4f;
//import automenta.spacegraph.shape.Rect;
//import automenta.spacegraph.shape.WideIcon;
//import automenta.spacegraph.ui.PointerLayer;
//import automenta.spacegraph.video.SGPanel;
//import automenta.vivisect.swing.NWindow;
//import com.syncleus.dann.graph.DirectedEdge;
//import javolution.context.ConcurrentContext;
//
//import javax.swing.*;
//import java.awt.*;
//
///**
// *
// * @author seh
// */
//public class RunMeshGraph<N, E extends DirectedEdge<N>> implements Demo {
//
//    public static void main(String[] args) {
//        NWindow sw = new NWindow(new RunMeshGraph().newPanel(), 800, 600, true);
//    }
//
//
//    public static class MeshGraph extends NotifyingDirectedGraph<Node, ValueEdge<Node, Link>> {
//        public Node[][] nodes;
//        private final int width;
//        private final int height;
//
//        public MeshGraph(int width, int height, float connectivity) {
//            super();
//
//            this.width = width;
//            this.height = height;
//
//            nodes = new Node[width][];
//            for (int h = 0; h < width; h++) {
//                nodes[h] = new Node[height];
//            }
//
//            for (int w = 0; w < width; w++) {
//                for (int h = 0; h < height; h++) {
//                    Node n = new StringNode(w + "_" + h);
//                    add(n);
//                    nodes[w][h] = n;
//                }
//            }
//            for (int w = 0; w < width; w++) {
//                for (int h = 0; h < height; h++) {
//
//                    if ((w > 0) && (Math.random() < connectivity))
//                        add(new ValueEdge<Node, Link>(new Next(), nodes[w-1][h], nodes[w][h]));
//                    if ((w < width-1) && (Math.random() < connectivity))
//                        add(new ValueEdge<Node, Link>(new Next(), nodes[w+1][h], nodes[w][h]));
//                    if ((h > 0) && (Math.random() < connectivity))
//                        add(new ValueEdge<Node, Link>(new Next(), nodes[w][h-1], nodes[w][h]));
//                    if ((h < height-1) && (Math.random() < connectivity))
//                        add(new ValueEdge<Node, Link>(new Next(), nodes[w][h+1], nodes[w][h]));
//                }
//            }
//        }
//    }
//
//
//    public JPanel newPanel() {
//        ConcurrentContext.setConcurrency(Runtime.getRuntime().availableProcessors());
//
//        MeshGraph target = new MeshGraph(8,8, 0.8f);
//
//
//        int numDimensions = 3;
//
//        System.out.println(target.getNodes().size() + " : " + target.getEdges().size());
//
//        SeHHyperassociativeMap layout = new SeHHyperassociativeMap(target, numDimensions, 0.25, true);
//        float r = 1.5f;
//        //layout.anchor(target.nodes[0][0], new Vec3f(-r,-r,0));
//        //layout.anchor(target.nodes[target.width-1][0], new Vec3f(r,-r,0));
//        ////layout.anchor(target.nodes[0][target.height-1], new Vec3f(-r,r,0));
//        ////layout.anchor(target.nodes[target.width-1][target.height-1], new Vec3f(r,r,0));
//
//        //final JungGraphDrawer layout = new JungGraphDrawer(target, new ISOMLayout(new JungGraph(target)), 4, 4);
//
//        final GraphSpace graphCanvas = new GraphSpace(target, layout) {
//
//            @Override
//            public Rect newNodeRect(Object n) {
//                if (n instanceof BusinessPerformance) {
//                    BusinessPerformance bp = (BusinessPerformance) n;
//                    WideIcon i = new WideIcon("" /*bp.toString()*/, getBPColor(bp), new Vec4f(Color.WHITE));
//                    float s = getBPSize(bp);
//                    i.getScale().set(s, s, s);
//                    return i;
//                } else if (n instanceof TimePoint) {
//                    TimePoint ti = (TimePoint) n;
//                    WideIcon i = new WideIcon("" /*+ ti.date.getTime()*/, new Vec4f(Color.BLUE), new Vec4f(Color.WHITE));
//                    return i;
//                } else {
//                    return super.newNodeRect(n);
//                }
//            }
//
//            @Override
//            protected void updateRect(Object n, Rect r) {
//                if (n instanceof TimePoint) {
//                    r.setBackgroundColor(new Vec4f(Color.BLUE));
//                    r.getScale().set(0.2F, 0.2F, 0.2F);
//                } else if (n instanceof BusinessPerformance) {
//                } else {
//                    super.updateRect(n, r);
//                }
//            }
//
//            public float getBPSize(BusinessPerformance bp) {
//                float lowest = bp.getBusiness().getLow();
//                float highest = bp.getBusiness().getHigh();
//                float s = 0.05F + 0.25F * (bp.high - lowest) / (highest - lowest);
//                return s;
//            }
//
//            public Vec4f getBPColor(BusinessPerformance bp) {
//                float lowest = bp.getBusiness().getLow();
//                float highest = bp.getBusiness().getHigh();
//                float r = (bp.high - lowest) / (highest - lowest);
//                float g = 0.1F;
//                float b = 0.1F;
//                Vec4f v = new Vec4f(r, g, b, 0.75f);
//                return v;
//            }
//
//        };
//
//        SGPanel j = new SGPanel(graphCanvas);
//
//        graphCanvas.add(new FractalControl(j));
//        graphCanvas.add(new PointerLayer(graphCanvas));
//
//        JPanel panel = new JPanel(new BorderLayout());
//        panel.add(j, BorderLayout.CENTER);
////
////        JButton pb = new JButton("+");
////        pb.addActionListener(new ActionListener() {
////
////            @Override
////            public void actionPerformed(ActionEvent e) {
////                double n = layout.getEquilibriumDistance() * 1.1;
////                layout.resetLearning();
////                layout.setEquilibriumDistance(n);
////            }
////        });
////        JButton mb = new JButton("-");
////        mb.addActionListener(new ActionListener() {
////
////            @Override
////            public void actionPerformed(ActionEvent e) {
////                double n = layout.getEquilibriumDistance() * 0.9;
////                layout.resetLearning();
////                layout.setEquilibriumDistance(n);
////            }
////        });
////
////        JPanel px = new JPanel(new FlowLayout());
////        px.add(mb);
////        px.add(pb);
//
////        panel.add(px, BorderLayout.SOUTH);
//
//        return panel;
//    }
//
//    @Override
//    public String getName() {
//        return "Hyperassociative Finance";
//    }
//
//    @Override
//    public String getDescription() {
//        return "..";
//    }
//}
