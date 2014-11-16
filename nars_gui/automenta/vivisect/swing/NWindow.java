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
package automenta.vivisect.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * Specify shared properties of NARS windows
 */
public class NWindow extends JFrame {
    //http://paletton.com/#uid=70u0u0kllllaFw0g0qFqFg0w0aF
    
    //static final Font NarsFont = new Font("Arial", Font.PLAIN, 13);
    

    /**
     * Default constructor
     */
    public NWindow() {
        this(" ");
    }

    /**
     * Constructor with title and font setting
     *
     * @param title The title displayed by the window
     */
    public NWindow(String title) {
        super(title);
        //setFont(NarsFont);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }                        
        });
    }
    
    public NWindow(String title, Component component) {
        this(title);
        getContentPane().add(component);
    }
    
    protected void close() {
        
    }
    
    
    public void show(int w, int h) {
        setSize(w, h);
        setVisible(true);
    }
    
    public void show(int w, int h, boolean exitOnClose) {
        show(w, h);
        if (exitOnClose)
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);                
    }


}