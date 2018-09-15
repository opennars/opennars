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
package org.opennars.plugin.mental;

import java.io.Serializable;
import org.opennars.control.DerivationContext;
import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.inference.TemporalRules;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events;
import org.opennars.language.*;
import org.opennars.main.Nar;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.plugin.Plugin;
import org.opennars.storage.Memory;

import java.util.Arrays;

/**
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 */
public class InternalExperience implements Plugin, EventObserver, Serializable {
    private Memory memory;

    public static boolean enabled=false;

    private Nar nar;


    public volatile float MINIMUM_PRIORITY_TO_CREATE_WANT_BELIEVE_ETC=0.3f;
    public volatile float MINIMUM_PRIORITY_TO_CREATE_WONDER_EVALUATE=0.3f;

    //internal experience has less durability?
    public volatile float INTERNAL_EXPERIENCE_PROBABILITY=0.0001f;

    //internal experience has less durability?
    public volatile float INTERNAL_EXPERIENCE_DURABILITY_MUL=0.1f; //0.1

    //internal experience has less priority?
    public volatile float INTERNAL_EXPERIENCE_PRIORITY_MUL=0.1f; //0.1


    /** less probable form */
    public volatile float INTERNAL_EXPERIENCE_RARE_PROBABILITY = 0.000025f;

    /** dont use internal experience for want and believe if this setting is true */
    public volatile boolean ALLOW_WANT_BELIEF=true;

    //https://groups.google.com/forum/#!topic/open-nars/DVE5FJd7FaM
    public volatile boolean OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY=false;


    public volatile boolean FULL_REFLECTION = false;


    public void setMINIMUM_PRIORITY_TO_CREATE_WANT_BELIEVE_ETC(double val) {
        this.MINIMUM_PRIORITY_TO_CREATE_WANT_BELIEVE_ETC = (float) val;
    }
    public double getMINIMUM_PRIORITY_TO_CREATE_WANT_BELIEVE_ETC() {
        return MINIMUM_PRIORITY_TO_CREATE_WANT_BELIEVE_ETC;
    }

    public void setMINIMUM_PRIORITY_TO_CREATE_WONDER_EVALUATE(double val) {
        this.MINIMUM_PRIORITY_TO_CREATE_WONDER_EVALUATE = (float) val;
    }
    public double getMINIMUM_PRIORITY_TO_CREATE_WONDER_EVALUATE() {
        return MINIMUM_PRIORITY_TO_CREATE_WONDER_EVALUATE;
    }


    public void setINTERNAL_EXPERIENCE_PROBABILITY(double val) {
        this.INTERNAL_EXPERIENCE_PROBABILITY = (float) val;
    }
    public double getINTERNAL_EXPERIENCE_PROBABILITY() {
        return INTERNAL_EXPERIENCE_PROBABILITY;
    }

    public void setINTERNAL_EXPERIENCE_RARE_PROBABILITY(double val) {
        this.INTERNAL_EXPERIENCE_RARE_PROBABILITY = (float) val;
    }
    public double getINTERNAL_EXPERIENCE_RARE_PROBABILITY() {
        return INTERNAL_EXPERIENCE_RARE_PROBABILITY;
    }

    public void setINTERNAL_EXPERIENCE_DURABILITY_MUL(double val) {
        this.INTERNAL_EXPERIENCE_DURABILITY_MUL = (float) val;
    }
    public double getINTERNAL_EXPERIENCE_DURABILITY_MUL() {
        return INTERNAL_EXPERIENCE_DURABILITY_MUL;
    }

    public void setINTERNAL_EXPERIENCE_PRIORITY_MUL(double val) {
        this.INTERNAL_EXPERIENCE_PRIORITY_MUL = (float) val;
    }
    public double getINTERNAL_EXPERIENCE_PRIORITY_MUL() {
        return INTERNAL_EXPERIENCE_PRIORITY_MUL;
    }
    

