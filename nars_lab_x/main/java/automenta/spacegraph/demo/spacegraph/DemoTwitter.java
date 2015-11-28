///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package automenta.spacegraph.demo.spacegraph;
//
//import automenta.netention.Link;
//import automenta.netention.Node;
//import automenta.netention.Node.StringNode;
//import automenta.netention.graph.NotifyingDirectedGraph;
//import automenta.netention.graph.ValueEdge;
//import automenta.netention.link.Next;
//import automenta.netention.plugin.jung.JungGraph;
//import automenta.netention.plugin.jung.JungGraphDrawer;
//import automenta.spacegraph.control.FractalControl;
//import automenta.spacegraph.graph.GraphSpace;
//import automenta.spacegraph.math.linalg.Vec4f;
//import automenta.spacegraph.shape.Rect;
//import automenta.spacegraph.shape.WideIcon;
//import automenta.spacegraph.swing.SwingWindow;
//import automenta.spacegraph.ui.PointerLayer;
//import automenta.spacegraph.ui.Window;
//import automenta.spacegraph.video.SGPanel;
//import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
//import java.awt.BorderLayout;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.media.opengl.GL2;
//import javax.swing.JPanel;
//import javolution.context.ConcurrentContext;
//import twitter4j.ResponseList;
//import twitter4j.Status;
//import twitter4j.TwitterException;
//
///**
// *
// * @author seh
// */
//public class DemoTwitter extends AbstractSurfaceDemo {
//
//    @Override
//    public String getName() {
//        return "";
//    }
//
//    @Override
//    public String getDescription() {
//        return "";
//    }
//
//    public static class TweetPanel extends WideIcon {
//
//        public TweetPanel(Status s) {
//            super(s.getText(), new Vec4f(0.5f, 0.5f, 0.5f, 1.0f), new Vec4f(0f, 0f, 0f, 1f));
//        }
//
//        @Override
//        public void draw(GL2 gl) {
//            super.draw(gl);
//            //System.out.println("tweet panel drawn " + getCenter() + " " + getScale());
//        }
//
//
//    }
//
//    public static class TwitterGrapher {
//        twitter4j.Twitter t = new twitter4j.Twitter();
//
//        private final NotifyingDirectedGraph<Node, ValueEdge<Node, Link>> graph;
//
//        public TwitterGrapher(NotifyingDirectedGraph<Node, ValueEdge<Node, Link>> graph) {
//            this.graph = graph;
//        }
//
//        public void addTimeline(String username) {
//            ResponseList timeline;
//            Node nextNode, prevNode = null;
//            try {
//                timeline = t.getUserTimeline(username);
//                for (Object o : timeline) {
//                    Status s = ((Status)o);
//
//                    //nextNode = new ObjectNode<Status>("t." + s.getId(), ((Status)o));
//                    nextNode = new StringNode(s.getText());
//                    graph.add(nextNode);
//
//                    if (prevNode!=null) {
//                        graph.add(new ValueEdge<Node, Link>(new Next(), prevNode, nextNode));
//                    }
//
//                    prevNode = nextNode;
//                }
//            } catch (TwitterException ex) {
//                Logger.getLogger(DemoTwitter.class.getName()).log(Level.SEVERE, null, ex);
//
//                //TODO extract a 'ExceptionNode' to display an exception in the graph
//                graph.add(new StringNode(ex.toString()));
//            }
//        }
//    }
//
//
//
//    public static void main(String[] args) {
//           SwingWindow sw = new SwingWindow(new DemoTwitter().newPanel(), 800, 600, true);
//    }
//
//
//
//    public JPanel newPanel() {
//        ConcurrentContext.setConcurrency(Runtime.getRuntime().availableProcessors());
//
//        NotifyingDirectedGraph<Node, ValueEdge<Node, Link>> graph = new NotifyingDirectedGraph<Node, ValueEdge<Node, Link>>();
//
//        new TwitterGrapher(graph).addTimeline("sseehh");
//
//        int numDimensions = 2;
//
//        System.out.println(graph.getNodes().size() + " : " + graph.getEdges().size());
//
//        ////SeHHyperassociativeMap layout = new SeHHyperassociativeMap(graph, numDimensions, 0.25, true);
//        //HyperassociativeMap layout = new HyperassociativeMap(graph, numDimensions);
//        final JungGraphDrawer layout = new JungGraphDrawer(graph, new ISOMLayout(new JungGraph(graph)), 4, 4);
//
//        //float r = 1.5f;
//        //layout.anchor(target.nodes[0][0], new Vec3f(-r,-r,0));
//        //layout.anchor(target.nodes[target.width-1][0], new Vec3f(r,-r,0));
//        ////layout.anchor(target.nodes[0][target.height-1], new Vec3f(-r,r,0));
//        ////layout.anchor(target.nodes[target.width-1][target.height-1], new Vec3f(r,r,0));
//
//        //final JungGraphDrawer layout = new JungGraphDrawer(target, new ISOMLayout(new JungGraph(target)), 4, 4);
//
//        final GraphSpace graphCanvas = new GraphSpace(this, graph, layout) {
//
//            @Override
//            public Rect newNodeRect(Object n) {
////                if (n instanceof ObjectNode) {
////                    Object v = ((ObjectNode)n).getValue();
////                    if (v instanceof Status) {
////                        System.out.println("new Node for: " + v);
////                        //return new WideIcon(((Status)v).getText(), new Vec4f(0.5f, 0.5f, 0.5f, 1.0f), new Vec4f(0f, 0f, 0f, 1f));
////                        return new Window();
////                    }
////                    return super.newNodeRect(n);
////                } else {
////                    return super.newNodeRect(n);
////                }
//                return new Window();
//            }
//
//        };
//        add(new Window());
//
//        SGPanel j = new SGPanel(this);
//        add(graphCanvas);
//
//        add(new FractalControl(j));
//        add(new PointerLayer(this));
//
//        JPanel panel = new JPanel(new BorderLayout());
//        panel.add(j, BorderLayout.CENTER);
//
//        return panel;
//    }
//
//
//
//}
