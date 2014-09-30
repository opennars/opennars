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
 * along with Open-NARSwing.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder.CommandLineNARBuilder;
import nars.gui.input.TextInputPanel;
import nars.gui.output.LogPanel;
import nars.gui.output.SwingLogPanel;
import nars.io.TextInput;
import nars.io.TextOutput;

/**
 * The main Swing GUI class of the open-nars project.  
 * Creates default Swing GUI windows to operate a NAR.
 */
public class NARSwing  {

    /**
     * The information about the version and date of the project.
     */
    public static final String INFO = "Open-NARS v1.6.1";
    /**
     * The project web sites.
     */
    public static final String WEBSITE =
            " Open-NARS website:  http://code.google.com/p/open-nars/ \n"
            + "      NARS website:  http://sites.google.com/site/narswang/";


    
    static {
        System.setProperty("sun.java2d.opengl","True");        
    }


    public final NAR nar;
    private final Window mainWindow;
    private final NARControls narControls;

    public NARSwing(NAR nar) {
        this(nar, true);
    }

    public NARSwing(NAR nar, boolean logPanel) {
        super();
        
        this.nar = nar;                
        
        narControls = new NARControls(nar);        
        mainWindow = new Window(INFO, narControls);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setBounds(10, 10, 270, 600);
        mainWindow.setVisible(true);
        mainWindow.setVisible(true);
        
        //TEMPORARY
        //new Window("Plugins", new PluginPanel(nar)).show(300, 400);
        
        
        if (logPanel) {
            LogPanel outputLog = new SwingLogPanel(narControls); //new HTMLLogPanel(narControls);
            Window outputWindow = new Window("Log", outputLog);        
            outputWindow.setLocation(narControls.getLocation().x + narControls.getWidth(), narControls.getLocation().y);        outputWindow.setSize(800, 400);
            outputWindow.setVisible(true);
        }
        else {
            new TextOutput(nar, System.out);
            //new Log4JOutput(nar, false);            
        }
        
        
//        Window outputWindow = new Window("Activity", new MultiOutputPanel(swing.narControls));
//        outputWindow.setLocation(swing.mainWindow.getLocation().x + swing.mainWindow.getWidth(), swing.mainWindow.getLocation().y);        outputWindow.setSize(800, 400);
//        outputWindow.setVisible(true);
        
        
        TextInputPanel inputPanel = new TextInputPanel(nar);
        Window inputWindow = new Window("Input", inputPanel);
        inputWindow.setLocation(narControls.getLocation().x + narControls.getWidth(), narControls.getLocation().y);
        inputWindow.setSize(800, 200);
        inputWindow.setVisible(true);
        
                
        
    }


    public static Font monofont;
    static {
        monofont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        /*
        try {
            //monofont = Font.createFont(Font.TRUETYPE_FONT, NARSwing.class.getResourceAsStream("Inconsolata-Regular.ttf"));
            
            
        } catch (FontFormatException ex) {
            Logger.getLogger(NARSwing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NARSwing.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
    }
        
    
    public static Font FontAwesome;
    static {        
        try {
            FontAwesome = Font.createFont(Font.TRUETYPE_FONT, NARSwing.class.getResourceAsStream("FontAwesome.ttf")).deriveFont(Font.PLAIN, 14);
        } catch (FontFormatException ex) {
            Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
        }

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
        NAR nar = new CommandLineNARBuilder(args).build();
        
        //temporary:
        //NAR nar = new ContinuousBagNARBuilder(false).build();
        //NAR nar = new RealTimeNARBuilder(false).build();
        
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
    

    public static Color getColor(final String s, float saturation, float brightness) {            
        float hue = (((float)s.hashCode()) / Integer.MAX_VALUE);
        return Color.getHSBColor(hue, saturation, brightness);
    }
    public static Color getColor(final Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(255.0 * alpha));
    }
    public static Color getColor(final String s, float sat, float bright, float alpha) {
        return getColor(getColor(s, sat, bright), alpha);
    }
    
//    //NOT WORKING YET
//    public static Color getColor(final String s, float saturation, float brightness, float alpha) {            
//        float hue = (((float)s.hashCode()) / Integer.MAX_VALUE);
//        int a = (int)(255.0*alpha);
//        
//        int c = Color.HSBtoRGB(hue, saturation, brightness);
//        c |= (a << 24);
//        
//        return new Color(c, true);
//    }
    
    public static Font fontMono(float size) {
        return monofont.deriveFont(size);
    }



}
