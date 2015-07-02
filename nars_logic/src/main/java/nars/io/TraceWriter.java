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
package nars.io;

import nars.Events;
import nars.Events.OUT;
import nars.Events.TaskRemove;
import nars.NAR;
import nars.clock.Clock;
import nars.concept.Concept;
import nars.event.MemoryReaction;
import nars.task.Task;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Inference log, which record input/output of each logic step interface
 * with 1 implementation: GUI ( batch not implemented )
 */
public class TraceWriter extends MemoryReaction {


    public static interface LogOutput {
        public void traceAppend(Class channel, String msg);
    }
    
    private final List<LogOutput> outputs = new CopyOnWriteArrayList<>();

    public TraceWriter(NAR n) {
        this(n, true);
    }
    public TraceWriter(NAR n, boolean active) {
        super(n, active);
    }

    public TraceWriter(NAR n, PrintStream p) {
        super(n, true);
        addOutput(p);
    }
    
    public TraceWriter(NAR n, LogOutput l) {
        super(n, true);
        addOutput(l);        
    }
    
    public LogOutput addOutput(final LogOutput l) {
        outputs.add(l);
        return l;
    }
    
    public PrintStreamLogOutput addOutput(final PrintStream p) {
        PrintStreamLogOutput r = new PrintStreamLogOutput(p);
        addOutput(r);
        return r;
    }

    public static class PrintStreamLogOutput implements LogOutput {

        private final PrintStream p;

        public PrintStreamLogOutput(PrintStream p) {
            this.p = p;
        }

        @Override
        public void traceAppend(final Class channel, final String s) {
            p.print(channel.getSimpleName());
            p.print(": ");
            p.println(s);
        }
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
        int n = outputs.size();
        for (int i = 0; i < n; i++) {
            final LogOutput o = outputs.get(i);
            o.traceAppend(channel, s);
        }
    }


    

    @Override
    public void onCycleStart(long clock) {
        output(Clock.class, clock);
    }

    @Override
    public void onCycleEnd(long clock) {
        
    }

    @Override
    public void onTaskAdd(Task task) {
        output(OUT.class, task, task.getHistory());
    }

    @Override
    public void onTaskRemove(Task task, String reason) {        
        output(TaskRemove.class, reason, task);
    }
    
    @Override
    public void onConceptActive(Concept concept) {
        output(Events.ConceptActive.class, concept);
    }    
    
    
}
