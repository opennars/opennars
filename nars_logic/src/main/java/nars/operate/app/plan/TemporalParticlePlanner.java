/*
 * Copyright (C) 2014 tc
 *
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

package nars.operate.app.plan;

import nars.Events.NewTaskExecution;
import nars.Events.UnexecutableOperation;
import nars.NAR;
import nars.operate.IOperator;
import nars.event.Reaction;
import nars.nal.NAL;
import nars.nal.NALOperator;
import nars.nal.TruthFunctions;
import nars.nal.term.Compound;
import nars.nal.concept.Concept;
import nars.nal.Task;
import nars.nal.term.Term;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Implication;
import nars.nal.nal7.Interval;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal8.Operation;
import nars.operate.app.plan.MultipleExecutionManager.Execution;

import java.util.ArrayDeque;
import java.util.List;
import java.util.TreeSet;

import static nars.operate.app.plan.MultipleExecutionManager.isPlanTerm;

/**
 * PROBABLY BROKEN TEMPORARILY DUE TO UnexecutableGoal event being in construction
 * @author tc
 */
@Deprecated public class TemporalParticlePlanner implements IOperator, Reaction {
    
    /**
     * global plan search parameters
     */

    float searchDepth;
    int planParticles;

    /**
     * inline search parameters -- is this used any more?
     */
    float inlineSearchDepth;
    int inlineParticles;
    
    /**
     * max number of tasks that a plan can generate. chooses the N best
     */
    int maxPlannedTasks;
       
    MultipleExecutionManager executive;
    GraphExecutive graph;

    public TemporalParticlePlanner() {
        this(120, 128, 16, 4);
    }


    
    public TemporalParticlePlanner(float searchDepth, int planParticles, int inlineParticles, int maxPlannedTasks) {
        super();
        this.searchDepth = this.inlineSearchDepth = searchDepth;
        this.planParticles = planParticles;
        this.inlineParticles = inlineParticles;
        this.maxPlannedTasks = maxPlannedTasks;
    }

  
    public void setPlanParticles(int planParticles) {
        this.planParticles = planParticles;
    }

    public void setSearchDepth(float searchDepth) {
        this.searchDepth = searchDepth;
    }

    public void setMaxPlannedTasks(int maxPlannedTasks) {
        this.maxPlannedTasks = maxPlannedTasks;
    }

    public int getMaxPlannedTasks() {
        return maxPlannedTasks;
    }

    public int getPlanParticles() {
        return planParticles;
    }

    public float getSearchDepth() {
        return searchDepth;
    }
        
