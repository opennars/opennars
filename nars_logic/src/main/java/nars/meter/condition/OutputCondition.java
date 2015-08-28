/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.meter.condition;

import nars.Events;
import nars.NAR;
import nars.event.NARReaction;
import nars.io.Texts;
import nars.io.out.TextOutput;
import nars.narsese.InvalidInputException;
import nars.task.Task;
import nars.task.stamp.Stamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Monitors NAR behavior for certain conditions. Used in testing and
 * analysis.
 * 
 * Parameter O is the type of object which will be remembered that can make
 * the condition true
 */
public abstract class OutputCondition extends NARReaction implements Serializable {

    boolean succeeded = false;

    transient private final NAR nar;

    long successAt = Stamp.TIMELESS;

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

    public long time() { return nar.time(); }

    @Override
    public void event(Class channel, Object... args) {
        if ((succeeded) && (!isInverse() && (!continueAfterSuccess()))) {
            return;
        }

        Object signal = args[0];

        if (condition(channel, signal)) {
            setTrue();
        }
    }

    public String toString(Task s) {
        return s.toString(nar.memory).toString();
    }

    public String toString(Class channel, Object signal) {
        return TextOutput.append(channel, signal, false, nar, new StringBuilder(16)).toString();
    }

    protected boolean continueAfterSuccess() { return false; }

    protected void setTrue() {
        succeeded = true;

        if (successAt == Stamp.TIMELESS) {
            successAt = nar.time();
            nar.emit(OutputCondition.class, this);
        }
    }

    public boolean isTrue() {
        return succeeded;
    }

    /** returns true if condition was satisfied */
    public abstract boolean condition(Class channel, Object signal);


    public static List<OutputCondition> getConditions(NAR n, String example, int similarResultsToSave)  {
        return getConditions(n, example, similarResultsToSave, x -> n.narsese.task(x, n.memory));
    }

    /** with caching, useful for repeated tests to avoid re-parsing the same text */
    public static List<OutputCondition> getConditions(NAR n, String example, int similarsToSave, Map<String,Task> conditionCache)  {
        return getConditions(n, example, similarsToSave, x -> {
            Task t = conditionCache.get(x);



            if (t == null) {
                try {
                    t = n.narsese.task(x, n.memory);
                    conditionCache.put(x, t);
                }
                catch (Exception e) {
                    System.err.println(e);
                }

            }
            return t;
        });
    }

    /** reads an example file line-by-line, before being processed, to extract expectations */
    public static List<OutputCondition> getConditions(NAR n, String example, int similarResultsToSave, Function<String,Task> task)  {
        List<OutputCondition> conditions = new ArrayList();
        String[] lines = example.split("\n");

        int cycle = 0;
        final String expectOutNotContains2 = "''outputMustNotContain('";
        final String expectInContains = "''inputMustContain('";
        final String expectOutContains = "''outputMustContain('";
        final String expectOutEmpty = "''expect.outEmpty";

        for (String s : lines) {
            s = s.trim();
            if (s.isEmpty()) continue;

            if (s.startsWith(expectOutContains)) {

                if (!s.endsWith("')"))
                    throw new RuntimeException("invalid " + expectOutContains + " syntax: missing ending: \')");

                String match = s.substring(expectOutContains.length(), s.length() - 2); //remove ') suffix:

                try {
                    Task t = task.apply(match);
                    if (t != null)
                        conditions.add(new TaskCondition(n, t, Events.OUT.class,
                                -Stamp.TIMELESS, /* to cancel it */
                                false, Events.OUT.class, Events.Answer.class));
                    else
                        conditions.add(new OutputContainsCondition(n, match, similarResultsToSave));
                }
                catch (InvalidInputException e) {
                    System.err.println(OutputCondition.class.getSimpleName() + ": Error parsing: " + example);
                    e.printStackTrace();
                }
            }

            else if (s.startsWith(expectInContains)) {
                String match = s.substring(expectInContains.length(), s.length()-2); //remove ') suffix:

                Task t = task.apply(match);//n.narsese.parseTask(match);
                if (t!=null)
                    conditions.add(new TaskCondition(n, Events.IN.class, t, cycle, false, similarResultsToSave, Events.IN.class));
                else
                    throw new RuntimeException("API upgrade incomplete"); //conditions.add(new OutputContainsCondition(n, match, similarResultsToSave));

            }
            

            else if (s.startsWith(expectOutNotContains2)) {

                //remove ') suffix:
                String e = s.substring(expectOutNotContains2.length(), s.length()-2);                 
                conditions.add(new OutputNotContainsCondition(n, e));

            }   
            
            else if (s.indexOf(expectOutEmpty)==0) {
                conditions.add(new OutputEmptyCondition(n));
            }                
            else if (Texts.i(s.charAt(0))!=-1)  {

                try {
                    //parse sleep cycles to advance the correlated 'cycle' time
                    int sleepCycles = Texts.i(s);
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
    public abstract Object getFalseReason();

    /** if true, when it became true */
    public long getTrueTime() {
        return successAt;
    }


    /** calculates the "cost" of an execution according to certain evaluated condtions
     *  this is the soonest time at which all output conditions were successful.
     *  if any conditions were not successful, the cost is infinity
     * */
    public static double cost(Iterable<OutputCondition> conditions) {
        long lastSuccess = Stamp.TIMELESS;
        for (OutputCondition e : conditions) {
            if (e.getTrueTime() != Stamp.TIMELESS) {
                if (lastSuccess < e.getTrueTime()) {
                    lastSuccess = e.getTrueTime();
                }
            }
        }
        if (lastSuccess != Stamp.TIMELESS) {
            //score = 1.0 + 1.0 / (1+lastSuccess);
            return lastSuccess;
        }

        return Double.POSITIVE_INFINITY;
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

    public NAR getNAR() {
        return nar;
    }

}
