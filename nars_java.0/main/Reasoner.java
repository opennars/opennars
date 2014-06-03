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
import nars.main_nogui.ReasonerBatch;

/**
 * A NARS Reasoner has its memory, I/O channels, and internal clock.
 * <p>
 * Create static main window and input channel, reset memory, and manage system clock.
 */
public class Reasoner extends ReasonerBatch {

	/** The unique main window */
    MainWindow mainWindow;
    /** Input experience from a window */
    private InputWindow inputWindow;
    /**
     * Start the initial windows and memory. Called from NARS only.
     * @param name The name of the reasoner
     */
    Reasoner(String name) {
    	super();
        this.name = name;
        mainWindow = new MainWindow(this, name);
        inputWindow = new InputWindow(this, name);
        inputChannels.add(inputWindow);
        outputChannels.add(mainWindow);
        mainWindow.setVisible(true);
    }

	@Override
	public void tick() {
		final ReasonerBatch reasoner = this;
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				reasoner.doTick();
		} } );
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
	
	public long getTimer() {
		return mainWindow.getTimer();
	}
}