    public boolean isALLOW_WANT_BELIEF() {
        return ALLOW_WANT_BELIEF;
    }
    public void setALLOW_WANT_BELIEF(final boolean val) {
        ALLOW_WANT_BELIEF=val;
    }

    public boolean isOLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY() {
        return OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY;
    }
    public void setOLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY(final boolean val) {
        OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY=val;
    }


    public boolean isFULL_REFLECTION() {
        return FULL_REFLECTION;
    }
    public void setFULL_REFLECTION(final boolean val) {
        FULL_REFLECTION=val;
    }
    public InternalExperience() {}
    public InternalExperience(float MINIMUM_PRIORITY_TO_CREATE_WANT_BELIEVE_ETC,
            float MINIMUM_PRIORITY_TO_CREATE_WONDER_EVALUATE,
            float INTERNAL_EXPERIENCE_PROBABILITY,
            float INTERNAL_EXPERIENCE_RARE_PROBABILITY,
            float INTERNAL_EXPERIENCE_DURABILITY_MUL,
            float INTERNAL_EXPERIENCE_PRIORITY_MUL,
            boolean ALLOW_WANT_BELIEF,
            boolean OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY,
            boolean FULL_REFLECTION) {
        this.MINIMUM_PRIORITY_TO_CREATE_WANT_BELIEVE_ETC = MINIMUM_PRIORITY_TO_CREATE_WANT_BELIEVE_ETC;
        this.MINIMUM_PRIORITY_TO_CREATE_WONDER_EVALUATE = MINIMUM_PRIORITY_TO_CREATE_WONDER_EVALUATE;
        this.INTERNAL_EXPERIENCE_PROBABILITY = INTERNAL_EXPERIENCE_PROBABILITY;
        this.INTERNAL_EXPERIENCE_RARE_PROBABILITY = INTERNAL_EXPERIENCE_RARE_PROBABILITY;
        this.INTERNAL_EXPERIENCE_DURABILITY_MUL = INTERNAL_EXPERIENCE_DURABILITY_MUL;
        this.INTERNAL_EXPERIENCE_PRIORITY_MUL = INTERNAL_EXPERIENCE_PRIORITY_MUL;
        this.ALLOW_WANT_BELIEF = ALLOW_WANT_BELIEF;
        this.OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY = OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY;
        this.FULL_REFLECTION = FULL_REFLECTION;
    }
    @Override public boolean setEnabled(final Nar n, final boolean enable) {
        memory = n.memory;
        this.nar = n;
        
        memory.event.set(this, enable, Events.ConceptDirectProcessedTask.class);
        
        if (FULL_REFLECTION)
            memory.event.set(this, enable, Events.BeliefReason.class);
        
        enabled=enable;
        
        return true;
    }
    
        public static Term toTerm(final Sentence s, final Memory mem, final Timable time) {
        final String opName;
        switch (s.punctuation) {
            case Symbols.JUDGMENT_MARK:
                opName = "^believe";
                if(!mem.internalExperience.ALLOW_WANT_BELIEF) {
                    return null;
                }
                break;
            case Symbols.GOAL_MARK:
                opName = "^want";
                if(!mem.internalExperience.ALLOW_WANT_BELIEF) {
                    return null;
                }
                break;
            case Symbols.QUESTION_MARK:
                opName = "^wonder";
                break;
            case Symbols.QUEST_MARK:
                opName = "^evaluate";
                break;
            default:
                return null;
        }
        
        final Term opTerm = mem.getOperator(opName);
        final Term[] arg = new Term[ s.truth==null ? 2 : 3 ];
        arg[0]=Term.SELF;
        arg[1]=s.getTerm();
        if (s.truth != null) {
            arg[2] = s.projection(time.time(), time.time(), mem).truth.toWordTerm();
        }
        
        //Operation.make ?
        final Term operation = Inheritance.make(new Product(arg), opTerm);
        if (operation == null) {
            throw new IllegalStateException("Unable to create Inheritance: " + opTerm + ", " + Arrays.toString(arg));
        }
        return operation;
    }


