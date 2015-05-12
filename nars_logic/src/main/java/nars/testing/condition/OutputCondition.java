/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.testing.condition;

import nars.Events;
import nars.NAR;
import nars.event.NARReaction;
import nars.nal.Task;
import nars.nal.stamp.Stamp;
import nars.narsese.InvalidInputException;
import nars.testing.TestNAR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Monitors NAR behavior for certain conditions. Used in testing and
 * analysis.
 * 
 * Parameter O is the type of object which will be remembered that can make
 * the condition true
 */
public abstract class OutputCondition extends NARReaction {
    public boolean succeeded = false;
    
    
    
    public final NAR nar;
    long successAt = Stamp.UNPERCEIVED;

    public OutputCondition(NAR nar, Class... events) {
        super(nar, events);
        this.nar = nar;
    }

    public OutputCondition(NAR nar) {
        this(nar, Events.OUT.class, Events.EXE.class, Events.Answer.class );
    }

    /** whether this is an "inverse" condition */
    public boolean isInverse() {
        return false;
    }

    @Override
    public void event(Class channel, Object... args) {
        if ((succeeded) && (!isInverse() && (!continueAfterSuccess()))) {
            return;
        }

        Object signal;
        if (channel == Events.Answer.class)
            signal = args[1]; //answer is 2nd arg, question is 1st. we are interested in comparing the answer
        else
            signal = args[0];

        if (condition(channel, signal)) {
            setTrue();
        }
    }

    protected boolean continueAfterSuccess() { return false; }

    protected void setTrue() {
        succeeded = true;

        if (successAt == Stamp.UNPERCEIVED) {
            successAt = nar.time();
            nar.emit(OutputCondition.class, this);
        }
    }

    public boolean isTrue() {
        return succeeded;
    }

    /** returns true if condition was satisfied */
    public abstract boolean condition(Class channel, Object signal);

            
    /** reads an example file line-by-line, before being processed, to extract expectations */
    public static List<OutputCondition> getConditions(NAR n, String example, int similarResultsToSave)  {
        List<OutputCondition> conditions = new ArrayList();
        String[] lines = example.split("\n");

        int cycle = 0;
        final String expectOutNotContains2 = "''outputMustNotContain('";
        final String expectInContains = "''inputMustContain('";
        final String expectOutContains = "''outputMustContain('";
        final String expectOutEmpty = "''expect.outEmpty";

        for (String s : lines) {
            s = s.trim();
            if (s.length() == 0) continue;
            
            

            if (s.indexOf(expectOutContains)==0) {

                if (!s.endsWith("')"))
                    throw new RuntimeException("invalid " + expectOutContains + " syntax: missing ending: \')");

                String match = s.substring(expectOutContains.length(), s.length() - 2); //remove ') suffix:

                try {
                    Task t = n.narsese.parseTask(match, false);
                    if (t != null)
                        conditions.add(new TaskCondition(n, Events.OUT.class, t,
                                -Stamp.UNPERCEIVED, /* to cancel it */
                                false));
                    else
                        conditions.add(new OutputContainsCondition(n, match, similarResultsToSave));
                }
                catch (InvalidInputException e) {
                    System.err.println(OutputCondition.class.getSimpleName() + ": Error parsing: " + example);
                    e.printStackTrace();
                }
            }

            else if (s.indexOf(expectInContains)==0) {
                String match = s.substring(expectInContains.length(), s.length()-2); //remove ') suffix:

                Task t = n.narsese.parseTask(match);
                if (t!=null)
                    conditions.add(new TaskCondition(n, Events.IN.class, t, cycle, false));
                else
                    throw new RuntimeException("API upgrade incomplete"); //conditions.add(new OutputContainsCondition(n, match, similarResultsToSave));

            }
            

            else if (s.indexOf(expectOutNotContains2)==0) {

                //remove ') suffix:
                String e = s.substring(expectOutNotContains2.length(), s.length()-2);                 
                conditions.add(new OutputNotContainsCondition(n, e));

            }   
            
            else if (s.indexOf(expectOutEmpty)==0) {
                conditions.add(new OutputEmptyCondition(n));
            }                
            else if (!s.startsWith("'")) {
                //skip comment lines


                try {
                    //parse sleep cycles to advance the correlated 'cycle' time
                    int sleepCycles = Integer.parseInt(s);
                    cycle += sleepCycles;
                }
                catch (NumberFormatException e) {
                    //non-sleep #, assume it is ordinary input that consumes 1 cycle to input
                    cycle++;
                }
            }

        }
        
        return conditions;
    }



    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + (succeeded ? "OK: " + getTrueReasons() : getFalseReason());
    }

    public List<Task> getTrueReasons() {
        if (!isTrue()) throw new RuntimeException(this + " is not true so has no true reasons");
        return Collections.EMPTY_LIST;
    }
    
    /** if false, a reported rule why this condition is false */
    public abstract String getFalseReason();

    /** if true, when it became true */
    public long getTrueTime() {
        return successAt;
    }


    /** calculates the "cost" of an execution according to certain evaluated condtions
     *  this is the soonest time at which all output conditions were successful.
     *  if any conditions were not successful, the cost is infinity
     * */
    public static double cost(List<OutputCondition> conditions) {
        long lastSuccess = Stamp.UNPERCEIVED;
        for (OutputCondition e : conditions) {
            if (e.getTrueTime() != Stamp.UNPERCEIVED) {
                if (lastSuccess < e.getTrueTime()) {
                    lastSuccess = e.getTrueTime();
                }
            }
        }
        if (lastSuccess != Stamp.UNPERCEIVED) {
            //score = 1.0 + 1.0 / (1+lastSuccess);
            return lastSuccess;
        }

        return Double.POSITIVE_INFINITY;
    }

    public static double cost(TestNAR testNAR) {
        return cost(testNAR.requires);
    }

    /** returns a function of the cost characterizing the optimality of the conditions
     *  monotonically increasing from -1..+1 (-1 if there were errors,
     *  0..1.0 if all successful.  limit 0 = takes forever, limit 1.0 = instantaneous
     */
    public static double score(List<OutputCondition> musts) {
        double cost = cost(musts);
        if (Double.isFinite(cost))
            return 1.0 / (1.0 + cost);
        else
            return -1;

    }
}
