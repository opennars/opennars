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
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import nars.core.NAR;
import nars.core.build.Default.CommandLineNARBuilder;
import nars.io.TextInput;
import nars.io.TextOutput;

/**
 * The main Swing GUI class of the open-nars project.  
 * Creates default Swing GUI windows to operate a NAR.
 */
public class NARSwing  {

    




    
    /*static {
        System.setProperty("sun.java2d.opengl","True");        
    }*/

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
    }

    public final NAR nar;
    private final NWindow mainWindow;
    private final NARControls controls;

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
        themeInvert();
          
        NAR nar = NAR.build(new CommandLineNARBuilder(args));
        
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

    public final static float hashFloat(final int h) {
        //return ((float)h) / (((float)Integer.MAX_VALUE) - ((float)Integer.MIN_VALUE));
        return ((float)(h%256)) / (256.0f);
    }

    public final static Color getColor(final String s, final float saturation, final float brightness) {             return getColor(s.hashCode(), saturation, brightness);
    }
    public final static Color getColor(int hashCode, float saturation, float brightness) {
        float hue = hashFloat(hashCode);
        return Color.getHSBColor(hue, saturation, brightness);        
    }
    public final static Color getColor(final Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(255.0 * alpha));
    }
    public final static Color getColor(final String s, float sat, float bright, float alpha) {
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
