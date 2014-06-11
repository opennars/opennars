
/*
 * Reasoner.java
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
package nars.main;

import javax.swing.SwingUtilities;

import nars.gui.InputWindow;
import nars.gui.MainWindow;
import nars.main_nogui.NAR;

/**
 * A NARS Reasoner has its memory, I/O channels, and internal clock.
 * <p>
 * Create static main window and input channel, reset memory, and manage system
 * clock.
 */
public class Reasoner extends NAR {

    /**
     * The unique main window
     */
    public final MainWindow mainWindow;
    /**
     * Input experience from a window
     */
    private InputWindow inputWindow;

    /**
     * Start the initial windows and memory. Called from NARS only.
     *
     * @param name The name of the reasoner
     */
    Reasoner(String name) {
        super();
        this.name = name;
        inputWindow = new InputWindow(this, name);
        mainWindow = new MainWindow(this, name);        
        outputChannels.add(mainWindow);
        mainWindow.setVisible(true);
    }


    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public InputWindow getInputWindow() {
        return inputWindow;
    }

    @Override
    public long updateTimer() {
        return mainWindow.updateTimer();
    }

    @Override
    public void initTimer() {
        mainWindow.initTimer();
    }

    @Override
    public void tickTimer() {
        mainWindow.tickTimer();
    }

    @Override
    public long getSystemClock() {
        return mainWindow.getTimer();
    }
}
