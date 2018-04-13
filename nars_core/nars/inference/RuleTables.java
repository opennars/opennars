/*
 * RuleTables.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.inference;

import nars.io.events.Events;
import nars.storage.Memory;
import nars.main.Parameters;
import nars.control.DerivationContext;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.TLink;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.entity.TruthValue;
import nars.io.Symbols;
import static nars.io.Symbols.VAR_DEPENDENT;
import static nars.io.Symbols.VAR_INDEPENDENT;
import static nars.io.Symbols.VAR_QUERY;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Disjunction;
import nars.language.Equivalence;
import nars.language.Implication;
import nars.language.Inheritance;
import nars.language.Negation;
import nars.language.SetExt;
import nars.language.SetInt;
import nars.language.Similarity;
import nars.language.Statement;
import nars.language.Term;
import static nars.language.Terms.equalSubTermsInRespectToImageAndProduct;
import nars.language.Variable;
import nars.language.Variables;
import nars.operator.Operation;

/**
 * Table of inference rules, indexed by the TermLinks for the task and the
 * belief. Used in indirective processing of a task, to dispatch inference cases
 * to the relevant inference rules.
 */
public class RuleTables {
    
    
    /**
     * Entry point of the inference engine
     *
     * @param tLink The selected TaskLink, which will provide a task
     * @param bLink The selected TermLink, which may provide a belief
     * @param memory Reference to the memory
     */
    public static void reason(final TaskLink tLink, final TermLink bLink, final DerivationContext nal) {
        final Memory memory = nal.mem();
        
        final Task task = nal.getCurrentTask();
        final Sentence taskSentence = task.sentence;
        
        final Term taskTerm = taskSentence.term;         // cloning for substitution
        Term beliefTerm = bLink.target;       // cloning for substitution
        
        final Concept beliefConcept = memory.concept(beliefTerm);
        
        Sentence belief = (beliefConcept != null) ? beliefConcept.getBelief(nal, task) : null;
        
        nal.setCurrentBelief( belief );
        
        if (belief != null) {   
            beliefTerm = belief.term; //because interval handling that differs on conceptual level
            
          /*Sentence belief_event = beliefConcept.getBeliefForTemporalInference(task);
            if(belief_event != null) {
                boolean found_overlap = false;
                if(Stamp.baseOverlap(task.sentence.stamp.evidentialBase, belief_event.stamp.evidentialBase)) {
                    found_overlap = true;
                }
                if(!found_overlap) { //temporal rules are inductive so no chance to succeed if there is an overlap
                                     //and since the temporal rule is relatively expensive the check here was good.
                    Sentence inference_belief = belief;
                    nal.setCurrentBelief(belief_event);
                    nal.setTheNewStamp(task.sentence.stamp, belief_event.stamp, nal.memory.time());
                    TemporalRules.temporalInduction(task.sentence, belief_event, nal, true);
                    nal.setCurrentBelief(inference_belief);
                    nal.setTheNewStamp(task.sentence.stamp, belief.stamp, nal.memory.time());
                }
            }*/
            
            //too restrictive, its checked for non-deductive inference rules in derivedTask (also for single prem)
            nal.evidentalOverlap = Stamp.baseOverlap(task.sentence.stamp.evidentialBase, belief.stamp.evidentialBase);
            if(nal.evidentalOverlap && (!task.sentence.isEternal() || !belief.isEternal())) {
                return; //only allow for eternal reasoning for now to prevent derived event floods
            }
            
            nal.emit(Events.BeliefReason.class, belief, beliefTerm, taskTerm, nal);
            
            if (LocalRules.match(task, belief, nal)) { //new tasks resulted from the match, so return
                return;
            }
        }
        
        //current belief and task may have changed, so set again:
        nal.setCurrentBelief(belief);
        nal.setCurrentTask(task);
        
        //put here since LocalRules match should be possible even if the belief is foreign
        if(equalSubTermsInRespectToImageAndProduct(taskTerm,beliefTerm))
           return;
        
        /*if ((memory.getNewTaskCount() > 0) && taskSentence.isJudgment()) {
            return;
        }*/
        
        final short tIndex = tLink.getIndex(0);
        short bIndex = bLink.getIndex(0);
        switch (tLink.type) {          // dispatch first by TaskLink type
            case TermLink.SELF:
                switch (bLink.type) {
                    case TermLink.COMPONENT:
                        compoundAndSelf((CompoundTerm) taskTerm, beliefTerm, true, bIndex,  nal);
                        break;
                    case TermLink.COMPOUND:
                        compoundAndSelf((CompoundTerm) beliefTerm, taskTerm, false, bIndex, nal);
                        break;
                    case TermLink.COMPONENT_STATEMENT:
                        if (belief != null) {
                            if (taskTerm instanceof Statement) {
                                SyllogisticRules.detachment(taskSentence, belief, bIndex, nal);
                            }
                        } //else {
                        if(taskSentence.term instanceof Inheritance || taskSentence.term instanceof Similarity) {
                            StructuralRules.transformNegation((CompoundTerm) Negation.make(taskSentence.term), nal);
                        }
                        try {
                            goalFromQuestion(task, taskTerm, nal); 
                        }catch(Exception ex) {
                            if(Parameters.DEBUG) {
                                System.out.print("Error in goalFromQuestion");
                            }
                        } //todo fix
                        //}
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            SyllogisticRules.detachment(belief, taskSentence, bIndex, nal);
                        }
                        break;
                    case TermLink.COMPONENT_CONDITION:
                        if ((belief != null) && (taskTerm instanceof Implication)) {
                            bIndex = bLink.getIndex(1);
                            SyllogisticRules.conditionalDedInd(task.sentence,(Implication) taskTerm, bIndex, beliefTerm, tIndex, nal);
                        }
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if ((belief != null) && (taskTerm instanceof Implication) && (beliefTerm instanceof Implication)) {
                            bIndex = bLink.getIndex(1);
                            SyllogisticRules.conditionalDedInd(belief,(Implication) beliefTerm, bIndex, taskTerm, tIndex, nal);
                        }
                        break;
                }
                break;
            case TermLink.COMPOUND:
                switch (bLink.type) {
                    case TermLink.COMPOUND:
                        compoundAndCompound((CompoundTerm) taskTerm, (CompoundTerm) beliefTerm, tIndex, bIndex, nal);
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        compoundAndStatement((CompoundTerm) taskTerm, tIndex, (Statement) beliefTerm, bIndex, beliefTerm, nal);
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if (belief != null) {
                            if (beliefTerm instanceof Implication) {
                                Term[] u = new Term[] { beliefTerm, taskTerm };
                                if (Variables.unify(VAR_INDEPENDENT, ((Statement) beliefTerm).getSubject(), taskTerm, u, true)) { //only secure place that
                                    Sentence newBelief = belief.clone(u[0]);                                                //allows partial match
                                    Sentence newTaskSentence = taskSentence.clone(u[1]);
                                    detachmentWithVar(newBelief, newTaskSentence, bIndex, false, nal);
                                } else {
                                    SyllogisticRules.conditionalDedInd(belief, (Implication) beliefTerm, bIndex, taskTerm, -1, nal);
                                }                                
                                
                            } else if (beliefTerm instanceof Equivalence) {
                                SyllogisticRules.conditionalAna((Equivalence) beliefTerm, bIndex, taskTerm, -1, nal);
                            }
                        }
                        break;
                }
                break;
            case TermLink.COMPOUND_STATEMENT:
                switch (bLink.type) {
                    case TermLink.COMPONENT:
                        if (taskTerm instanceof Statement) {
                            goalFromWantBelief(task, tIndex, bIndex, taskTerm, nal, beliefTerm);
                            componentAndStatement((CompoundTerm) nal.getCurrentTerm(), bIndex, (Statement) taskTerm, tIndex, nal);
                        }
                        break;
                    case TermLink.COMPOUND:
                        if (taskTerm instanceof Statement) {
                            compoundAndStatement((CompoundTerm) beliefTerm, bIndex, (Statement) taskTerm, tIndex, beliefTerm, nal);
                        }
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            syllogisms(tLink, bLink, taskTerm, beliefTerm, nal);
                        }
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if (belief != null) {
                            bIndex = bLink.getIndex(1);
                            if ((taskTerm instanceof Statement) && (beliefTerm instanceof Implication)) {
                                conditionalDedIndWithVar(belief, (Implication) beliefTerm, bIndex, (Statement) taskTerm, tIndex, nal);
                            }
                        }
                        break;
                }
                break;
            case TermLink.COMPOUND_CONDITION:
                switch (bLink.type) {
                      case TermLink.COMPOUND:
                        if (belief != null) {
                            detachmentWithVar(taskSentence, belief, tIndex, nal);
                        }
                        break;
                    
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            if (taskTerm instanceof Implication) // TODO maybe put instanceof test within conditionalDedIndWithVar()
                            {
                                Term subj = ((Statement) taskTerm).getSubject();
                                if (subj instanceof Negation) {
                                    if (taskSentence.isJudgment()) {
                                        componentAndStatement((CompoundTerm) subj, bIndex, (Statement) taskTerm, tIndex, nal);
                                    } else {
                                        componentAndStatement((CompoundTerm) subj, tIndex, (Statement) beliefTerm, bIndex, nal);
                                    }
                                } else {
                                    conditionalDedIndWithVar(task.sentence, (Implication) taskTerm, tIndex, (Statement) beliefTerm, bIndex, nal);
                                }
                            }
                            break;
                        }
                        break;
                }
        }
    }

    public static void goalFromWantBelief(final Task task, final short tIndex, short bIndex, final Term taskTerm, final DerivationContext nal, Term beliefTerm) {
        if(task.sentence.isJudgment() && tIndex == 0 && bIndex == 1 && taskTerm instanceof Operation) {
            Operation op = (Operation) taskTerm;
            if(op.getPredicate() == nal.memory.getOperator("^want")) {
                TruthValue newTruth = TruthFunctions.deduction(task.sentence.truth, Parameters.reliance);
                nal.singlePremiseTask(((Operation)taskTerm).getArguments().term[1], Symbols.GOAL_MARK, newTruth, BudgetFunctions.forward(newTruth, nal));
            }
        }
    }

    private static void goalFromQuestion(final Task task, final Term taskTerm, final DerivationContext nal) {
        if(task.sentence.punctuation==Symbols.QUESTION_MARK && (taskTerm instanceof Implication || taskTerm instanceof Equivalence)) { //<a =/> b>? |- a!
            Term goalterm=null;
            Term goalterm2=null;
            if(taskTerm instanceof Implication) {
                Implication imp=(Implication)taskTerm;
                if(imp.getTemporalOrder()!=TemporalRules.ORDER_BACKWARD || imp.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT) {
                    if(!Parameters.CURIOSITY_FOR_OPERATOR_ONLY || imp.getSubject() instanceof Operation) {
                        goalterm=imp.getSubject();
                    }
                    if(goalterm instanceof Variable && goalterm.hasVarQuery() && (!Parameters.CURIOSITY_FOR_OPERATOR_ONLY || imp.getPredicate() instanceof Operation)) {
                        goalterm=imp.getPredicate(); //overwrite, it is a how question, in case of <?how =/> b> it is b! which is desired
                    }
                }
                else
                    if(imp.getTemporalOrder()==TemporalRules.ORDER_BACKWARD) {
                        if(!Parameters.CURIOSITY_FOR_OPERATOR_ONLY || imp.getPredicate() instanceof Operation) {
                            goalterm=imp.getPredicate();
                        }
                        if(goalterm instanceof Variable && goalterm.hasVarQuery() && (!Parameters.CURIOSITY_FOR_OPERATOR_ONLY || imp.getSubject() instanceof Operation)) {
                            goalterm=imp.getSubject(); //overwrite, it is a how question, in case of <?how =/> b> it is b! which is desired
                        }
                    }
            }
            else
                if(taskTerm instanceof Equivalence) {
                    Equivalence qu=(Equivalence)taskTerm;
                    if(qu.getTemporalOrder()==TemporalRules.ORDER_FORWARD || qu.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT) {
                        if(!Parameters.CURIOSITY_FOR_OPERATOR_ONLY || qu.getSubject() instanceof Operation) {
                            goalterm=qu.getSubject();
                        }
                        if(!Parameters.CURIOSITY_FOR_OPERATOR_ONLY || qu.getPredicate() instanceof Operation) {
                            goalterm2=qu.getPredicate();
                        }
                    }
                }
            TruthValue truth=new TruthValue(1.0f,Parameters.DEFAULT_GOAL_CONFIDENCE*Parameters.CURIOSITY_DESIRE_CONFIDENCE_MUL);
            if(goalterm!=null && !(goalterm instanceof Variable) && goalterm instanceof CompoundTerm) {
                goalterm=((CompoundTerm)goalterm).transformIndependentVariableToDependentVar((CompoundTerm) goalterm);
                Sentence sent=new Sentence(
                    goalterm,
                    Symbols.GOAL_MARK,
                    truth,
                    new Stamp(task.sentence.stamp,nal.memory.time()));

                nal.singlePremiseTask(sent, new BudgetValue(task.getPriority()*Parameters.CURIOSITY_DESIRE_PRIORITY_MUL,task.getDurability()*Parameters.CURIOSITY_DESIRE_DURABILITY_MUL,BudgetFunctions.truthToQuality(truth)));
            }
            if(goalterm2!=null && !(goalterm2 instanceof Variable) && goalterm2 instanceof CompoundTerm) {
                goalterm2=((CompoundTerm)goalterm).transformIndependentVariableToDependentVar((CompoundTerm) goalterm2);
                Sentence sent=new Sentence(
                    goalterm2,
                    Symbols.GOAL_MARK,
                    truth.clone(),
                    new Stamp(task.sentence.stamp,nal.memory.time()));

                nal.singlePremiseTask(sent, new BudgetValue(task.getPriority()*Parameters.CURIOSITY_DESIRE_PRIORITY_MUL,task.getDurability()*Parameters.CURIOSITY_DESIRE_DURABILITY_MUL,BudgetFunctions.truthToQuality(truth)));
            }
        }
    }

    /* ----- syllogistic inferences ----- */
    /**
     * Meta-table of syllogistic rules, indexed by the content classes of the
     * taskSentence and the belief
     *
     * @param tLink The link to task
     * @param bLink The link to belief
     * @param taskTerm The content of task
     * @param beliefTerm The content of belief
     * @param nal Reference to the memory
     */
    private static void syllogisms(TaskLink tLink, TermLink bLink, Term taskTerm, Term beliefTerm, DerivationContext nal) {
        Sentence taskSentence = nal.getCurrentTask().sentence;
        Sentence belief = nal.getCurrentBelief();
        int figure;
        if (taskTerm instanceof Inheritance) {
            if (beliefTerm instanceof Inheritance) {
                figure = indexToFigure(tLink, bLink);
                asymmetricAsymmetric(taskSentence, belief, figure, nal);
            } else if (beliefTerm instanceof Similarity) {
                figure = indexToFigure(tLink, bLink);
                asymmetricSymmetric(taskSentence, belief, figure, nal);
            } else {
                detachmentWithVar(belief, taskSentence, bLink.getIndex(0), nal);
            }
        } else if (taskTerm instanceof Similarity) {
            if (beliefTerm instanceof Inheritance) {
                figure = indexToFigure(bLink, tLink);
                asymmetricSymmetric(belief, taskSentence, figure, nal);
            } else if (beliefTerm instanceof Similarity) {
                figure = indexToFigure(bLink, tLink);
                symmetricSymmetric(belief, taskSentence, figure, nal);
            } else if (beliefTerm instanceof Implication) {
                //Bridge to higher order statements:
                figure = indexToFigure(tLink, bLink);
                asymmetricSymmetric(belief, taskSentence, figure, nal);
            } else if (beliefTerm instanceof Equivalence) {
                //Bridge to higher order statements:
                figure = indexToFigure(tLink, bLink);
                symmetricSymmetric(belief, taskSentence, figure, nal);
            }
        } else if (taskTerm instanceof Implication) {
            if (beliefTerm instanceof Implication) {
                figure = indexToFigure(tLink, bLink);
                asymmetricAsymmetric(taskSentence, belief, figure, nal);
            } else if (beliefTerm instanceof Equivalence) {
                figure = indexToFigure(tLink, bLink);
                asymmetricSymmetric(taskSentence, belief, figure, nal);
            } else if (beliefTerm instanceof Inheritance) {
                detachmentWithVar(taskSentence, belief, tLink.getIndex(0), nal);
            } else if (beliefTerm instanceof Similarity) {
                //Bridge to higher order statements:
                figure = indexToFigure(tLink, bLink);
                asymmetricSymmetric(taskSentence, belief, figure, nal);
            }
        } else if (taskTerm instanceof Equivalence) {
            if (beliefTerm instanceof Implication) {
                figure = indexToFigure(bLink, tLink);
                asymmetricSymmetric(belief, taskSentence, figure, nal);
            } else if (beliefTerm instanceof Equivalence) {
                figure = indexToFigure(bLink, tLink);
                symmetricSymmetric(belief, taskSentence, figure, nal);
            } else if (beliefTerm instanceof Inheritance) {
                detachmentWithVar(taskSentence, belief, tLink.getIndex(0), nal);
            } else if (beliefTerm instanceof Similarity) {
                //Bridge to higher order statements:
                figure = indexToFigure(tLink, bLink);
                symmetricSymmetric(belief, taskSentence, figure, nal);
            }
        }
    }

    /**
     * Decide the figure of syllogism according to the locations of the common
     * term in the premises
     *
     * @param link1 The link to the first premise
     * @param link2 The link to the second premise
     * @return The figure of the syllogism, one of the four: 11, 12, 21, or 22
     */
    private static final  int indexToFigure(final TLink link1, final TLink link2) {
        return (link1.getIndex(0) + 1) * 10 + (link2.getIndex(0) + 1);
    }

    /**
     * Syllogistic rules whose both premises are on the same asymmetric relation
     *
     * @param taskSentence The taskSentence in the task
     * @param belief The judgment in the belief
     * @param figure The location of the shared term
     * @param nal Reference to the memory
     */
    private static void asymmetricAsymmetric(final Sentence taskSentence, final Sentence belief, int figure, final DerivationContext nal) {
        Statement taskStatement = (Statement) taskSentence.term;
        Statement beliefStatement = (Statement) belief.term;
        
        Term t1, t2;
        Term[] u = new Term[] { taskStatement, beliefStatement };
        switch (figure) {
            case 11:    // induction                
                if (Variables.unify(VAR_INDEPENDENT, taskStatement.getSubject(), beliefStatement.getSubject(), u)) {                    
                    taskStatement = (Statement) u[0];
                    beliefStatement = (Statement) u[1];
                    if (taskStatement.equals(beliefStatement)) {
                        return;
                    }
                    t1 = beliefStatement.getPredicate();
                    t2 = taskStatement.getPredicate();
                    boolean sensational = SyllogisticRules.abdIndCom(t1, t2, taskSentence, belief, figure, nal);
                    if(sensational) {
                        return;
                    }
                    CompositionalRules.composeCompound(taskStatement, beliefStatement, 0, nal);
                    //if(taskSentence.getOccurenceTime()==Stamp.ETERNAL && belief.getOccurenceTime()==Stamp.ETERNAL)
                    CompositionalRules.introVarOuter(taskStatement, beliefStatement, 0, nal);//introVarImage(taskContent, beliefContent, index, memory);             
                    CompositionalRules.eliminateVariableOfConditionAbductive(figure,taskSentence,belief,nal);
                    
                }

                break;
            case 12:    // deduction                
                if (Variables.unify(VAR_INDEPENDENT, taskStatement.getSubject(), beliefStatement.getPredicate(), u)) {
                    taskStatement = (Statement) u[0];
                    beliefStatement = (Statement) u[1];
                    if (taskStatement.equals(beliefStatement)) {
                        return;
                    }
                    t1 = beliefStatement.getSubject();
                    t2 = taskStatement.getPredicate();
                    if (Variables.unify(VAR_QUERY, t1, t2, new Term[] { taskStatement, beliefStatement })) {
                        LocalRules.matchReverse(nal);
                    } else {
                        SyllogisticRules.dedExe(t1, t2, taskSentence, belief, nal);
                    }
                }
                break;
            case 21:    // exemplification
                if (Variables.unify(VAR_INDEPENDENT, taskStatement.getPredicate(), beliefStatement.getSubject(), u)) {
                    taskStatement = (Statement) u[0];
                    beliefStatement = (Statement) u[1];
                    if (taskStatement.equals(beliefStatement)) {
                        return;
                    }
                    t1 = taskStatement.getSubject();
                    t2 = beliefStatement.getPredicate();
                    
                    
                    if (Variables.unify(VAR_QUERY, t1, t2, new Term[] { taskStatement, beliefStatement })) {
                        LocalRules.matchReverse(nal);
                    } else {
                        SyllogisticRules.dedExe(t1, t2, taskSentence, belief, nal);
                    }
                }
                break;
            case 22:    // abduction
                if (Variables.unify(VAR_INDEPENDENT, taskStatement.getPredicate(), beliefStatement.getPredicate(), u)) {
                    taskStatement = (Statement) u[0];
                    beliefStatement = (Statement) u[1];
                    
                    if (taskStatement.equals(beliefStatement)) {
                        return;
                    }
                    t1 = taskStatement.getSubject();
                    t2 = beliefStatement.getSubject();
                    if (!SyllogisticRules.conditionalAbd(t1, t2, taskStatement, beliefStatement, nal)) {         // if conditional abduction, skip the following
                        boolean sensational = SyllogisticRules.abdIndCom(t1, t2, taskSentence, belief, figure, nal);
                        if(sensational) {
                            return;
                        }
                        CompositionalRules.composeCompound(taskStatement, beliefStatement, 1, nal);
                        CompositionalRules.introVarOuter(taskStatement, beliefStatement, 1, nal);// introVarImage(taskContent, beliefContent, index, memory);

                    }

                    CompositionalRules.eliminateVariableOfConditionAbductive(figure,taskSentence,belief,nal);
                    
                }
                break;
            default:
        }
    }

    /**
     * Syllogistic rules whose first premise is on an asymmetric relation, and
     * the second on a symmetric relation
     *
     * @param asym The asymmetric premise
     * @param sym The symmetric premise
     * @param figure The location of the shared term
     * @param nal Reference to the memory
     */
    private static void asymmetricSymmetric(final Sentence asym, final Sentence sym, final int figure, final DerivationContext nal) {
        Statement asymSt = (Statement) asym.term;
        Statement symSt = (Statement) sym.term;
        Term t1, t2;
        Term[] u = new Term[] { asymSt, symSt };
        switch (figure) {
            case 11:
                if (Variables.unify(VAR_INDEPENDENT, asymSt.getSubject(), symSt.getSubject(), u)) {
                    asymSt = (Statement) u[0];
                    symSt = (Statement) u[1];
                    t1 = asymSt.getPredicate();
                    t2 = symSt.getPredicate();
                    
                    if (Variables.unify(VAR_QUERY, t1, t2, u)) {                        
                        LocalRules.matchAsymSym(asym, sym, figure, nal);
                        
                    } else {
                        SyllogisticRules.analogy(t2, t1, asym, sym, figure, nal);
                    }
                    
                }
                break;
            case 12:
                if (Variables.unify(VAR_INDEPENDENT, asymSt.getSubject(), symSt.getPredicate(), u)) {
                    asymSt = (Statement) u[0];
                    symSt = (Statement) u[1];
                    t1 = asymSt.getPredicate();
                    t2 = symSt.getSubject();
                    
                    if (Variables.unify(VAR_QUERY, t1, t2, u)) {
                        LocalRules.matchAsymSym(asym, sym, figure, nal);
                    } else {
                        SyllogisticRules.analogy(t2, t1, asym, sym, figure, nal);
                    }
                }
                break;
            case 21:
                if (Variables.unify(VAR_INDEPENDENT, asymSt.getPredicate(), symSt.getSubject(), u)) {
                    asymSt = (Statement) u[0];
                    symSt = (Statement) u[1];
                    t1 = asymSt.getSubject();
                    t2 = symSt.getPredicate();
                    
                    if (Variables.unify(VAR_QUERY, t1, t2, u)) {                        
                        LocalRules.matchAsymSym(asym, sym, figure, nal);
                    } else {
                        SyllogisticRules.analogy(t1, t2, asym, sym, figure, nal);
                    }
                }
                break;
            case 22:
                if (Variables.unify(VAR_INDEPENDENT, asymSt.getPredicate(), symSt.getPredicate(), u)) {
                    asymSt = (Statement) u[0];
                    symSt = (Statement) u[1];
                    t1 = asymSt.getSubject();
                    t2 = symSt.getSubject();                    
                    
                    if (Variables.unify(VAR_QUERY, t1, t2, u)) {                        
                        LocalRules.matchAsymSym(asym, sym, figure, nal);
                    } else {
                        SyllogisticRules.analogy(t1, t2, asym, sym, figure, nal);
                    }
                }
                break;
        }
    }

    /**
     * Syllogistic rules whose both premises are on the same symmetric relation
     *
     * @param belief The premise that comes from a belief
     * @param taskSentence The premise that comes from a task
     * @param figure The location of the shared term
     * @param nal Reference to the memory
     */
    private static void symmetricSymmetric(final Sentence belief, final Sentence taskSentence, int figure, final DerivationContext nal) {
        Statement s1 = (Statement) belief.term;
        Statement s2 = (Statement) taskSentence.term;
        
        Term ut1, ut2;  //parameters for unify()
        Term rt1, rt2;  //parameters for resemblance()
        
        switch (figure) {
            case 11:
                ut1 = s1.getSubject();
                ut2 = s2.getSubject();
                rt1 = s1.getPredicate();
                rt2 = s2.getPredicate();
                break;
            case 12:
                ut1 = s1.getSubject();
                ut2 = s2.getPredicate();
                rt1 = s1.getPredicate();
                rt2 = s2.getSubject();
                break;
            case 21:
                ut1 = s1.getPredicate();
                ut2 = s2.getSubject();
                rt1 = s1.getSubject();
                rt2 = s2.getPredicate();
                break;
            case 22:
                ut1 = s1.getPredicate();
                ut2 = s2.getPredicate();
                rt1 = s1.getSubject();
                rt2 = s2.getSubject();
                break;
            default: 
                throw new RuntimeException("Invalid figure: " + figure);
        }
        
        Term[] u = new Term[] { s1, s2 };
        if (Variables.unify(VAR_INDEPENDENT, ut1, ut2, u)) {
            //recalculate rt1, rt2 from above:
            switch (figure) {
                case 11: rt1 = s1.getPredicate();   rt2 = s2.getPredicate(); break;
                case 12: rt1 = s1.getPredicate();   rt2 = s2.getSubject();  break;
                case 21: rt1 = s1.getSubject();     rt2 = s2.getPredicate(); break;
                case 22: rt1 = s1.getSubject();     rt2 = s2.getSubject();   break;
            }
            
            SyllogisticRules.resemblance(rt1, rt2, belief, taskSentence, figure, nal);

            CompositionalRules.eliminateVariableOfConditionAbductive(
                    figure, taskSentence, belief, nal);
            
        }

    }

    /* ----- conditional inferences ----- */
    /**
     * The detachment rule, with variable unification
     *
     * @param originalMainSentence The premise that is an Implication or
     * Equivalence
     * @param subSentence The premise that is the subject or predicate of the
     * first one
     * @param index The location of the second premise in the first
     * @param nal Reference to the memory
     */
    private static void detachmentWithVar(Sentence originalMainSentence, Sentence subSentence, int index, DerivationContext nal) {
        detachmentWithVar(originalMainSentence, subSentence, index, true, nal);
    }
    private static void detachmentWithVar(Sentence originalMainSentence, Sentence subSentence, int index, boolean checkTermAgain, DerivationContext nal) {
        if(originalMainSentence==null)  {
            return;
        }
        Sentence mainSentence = originalMainSentence;   // for substitution
        
        if (!(mainSentence.term instanceof Statement))
            return;
        
        Statement statement = (Statement) mainSentence.term;
        
        Term component = statement.term[index];
        Term content = subSentence.term;
        if (nal.getCurrentBelief() != null) {
            
            Term[] u = new Term[] { statement, content };
            
            if (!component.hasVarIndep() && !component.hasVarDep()) { //because of example: <<(*,w1,#2) --> [good]> ==> <w1 --> TRANSLATE>>. <(*,w1,w2) --> [good]>.
                SyllogisticRules.detachment(mainSentence, subSentence, index, checkTermAgain, nal);
            } else if (Variables.unify(VAR_INDEPENDENT, component, content, u)) { //happens through syllogisms
                mainSentence = mainSentence.clone(u[0]);
                subSentence = subSentence.clone(u[1]);
                SyllogisticRules.detachment(mainSentence, subSentence, index, false, nal);
            } else if ((statement instanceof Implication) && (statement.getPredicate() instanceof Statement) && (nal.getCurrentTask().sentence.isJudgment())) {
                Statement s2 = (Statement) statement.getPredicate();
                if ((content instanceof Statement) && (s2.getSubject().equals(((Statement) content).getSubject()))) {
                    CompositionalRules.introVarInner((Statement) content, s2, statement, nal);
                }
                CompositionalRules.IntroVarSameSubjectOrPredicate(originalMainSentence,subSentence,component,content,index,nal);
            } else if ((statement instanceof Equivalence) && (statement.getPredicate() instanceof Statement) && (nal.getCurrentTask().sentence.isJudgment())) {
                CompositionalRules.IntroVarSameSubjectOrPredicate(originalMainSentence,subSentence,component,content,index,nal);                
            }
        }
    }

    /**
     * Conditional deduction or induction, with variable unification
     *
     * @param conditional The premise that is an Implication with a Conjunction
     * as condition
     * @param index The location of the shared term in the condition
     * @param statement The second premise that is a statement
     * @param side The location of the shared term in the statement
     * @param nal Reference to the memory
     */
    private static void conditionalDedIndWithVar(Sentence conditionalSentence, Implication conditional, short index, Statement statement, short side, DerivationContext nal) {
        
        if (!(conditional.getSubject() instanceof CompoundTerm))
            return;
        
        CompoundTerm condition = (CompoundTerm) conditional.getSubject();  
        
        if(condition instanceof Conjunction) { //conditionalDedIndWithVar
            for(Term t : condition.term) {     //does not support the case where
                if(t instanceof Variable) {    //we have a variable inside of a conjunction
                    return;                    //(this can happen since we have # due to image transform,
                }                              //although not for other conjunctions)
            }
        }
        
        Term component = condition.term[index];
        Term component2 = null;
        if (statement instanceof Inheritance || statement instanceof Similarity) {
            component2 = statement;
            side = -1;
        } else if (statement instanceof Implication) {
            component2 = statement.term[side];
        }

        if (component2 != null) {
            Term[] u = new Term[] { conditional, statement };
            if (Variables.unify(VAR_INDEPENDENT, component, component2, u)) {
                conditional = (Implication) u[0];
                statement = (Statement) u[1];
                SyllogisticRules.conditionalDedInd(conditionalSentence, conditional, index, statement, side, nal);
            }
        }
    }

    /* ----- structural inferences ----- */
    /**
     * Inference between a compound term and a component of it
     *
     * @param compound The compound term
     * @param component The component term
     * @param compoundTask Whether the compound comes from the task
     * @param nal Reference to the memory
     */
     private static void compoundAndSelf(CompoundTerm compound, Term component, boolean compoundTask, int index, DerivationContext nal) {
        if ((compound instanceof Conjunction) || (compound instanceof Disjunction)) {
            if (nal.getCurrentBelief() != null) {
                if(compound.containsTerm(component)) {
                    StructuralRules.structuralCompound(compound, component, compoundTask, index, nal);
                }
                CompositionalRules.decomposeStatement(compound, component, compoundTask, index, nal);
            } else if (compound.containsTerm(component)) {
                StructuralRules.structuralCompound(compound, component, compoundTask, index, nal);
            }
        } else if (compound instanceof Negation) {
            if (compoundTask) {
                if (compound.term[0] instanceof CompoundTerm)
                    StructuralRules.transformNegation((CompoundTerm)compound.term[0], nal);
            }
        }
    }

    /**
     * Inference between two compound terms
     *
     * @param taskTerm The compound from the task
     * @param beliefTerm The compound from the belief
     * @param nal Reference to the memory
     */
    private static void compoundAndCompound(CompoundTerm taskTerm, CompoundTerm beliefTerm, int tindex, int bindex, DerivationContext nal) {
        if (taskTerm.getClass() == beliefTerm.getClass()) {
            if (taskTerm.size() >= beliefTerm.size()) {
                compoundAndSelf(taskTerm, beliefTerm, true, tindex, nal);
            } else if (taskTerm.size() < beliefTerm.size()) {
                compoundAndSelf(beliefTerm, taskTerm, false, bindex, nal);
            }
        }
    }

    /**
     * Inference between a compound term and a statement
     *
     * @param compound The compound term
     * @param index The location of the current term in the compound
     * @param statement The statement
     * @param side The location of the current term in the statement
     * @param beliefTerm The content of the belief
     * @param nal Reference to the memory
     */
    private static void compoundAndStatement(CompoundTerm compound, short index, Statement statement, short side, Term beliefTerm, DerivationContext nal) {        
        
        if(index >= compound.term.length) {
            return;
        }
        Term component = compound.term[index];
        
        Task task = nal.getCurrentTask();
        if (component.getClass() == statement.getClass()) {
            if ((compound instanceof Conjunction) && (nal.getCurrentBelief() != null)) {
                Conjunction conj = (Conjunction) compound;
                Term[] u = new Term[] { compound, statement };
                if (Variables.unify(VAR_DEPENDENT, component, statement, u)) {
                    compound = (Conjunction) u[0];
                    statement = (Statement) u[1];
                    if(conj.isSpatial || compound.getTemporalOrder() != TemporalRules.ORDER_FORWARD || //only allow dep var elimination
                            index == 0) { //for (&/ on first component!!
                        SyllogisticRules.elimiVarDep(compound, component, 
                                statement.equals(beliefTerm),
                                nal);
                    }
                } else if (task.sentence.isJudgment()) { // && !compound.containsTerm(component)) {
                    CompositionalRules.introVarInner(statement, (Statement) component, compound, nal);
                }
            }
        } else {
            if (task.sentence.isJudgment()) {
                if (statement instanceof Inheritance) {
                    StructuralRules.structuralCompose1(compound, index, statement, nal);
                    if (!(compound instanceof SetExt || compound instanceof SetInt || compound instanceof Negation
                            || compound instanceof Conjunction || compound instanceof Disjunction)) {
                        StructuralRules.structuralCompose2(compound, index, statement, side, nal);
                    }    // {A --> B, A @ (A&C)} |- (A&C) --> (B&C)
                } else if (!(compound instanceof Negation || compound instanceof Conjunction || compound instanceof Disjunction)) {
                    StructuralRules.structuralCompose2(compound, index, statement, side, nal);
                }       // {A <-> B, A @ (A&C)} |- (A&C) <-> (B&C)
            }
        }
    }

    /**
     * Inference between a component term (of the current term) and a statement
     *
     * @param compound The compound term
     * @param index The location of the current term in the compound
     * @param statement The statement
     * @param side The location of the current term in the statement
     * @param nal Reference to the memory
     */
    private static void componentAndStatement(CompoundTerm compound, short index, Statement statement, short side, DerivationContext nal) {
        if (statement instanceof Inheritance) {
            StructuralRules.structuralDecompose1(compound, index, statement, nal);
            if (!(compound instanceof SetExt) && !(compound instanceof SetInt)) {
                StructuralRules.structuralDecompose2(statement, index, nal);    // {(C-B) --> (C-A), A @ (C-A)} |- A --> B
            } else {
                StructuralRules.transformSetRelation(compound, statement, side, nal);
            }
        } else if (statement instanceof Similarity) {
            StructuralRules.structuralDecompose2(statement, index, nal);        // {(C-B) --> (C-A), A @ (C-A)} |- A --> B
            if ((compound instanceof SetExt) || (compound instanceof SetInt)) {
                StructuralRules.transformSetRelation(compound, statement, side, nal);
            }            
        } 
        
        else if ((statement instanceof Implication) && (compound instanceof Negation)) {
            if (index == 0) {
                StructuralRules.contraposition(statement, nal.getCurrentTask().sentence, nal);
            } else {
                StructuralRules.contraposition(statement, nal.getCurrentBelief(), nal);
            }        
        }
        
    }

    /* ----- inference with one TaskLink only ----- */
    /**
     * The TaskLink is of type TRANSFORM, and the conclusion is an equivalent
     * transformation
     *
     * @param tLink The task link
     * @param nal Reference to the memory
     */
    public static void transformTask(TaskLink tLink, DerivationContext nal) {
        CompoundTerm content = (CompoundTerm) nal.getCurrentTask().getTerm();
        short[] indices = tLink.index;
        Term inh = null;
        if ((indices.length == 2) || (content instanceof Inheritance)) {          // <(*, term, #) --> #>
            inh = content;
        } else if (indices.length == 3) {   // <<(*, term, #) --> #> ==> #>
            inh = content.term[indices[0]];
        } else if (indices.length == 4) {   // <(&&, <(*, term, #) --> #>, #) ==> #>
            Term component = content.term[indices[0]];
            if ((component instanceof Conjunction) && (((content instanceof Implication) && (indices[0] == 0)) || (content instanceof Equivalence))) {
                
                Term[] cterms = ((CompoundTerm) component).term;
                if (indices[1] < cterms.length-1) {
                    inh = cterms[indices[1]];
                }
                else {
                    return;
                }
            } else {
                return;
            }
        }
        if (inh instanceof Inheritance) {
            StructuralRules.transformProductImage((Inheritance) inh, content, indices, nal);
        }
    }
}
