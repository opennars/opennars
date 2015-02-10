/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io.condition;

import nars.core.Events;
import nars.core.NAR;
import nars.event.AbstractReaction;
import nars.io.Output;
import nars.io.narsese.InvalidInputException;
import nars.logic.entity.Task;

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
public abstract class OutputCondition<O> extends AbstractReaction {
    public boolean succeeded = false;
    
    
    
    public final NAR nar;
    long successAt = -1;

    public OutputCondition(NAR nar, Class... events) {
        super(nar, events);
        this.nar = nar;
    }

    public OutputCondition(NAR nar) {
        this(nar, Output.DefaultOutputEvents);
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
        if ((channel == Events.OUT.class) || (channel == Events.EXE.class)) {
            Object signal = args[0];
            if (condition(channel, signal)) {
                setTrue();
            }
        }
    }

    protected boolean continueAfterSuccess() { return false; }

    protected void setTrue() {
        if (successAt == -1) {
            successAt = nar.time();
        }
        succeeded = true;
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
        
        for (String s : lines) {
            s = s.trim();
            
            
            final String expectOutContains2 = "''outputMustContain('";

            if (s.indexOf(expectOutContains2)==0) {

                //remove ') suffix:
                String match = s.substring(expectOutContains2.length(), s.length()-2);
                

                //TEMPORARY: try to create TaskCondition which evaluate much faster than the string processing of OutputContainsCondition
                boolean added = false;
                try {
                    Task t = n.narsese.parseTask(match);
                    if (t!=null) {
                        if (t.sentence.isEternal()) {

                            conditions.add(new TaskCondition(n, Events.OUT.class, t));
                            added = true;
                        }
                    }

                } catch (InvalidInputException e1) {
                    //System.err.println("NOT A TASK: " + match);
                    //...
                }


                if (!added)
                    conditions.add(new OutputContainsCondition(n, match, similarResultsToSave));


            }     
            
            final String expectOutNotContains2 = "''outputMustNotContain('";

            if (s.indexOf(expectOutNotContains2)==0) {

                //remove ') suffix:
                String e = s.substring(expectOutNotContains2.length(), s.length()-2);                 
                conditions.add(new OutputNotContainsCondition(n, e));

            }   
            
            final String expectOutEmpty = "''expect.outEmpty";
            if (s.indexOf(expectOutEmpty)==0) {                                
                conditions.add(new OutputEmptyCondition(n));
            }                
            

        }
        
        return conditions;
    }
    

    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + (succeeded ? "OK: " + getTrueReasons() : getFalseReason());
    }

    public List<O> getTrueReasons() {
        if (!isTrue()) throw new RuntimeException(this + " is not true so has no true reasons");
        return Collections.EMPTY_LIST;
    }
    
    /** if false, a reported reason why this condition is false */
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
        long lastSuccess = -1;
        for (OutputCondition e : conditions) {
            if (e.getTrueTime() != -1) {
                if (lastSuccess < e.getTrueTime()) {
                    lastSuccess = e.getTrueTime();
                }
            }
        }
        if (lastSuccess != -1) {
            //score = 1.0 + 1.0 / (1+lastSuccess);
            return lastSuccess;
        }

        return Double.POSITIVE_INFINITY;
    }
}
