//package nars.testchamber.particle;
//
//import javax.swing.*;
//import java.awt.*;
//
//// TODO: make resizeable when paused (have getPaused method in renderClass
//// and if true setUndecorated(false);, and vice versa
//
//public class ParticleSystem_v7 extends Canvas {
//
//	public static final int WIDTH = 1280;
//	public static final int HEIGHT = 720;
//	public static final int TICK = 16;
//
//	public boolean paused, quit = false;
//
//	{
//
//		JFrame frame = new JFrame("");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setSize(WIDTH, HEIGHT);
//		frame.setUndecorated(false);
//		frame.setResizable(false);
//		frame.setFocusable(true);
//
//		RenderClass_v7 ren = new RenderClass_v7(WIDTH, HEIGHT);
//		frame.add(ren);
//
//		frame.setVisible(true);
//
//		Thread runThread = new Thread(() -> {
//            //noinspection InfiniteLoopStatement
//            while (true) {
//    long time = System.currentTimeMillis();
//
//    paused = ren.getPaused();
//
//    if( !paused ){
//        //ren.tick();
//        //frame.setOpacity(1f);
//        ren.repaint();
//    } else {
//        //frame.setOpacity(.5f);
//    }
//
//
//    quit = ren.getQuit();
//
//    if( quit ){
//        frame.dispose();
//    }
//
//
//    long endtime = System.currentTimeMillis();
//    try {
//        Thread.sleep( - (endtime - time));
//    } catch (Exception e) {
//        // System.out.println("cannot sleep");
//    }
//}
//});
//
//		runThread.start();
//
//	}
//
//        public static void main(String[] args) { new ParticleSystem_v7(); }
//
// }