    @Override
    public void event(final Class event, final Object[] a) {
        
        if (event==Events.ConceptDirectProcessedTask.class) {
            final Task task = (Task)a[0];
            
            //old strategy always, new strategy only for QUESTION and QUEST:
            if(OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY || (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY && (task.sentence.punctuation == Symbols.QUESTION_MARK || task.sentence.punctuation == Symbols.QUEST_MARK))) {
                InternalExperienceFromTaskInternal(memory,task, FULL_REFLECTION, nar);
            }
        }
        else if (event == Events.BeliefReason.class) {
            //belief, beliefTerm, taskTerm, nal
            final Sentence belief = (Sentence)a[0];
            final Term beliefTerm = (Term)a[1];
            final Term taskTerm = (Term)a[2];
            final DerivationContext nal = (DerivationContext)a[3];
            beliefReason(belief, beliefTerm, taskTerm, nal);
        }
    }
    
    public static void InternalExperienceFromBelief(final Memory memory, final Task task, final Sentence belief, final Timable time) {
        final Task newTask = new Task(belief.clone(), task.budget.clone(), Task.EnumType.INPUT);

        InternalExperienceFromTask(memory, newTask, false, time);
    }
    
    public static void InternalExperienceFromTask(final Memory memory, final Task task, final boolean full, final Timable time) {
        if(memory.internalExperience == null) {
            return;
        }
        if(!memory.internalExperience.OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY) {
            InternalExperienceFromTaskInternal(memory, task, full, time);
        }
    }

    public static boolean InternalExperienceFromTaskInternal(final Memory memory, final Task task, final boolean full, final Timable time) {
        if(!enabled) {
            return false;
        }
        
       // if(OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY ||
       //         (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY && (task.sentence.punctuation==Symbols.QUESTION_MARK || task.sentence.punctuation==Symbols.QUEST_MARK))) {
        {
            if(task.sentence.punctuation == Symbols.QUESTION_MARK || task.sentence.punctuation == Symbols.QUEST_MARK) {
                if(task.getPriority()<memory.internalExperience.MINIMUM_PRIORITY_TO_CREATE_WONDER_EVALUATE) {
                    return false;
                }
            }
            else
            if(task.getPriority()<memory.internalExperience.MINIMUM_PRIORITY_TO_CREATE_WANT_BELIEVE_ETC) {
                return false;
            }
        }
        
        final Term content=task.getTerm();
        // to prevent infinite recursions
        if (content instanceof Operation/* ||  Memory.randomNumber.nextDouble()>Parameters.INTERNAL_EXPERIENCE_PROBABILITY*/) {
            return true;
        }
        final Sentence sentence = task.sentence;
        final TruthValue truth = new TruthValue(1.0f, memory.narParameters.DEFAULT_JUDGMENT_CONFIDENCE, memory.narParameters);
        final Stamp stamp = task.sentence.stamp.clone();
        stamp.setOccurrenceTime(time.time());
        final Term ret=toTerm(sentence, memory, time);
        if (ret==null) {
            return true;
        }
        final Sentence j = new Sentence(
            ret,
            Symbols.JUDGMENT_MARK,
            truth,
            stamp);

        final BudgetValue newbudget=new BudgetValue(
                memory.narParameters.DEFAULT_JUDGMENT_CONFIDENCE*memory.internalExperience.INTERNAL_EXPERIENCE_PRIORITY_MUL,
                memory.narParameters.DEFAULT_JUDGMENT_PRIORITY*memory.internalExperience.INTERNAL_EXPERIENCE_DURABILITY_MUL,
                BudgetFunctions.truthToQuality(truth), memory.narParameters);
        
        if(!memory.internalExperience.OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY) {
            newbudget.setPriority(task.getPriority()*memory.internalExperience.INTERNAL_EXPERIENCE_PRIORITY_MUL);
            newbudget.setDurability(task.getDurability()*memory.internalExperience.INTERNAL_EXPERIENCE_DURABILITY_MUL);
        }

        final Task newTask = new Task(j, newbudget, Task.EnumType.INPUT);

        memory.addNewTask(newTask, "Reflected mental operation (Internal Experience)");
        return false;
    }

