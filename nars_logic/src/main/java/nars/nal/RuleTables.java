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
package nars.nal;

import nars.Global;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.LocalRules;
import nars.nal.nal1.Negation;
import nars.nal.nal2.Similarity;
import nars.nal.nal3.SetTensional;
import nars.nal.nal5.*;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal8.Operation;
import nars.nal.term.Compound;
import nars.nal.term.Statement;
import nars.nal.term.Term;
import nars.nal.term.Variable;
import nars.nal.tlink.TLink;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;

import java.util.Arrays;

import static nars.Symbols.*;

/**
 * Table of logic rules, indexed by the TermLinks for the task and the
 * belief. Used in indirective processing of a task, to dispatch logic cases
 * to the relevant logic rules.
 */
public class RuleTables {

    
    /* ----- syllogistic inferences ----- */
    /**
     * Meta-table of syllogistic rules, indexed by the content classes of the
     * taskSentence and the belief
     *
     * @param tLink The tlink to task
     * @param bLink The tlink to belief
     * @param taskTerm The content of task
     * @param beliefTerm The content of belief
     * @param nal Reference to the memory
     */
    public static void syllogisms(TaskLink tLink, TermLink bLink, Term taskTerm, Term beliefTerm, NAL nal) {
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
     * @param link1 The tlink to the first premise
     * @param link2 The tlink to the second premise
     * @return The figure of the syllogism, one of the four: 11, 12, 21, or 22
     */
    public static final  int indexToFigure(final TLink link1, final TLink link2) {
        final int i1 = link1.getFigureIndex(0);
        final int i2 = link2.getFigureIndex(0);
        return ((i1 + 1) * 10) + (i2 + 1);
    }

    /**
     * Syllogistic rules whose both premises are on the same asymmetric relation
     *
     * @param taskSentence The taskSentence in the task
     * @param belief The judgment in the belief
     * @param figure The location of the shared term
     * @param nal Reference to the memory
     */
    public static void asymmetricAsymmetric(final Sentence taskSentence, final Sentence belief, int figure, final NAL nal) {
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

    public static void goalFromQuestion(final Task task, final Term taskTerm, final NAL nal) {
        if(task.sentence.punctuation==Symbols.QUESTION && (taskTerm instanceof Implication || taskTerm instanceof Equivalence)) { //<a =/> b>? |- a!
            Term goalterm=null;
            Term goalterm2=null;
            if(taskTerm instanceof Implication) {
                Implication imp=(Implication)taskTerm;
                if(imp.getTemporalOrder()==TemporalRules.ORDER_FORWARD || imp.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT) {
                    if(!Global.CURIOSITY_FOR_OPERATOR_ONLY || imp.getSubject() instanceof Operation) {
                        goalterm=imp.getSubject();
                    }
                    if(goalterm instanceof Variable && goalterm.hasVarQuery() && (!Global.CURIOSITY_FOR_OPERATOR_ONLY || imp.getPredicate() instanceof Operation)) {
                        goalterm=imp.getPredicate(); //overwrite, it is a how question, in case of <?how =/> b> it is b! which is desired
                    }
                }
                else
                if(imp.getTemporalOrder()==TemporalRules.ORDER_BACKWARD) {
                    if(!Global.CURIOSITY_FOR_OPERATOR_ONLY || imp.getPredicate() instanceof Operation) {
                        goalterm=imp.getPredicate();
                    }
                    if(goalterm instanceof Variable && goalterm.hasVarQuery() && (!Global.CURIOSITY_FOR_OPERATOR_ONLY || imp.getSubject() instanceof Operation)) {
                        goalterm=imp.getSubject(); //overwrite, it is a how question, in case of <?how =/> b> it is b! which is desired
                    }
                }
            }
            else
            if(taskTerm instanceof Equivalence) {
                Equivalence qu=(Equivalence)taskTerm;
                if(qu.getTemporalOrder()== TemporalRules.ORDER_FORWARD || qu.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT) {
                    if(!Global.CURIOSITY_FOR_OPERATOR_ONLY || qu.getSubject() instanceof Operation) {
                        goalterm=qu.getSubject();
                    }
                    if(!Global.CURIOSITY_FOR_OPERATOR_ONLY || qu.getPredicate() instanceof Operation) {
                        goalterm2=qu.getPredicate();
                    }
                }
            }


            if(goalterm!=null && (goalterm instanceof Compound) && !goalterm.hasVarIndep()) {
                Truth truth=new Truth.DefaultTruth(1.0f, Global.DEFAULT_GOAL_CONFIDENCE*Global.CURIOSITY_DESIRE_CONFIDENCE_MUL);
                nal.singlePremiseTask((Compound) goalterm, Symbols.GOAL, truth,
                        new Budget(task.getPriority()*Global.CURIOSITY_DESIRE_PRIORITY_MUL,task.getDurability()*Global.CURIOSITY_DESIRE_DURABILITY_MUL, BudgetFunctions.truthToQuality(truth)),
                        nal.newStamp(task.sentence,nal.memory.time())
                );
            }
            if(goalterm2!=null && (goalterm2 instanceof Compound) && !goalterm2.hasVarIndep()) {
                Truth truth=new Truth.DefaultTruth(1.0f, Global.DEFAULT_GOAL_CONFIDENCE*Global.CURIOSITY_DESIRE_CONFIDENCE_MUL);
                nal.singlePremiseTask((Compound) goalterm2, Symbols.GOAL, truth,
                        new Budget(task.getPriority()*Global.CURIOSITY_DESIRE_PRIORITY_MUL,task.getDurability()*Global.CURIOSITY_DESIRE_DURABILITY_MUL,BudgetFunctions.truthToQuality(truth)),
                        nal.newStamp(task.sentence,nal.memory.time())
                );
            }
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
    public static void asymmetricSymmetric(final Sentence asym, final Sentence sym, final int figure, final NAL nal) {
        Statement asymSt = (Statement) asym.term;
        Statement symSt = (Statement) sym.term;
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
    public static void symmetricSymmetric(final Sentence belief, final Sentence taskSentence, int figure, final NAL nal) {
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
                default:
                    throw new RuntimeException("Invalid figure: " + figure);
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
    public static void detachmentWithVar(Sentence<Statement> originalMainSentence, Sentence subSentence, int index, NAL nal) {
        if(originalMainSentence==null)
            return;

        if (!(originalMainSentence.term instanceof Statement)) {
            return;
        }

        Sentence mainSentence = originalMainSentence;   // for substitution

        Statement statement = (Statement) mainSentence.term;
        
        Term component = statement.term[index];
        Term content = subSentence.term;
        if (((component instanceof Inheritance) || (component instanceof Negation)) && (nal.getCurrentBelief() != null)) {
            
            Term[] u = new Term[] { statement, content };
            
            if (!component.hasVar()) {
                SyllogisticRules.detachment(mainSentence, subSentence, index, nal);
            } else if (Variables.unify(VAR_INDEPENDENT, component, content, u)) {
                mainSentence = mainSentence.clone(u[0]);
                if (mainSentence!=null) {
                    subSentence = subSentence.clone(u[1]);
                    if (subSentence != null)
                        SyllogisticRules.detachment(mainSentence, subSentence, index, nal);
                }
            } else if ((statement instanceof Implication) && (statement.getPredicate() instanceof Statement) && (nal.getCurrentTask().sentence.isJudgment())) {
                Statement s2 = (Statement) statement.getPredicate();
                if ((content instanceof Statement) && (s2.getSubject().equals(((Statement) content).getSubject()))) {
                    CompositionalRules.introVarInner((Statement) content, s2, statement, nal);
                }

                CompositionalRules.introVarSameSubjectOrPredicate(originalMainSentence, subSentence, component, content, index, nal);
            } else if ((statement instanceof Equivalence) && (statement.getPredicate() instanceof Statement) && (nal.getCurrentTask().sentence.isJudgment())) {
                CompositionalRules.introVarSameSubjectOrPredicate(originalMainSentence, subSentence, component, content, index, nal);
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
    public static void conditionalDedIndWithVar(Implication conditional, short index, Statement statement, short side, NAL nal) {
        
        if (!(conditional.getSubject() instanceof Compound))
            return;
        
        Compound condition = (Compound) conditional.getSubject();
        
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
     public static boolean compoundAndSelf(Compound compound, Term component, boolean compoundTask, int index, NAL nal) {
        if (compound instanceof Junction) {
            if (nal.getCurrentBelief() != null) {
                return CompositionalRules.decomposeStatement(compound, component, compoundTask, index, nal);
            } else if (compound.containsTerm(component)) {
                return StructuralRules.structuralCompound(compound, component, compoundTask, index, nal);
            }
//        } else if ((compound instanceof Negation) && !memory.getCurrentTask().isStructural()) {
        } else if (compound instanceof Negation) {
            if (compoundTask) {
                if (compound.term[0] instanceof Compound)
                    return StructuralRules.transformNegation((Compound)compound.term[0], nal);
            } else {
                return StructuralRules.transformNegation(compound, nal);
            }
        }
        return false;
    }

    /**
     * Inference between two compound terms
     *
     * @param taskTerm The compound from the task
     * @param beliefTerm The compound from the belief
     * @param nal Reference to the memory
     */
    public static boolean compoundAndCompound(Compound taskTerm, Compound beliefTerm, int index, NAL nal) {
        if (Terms.equalType(taskTerm, beliefTerm)) {
            if (taskTerm.length() >= beliefTerm.length()) {
                return compoundAndSelf(taskTerm, beliefTerm, true, index, nal);
            } else if (taskTerm.length() < beliefTerm.length()) {
                return compoundAndSelf(beliefTerm, taskTerm, false, index, nal);
            }
        }
        return false;
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
    public static void compoundAndStatement(Compound compound, short index, Statement statement, short side, Term beliefTerm, NAL nal) {
        
        if(index>=compound.term.length) {
            throw new RuntimeException(index + " index out of bounds for compound " + compound + "( " + compound.getClass() + " = " + Arrays.toString(compound.term) + ") in compoundAndStatement with statement=" + statement);
        }
        Term component = compound.term[index];
        
        Task task = nal.getCurrentTask();
        if (Terms.equalType(component, statement, true)) {
            if ((compound instanceof Conjunction) && (nal.getCurrentBelief() != null)) {
                Term[] u = new Term[] { compound, statement };
                if (Variables.unify(VAR_DEPENDENT, component, statement, u)) {
                    compound = (Compound) u[0];
                    statement = (Statement) u[1];
                    SyllogisticRules.elimiVarDep(compound, component, 
                            statement.equals(beliefTerm),
                            nal);
                } else if (task.sentence.isJudgment()) { // && !compound.containsTerm(component)) {
                    CompositionalRules.introVarInner(statement, (Statement) component, compound, nal);
                } else if (Variables.unify(VAR_QUERY, component, statement, u)) {
                    compound = (Compound) u[0];
                    //statement = (Statement) u[1];
                    CompositionalRules.decomposeStatement(compound, component, true, index, nal);                    
                }
            }
        } else {
            if (task.sentence.isJudgment()) {
                if (statement instanceof Inheritance) {
                    StructuralRules.structuralCompose1(compound, index, statement, nal);
                    if (!(compound instanceof SetTensional || compound instanceof Negation)) {
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
    public static void componentAndStatement(Compound compound, short index, Statement statement, short side, NAL nal) {
        if (statement instanceof Inheritance) {
            StructuralRules.structuralDecompose1(compound, index, statement, nal);
            if (compound instanceof SetTensional) {
                StructuralRules.transformSetRelation(compound, statement, side, nal);
            }
            else {
                StructuralRules.structuralDecompose2(statement, index, nal);    // {(C-B) --> (C-A), A @ (C-A)} |- A --> B
            }
        } else if (statement instanceof Similarity) {
            StructuralRules.structuralDecompose2(statement, index, nal);        // {(C-B) --> (C-A), A @ (C-A)} |- A --> B
            if (compound instanceof SetTensional) {
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


}
