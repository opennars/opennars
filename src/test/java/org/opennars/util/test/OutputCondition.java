/*
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.util.test;

import org.opennars.io.events.OutputHandler;
import org.opennars.main.Nar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Monitors an output stream for certain conditions. Used in testing and
 * analysis.
 * 
 * Parameter O is the type of object which will be remembered that can make
 * the condition true
 */
public abstract class OutputCondition<O> extends OutputHandler {
    public boolean succeeded = false;
    
    
    
    public final Nar nar;
    long successAt = -1;

    public OutputCondition(final Nar nar) {
        super(nar);
        this.nar = nar;
    }

    /** whether this is an "inverse" condition */
    public boolean isInverse() {
        return false;
    }

    @Override
    public void event(final Class channel, final Object... args) {
        if ((succeeded) && (!isInverse())) {
            return;
        }
        if ((channel == OUT.class) || (channel == EXE.class)) {
            final Object signal = args[0];
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
    public static List<OutputCondition> getConditions(final Nar n, final String example, final int similarResultsToSave)  {
        final List<OutputCondition> conditions = new ArrayList();
        final String[] lines = example.split("\n");
        
        for (String s : lines) {
            s = s.trim();
            
            
            final String expectOutContains2 = "''outputMustContain('";

            if (s.indexOf(expectOutContains2)==0) {

                //remove ') suffix:
                final String e = s.substring(expectOutContains2.length(), s.length()-2);
                
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
                final String e = s.substring(expectOutNotContains2.length(), s.length()-2);
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
        if (!isTrue()) throw new IllegalStateException(this + " is not true so has no true reasons");
        return Collections.emptyList();
    }
    
    /** if false, a reported reason why this condition is false */
    public abstract String getFalseReason();

    /** if true, when it became true */
    public long getTrueTime() {
        return successAt;
    }
    
    
}
