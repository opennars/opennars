/*
 * InferenceLogger.java
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

import nars.gui.output.InferenceWindow;
import java.awt.FileDialog;
import java.io.*;
import nars.entity.Concept;
import nars.entity.Task;

/**
 * Inference log, which record input/output of each inference step interface
 * with 1 implementation: GUI ( batch not implemented )
 */
public class InferenceLogger implements nars.inference.InferenceRecorder {

    /**
     * the display window
     */
    private InferenceWindow window = new InferenceWindow(this);
    /**
     * whether to display
     */
    private boolean isReporting = false;
    /**
     * the log file
     */
    private PrintWriter logFile = null;

    @Override
    public void init() {
        window.clear();
    }

    @Override
    public void show() {
        window.setVisible(true);
    }

    @Override
    public boolean isActive() {
        return (isReporting || (logFile != null));
    }

    @Override
    public void play() {
        isReporting = true;
    }

    @Override
    public void stop() {
        isReporting = false;
    }

    @Override
    public void append(String s) {
        if (isReporting) {
            window.append(s);
        }
        if (isLogging()) {
            logFile.println(s);
        }
    }

    public void openLogFile() {
        FileDialog dialog = new FileDialog((FileDialog) null, "Inference Log", FileDialog.SAVE);
        dialog.setVisible(true);
        String directoryName = dialog.getDirectory();
        String fileName = dialog.getFile();
        try {
            logFile = new PrintWriter(new FileWriter(directoryName + fileName));
        } catch (IOException ex) {
            System.out.println("i/o error: " + ex.getMessage());
        }
        window.switchBackground();
        window.setVisible(true);
    }

    public void closeLogFile() {
        logFile.close();
        logFile = null;
        window.resetBackground();
    }

    public boolean isLogging() {
        return (logFile != null);
    }

    

    @Override
    public void preCycle(long clock) {
        append("\n --- " + clock + " ---\n");
    }

    @Override
    public void postCycle(long clock) {
        append("\n");
    }

    @Override
    public void onTaskAdd(Task task, String reason) {
        append("Task Added (" + reason + "): " + task + "\n");
    }

    @Override
    public void onTaskRemove(Task task, String reason) {
        append("Task Removed (" + reason + "): " + task + "\n");
    }
    
    @Override
    public void onConceptNew(Concept concept) {
        append("Concept Created: " + concept + "\n");
    }    
    
    
}