    @Override
    public void event(Class event, Object[] a) {
        /*if (event == UnexecutableGoal.class) {
            Task t = (Task)a[0];
            Concept c = (Concept)a[1];
            NAL n = (NAL)a[2];
            decisionPlanning(n, t, c);            
        }
        else*/ if (event == UnexecutableOperation.class) {

            Execution executing = (Execution)a[0];
            Task task = executing.t;
            Term term = task.getTerm();            
        
            if (term instanceof Conjunction) {
                Conjunction c = (Conjunction) term;
                if (c.operator() == NALOperator.SEQUENCE) {
                    executive.executeConjunctionSequence(executing, c);
                    return;
                }

            } else if (term instanceof Implication) {
                Implication it = (Implication) term;
                if ((it.getTemporalOrder() == TemporalRules.ORDER_FORWARD) || (it.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {
                    if (it.getSubject() instanceof Conjunction) {
                        Conjunction c = (Conjunction) it.getSubject();
                        if (c.operator() == NALOperator.SEQUENCE) {
                            executive.executeConjunctionSequence(executing, c);
                            return;
                        }
                    } else if (it.getSubject() instanceof Operation) {
                        executive.execute(executing, (Operation) it.getSubject(), task); //directly execute
                        return;
                    } else if (term instanceof Compound) {
                        executive.inputGoal((Compound)term);
                        return;
                    }
                }
            }
        }
        else if (event == NewTaskExecution.class) {
            Execution te = (Execution)a[0];
            Task t = te.getTask();
            
            Term term = t.getTerm();
            if (term instanceof Implication) {
                Implication it = (Implication) term;
                if ((it.getTemporalOrder() == TemporalRules.ORDER_FORWARD) || (it.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {
                    if (it.getSubject() instanceof Conjunction) {
                        t = inlineConjunction(te, t, (Conjunction) it.getSubject());
                    }
                }
            } else if (term instanceof Conjunction) {
                t = inlineConjunction(te, t, (Conjunction) term);
            }
            
            te.setTask(t);

        }
    }
    
    public void decisionPlanning(final NAL nal, final Task t, final Concept concept) {

        if (!concept.isDesired()) {
            return;
        }

        boolean plannable = graph.isPlannable(t.getTerm());
        if (plannable) {
            graph.plan(nal, concept, t, t.getTerm(), planParticles, searchDepth, '!', maxPlannedTasks);
        }

    }

    
    
    //TODO support multiple inline replacements        
    protected Task inlineConjunction(Execution te, Task t, final Conjunction c) {
        ArrayDeque<Term> inlined = new ArrayDeque();
        boolean modified = false;

        if (c.operator() == NALOperator.SEQUENCE) {
            for (Term e : c.term) {

                if (!isPlanTerm(e)) {
                    if (graph.isPlannable(e)) {

                        TreeSet<GraphExecutive.ParticlePlan> plans = 
                                graph.particlePlan(e, 
                                        inlineSearchDepth, inlineParticles);
                        
                        if (plans.size() > 0) {
                            //use the first
                            GraphExecutive.ParticlePlan pp = plans.first();

                            //if terms precede this one, remove a common prefix
                            //scan from the end of the sequence backward until a term matches the previous, and splice it there
                            //TODO more rigorous prefix compraison. compare sublist prefix
                            List<Term> seq = pp.sequence;

//                                if (prev!=null) {
//                                    int previousTermIndex = pp.sequence.lastIndexOf(prev);
//                                    
//                                    if (previousTermIndex!=-1) {
//                                        if (previousTermIndex == seq.size()-1)
//                                            seq = Collections.EMPTY_LIST;
//                                        else {                                            
//                                            seq = seq.subList(previousTermIndex+1, seq.size());
//                                        }
//                                    }
//                                }
                            //System.out.println("inline: " + seq + " -> " + e + " in " + c);
                            //TODO adjust the truth value according to the ratio of term length, so that a small inlined sequence affects less than a larger one
                            
                            te.setDesire( TruthFunctions.deduction(te.getDesireValue(), pp.getTruth()) );

                            //System.out.println(t.sentence.truth + " <- " + pp.truth + "    -> " + desire);
                            inlined.addAll(seq);

                            modified = true;
                        } else {
                            //no plan available, this wont be able to execute   
                            te.end();
                        }
                    } else {
                        //this won't be able to execute here
                        te.end();
                    }
                } else {
                    //executable term, add
                    inlined.add(e);
                }
                
            }
        }

        //remove suffix intervals
        if (inlined.size() > 0) {
            while (inlined.peekLast() instanceof Interval) {
                inlined.removeLast();
                modified = true;
            }
        }

        if (inlined.isEmpty()) {
            te.end();
        }

        if (modified) {
            Term nc = c.clone(inlined.toArray(new Term[inlined.size()]));
            if (nc == null) {
                te.end();
            } else {
                t = t.clone(t.sentence.clone(nc));
            }
        }
        return t;
    }
    
    @Override public boolean setEnabled(NAR n, boolean enabled) {
        this.executive = null; //n.memory.executive;
        this.graph = executive.graph;
        
        n.memory.event.set(this, enabled, 
                //UnexecutableGoal.class,
                UnexecutableOperation.class);

        return true;
    }

}
