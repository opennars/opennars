///*
// * NARS.java
// *
// * Copyright (C) 2008  Pei Wang
// *
// * This file is part of Open-NARS.
// *
// * Open-NARS is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 2 of the License, or
// * (at your option) any later version.
// *
// * Open-NARS is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
// */
//package nars.gui;
//
//import automenta.vivisect.swing.NWindow;
//import nars.NAR;
//import nars.Video;
//import nars.nar.Default.CommandLineNARBuilder;
//
//import javax.management.MBeanServer;
//import javax.management.ObjectName;
//import javax.swing.*;
//import java.awt.*;
//import java.lang.management.ManagementFactory;
//
///**
// * The main Swing GUI class of the open-nars project.
// * Creates default Swing GUI windows to operate a NAR.
// * May need to first call Video.themeInvert() to display controls correctly
// */
//public class NARSwing /*extends NARControlPanel*/ {
//
//
//    static {
//        Video.themeInvert();
//    }
//
//    //public final NWindow mainWindow;
//
//    public NARSwing(NAR nar) {
//        this(nar, true);
//    }
//
//    public NARSwing(NAR nar, boolean logPanel) {
////        super(nar);
////
////        mainWindow = new NWindow(NAR.VERSION, this);
////        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////        mainWindow.setBounds(10, 10, 270, 650);
////        mainWindow.setVisible(true);
//
//
//
//
//        //TEMPORARY
//        /*SoundEngineTestPanel soundEngineTestPanel = new SoundEngineTestPanel(nar);
//        new NWindow("Sound Test", soundEngineTestPanel).show(500,400);
//        try {
//            new ConceptSonification(nar, soundEngineTestPanel.sound);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }*/
//
//        //TEMPORARY
//        //new NWindow("Tasks", new TaskTree(nar)).show(300, 400);
//
//
//        //TEMPORARY
//        //new Window("Plugins", new PluginPanel(nar)).show(300, 400);
//
//
//        if (logPanel) {
//            NWindow nw = new NWindow("I/O", new ConsolePanel(nar));
//            nw.setBounds(mainWindow.getX() + mainWindow.getWidth(), mainWindow.getY(), 800, 650);
//            nw.setVisible(true);
//
//
//
//        }
//        else {
//
//            //new TextOutput(nar, System.out);
//
//            //new Log4JOutput(nar, false);
//        }
//
//
//        //new TaskGraphVis(nar).newWindow().show(900,700,true);
//
//
////        Window outputWindow = new Window("Activity", new MultiOutputPanel(swing.narControls));
////        outputWindow.setLocation(swing.mainWindow.getLocation().x + swing.mainWindow.getWidth(), swing.mainWindow.getLocation().y);        outputWindow.setSize(800, 400);
////        outputWindow.setVisible(true);
//
//
//
//    }
//
//
//    /**
//     * The entry point of the standalone application.
//     * <p>
//     * Create an instance of the class
//     *
//     * @param args optional argument used : one addInput file, possibly followed by
// --silence <integer>
//     */
//    public static void main(String args[]) {
//
////        Video.themeInvert();
////        NAR nar = new NAR(new CommandLineNARBuilder(args));
////
////        NARSwing swing = new NARSwing(nar);
////
//////        if (args.length > 0
//////                && CommandLineNARBuilder.isReallyFile(args[0])) {
//////
//////            try {
//////                nar.addInput( new File(args[0] ) );
//////            } catch (IOException ex) {
//////                ex.printStackTrace();
//////            }
//////        }
////
////        /*
////        if (args.length > 1)
////            swing.nar.start(0);
////            */
//
//    }
//
//
//
//
//
//
////    static {
////        try {
////            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
////
////            UIManager.setLookAndFeel(new MetalLookAndFeel());
////            //UIManager.setLookAndFeel(new GTKLookAndFeel());
////
////            /*
////            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
////                System.out.println(info + " " + info.getName());
////                if ("Nimbus".equals(info.getName())) {
////                    UIManager.setLookAndFeel(info.getClassName());
////                    break;
////                }
////            }*/
////
////        } catch (Exception e) {
////            // If Nimbus is not available, you can set the GUI to another look and feel.
////        }
////    }
//
//    /**
//     * Color for the background of the main window
//     */
//    static final Color MAIN_WINDOW_COLOR = new Color( 172,170,194);
//    /**
//     * Color for the background of the windows with unique instantiation
//     */
//    static final Color SINGLE_WINDOW_COLOR = new Color(213,212,223);
//    /**
//     * Color for the background of the windows with multiple instantiations
//     */
//    static final Color MULTIPLE_WINDOW_COLOR = new Color(34,102,102);
//    /**
//     * Color for the background of the text components that are read-only
//     */
//    static final Color DISPLAY_BACKGROUND_COLOR = new Color(240,240,240);
//    /**
//     * Color for the background of the text components that are being saved into
//     * a file
//     */
//    static final Color SAVING_BACKGROUND_COLOR = new Color(230,230,230);
//
////    /**
////     * Font for NARS GUI
////     */
////
////    /**
////     * Message for unimplemented functions
////     */
////    public static final String UNAVAILABLE = "\n Not implemented in this version.";
////    public static final String ON_LABEL = "On";
////    public static final String OFF_LABEL = "Off";
////
////    public static interface NARManagerMBean {
////        public int getNumConcepts();
////
////    }
////
////    public static class NARManager implements NARManagerMBean {
////        private final NAR nar;
////
////        public NARManager(NAR n) {
////            this.nar = n;
////        }
////
////        @Override
////        public int getNumConcepts() {
////            return nar.memory.concepts.size();
////        }
////
////    }
//
////    public void enableJMX() throws Exception  {
////
////        /*
////         *java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1617 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
////         */
////        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
////        ObjectName name = new ObjectName("nar:type=NAR"/* + this.nar.getClass().getSimpleName() + "_" + this.nar.hashCode()*/);
////
////        mbs.registerMBean(new NARManager(nar), name);
////
////        System.out.println("JMX Enabled:\n" + name.toString() + "\n" + mbs.toString() + "\n");
////    }
//
// }
