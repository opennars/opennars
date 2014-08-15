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

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import nars.entity.Concept;
import nars.entity.Task;

/**
 * Inference log, which record input/output of each inference step interface
 * with 1 implementation: GUI ( batch not implemented )
 */
public class InferenceLogger implements nars.inference.InferenceRecorder {

    public static interface LogOutput {
        public void logAppend(String s);
    }
    
    private final List<LogOutput> outputs = new CopyOnWriteArrayList<LogOutput>();

    public InferenceLogger() {
    }
    
    public InferenceLogger(PrintStream p) {
        addOutput(p);
    }
    
    public void addOutput(LogOutput l) {
        outputs.add(l);
    }
    
    public void addOutput(PrintStream p) {
        //TODO removeOutput(p)
        
        addOutput(new LogOutput() {
            @Override public void logAppend(String s) {
                p.println("    " + s);
            }
        });        
    }
    
    public void removeOutput(LogOutput l) {
        outputs.remove(l);
    }

    @Override
    public boolean isActive() {
        return !outputs.isEmpty();
    }


    @Override
    public void append(String s) {
        for (LogOutput o : outputs)
            o.logAppend(s);
    }


    

    @Override
    public void onCycleStart(long clock) {
        append("@ " + clock);
    }

    @Override
    public void onCycleEnd(long clock) {
        
    }

    @Override
    public void onTaskAdd(Task task, String reason) {
        append("Task Added (" + reason + "): " + task);
    }

    @Override
    public void onTaskRemove(Task task, String reason) {
        append("Task Removed (" + reason + "): " + task);
    }
    
    @Override
    public void onConceptNew(Concept concept) {
        append("Concept Created: " + concept);
    }    
    
    
}
