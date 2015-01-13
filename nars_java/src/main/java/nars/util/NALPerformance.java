/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util;

import nars.core.NAR;
import nars.core.Parameters;
import nars.io.TextInput;
import nars.io.condition.OutputCondition;

import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author me
 */
public class NALPerformance  {
    
    static public int similarsToSave = 2;

    double score;
    private boolean success;
    private boolean error;
    public final List<OutputCondition> expects;
    private final NAR nar;
    private final String input;

    public NALPerformance(NAR n, String input) {
        this.nar = n;
        this.input = input;
        this.expects = OutputCondition.getConditions(nar, input, similarsToSave);
    }

    public void run(int maxCycles) {
        run(-1, maxCycles);
    }

    public void run(int minCycles, int maxCycles) {
        try {
            

            
            nar.addInput(new TextInput(input));

            if (minCycles == -1)
                nar.run(maxCycles);
            else
                nar.run(minCycles, maxCycles);
            
        } catch (Throwable e) {
            System.err.println(e);
            if (Parameters.DEBUG) {
                e.printStackTrace();
            }
            error = true;
        }
        score = Double.POSITIVE_INFINITY;
        success = expects.size() > 0 && (!error);
        if (success) {
            for (OutputCondition e : expects) {
                if (!e.succeeded) {
                    success = false;
                    break;
                }
            }
        }
        if (success) {
            long lastSuccess = -1;
            for (OutputCondition e : expects) {
                if (e.getTrueTime() != -1) {
                    if (lastSuccess < e.getTrueTime()) {
                        lastSuccess = e.getTrueTime();
                    }
                }
            }
            if (lastSuccess != -1) {
                //score = 1.0 + 1.0 / (1+lastSuccess);
                score = lastSuccess;
            }
        }
    }

    public double getScore() {
        return score;
    }

    public boolean getSuccess() {
        return success;
    }

    public List<OutputCondition> getExpects() {
        return expects;
    }

    private long getCycleTime() {
        return nar.memory.getCycleTime();
    }

    public void printResults(PrintStream p) {
        p.println(" @" + getCycleTime());
        for (OutputCondition e : expects) {
            p.println("  " + e);
        }
    }
    
}
