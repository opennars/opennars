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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import nars.core.CommandLineArguments;
import nars.core.NAR;
import nars.gui.input.InputPanel;
import nars.gui.output.OutputLogPanel;
import nars.io.TextInput;

/**
 * The main class of the open-nars project.
 * <p>
 * Manage the internal working thread. Communicate with Reasoner only.
 */
public class NARSwing extends NAR  {

    /**
     * The information about the version and date of the project.
     */
    public static final String INFO = "Open-NARS v1.6.0";
    /**
     * The project web sites.
     */
    public static final String WEBSITE =
            " Open-NARS website:  http://code.google.com/p/open-nars/ \n"
            + "      NARS website:  http://sites.google.com/site/narswang/";


    

    public NARSwing() {
        super();
        
        NARWindow mainWindow = new NARWindow(this, INFO);        
        mainWindow.setBounds(10, 10, 270, 600);
        mainWindow.setVisible(true);
        mainWindow.setVisible(true);
        
        
        OutputLogPanel outputLog = new OutputLogPanel(this);
        Window outputWindow = new Window("Output Log", outputLog);        
        outputWindow.setLocation(mainWindow.getLocation().x + mainWindow.getWidth(), mainWindow.getLocation().y);        outputWindow.setSize(800, 400);
        outputWindow.setVisible(true);
        
        
        InputPanel inputPanel = new InputPanel(this);
        Window inputWindow = new Window("Text Input", inputPanel);
        inputWindow.setLocation(outputWindow.getLocation().x, outputWindow.getLocation().y+outputWindow.getHeight());
        inputWindow.setSize(800, 200);
        inputWindow.setVisible(true);
                
        
    }
    
    public NARSwing(String... args) {
        this();
        init(args);
    }

    
    
    /**
     * The entry point of the standalone application.
     * <p>
     * Create an instance of the class
     *
     * @param args optional argument used : one input file, possibly followed by
     * --silence <integer>
     */
    public static void main(String args[]) {
        NARSwing nars = new NARSwing();
        nars.init(args);
        if (args.length > 1)
            nars.start(0);
                
    }

    /**
     * TODO multiple files
     */
    public void init(String[] args) {
        
        if (args.length > 0
                && CommandLineArguments.isReallyFile(args[0])) {

            try {
                loadFile(args[0]);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        CommandLineArguments.decode(args, this);
    }



    public void evaluate(String input) {
        new TextInput(this, input);
        tick();
        run(0);
        
    }

    public void loadFile(String filePath) throws IOException, FileNotFoundException {
        BufferedReader r = new BufferedReader(new FileReader(filePath));
        String s;

        while ((s = r.readLine()) != null) {
            new TextInput(this, s); //TODO use a stream to avoid reallocate experiencereader
        }
    }



}
