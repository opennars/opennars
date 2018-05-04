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
package org.opennars.operator;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import org.opennars.storage.Memory;
import org.opennars.main.Parameters;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import static org.opennars.inference.BudgetFunctions.truthToQuality;
import org.opennars.io.Symbols;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Term;
import org.opennars.language.Variable;
//import org.opennars.operator.misc.Javascript;


/** 
 * Superclass of functions that execute synchronously (blocking, in thread) and take
 * N input parameters and one variable argument (as the final argument), generating a new task
 * with the result of the function substituted in the variable's place.
 */
public abstract class FunctionOperator extends Operator {

    
    protected FunctionOperator(String name) {
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
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory m) {
        //TODO make memory access optional by constructor argument
        //TODO allow access to NAR instance?
        int numArgs = args.length -1;
        
        if (numArgs < 1) {
            throw new RuntimeException("Requires at least 1 arguments");
        }
        
        if (numArgs < 2 /*&& !(this instanceof Javascript)*/) {
            throw new RuntimeException("Requires at least 2 arguments");
        }
        
        //last argument a variable?
        Term lastTerm = args[numArgs];
        boolean variable = lastTerm instanceof Variable;
        
        if(!variable /*&& !(this instanceof Javascript)*/) { 
            throw new RuntimeException("output can not be specified");
        }
        
        
        
        int numParam = numArgs-1;
        
        /*if(this instanceof Javascript && !variable) {
            numParam++;
        }*/
        
        Term[] x = new Term[numParam];
        System.arraycopy(args, 1, x, 0, numParam);
        
        Term y;
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
          
        Variable var=new Variable("$1");
      //  Term actual_part = Similarity.make(var, y);
      //  Variable vardep=new Variable("#1");
        //Term actual_dep_part = Similarity.make(vardep, y);
        operation=(Operation) operation.setComponent(0, 
                ((CompoundTerm)operation.getSubject()).setComponent(
                        numArgs, y, m), m); 

        float confidence = Parameters.DEFAULT_JUDGMENT_CONFIDENCE;
        if (variable) {
            Sentence s = new Sentence(operation, 
                                      Symbols.JUDGMENT_MARK,
                                      new TruthValue(1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE),
                                      new Stamp(m));
            return Lists.newArrayList( 
                    new Task(s, 
                            new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, 
                                            Parameters.DEFAULT_FEEDBACK_DURABILITY,
                                            truthToQuality(s.getTruth())), 
                            true));
        }
        else {
            
            return null;
            
        }
    }

    /** (can be overridden in subclasses) the extent to which it is truth 
     * that the 2 given terms are equal.  in other words, a distance metric
     */
    public float equals(Term a, Term b) {
        //default: Term equality
        return a.equals(b) ? 1.0f : 0.0f;
    }
}
