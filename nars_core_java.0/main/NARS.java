/*
 * NARS.java
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

import java.applet.Applet;

import nars.io.ExperienceReader;
import nars.main_nogui.NARSBatch;
import nars.main_nogui.ReasonerBatch;

/**
 * The main class of the project.
 * <p>
 * Define an application with full functionality and an applet with partial functionality.
 * <p>
 * Manage the internal working thread. Communicate with Reasoner only.
 */
public class NARS extends Applet implements Runnable {

    /**
     * The information about the version and date of the project.
     */
    public static final String INFO =
            "     Open-NARS     Version 1.5.2     March 2013  \n";
    /**
     * The project websites.
     */
    public static final String WEBSITE =
            " Open-NARS website:  http://code.google.com/p/open-nars/ \n" +
            "      NARS website:  http://sites.google.com/site/narswang/ ";
    /**
     * The internal working thread of the system.
     */
    Thread narsThread = null;
    /**
     * The reasoner
     */
    ReasonerBatch reasoner;

    /* Application-only code */
    /**
     * The entry point of the standalone application.
     * <p>
     * Create an instance of the class, then run the init and start methods.
     * @param args optional argument used : one input file
     */
    public static void main(String args[]) {
        NARSBatch.setStandAlone(true);
        NARS nars = new NARS();
        nars.init(args);
        nars.start();
    }

    public void init(String[] args) {
        init();
        if (args.length > 0) {
            ExperienceReader experienceReader = new ExperienceReader(reasoner);
            experienceReader.openLoadFile(args[0]);
        }
    }

    /* Applet/Application code */
    /**
     * Initialize the system at the control center.<p>
     * Can instantiate multiple reasoners
     */
    @Override
    public void init() {
        reasoner = new Reasoner("NARS Reasoner");
    }

    /**
     * Start the thread if necessary, called when the page containing the applet first appears on the screen.
     */
    @Override
    public void start() {
        if (narsThread == null) {
            narsThread = new Thread(this, "Inference" );
            narsThread.start();
        }
    }

    /**
     * Called when the page containing the applet is no longer on the screen.
     */
    @Override
    public void stop() {
        narsThread = null;
    }

    /* Implementing the Runnable Interface */
    /**
     * Repeatedly execute NARS working cycle. This method is called when the Runnable's thread is started.
     */
    @Override
    public void run() {
        Thread thisThread = Thread.currentThread();
        while (narsThread == thisThread) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
            try {
            	// NOTE: try/catch not necessary for input errors , but may be useful for other troubles
				reasoner.tick();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }

    /**
     * Provide system information for the applet.
     * @return The string containing the information about the applet.
     */
    @Override
    public String getAppletInfo() {
        return INFO;
    }
}
