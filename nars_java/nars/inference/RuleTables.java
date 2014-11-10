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

import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.core.control.NAL;
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
import nars.language.Interval;
import nars.language.Negation;
import nars.language.Product;
import nars.language.SetExt;
import nars.language.SetInt;
import nars.language.Similarity;
import nars.language.Statement;
import nars.language.Term;
import static nars.language.Terms.equalSubTermsInRespectToImageAndProduct;
import nars.language.Variables;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * Table of inference rules, indexed by the TermLinks for the task and the
 * belief. Used in indirective processing of a task, to dispatch inference cases
 * to the relevant inference rules.
 */
public class RuleTables {

    
    public static void InternalOperations(Memory memory,Sentence belief, NAL nal, Term beliefTerm, Term taskTerm) {
    
        if(Parameters.INTERNAL_EXPERIENCE_FULL && Memory.randomNumber.nextDouble()<Parameters.INTERNAL_EXPERIENCE_PROBABILITY &&
                Memory.randomNumber.nextDouble()<0.25) { //make 4 times less probable to do one of those than by innate beliefs for now
            String[] ops=new String[]{"^remind","^doubt","^consider","^evaluate","hestitate","^wonder","^belief","^want"}; //the operators which dont have a innate belief
            //also get a chance to reveal its effects to the system this way
            Operator op=memory.getOperator(ops[Memory.randomNumber.nextInt(ops.length)]);
            Product prod=new Product(new Term[]{belief.content});
            
            if(op!=null && prod!=null) {
                
                Term new_term=Inheritance.make(prod, op);
                Sentence sentence = new Sentence(
                    new_term, Symbols.GOAL_MARK, 
                    new TruthValue(1, Parameters.DEFAULT_JUDGMENT_CONFIDENCE),  // a naming convension
                    new Stamp(memory));
                
                float quality = BudgetFunctions.truthToQuality(sentence.truth);
                BudgetValue budget = new BudgetValue(
                    Parameters.DEFAULT_GOAL_PRIORITY*Parameters.INTERNAL_EXPERIENCE_PRIORITY_MUL, 
                    Parameters.DEFAULT_GOAL_DURABILITY*Parameters.INTERNAL_EXPERIENCE_DURABILITY_MUL, 
                    quality);

                Task newTask = new Task(sentence, budget);       
                nal.derivedTask(newTask, false, false, null, null);
            }
        }

        if(Parameters.INTERNAL_EXPERIENCE_FULL && beliefTerm instanceof Implication && Memory.randomNumber.nextDouble()<=Parameters.INTERNAL_EXPERIENCE_PROBABILITY) {
            Implication imp=(Implication) beliefTerm;
            if(imp.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
                //1. check if its (&/,term,+i1,...,+in) =/> anticipateTerm form:
                boolean valid=true;
                if(imp.getSubject() instanceof Conjunction) {
                    Conjunction conj=(Conjunction) imp.getSubject();
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
                    Operator op=memory.getOperator("^anticipate");
                    Product args=new Product(new Term[]{imp.getPredicate()});
                    Term new_term=Operation.make(args, /* --> */ op);

                    Sentence sentence = new Sentence(
                        new_term, Symbols.GOAL_MARK, 
                        new TruthValue(1, Parameters.DEFAULT_JUDGMENT_CONFIDENCE),  // a naming convension
                        new Stamp(memory));

                    float quality = BudgetFunctions.truthToQuality(sentence.truth);
                    BudgetValue budget = new BudgetValue(
                        Parameters.DEFAULT_GOAL_PRIORITY*Parameters.INTERNAL_EXPERIENCE_PRIORITY_MUL, 
                        Parameters.DEFAULT_GOAL_DURABILITY*Parameters.INTERNAL_EXPERIENCE_DURABILITY_MUL, 
                        quality);

                    Task newTask = new Task(sentence, budget);       
                    nal.derivedTask(newTask, false, false, null, null);
                }
            }
        }
    }
    