    final static String[] nonInnateBeliefOperators = new String[] {
        "^remind","^doubt","^consider","^evaluate","hestitate","^wonder","^belief","^want"
    }; 
    
    /** used in full internal experience mode only */
    protected void beliefReason(final Sentence belief, final Term beliefTerm, final Term taskTerm, final DerivationContext nal) {
        
        final Memory memory = nal.memory;
    
        if (Memory.randomNumber.nextDouble() < INTERNAL_EXPERIENCE_RARE_PROBABILITY ) {
            
            //the operators which dont have a innate belief
            //also get a chance to reveal its effects to the system this way
            final Operator op=memory.getOperator(nonInnateBeliefOperators[Memory.randomNumber.nextInt(nonInnateBeliefOperators.length)]);
            
            final Product prod=new Product(belief.term);
            
            if(op!=null && prod!=null) {
                
                final Term new_term=Inheritance.make(prod, op);
                final Sentence sentence = new Sentence(
                    new_term,
                    Symbols.GOAL_MARK,
                    new TruthValue(1, memory.narParameters.DEFAULT_JUDGMENT_CONFIDENCE, memory.narParameters),  // a naming convension
                    new Stamp(nal.time, memory));
                
                final float quality = BudgetFunctions.truthToQuality(sentence.truth);
                final BudgetValue budget = new BudgetValue(
                    memory.narParameters.DEFAULT_GOAL_PRIORITY*INTERNAL_EXPERIENCE_PRIORITY_MUL, 
                    memory.narParameters.DEFAULT_GOAL_DURABILITY*INTERNAL_EXPERIENCE_DURABILITY_MUL, 
                    quality, memory.narParameters);

                final Task newTask = new Task(sentence, budget, Task.EnumType.INPUT);

                nal.derivedTask(newTask, false, false, false);
            }
        }

        if (beliefTerm instanceof Implication && Memory.randomNumber.nextDouble()<=INTERNAL_EXPERIENCE_PROBABILITY) {
            final Implication imp=(Implication) beliefTerm;
            if(imp.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
                //1. check if its (&/,term,+i1,...,+in) =/> anticipateTerm form:
                boolean valid=true;
                if(imp.getSubject() instanceof Conjunction) {
                    final Conjunction conj=(Conjunction) imp.getSubject();
                    if(!conj.term[0].equals(taskTerm)) {
                        valid=false; //the expected needed term is not included
                    }
                    for(int i=1;i<conj.term.length;i++) {
                        if(!(conj.term[i] instanceof Interval)) {
                            valid=false;
                            break;
                        }
                    }
                } else {
                    if(!imp.getSubject().equals(taskTerm)) {
                        valid=false;
                    }
                }    

                if(valid) {
                    final Operator op=memory.getOperator("^anticipate");
                    if (op == null)
                        throw new IllegalStateException(this + " requires ^anticipate operator");
                    
                    final Product args=new Product(imp.getPredicate());
                    final Term new_term=Operation.make(args,op);

                    final Sentence sentence = new Sentence(
                        new_term,
                        Symbols.GOAL_MARK,
                        new TruthValue(1, memory.narParameters.DEFAULT_JUDGMENT_CONFIDENCE, memory.narParameters),  // a naming convension
                        new Stamp(nal.time, memory));

                    final float quality = BudgetFunctions.truthToQuality(sentence.truth);
                    final BudgetValue budget = new BudgetValue(
                        memory.narParameters.DEFAULT_GOAL_PRIORITY*INTERNAL_EXPERIENCE_PRIORITY_MUL, 
                        memory.narParameters.DEFAULT_GOAL_DURABILITY*INTERNAL_EXPERIENCE_DURABILITY_MUL, 
                        quality, memory.narParameters);

                    final Task newTask = new Task(sentence, budget, Task.EnumType.INPUT);

                    nal.derivedTask(newTask, false, false, false);
                }
            }
        }
    }    
}
