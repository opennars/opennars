///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.demo.swing;
//
//import automenta.netention.Link;
//import automenta.netention.Node;
//import automenta.netention.graph.NotifyingDirectedGraph;
//import automenta.netention.graph.ValueEdge;
//import automenta.netention.impl.MemorySelf;
//import automenta.netention.linker.MetadataGrapher;
//import automenta.netention.demo.Demo;
//import automenta.netention.demo.Demo;
//import automenta.netention.swing.GraphSpace;
//import automenta.netention.swing.SeedSelfBuilder;
//import automenta.spacegraph.swing.SwingWindow;
//import automenta.spacegraph.Surface;
//import automenta.spacegraph.video.SGPanel;
//import com.syncleus.dann.graph.drawing.hyperassociativemap.HyperassociativeMap;
//import java.awt.BorderLayout;
//import javax.swing.JButton;
//import javax.swing.JPanel;
//import javolution.context.ConcurrentContext;
//
///**
// *
// * @author seh
// */
//public class RunSelfGraphPanel  extends Surface implements Demo {
//
//
//    public static void main(String[] args) {
//        SwingWindow sw = new SwingWindow(new RunSelfGraphPanel().newPanel(), 400, 400, true);
//    }
//
//    @Override
//    public String getName() {
//        return "Self Graph";
//    }
//
//    @Override
//    public String getDescription() {
//        return "..";
//    }
//
//    @Override
//    public JPanel newPanel() {
//        ConcurrentContext.setConcurrency(Runtime.getRuntime().availableProcessors());
//
//        MemorySelf self = new MemorySelf("me", "Me");
//        new SeedSelfBuilder().build(self);
//
//        //self.addPlugin(new Twitter());
//
//        self.updateLinks(null);
//
//        NotifyingDirectedGraph<Node,ValueEdge<Node, Link>> target = new NotifyingDirectedGraph(self.getGraph());
//        MetadataGrapher.run(self, target, true, true, true, true);
//
//        JPanel j = new SGPanel(new GraphSpace(target, new HyperassociativeMap(target, 2)));
//
//        JPanel panel = new JPanel(new BorderLayout());
//        panel.add(j, BorderLayout.CENTER);
//        panel.add(new JButton("X"), BorderLayout.SOUTH);
//
//        return panel;
//
//    }
//}
