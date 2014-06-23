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

import java.io.IOException;
import nars.core.CommandLineParameters;
import nars.core.NAR;
import nars.core.NARRun;
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


    
    /**
     * The unique main window
     */
    public final NARWindow mainWindow;


    public NARSwing() {
        super();
        mainWindow = new NARWindow(this, INFO);        
        outputChannels.add(mainWindow);
        mainWindow.setVisible(true);
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
        NARRun.setStandAlone(true);
        NARSwing nars = new NARSwing();
        nars.init(args);
        if (args.length > 1)
            nars.start(0);
        
        
        //temporary
        new MemoryView(nars);
    }

    /**
     * TODO multiple files
     */
    public void init(String[] args) {
        
        if (args.length > 0
                && CommandLineParameters.isReallyFile(args[0])) {

            try {
                mainWindow.loadFile(args[0]);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        CommandLineParameters.decode(args, this);
    }


    public NARWindow getMainWindow() {
        return mainWindow;
    }

    void evaluate(String input) {
        new TextInput(this, input);
        run(0, false);
    }




}
