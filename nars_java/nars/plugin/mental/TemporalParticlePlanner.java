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

package nars.plugin.mental;

import java.util.ArrayDeque;
import java.util.List;
import java.util.TreeSet;
import nars.core.EventEmitter.Observer;
import nars.core.Events.NewTaskExecution;
import nars.core.Events.UnexecutableGoal;
import nars.core.Events.UnexecutableOperation;
import nars.core.NAR;
import nars.core.Plugin;
import nars.core.control.NAL;
import nars.entity.Concept;
import nars.entity.Task;
import nars.inference.Executive;
import nars.inference.Executive.Execution;
import static nars.inference.Executive.isPlanTerm;
import nars.inference.GraphExecutive;
import nars.inference.TemporalRules;
import nars.inference.TruthFunctions;
import nars.io.Symbols;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Term;
import nars.operator.Operation;

/**
 *
 * @author tc
 */
public class TemporalParticlePlanner implements Plugin, Observer {
    
    /**
     * global plan search parameters
     */
    public static boolean used=false; //cause decison making breaks if conjunction sequence is added when planner is not active
    
    float searchDepth;
    int planParticles;

    /**
     * inline search parameters
     */
    float inlineSearchDepth;
    int inlineParticles;
    
    /**
     * max number of tasks that a plan can generate. chooses the N best
     */
    int maxPlannedTasks = 1;
       
    Executive executive;
    GraphExecutive graph;

    public TemporalParticlePlanner() {
        this(12, 64, 16);
    }
    
    
    public TemporalParticlePlanner(float searchDepth, int planParticles, int inlineParticles) {
        super();
        this.searchDepth = this.inlineSearchDepth = searchDepth;
        this.planParticles = planParticles;
        this.inlineParticles = inlineParticles;
    }

    @Override
    public void event(Class event, Object[] a) {
        if (event == UnexecutableGoal.class) {
            Task t = (Task)a[0];
            Concept c = (Concept)a[1];
            NAL n = (NAL)a[2];
            decisionPlanning(n, t, c);            
        }
        else if (event == UnexecutableOperation.class) {

            Execution executing = (Execution)a[0];
            Task task = executing.t;
            Term term = task.getContent();            
        
            if (term instanceof Conjunction) {
                Conjunction c = (Conjunction) term;
                if (c.operator() == Symbols.NativeOperator.SEQUENCE) {
                    executive.executeConjunctionSequence(executing, c);
                    return;
                }

            } else if (term instanceof Implication) {
                Implication it = (Implication) term;
                if ((it.getTemporalOrder() == TemporalRules.ORDER_FORWARD) || (it.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {
                    if (it.getSubject() instanceof Conjunction) {
                        Conjunction c = (Conjunction) it.getSubject();
                        if (c.operator() == Symbols.NativeOperator.SEQUENCE) {
                            executive.executeConjunctionSequence(executing, c);
                            return;
                        }
                    } else if (it.getSubject() instanceof Operation) {
                        executive.execute(executing, (Operation) it.getSubject(), task); //directly execute
                        return;
                    }
                }
            }                         
        }
        else if (event == NewTaskExecution.class) {
            Execution te = (Execution)a[0];
            Task t = te.getTask();
            
            Term term = t.getContent();
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

        if (!executive.isDesired(t, concept)) {
            return;
        }

        boolean plannable = graph.isPlannable(t.getContent());
        if (plannable) {
            graph.plan(nal, concept, t, t.getContent(), planParticles, searchDepth, '!', maxPlannedTasks);
        }

    }

    
    
    //TODO support multiple inline replacements        
    protected Task inlineConjunction(Execution te, Task t, final Conjunction c) {
        ArrayDeque<Term> inlined = new ArrayDeque();
        boolean modified = false;

        if (c.operator() == Symbols.NativeOperator.SEQUENCE) {
            Term prev = null;
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
                prev = e;
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
        this.executive = n.memory.executive;
        this.graph = executive.graph;
        
        used=enabled;
        n.memory.event.set(this, enabled, 
                UnexecutableGoal.class, 
                UnexecutableOperation.class);

        return true;
    }

}
