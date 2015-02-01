/*
 * NARSwing.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARSwing.
 *
 * Open-NARSwing is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARSwing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.gui;

import automenta.vivisect.swing.NWindow;
import nars.core.NAR;
import nars.build.Default.CommandLineNARBuilder;
import nars.control.experimental.AntCore;
import nars.io.TextInput;
import nars.io.TextOutput;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * The main Swing GUI class of the open-nars project.  
 * Creates default Swing GUI windows to operate a NAR.
 */
public class NARSwing  {

    //System.out.println(Files.list(Paths.get(getClass().getResource("/").toURI())).collect(Collectors.toList()) );


    static {
        System.setProperty("sun.java2d.opengl","False");        
    }

    public static void themeInvert() {
        //http://alvinalexander.com/java/java-swing-uimanager-defaults
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.background", Color.DARK_GRAY);
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("Button.border", new EmptyBorder(4,8,4,8));
        UIManager.put("ToggleButton.border", new EmptyBorder(4,8,4,8));
        UIManager.put("ScrollPane.border", new EmptyBorder(1,1,1,1));
        UIManager.put("SplitPane.border", new EmptyBorder(1,1,1,1));
        UIManager.put("TextEdit.border", new EmptyBorder(1,1,1,1));
        UIManager.put("TextArea.border", new EmptyBorder(1,1,1,1));
        UIManager.put("TextField.border", new EmptyBorder(1,1,1,1));

        UIManager.put("Label.foreground", Color.WHITE);

        UIManager.put("Tree.background", Color.BLACK);
        UIManager.put("Tree.foreground", Color.BLACK);
        UIManager.put("Tree.textForeground", Color.WHITE);
        UIManager.put("Tree.textBackground", Color.BLACK);
        UIManager.put("TextPane.background", Color.BLACK);
        UIManager.put("TextPane.foreground", Color.WHITE);
        UIManager.put("TextEdit.background", Color.BLACK);
        UIManager.put("TextEdit.foreground", Color.WHITE);
        UIManager.put("TextArea.background", Color.BLACK);
        UIManager.put("TextArea.foreground", Color.WHITE);

        UIManager.put("TextPane.border", new EmptyBorder(1,1,1,1));
        UIManager.put("TextPane.border", new EmptyBorder(1,1,1,1));
        UIManager.put("Panel.border", new EmptyBorder(1,1,1,1));
        UIManager.put("Button.select", Color.GREEN);
        UIManager.put("Button.highlight", Color.YELLOW);
        UIManager.put("ToggleButton.foreground", Color.WHITE);
        UIManager.put("ToggleButton.background", Color.DARK_GRAY);
        UIManager.put("ToggleButton.select", Color.GRAY);
        //UIManager.put("ToggleButton.border", Color.BLUE);
        //UIManager.put("ToggleButton.light", Color.DARK_GRAY);
        UIManager.put("Button.select", Color.ORANGE);
        UIManager.put("Button.opaque", false);
        UIManager.put("Panel.opaque", false);
        UIManager.put("ScrollBar.opaque", false);
        UIManager.put("ScrollBar.background", Color.BLACK);
        UIManager.put("ScrollBar.border", new EmptyBorder(1,1,1,1));
        
        UIManager.put("Table.background", Color.BLACK);
        UIManager.put("Table.foreground", Color.WHITE);
        UIManager.put("TableHeader.background", Color.BLACK);
        UIManager.put("TableHeader.foreground", Color.ORANGE);
    }

    public final NAR nar;
    public final NWindow mainWindow;
    public final NARControls controls;

    public NARSwing(NAR nar) {
        this(nar, true);
    }

    public NARSwing(NAR nar, boolean logPanel) {
        super();
                
        this.nar = nar;                
        
        controls = new NARControls(nar);        
        mainWindow = new NWindow(NAR.VERSION, controls);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setBounds(10, 10, 270, 650);
        mainWindow.setVisible(true);
        
        
        //TEMPORARY
        //new Window("Plugins", new PluginPanel(nar)).show(300, 400);
        
        
        if (logPanel) {
            NWindow nw = new NWindow("I/O", new ConsolePanel(controls));
            nw.setBounds(mainWindow.getX() + mainWindow.getWidth(), mainWindow.getY(), 800, 650);
            nw.setVisible(true);
            
            

        }
        else {
            new TextOutput(nar, System.out);
            //new Log4JOutput(nar, false);            
        }
        
                
//        Window outputWindow = new Window("Activity", new MultiOutputPanel(swing.narControls));
//        outputWindow.setLocation(swing.mainWindow.getLocation().x + swing.mainWindow.getWidth(), swing.mainWindow.getLocation().y);        outputWindow.setSize(800, 400);
//        outputWindow.setVisible(true);

                
        
    }

    
    /**
     * The entry point of the standalone application.
     * <p>
     * Create an instance of the class
     *
     * @param args optional argument used : one addInput file, possibly followed by
 --silence <integer>
     */
    public static void main(String args[]) {
        themeInvert();
          
        NAR nar = new NAR(new CommandLineNARBuilder(args));
        
        
        
        NARSwing swing = new NARSwing(nar);

        
        
        if (args.length > 0
                && CommandLineNARBuilder.isReallyFile(args[0])) {

            try {
                nar.addInput(new TextInput(new File(args[0])));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        
        if (args.length > 1)
            swing.nar.start(0);
                
    }




 
    
//    static {
//        try {
//            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
//            
//            UIManager.setLookAndFeel(new MetalLookAndFeel());
//            //UIManager.setLookAndFeel(new GTKLookAndFeel());
//
//            /*
//            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                System.out.println(info + " " + info.getName());
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }*/
//            
//        } catch (Exception e) {
//            // If Nimbus is not available, you can set the GUI to another look and feel.
//        }
//    }

    /**
     * Color for the background of the main window
     */
    static final Color MAIN_WINDOW_COLOR = new Color( 172,170,194);
    /**
     * Color for the background of the windows with unique instantiation
     */
    static final Color SINGLE_WINDOW_COLOR = new Color(213,212,223);
    /**
     * Color for the background of the windows with multiple instantiations
     */
    static final Color MULTIPLE_WINDOW_COLOR = new Color(34,102,102);
    /**
     * Color for the background of the text components that are read-only
     */
    static final Color DISPLAY_BACKGROUND_COLOR = new Color(240,240,240);
    /**
     * Color for the background of the text components that are being saved into
     * a file
     */
    static final Color SAVING_BACKGROUND_COLOR = new Color(230,230,230);
    
    /**
     * Font for NARS GUI
     */
    
    /**
     * Message for unimplemented functions
     */
    public static final String UNAVAILABLE = "\n Not implemented in this version.";
    public static final String ON_LABEL = "On";
    public static final String OFF_LABEL = "Off";

    public static interface NARManagerMBean {
        public int getNumConcepts();

    }
    
    public static class NARManager implements NARManagerMBean {
        private final NAR nar;

        public NARManager(NAR n) {
            this.nar = n;
        }

        @Override
        public int getNumConcepts() {
            return ((AntCore)nar.memory.concepts).concepts.size();
        }
        
    }
    
    public void enableJMX() throws Exception  {

        /*
         *java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1617 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
         */
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
        ObjectName name = new ObjectName("nar:type=NAR"/* + this.nar.getClass().getSimpleName() + "_" + this.nar.hashCode()*/); 
        
        mbs.registerMBean(new NARManager(nar), name);
        
        System.out.println("JMX Enabled:\n" + name.toString() + "\n" + mbs.toString() + "\n");
    }
    
}
