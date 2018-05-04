/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package org.opennars.util.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opennars.main.NAR;
import org.opennars.io.events.OutputHandler;

/**
 * Monitors an output stream for certain conditions. Used in testing and
 * analysis.
 * 
 * Parameter O is the type of object which will be remembered that can make
 * the condition true
 */
public abstract class OutputCondition<O> extends OutputHandler {
    public boolean succeeded = false;
    
    
    
    public final NAR nar;
    long successAt = -1;

    public OutputCondition(NAR nar) {
        super(nar);
        this.nar = nar;
    }

    /** whether this is an "inverse" condition */
    public boolean isInverse() {
        return false;
    }

    @Override
    public void event(Class channel, Object... args) {
        if ((succeeded) && (!isInverse())) {
            return;
        }
        if ((channel == OUT.class) || (channel == EXE.class)) {
            Object signal = args[0];
            if (condition(channel, signal)) {
                setTrue();
            }
        }
    }

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
                String e = s.substring(expectOutContains2.length(), s.length()-2); 
                
                /*try {                    
                    Task t = narsese.parseTask(e);                    
                    expects.add(new ExpectContainsSentence(n, t.sentence));
                } catch (Narsese.InvalidInputException ex) {
                    expects.add(new ExpectContains(n, e, saveSimilar));
                } */
                
                conditions.add(new OutputContainsCondition(n, e, similarResultsToSave));

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
    
    
}
