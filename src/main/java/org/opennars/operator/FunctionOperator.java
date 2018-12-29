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
package org.opennars.operator;

import com.google.common.collect.Lists;
import org.opennars.entity.*;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Term;
import org.opennars.language.Variable;
import org.opennars.storage.Memory;

import java.util.List;

import static org.opennars.inference.BudgetFunctions.truthToQuality;


/** 
 * Superclass of functions that execute synchronously (blocking, in thread) and take
 * N input parameters and one variable argument (as the final argument), generating a new task
 * with the result of the function substituted in the variable's place.
 */
public abstract class FunctionOperator extends Operator {

    
    protected FunctionOperator(final String name) {
        super(name);
    }

    /** y = function(x) 
     * @return y, or null if unsuccessful
     */
    abstract protected Term function(Memory memory, Term[] x);
    
    /** the term that the output will inherit from; analogous to the 'Range' of a function in mathematical terminology */
    @Deprecated abstract protected Term getRange();
        
    //abstract protected int getMinArity();
    //abstract protected int getMaxArity();
    
    @Override
    protected List<Task> execute(Operation operation, final Term[] args, final Memory m, final Timable time) {
        //TODO make memory access optional by constructor argument
        //TODO allow access to Nar instance?
        final int numArgs = args.length -1;
        
        if (numArgs < 1) {
            throw new IllegalStateException("Requires at least 1 arguments");
        }
        
        if (numArgs < 2 /*&& !(this instanceof Javascript)*/) {
            throw new IllegalStateException("Requires at least 2 arguments");
        }
        
        //last argument a variable?
        final Term lastTerm = args[numArgs];
        final boolean variable = lastTerm instanceof Variable;
        
        if(!variable /*&& !(this instanceof Javascript)*/) { 
            throw new IllegalStateException("output can not be specified");
        }
        
        
        
        final int numParam = numArgs-1;
        
        /*if(this instanceof Javascript && !variable) {
            numParam++;
        }*/
        
        final Term[] x = new Term[numParam];
        System.arraycopy(args, 1, x, 0, numParam);
        
        final Term y;
        //try {
            y = function(m, x);
            if (y == null) {
                return null;
            }
            /*if(!variable && this instanceof Javascript) {
                return null;
            }*/
            //m.emit(SynchronousFunctionOperator.class, Arrays.toString(x) + " | " + y);
        /*}
        catch (Exception e) {
            throw e;
        }*/
          
        final Variable var=new Variable("$1");
      //  Term actual_part = Similarity.make(var, y);
      //  Variable vardep=new Variable("#1");
        //Term actual_dep_part = Similarity.make(vardep, y);
        operation=(Operation) operation.setComponent(0, 
                ((CompoundTerm)operation.getSubject()).setComponent(
                        numArgs, y, m), m); 

        final float confidence = m.narParameters.DEFAULT_JUDGMENT_CONFIDENCE;
        if (variable) {
            final Sentence s = new Sentence(operation,
                                      Symbols.JUDGMENT_MARK,
                                      new TruthValue(1.0f, confidence, m.narParameters),
                                      new Stamp(time, m));

            final BudgetValue budgetForNewTask = new BudgetValue(m.narParameters.DEFAULT_JUDGMENT_PRIORITY,
                m.narParameters.DEFAULT_FEEDBACK_DURABILITY,
                truthToQuality(s.getTruth()), m.narParameters);
            final Task newTask = new Task(s, budgetForNewTask, Task.EnumType.INPUT);

            return Lists.newArrayList(newTask);
        }
        else {
            
            return null;
            
        }
    }

    /** (can be overridden in subclasses) the extent to which it is truth 
     * that the 2 given terms are equal.  in other words, a distance metric
     */
    public float equals(final Term a, final Term b) {
        //default: Term equality
        return a.equals(b) ? 1.0f : 0.0f;
    }
}
