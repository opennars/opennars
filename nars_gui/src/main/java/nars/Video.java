/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.InputStream;

/**
 * Utility class for video components
 */
@SuppressWarnings("HardcodedFileSeparator")
public enum Video {
    ;
    public static Font monofont;
    public static final Color transparent = new Color(0,0,0,10);
    public static Font FontAwesome;

    //System.out.println(Files.list(Paths.get(getClass().getResource("/").toURI())).collect(Collectors.toList()) );


    static {
        Object e = System.getProperty("sun.java2d.opengl");
        System.err.println("Java Swing OpenGL enabled: " + e);
        if ((e == null) || ("false".equals(e.toString().toLowerCase()))) {
            System.err.println("  To enable, add command line parameter: -Dsun.java2d.opengl=True");
            System.err.println("  Your system (not the JDK) is likely misconfigured if Java OpenGL pipeline malfunctions");
            System.err.println("  For more information: http://docs.oracle.com/javase/7/docs/technotes/guides/2d/new_features.html");
            System.err.println("  Please do not waste seconds or joules with a poorly configured computer!");
        }
    }


    public static void themeInvert() {
        //http://alvinalexander.com/java/java-swing-uimanager-defaults
        Color bgColor = new Color(0,0,0);

        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.background", Color.DARK_GRAY);
        UIManager.put("Button.border", new EmptyBorder(4,8,4,8));
        UIManager.put("ToggleButton.border", new EmptyBorder(4,8,4,8));
        UIManager.put("ScrollPane.border", new EmptyBorder(1,1,1,1));
        UIManager.put("SplitPane.border", new EmptyBorder(1,1,1,1));
        UIManager.put("TextEdit.border", new EmptyBorder(1,1,1,1));
        UIManager.put("TextArea.border", new EmptyBorder(1,1,1,1));
        UIManager.put("TextField.border", new EmptyBorder(1,1,1,1));

        UIManager.put("Label.foreground", Color.WHITE);

        UIManager.put("Tree.background", bgColor);
        UIManager.put("Tree.foreground", Color.BLACK);
        UIManager.put("Tree.textForeground", Color.WHITE);
        UIManager.put("Tree.textBackground", bgColor);
        UIManager.put("TextPane.background", bgColor);
        UIManager.put("TextPane.foreground", Color.WHITE);
        UIManager.put("TextEdit.background", bgColor);
        UIManager.put("TextEdit.foreground", Color.WHITE);

        UIManager.put("TextArea.background", bgColor);
        UIManager.put("SplitPane.background", bgColor);
        UIManager.put("ScrollPane.background", bgColor);

        UIManager.put("PopupMenu.background", bgColor);
        UIManager.put("PopupMenu.foreground", Color.WHITE);

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
        UIManager.put("Panel.background", bgColor);
        UIManager.put("ScrollBar.opaque", false);
        UIManager.put("ScrollBar.background", bgColor);
        UIManager.put("ScrollBar.border", new EmptyBorder(1,1,1,1));

        UIManager.put("Table.background", bgColor);
        UIManager.put("Table.foreground", Color.WHITE);
        UIManager.put("TableHeader.background", bgColor);
        UIManager.put("TableHeader.foreground", Color.ORANGE);
    }



    static {
        Video.monofont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
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


    static {
        InputStream is = FontAwesomeIconView.class.getResourceAsStream("/de/jensd/fx/glyphs/fontawesome/fontawesome-webfont.ttf");
        try {
            FontAwesome = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(18.0f);
        } catch (Exception e) {
            e.printStackTrace();
            FontAwesome = monofont;
        }
    }

    
  public static int color(int x, int y, int z, int a) {
    
      if (a > 255) a = 255; else if (a < 0) a = 0;
      if (x > 255) x = 255; else if (x < 0) x = 0;
      if (y > 255) y = 255; else if (y < 0) y = 0;
      if (z > 255) z = 255; else if (z < 0) z = 0;

      return (a << 24) | (x << 16) | (y << 8) | z;
  }


  public static int color(float x, float y, float z, float a) {
      if (a > 255) a = 255; else if (a < 0) a = 0;
      if (x > 255) x = 255; else if (x < 0) x = 0;
      if (y > 255) y = 255; else if (y < 0) y = 0;
      if (z > 255) z = 255; else if (z < 0) z = 0;

      return ((int)a << 24) | ((int)x << 16) | ((int)y << 8) | (int)z;
  }
  
    public static int getColor(String s, float alpha) {
        float hue = (((float)s.hashCode()) / Integer.MAX_VALUE);
        return colorHSB(hue,0.8f,0.9f,alpha);
    }    

    public static int getColor(Class c) {
        //float hue = (((float)c.hashCode()) / Integer.MAX_VALUE);
        float hue = hashFloat(c.hashCode());
        return color(hue,0.8f,0.9f, 1.0f);
    }
    
    public static int getColor(String s) {
        float hue = hashFloat(s.hashCode()); //(((float)s.hashCode()) / Integer.MAX_VALUE);
        return color(hue,0.8f,0.9f, 1.0f);
    }

    public static Font fontMono(float size) {
        return monofont.deriveFont(size);
    }

    public static float hashFloat(int h) {
        int max = 32;
        return Math.abs(h % max) / ((float)max);

        //return (h) / (((float) Integer.MAX_VALUE) - ((float) Integer.MIN_VALUE));
    }

    @Deprecated public static Color getColor(String s, float saturation, float brightness) {
        return getColor(s.hashCode(), saturation, brightness);
    }

    @Deprecated public static Color getColor(int hashCode, float saturation, float brightness) {
        float hue = hashFloat(hashCode);
        return Color.getHSBColor(hue, saturation, brightness);
    }
    @Deprecated public static Color getColorA(int hashCode, float saturation, float brightness, float alpha) {
        float hue = hashFloat(hashCode);
        return getColor(Color.getHSBColor(hue, saturation, brightness), alpha);
    }

    public static int getColor(int hashCode, float saturation, float brightness, float alpha) {
        return colorHSB(hashCode, saturation, brightness, alpha);
    }

    @Deprecated public static Color getColor(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255.0 * alpha));
    }

    public static int getColor(String s, float sat, float bright, float alpha) {
        return colorHSB( hashFloat(s.hashCode()), sat, bright, alpha);
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

    public static int colorHSB(float hue, float saturation, float brightness, float alpha) {
        return Color.HSBtoRGB(hue, saturation, brightness) & 0x00ffffff | ((int)(255.0f *alpha) << 24);
    }
    public static int colorHSB(float hue, float saturation, float brightness) {
        return Color.HSBtoRGB(hue, saturation, brightness);
    }
  
}
