///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package automenta.spacegraph.demo;
//
//import automenta.netention.demo.physics.CubesShootApp;
//import automenta.netention.demo.physics.CubesShootApp.ZeroGravityCubes;
//import automenta.spacegraph.physics.PhysicsPanel;
//import automenta.spacegraph.physics.PhysicsController;
//import automenta.spacegraph.physics.PhysicsApp;
//import automenta.spacegraph.swing.SwingWindow;
//import com.bulletphysics.demos.opengl.GLDebugDrawer;
//import java.awt.BorderLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.swing.BoxLayout;
//import javax.swing.JButton;
//import javax.swing.JPanel;
//
///**
// *
// * @author seh
// */
//public class RunPhysicsDemos extends JPanel {
//
//    private PhysicsPanel shownPhysicsPanel = null;
//
//    public RunPhysicsDemos() {
//        super(new BorderLayout());
//
//        JPanel sidePanel = new JPanel();
//        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
//
//        add(sidePanel, BorderLayout.WEST);
//
//        for (final Class<? extends PhysicsApp> pa : getDemos()) {
//            String label = pa.getSimpleName();
//            JButton b = new JButton(label);
//            sidePanel.add(b);
//            b.addActionListener(new ActionListener() {
//                @Override public void actionPerformed(ActionEvent e) {
//                    showDemo(pa);
//                }
//            });
//        }
//
//    }
//
//    public void showDemo(Class<? extends PhysicsApp> pa) {
//        if (shownPhysicsPanel!=null) {
//            remove(shownPhysicsPanel);
//        }
//
//        shownPhysicsPanel = new PhysicsPanel();
//
//        PhysicsApp p;
//        try {
//            p = pa.newInstance();
//            p.init(shownPhysicsPanel);
//            p.getDynamicsWorld().setDebugDrawer(new GLDebugDrawer(shownPhysicsPanel));
//            new PhysicsController(shownPhysicsPanel, p);
//        } catch (InstantiationException ex) {
//            Logger.getLogger(RunPhysicsDemos.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(RunPhysicsDemos.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        add(shownPhysicsPanel, BorderLayout.CENTER);
//    }
//
//    public List<Class<? extends PhysicsApp>> getDemos() {
//        List<Class<? extends PhysicsApp>> pa = new LinkedList();
//        pa.add(CubesShootApp.class);
//        pa.add(ZeroGravityCubes.class);
//        return pa;
//    }
//
//
//    public static void main(String[] args) {
//        new SwingWindow(new RunPhysicsDemos(), 900, 600, true);
//    }
//
//}
