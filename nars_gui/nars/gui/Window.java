/*
 * Window.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * Specify shared properties of NARS windows
 */
public class Window extends JFrame {
    //http://paletton.com/#uid=70u0u0kllllaFw0g0qFqFg0w0aF
    
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
    //static final Font NarsFont = new Font("Arial", Font.PLAIN, 13);
    
    
    /**
     * Message for unimplemented functions
     */
    public static final String UNAVAILABLE = "\n Not implemented in this version.";
    public static final String ON_LABEL = "On";
    public static final String OFF_LABEL = "Off";

    /**
     * Default constructor
     */
    public Window() {
        this(" ");
    }

    /**
     * Constructor with title and font setting
     *
     * @param title The title displayed by the window
     */
    public Window(String title) {
        super(title);
        //setFont(NarsFont);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }                        
        });
    }
    
    public Window(String title, Component component) {
        this(title);
        getContentPane().add(component);
    }
    
    protected void close() {
        
    }
    
    public void show(int w, int h) {
        setSize(w, h);
        setVisible(true);
    }

}