    /**
     * Entry point of the inference engine
     *
     * @param tLink The selected TaskLink, which will provide a task
     * @param bLink The selected TermLink, which may provide a belief
     * @param memory Reference to the memory
     */
    public static void reason(final TaskLink tLink, final TermLink bLink, final NAL nal) {
        final Memory memory = nal.mem();
        
        memory.logic.REASON.commit(tLink.getPriority());
        
        final Task task = nal.getCurrentTask();
        final Sentence taskSentence = task.sentence;
        
        final Term taskTerm = taskSentence.content;         // cloning for substitution
        final Term beliefTerm = bLink.target;       // cloning for substitution
        
        
        //CONTRAPOSITION //TODO: put into rule table
        if ((taskTerm instanceof Statement) && (taskTerm instanceof Implication) && (taskSentence.isJudgment())) {
            if(((Statement)taskTerm).getSubject() instanceof Negation && bLink.target.equals(((Statement)taskTerm).getSubject())) { 
                StructuralRules.contraposition((Statement)taskTerm, taskSentence, nal); 
            } 
        }        

        
        if(equalSubTermsInRespectToImageAndProduct(taskTerm,beliefTerm))
           return;
        
        
        Concept beliefConcept = memory.concept(beliefTerm);
        Sentence belief = (beliefConcept != null) ? beliefConcept.getBelief(nal, task) : null;
        
        nal.setCurrentBelief( belief );  // may be null
        
        if (belief != null) {   
            
            //TODO
            //(&/,a) goal didnt get unwinded, so lets unwind it
            if(task.sentence.content instanceof Conjunction && task.sentence.punctuation==Symbols.GOAL_MARK) {
                Conjunction s=(Conjunction) task.sentence.content;
                Term newterm=s.term[0];
                TruthValue truth=task.sentence.truth;
                BudgetValue newBudget=BudgetFunctions.forward(TruthFunctions.deduction(truth, truth), nal);
                nal.doublePremiseTask(newterm, truth, newBudget, false);
            }
            
            InternalOperations(memory, belief, nal, beliefTerm, taskTerm);
            
             //this is a new attempt/experiment to make nars effectively track temporal coherences
            if(beliefTerm instanceof Implication && belief.isEternal() && 
                    (beliefTerm.getTemporalOrder()==TemporalRules.ORDER_FORWARD || beliefTerm.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT)) {
                for(int i=0;i<Parameters.TEMPORAL_CHAINING_ATTEMPTS;i++) {
                    
                    Task best=nal.memory.temporalCoherences.takeNext();
                    if (best == null) {
                        break;                        
                    }
                    
                    nal.memory.temporalCoherences.putBack(best, memory.param.cycles(memory.param.termLinkForgetDurations), memory);
                    
                    Sentence s=best.sentence;
                    Term t=s.content;
                    
                    if(!(t instanceof Implication) || s.getOccurenceTime()!=Stamp.ETERNAL)
                        continue;
                    
                    Implication Imp=(Implication) t;
                    if(Imp.getTemporalOrder()!=TemporalRules.ORDER_FORWARD && Imp.getTemporalOrder()!=TemporalRules.ORDER_CONCURRENT) {
                        continue;
                    }

                    Task sich=nal.getCurrentTask();
                    nal.setCurrentTask(best);
                    
                    if(TemporalRules.temporalInductionChain(s, belief, nal)) {
                        break;
                    }
                    
                    nal.setCurrentTask(sich);
                }
            }

            //while this is the old way, which seem to miss so many temporal coherences that it is not even worth the rule:
            if(beliefTerm instanceof Implication && 
             (beliefTerm.getTemporalOrder()==TemporalRules.ORDER_FORWARD || beliefTerm.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT) &&
             taskTerm instanceof Implication && 
             (taskTerm.getTemporalOrder()==TemporalRules.ORDER_FORWARD || taskTerm.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT)) {
                if(taskSentence.isEternal() && belief.isEternal() ||
                   belief.after(taskSentence, memory.param.duration.get())) {
                    TemporalRules.temporalInductionChain(taskSentence, belief, nal);
                }
            }
            if (LocalRules.match(task, belief, nal)) {
                //new tasks resulted from the match, so return
                return;
            }
        }
        
        
        // to be invoked by the corresponding links 
        if (CompositionalRules.dedSecondLayerVariableUnification(task, nal)) {
            //unification ocurred, done reasoning in this cycle if it's judgment
            if (taskSentence.isJudgment())
                return;
        }

        
        //current belief and task may have changed, so set again:
        nal.setCurrentBelief(belief);
        nal.setCurrentTask(task);
        
        /*if ((memory.getNewTaskCount() > 0) && taskSentence.isJudgment()) {
            return;
        }*/
        
        CompositionalRules.dedConjunctionByQuestion(taskSentence, belief, nal);
        
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
                        }
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            SyllogisticRules.detachment(belief, taskSentence, bIndex, nal);
                        }
                        break;
                    case TermLink.COMPONENT_CONDITION:
                        if ((belief != null) && (taskTerm instanceof Implication)) {
                            bIndex = bLink.getIndex(1);
                            SyllogisticRules.conditionalDedInd((Implication) taskTerm, bIndex, beliefTerm, tIndex, nal);
                        }
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if ((belief != null) && (taskTerm instanceof Implication) && (beliefTerm instanceof Implication)) {
                            bIndex = bLink.getIndex(1);
                            SyllogisticRules.conditionalDedInd((Implication) beliefTerm, bIndex, taskTerm, tIndex, nal);
                        }
                        break;
                }
                break;
            case TermLink.COMPOUND:
                switch (bLink.type) {
                    case TermLink.COMPOUND:
                        compoundAndCompound((CompoundTerm) taskTerm, (CompoundTerm) beliefTerm, bIndex, nal);
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        compoundAndStatement((CompoundTerm) taskTerm, tIndex, (Statement) beliefTerm, bIndex, beliefTerm, nal);
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if (belief != null) {
                            if (beliefTerm instanceof Implication) {
                                Term[] u = new Term[] { beliefTerm, taskTerm };
                                if (Variables.unify(VAR_INDEPENDENT, ((Statement) beliefTerm).getSubject(), taskTerm, u)) {
                                    Sentence newBelief = belief.clone(u[0]);
                                    Sentence newTaskSentence = taskSentence.clone(u[1]);
                                    detachmentWithVar(newBelief, newTaskSentence, bIndex, nal);
                                } else {
                                    SyllogisticRules.conditionalDedInd((Implication) beliefTerm, bIndex, taskTerm, -1, nal);
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
                                
                                //THIS CONDITION IS NEW:
                                //Because this one shouldnt happen:
                                //IN <(&/,<{door5} --> opened>,+1) =/> (^deactivate,{switch0})>. %1.00;0.90%
                                //IN <{door5} --> opened>! %1.00;0.90%
                                //EXE (^deactivate,{switch0})
                                //TODO: ANALYZE IN DETAIL
                                if(taskSentence.punctuation==Symbols.JUDGMENT_MARK) {
                                    conditionalDedIndWithVar((Implication) beliefTerm, bIndex, (Statement) taskTerm, tIndex, nal);
                                }
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
                                    conditionalDedIndWithVar((Implication) taskTerm, tIndex, (Statement) beliefTerm, bIndex, nal);
                                    }
                                }
                                break;
                            
                        }
                        break;
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
    private static void syllogisms(TaskLink tLink, TermLink bLink, Term taskTerm, Term beliefTerm, NAL nal) {
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
    private static void asymmetricAsymmetric(final Sentence taskSentence, final Sentence belief, int figure, final NAL nal) {
        Statement taskStatement = (Statement) taskSentence.content;
        Statement beliefStatement = (Statement) belief.content;
        
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
                    SyllogisticRules.abdIndCom(t1, t2, taskSentence, belief, figure, nal);

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
                        SyllogisticRules.abdIndCom(t1, t2, taskSentence, belief, figure, nal);
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
    private static void asymmetricSymmetric(final Sentence asym, final Sentence sym, final int figure, final NAL nal) {
        Statement asymSt = (Statement) asym.content;
        Statement symSt = (Statement) sym.content;
        Term t1, t2;
        Term[] u = new Term[] { asymSt, symSt };
        switch (figure) {
            case 11:
                if (Variables.unify(VAR_INDEPENDENT, asymSt.getSubject(), symSt.getSubject(), u)) {
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
    private static void symmetricSymmetric(final Sentence belief, final Sentence taskSentence, int figure, final NAL nal) {
        Statement s1 = (Statement) belief.content;
        Statement s2 = (Statement) taskSentence.content;
        
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
    private static void detachmentWithVar(Sentence originalMainSentence, Sentence subSentence, int index, NAL nal) {
        if(originalMainSentence==null)  {
            return;
        }
        Sentence mainSentence = originalMainSentence;   // for substitution
        
        if (!(mainSentence.content instanceof Statement))
            return;
        
        Statement statement = (Statement) mainSentence.content;
        
        Term component = statement.term[index];
        Term content = subSentence.content;
        if (((component instanceof Inheritance) || (component instanceof Negation)) && (nal.getCurrentBelief() != null)) {
            
            Term[] u = new Term[] { statement, content };
            
            if (!component.hasVar()) {
                SyllogisticRules.detachment(mainSentence, subSentence, index, nal);
            } else if (Variables.unify(VAR_INDEPENDENT, component, content, u)) {
                mainSentence = mainSentence.clone(u[0]);
                subSentence = subSentence.clone(u[1]);
                SyllogisticRules.detachment(mainSentence, subSentence, index, nal);
            } else if ((statement instanceof Implication) && (statement.getPredicate() instanceof Statement) && (nal.getCurrentTask().sentence.isJudgment())) {
                Statement s2 = (Statement) statement.getPredicate();
                if (s2.getSubject().equals(((Statement) content).getSubject())) {
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
    private static void conditionalDedIndWithVar(Implication conditional, short index, Statement statement, short side, NAL nal) {
        
        if (!(conditional.getSubject() instanceof CompoundTerm))
            return;
        
        CompoundTerm condition = (CompoundTerm) conditional.getSubject();        
        
        Term component = condition.term[index];
        Term component2 = null;
        if (statement instanceof Inheritance) {
            component2 = statement;
            side = -1;
        } else if (statement instanceof Implication) {
            component2 = statement.term[side];
        }

        if (component2 != null) {
            Term[] u = new Term[] { conditional, statement };
            boolean unifiable = Variables.unify(VAR_INDEPENDENT, component, component2, u);
            if (!unifiable) {
                unifiable = Variables.unify(VAR_DEPENDENT, component, component2, u);
            }
            if (unifiable) {
                conditional = (Implication) u[0];
                statement = (Statement) u[1];
                SyllogisticRules.conditionalDedInd(conditional, index, statement, side, nal);
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
     private static void compoundAndSelf(CompoundTerm compound, Term component, boolean compoundTask, int index, NAL nal) {
        if ((compound instanceof Conjunction) || (compound instanceof Disjunction)) {
            if (nal.getCurrentBelief() != null) {
                CompositionalRules.decomposeStatement(compound, component, compoundTask, index, nal);
            } else if (compound.containsTerm(component)) {
                StructuralRules.structuralCompound(compound, component, compoundTask, index, nal);
            }
//        } else if ((compound instanceof Negation) && !memory.getCurrentTask().isStructural()) {
        } else if (compound instanceof Negation) {
            if (compoundTask) {
                if (compound.term[0] instanceof CompoundTerm)
                    StructuralRules.transformNegation((CompoundTerm)compound.term[0], nal);
            } else {
                StructuralRules.transformNegation(compound, nal);
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
    private static void compoundAndCompound(CompoundTerm taskTerm, CompoundTerm beliefTerm, int index, NAL nal) {
        if (taskTerm.getClass() == beliefTerm.getClass()) {
            if (taskTerm.size() > beliefTerm.size()) {
                compoundAndSelf(taskTerm, beliefTerm, true, index, nal);
            } else if (taskTerm.size() < beliefTerm.size()) {
                compoundAndSelf(beliefTerm, taskTerm, false, index, nal);
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
    private static void compoundAndStatement(CompoundTerm compound, short index, Statement statement, short side, Term beliefTerm, NAL nal) {
        Term component = compound.term[index];
        Task task = nal.getCurrentTask();
        if (component.getClass() == statement.getClass()) {
            if ((compound instanceof Conjunction) && (nal.getCurrentBelief() != null)) {
                Term[] u = new Term[] { compound, statement };
                if (Variables.unify(VAR_DEPENDENT, component, statement, u)) {
                    compound = (CompoundTerm) u[0];
                    statement = (Statement) u[1];
                    SyllogisticRules.elimiVarDep(compound, component, statement.equals(beliefTerm), nal);
                } else if (task.sentence.isJudgment()) { // && !compound.containsTerm(component)) {
                    CompositionalRules.introVarInner(statement, (Statement) component, compound, nal);
                } else if (Variables.unify(VAR_QUERY, component, statement, u)) {
                    compound = (CompoundTerm) u[0];
                    statement = (Statement) u[1];                    
                    CompositionalRules.decomposeStatement(compound, component, true, index, nal);                    
                }
            }
        } else {
            if (task.sentence.isJudgment()) {
                if (statement instanceof Inheritance) {
                    StructuralRules.structuralCompose1(compound, index, statement, nal);
                    if (!(compound instanceof SetExt || compound instanceof SetInt || compound instanceof Negation)) {
                        StructuralRules.structuralCompose2(compound, index, statement, side, nal);
                    }    // {A --> B, A @ (A&C)} |- (A&C) --> (B&C)
                } else if ((statement instanceof Similarity) && !(compound instanceof Conjunction)) {
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
    private static void componentAndStatement(CompoundTerm compound, short index, Statement statement, short side, NAL nal) {
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
        
       /* else if ((statement instanceof Implication) && (compound instanceof Negation)) {
            if (index == 0) {
                StructuralRules.contraposition(statement, nal.getCurrentTask().sentence, nal);
            } else {
                StructuralRules.contraposition(statement, nal.getCurrentBelief(), nal);
            }        
        }*/
        
    }

    /* ----- inference with one TaskLink only ----- */
    /**
     * The TaskLink is of type TRANSFORM, and the conclusion is an equivalent
     * transformation
     *
     * @param tLink The task link
     * @param nal Reference to the memory
     */
    public static void transformTask(TaskLink tLink, NAL nal) {
        CompoundTerm content = (CompoundTerm) nal.getCurrentTask().getContent();
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
                if (indices[1] < cterms.length-1)
                    inh = cterms[indices[1]];
                else
                    return;
                
            } else {
                return;
            }
        }
        if (inh instanceof Inheritance) {
            StructuralRules.transformProductImage((Inheritance) inh, content, indices, nal);
        }
    }
}
