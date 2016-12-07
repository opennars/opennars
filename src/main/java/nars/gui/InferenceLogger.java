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

import nars.NAR;
import nars.entity.Concept;
import nars.entity.Task;
import nars.gui.util.MemoryObserver;
import nars.util.Events.ConceptNew;
import nars.util.Events.TaskAdd;
import nars.util.Events.TaskRemove;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Inference log, which record input/output of each inference step interface
 * with 1 implementation: GUI ( batch not implemented )
 */
public class InferenceLogger extends MemoryObserver {


    public static interface LogOutput {
        public void traceAppend(Class channel, String msg);
    }
    
    private final List<LogOutput> outputs = new CopyOnWriteArrayList<>();

    public InferenceLogger(NAR n) {
        super(n, true);
    }
    
    public InferenceLogger(NAR n, PrintStream p) {
        super(n, true);
        addOutput(p);
    }
    
    public InferenceLogger(NAR n, LogOutput l) {
        super(n, true);
        addOutput(l);        
    }
    
    public void addOutput(LogOutput l) {
        outputs.add(l);
    }
    
    public void addOutput(PrintStream p) {
        //TODO removeOutput(p)
        
        addOutput(new LogOutput() {
            @Override public void traceAppend(Class channel, String s) {
                p.println(channel + ": " + s);
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
    public void output(Class channel, Object... args) {
        if (outputs.isEmpty())
            return;
        
        String s = args.length == 1 ? args[0].toString() : Arrays.toString(args);
        for (final LogOutput o : outputs) {            
            o.traceAppend(channel, s);
        }
    }


    public enum Timing {
        Iterative
    }

    @Override
    public void onCycleStart(long clock) {
        output(Timing.class, clock);
    }

    @Override
    public void onCycleEnd(long clock) {
        
    }

    @Override
    public void onTaskAdd(Task task, String reason) {
        output(TaskAdd.class, reason, task);
    }

    @Override
    public void onTaskRemove(Task task, String reason) {        
        output(TaskRemove.class, reason, task);
    }
    
    @Override
    public void onConceptAdd(Concept concept) {        
        output(ConceptNew.class, concept);
    }    
    
    
}
