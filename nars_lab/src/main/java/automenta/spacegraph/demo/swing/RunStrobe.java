///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.demo.swing;
//
//import automenta.netention.demo.Demo;
//import automenta.netention.demo.Demo;
//import automenta.spacegraph.swing.SwingWindow;
//import automenta.spacegraph.Surface;
//import automenta.spacegraph.control.Repeat;
//import automenta.spacegraph.video.SGPanel;
//import automenta.spacegraph.math.linalg.Vec3f;
//import com.syncleus.dann.graph.DirectedEdge;
//import java.awt.BorderLayout;
//import javax.swing.JPanel;
//import javolution.context.ConcurrentContext;
//
///**
// *
// * @author seh
// */
//public class RunStrobe<N, E extends DirectedEdge<N>> implements Demo {
//
//    private double frequency = 6.8f;
//
//    public static void main(String[] args) {
//           SwingWindow sw = new SwingWindow(new RunStrobe().newPanel(), 800, 600, true);
//    }
//
//    public double getFrequency() {
//        return frequency;
//    }
//
//    public JPanel newPanel() {
//        ConcurrentContext.setConcurrency(Runtime.getRuntime().availableProcessors());
//
//        final Surface ds = new Surface();
//        SGPanel j = new SGPanel(ds);
//
//        ds.add(new Repeat() {
//
//            Vec3f background = new Vec3f();
//
//            @Override
//            public void update(double dt, double t) {
//                float a = (float)Math.sin(t * getFrequency() * Math.PI * 2.0) * 0.5f + 0.5f;
//                a = a*a;
//                background.set(a, a, a);
//                ds.setBackground(background);
//            }
//
//        });
//
//        JPanel panel = new JPanel(new BorderLayout());
//        panel.add(j, BorderLayout.CENTER);
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
