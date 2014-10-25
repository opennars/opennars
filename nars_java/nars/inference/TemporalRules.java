/*
 * Copyright (C) 2014 peiwang
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
package nars.inference;


import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.entity.TruthValue;
import nars.io.Symbols;
import nars.language.Conjunction;
import nars.language.Equivalence;
import nars.language.Implication;
import nars.language.Inheritance;
import nars.language.Interval;
import nars.language.Product;
import nars.language.Statement;
import nars.language.Term;
import nars.language.Variable;
import nars.language.Variables;
import nars.operator.Operation;

/**
 *
 * @author peiwang
 */
public class TemporalRules {

    public static final int ORDER_NONE = 2;
    public static final int ORDER_FORWARD = 1;
    public static final int ORDER_CONCURRENT = 0;
    public static final int ORDER_BACKWARD = -1;
    public static final int ORDER_INVALID = -2;

    public final static int reverseOrder(final int order) {
        if (order == ORDER_NONE) {
            return ORDER_NONE;
        } else {
            return -order;
        }
    }

    public final static boolean matchingOrder(final Sentence a, final Sentence b) {
        return matchingOrder(a.getTemporalOrder(), b.getTemporalOrder());
    }
    

    public final static boolean matchingOrder(final int order1, final int order2) {
        return (order1 == order2) || (order1 == ORDER_NONE) || (order2 == ORDER_NONE);
    }

    public final static int dedExeOrder(final int order1, final int order2) {
        int order = ORDER_INVALID;
        if ((order1 == order2) || (order2 == TemporalRules.ORDER_NONE)) {
            order = order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = order2;
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            order = order1;
        }
        return order;
    }

    public final static int abdIndComOrder(final int order1, final int order2) {
        int order = ORDER_INVALID;
        if (order2 == TemporalRules.ORDER_NONE) {
            order = order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = reverseOrder(order2);
        } else if ((order2 == TemporalRules.ORDER_CONCURRENT) || (order1 == -order2)) {
            order = order1;
        }
        return order;
    }

    public final static int analogyOrder(final int order1, final int order2, final int figure) {
        int order = ORDER_INVALID;
        if ((order2 == TemporalRules.ORDER_NONE) || (order2 == TemporalRules.ORDER_CONCURRENT)) {
            order = order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = (figure < 20) ? order2 : reverseOrder(order2);
        } else if (order1 == order2) {
            if ((figure == 12) || (figure == 21)) {
                order = order1;
            }
        } else if ((order1 == -order2)) {
            if ((figure == 11) || (figure == 22)) {
                order = order1;
            }
        }
        return order;
    }

    public static final int resemblanceOrder(final int order1, final int order2, final int figure) {
        int order = ORDER_INVALID;
        if ((order2 == TemporalRules.ORDER_NONE)) {
            order = (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = (figure % 10 == 1) ? order2 : reverseOrder(order2); // switch when 12 or 22
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            order = (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
        } else if (order1 == order2) {
            order = (figure == 21) ? order1 : -order1;
        }
        return order;
    }

    public static final int composeOrder(final int order1, final int order2) {
        int order = ORDER_INVALID;
        if (order2 == TemporalRules.ORDER_NONE) {
            order = order1;
        } else if (order1 == TemporalRules.ORDER_NONE) {
            order = order2;
        } else if (order1 == order2) {
            order = order1;
        }
        return order;
    }
    
    //helper function for temporal induction to not produce wrong terms, only one temporal operator is allowed
    public final static boolean tooMuchTemporalStatements(final Term t) {
        if(t==null) {
            return true;
        }
        return t.containedTemporalRelations() > 1;
    }
      
    // { A =/> B, B =/> C } |- (&/,A,B) =/> C
    // { A =/> B, (&/,B,...) =/> C } |-  (&/,A,B,...) =/> C
    //https://groups.google.com/forum/#!topic/open-nars/L1spXagCOh4
    public static void temporalInductionChain(final Sentence s1, final Sentence s2, final NAL nal) {
        //TODO prevent trying question sentences, may cause NPE
        
        //try if B1 unifies with B2, if yes, create new judgement
        Statement S1=(Implication) s1.content;
        Statement S2=(Implication) s2.content;
        Term A=S1.getSubject();
        Term B1=S1.getPredicate();
        Term B2=S2.getSubject();
        Term C=S2.getPredicate();
        ArrayList<Term> args=null;
        if(B2 instanceof Conjunction) {
            Conjunction CB2=((Conjunction)B2);
            if(CB2.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {                
                args = new ArrayList(CB2.term.length + 1);
                args.add(A);
                for (final Term t : CB2.term) args.add(t);
            }
        }
        else {
            args=Lists.newArrayList(A, B1);
        }
        
        if(args==null)
            return;
                
        //ok we have our B2, no matter if packed as first argument of &/ or directly, lets see if it unifies
        Term[] term = args.toArray(new Term[args.size()]);
        Term realB2 = term[1];
        if(Variables.hasSubstitute(Symbols.VAR_INDEPENDENT, B1, realB2)) {
            //ok it unifies, so lets create a &/ term
            int order1=s1.getTemporalOrder();
            int order2=s2.getTemporalOrder();
            Conjunction S=(Conjunction) Conjunction.make(term,order1);
            Implication whole=Implication.make(S, C,order2);
            if(whole!=null) {
                TruthValue truth = TruthFunctions.deduction(s1.truth, s2.truth);
                BudgetValue budget = BudgetFunctions.forward(truth, nal);
                /*if(budget!=null) {
                    budget.setPriority(Math.min(0.99f, budget.getPriority()+Parameters.TEMPORAL_JUDGEMENT_PRIORITY_INCREMENT));
                    budget.setDurability(Math.min(0.99f,budget.getDurability()+Parameters.TEMPORAL_JUDGEMENT_DURABILITY_INCREMENT));
                }*/
                nal.doublePremiseTask(whole, truth, budget, true);
            }
        }
    }
    
    
    static final Variable varInd0 = new Variable("$0");
    
    public static void temporalInduction(final Sentence s1, final Sentence s2, final NAL nal) {
        if ((s1.truth==null) || (s2.truth==null))
            return;
        
        Term t1 = s1.content;
        Term t2 = s2.content;
        Term t11=null;
        Term t22=null;
        
        //if(t1 instanceof Operation && t2 instanceof Operation) {
        //   return; //maybe too restrictive
        //}
        /*if(((t1 instanceof Implication || t1 instanceof Equivalence) && t1.getTemporalOrder()!=TemporalRules.ORDER_NONE) ||
           ((t2 instanceof Implication || t2 instanceof Equivalence) && t2.getTemporalOrder()!=TemporalRules.ORDER_NONE)) {
            return; //better, if this is fullfilled, there would be more than one temporal operator in the statement, return
        }*/
        
        //since induction shouldnt miss something trivial random is not good here
        if (/*Memory.randomNumber.nextDouble()>0.5 &&*/ (t1 instanceof Inheritance) && (t2 instanceof Inheritance)) {
            Statement ss1 = (Statement) t1;
            Statement ss2 = (Statement) t2;

            
            
            Variable var1 = varInd0;
            Variable var2 = var1;

            if (ss1.getSubject().equals(ss2.getSubject())) {
                t11 = Statement.make(ss1, var1, ss1.getPredicate());
                t22 = Statement.make(ss2, var2, ss2.getPredicate());
            } else if (ss1.getPredicate().equals(ss2.getPredicate())) {
                t11 = Statement.make(ss1, ss1.getSubject(), var1);
                t22 = Statement.make(ss2, ss2.getSubject(), var2);
            }
            //allow also temporal induction on operator arguments:
            if(ss2 instanceof Operation ^ ss1 instanceof Operation) {
                if(ss2 instanceof Operation && !(ss2.getSubject() instanceof Variable)) {//it is an operation, let's look if one of the arguments is same as the subject of the other term
                    boolean anyone=false;
                    Term comp=ss1.getSubject();
                    Term ss2_term = ((Operation)ss2).getSubject();
                    if(ss2_term instanceof Product) {
                        Product ss2_prod=(Product) ss2_term;
                        for(final Term t : ss2_prod.term)
                        {
                            if(t.equals(comp)) {
                                anyone=true;
                            }
                        }
                        if(anyone && !(comp instanceof Variable && ((Variable)comp).getType()==Symbols.VAR_INDEPENDENT)) { //only if there is one and it isnt a variable already
                            Term[] ars = ss2_prod.cloneTerms();
                            for(int i=0;i<ars.length;i++) {
                                if(ars[i].equals(comp)) {
                                    ars[i]=var1;
                                }
                            }

                            t11 = Statement.make(ss1, var1, ss1.getPredicate());
                            Product S=(Product) Product.make(ars);
                            Operation op=(Operation) Operation.make(S, ss2.getPredicate());
                            t22 = op;
                        }
                    }
                }
            }
        }
        
        if (Statement.invalidStatement(t1, t2)) {
            return;
        }
        
        final Interval.AtomicDuration duration = nal.mem().param.duration;
        int durationCycles = duration.get();
        
        long time1 = s1.getOccurenceTime();
        long time2 = s2.getOccurenceTime();
        long timeDiff = time2 - time1;
        List<Interval> interval;
        if (Math.abs(timeDiff) > durationCycles) {
            interval = Interval.intervalTimeSequence(Math.abs(timeDiff), Parameters.TEMPORAL_INTERVAL_PRECISION, nal.mem());
            if (timeDiff > 0) {
                t1 = Conjunction.make(t1, interval, ORDER_FORWARD);
                if(t11!=null) {
                    t11 = Conjunction.make(t11, interval, ORDER_FORWARD);
                }
            } else {
                t2 = Conjunction.make(t2, interval, ORDER_FORWARD);
                if(t22!=null) {
                    t22 = Conjunction.make(t22, interval, ORDER_FORWARD);
                }
            }
        }
        int order;
        if (timeDiff > durationCycles) {
            order = TemporalRules.ORDER_FORWARD;
        } else if (timeDiff < -durationCycles) {
            order = TemporalRules.ORDER_BACKWARD;
        } else {
            order = TemporalRules.ORDER_CONCURRENT;
        }
        TruthValue givenTruth1 = s1.truth;
        TruthValue givenTruth2 = s2.truth;
        TruthValue truth1 = TruthFunctions.abduction(givenTruth1, givenTruth2);
        TruthValue truth2 = TruthFunctions.abduction(givenTruth2, givenTruth1);
        TruthValue truth3 = TruthFunctions.comparison(givenTruth1, givenTruth2);
        BudgetValue budget1 = BudgetFunctions.forward(truth1, nal);
        BudgetValue budget2 = BudgetFunctions.forward(truth2, nal);
        //only boost for this one
        /*if(budget2!=null) {
            budget2.setPriority(Math.min(0.99f, budget2.getPriority()+Parameters.TEMPORAL_JUDGEMENT_PRIORITY_INCREMENT));
            budget2.setDurability(Math.min(0.99f,budget2.getDurability()+Parameters.TEMPORAL_JUDGEMENT_DURABILITY_INCREMENT));
        }*/
        BudgetValue budget3 = BudgetFunctions.forward(truth3, nal);
        Statement statement1 = Implication.make(t1, t2, order);
        Statement statement2 = Implication.make(t2, t1, reverseOrder(order));
        Statement statement3 = Equivalence.make(t1, t2, order);
        if(t11!=null && t22!=null) {
            Statement statement11 = Implication.make(t11, t22, order);
            Statement statement22 = Implication.make(t22, t11, reverseOrder(order));
            Statement statement33 = Equivalence.make(t11, t22, order);
            if(!tooMuchTemporalStatements(statement11)) {
                nal.doublePremiseTask(statement11, truth1, budget1,false);
            }
            if(!tooMuchTemporalStatements(statement22)) {
                nal.doublePremiseTask(statement22, truth2, budget2,false);
            }
            if(!tooMuchTemporalStatements(statement33)) {
                nal.doublePremiseTask(statement33, truth3, budget3,false);
            }
        }
        if(!tooMuchTemporalStatements(statement1)) {
            nal.doublePremiseTask(statement1, truth1, budget1,false);
        }
        if(!tooMuchTemporalStatements(statement2)) {
            nal.doublePremiseTask(statement2, truth2, budget2,true); //=/> only to  keep graph simple for now
        }
        if(!tooMuchTemporalStatements(statement3)) {
            nal.doublePremiseTask(statement3, truth3, budget3,false);
        }
    }
    
    /**
     * Evaluate the quality of the judgment as a solution to a problem
     *
     * @param problem A goal or question
     * @param solution The solution to be evaluated
     * @return The quality of the judgment as the solution
     */
    public static float solutionQuality(final Sentence problem, final Sentence solution, Memory memory) {
        if (!matchingOrder(problem.getTemporalOrder(), solution.getTemporalOrder())) {
            return 0.0F;
        }
        TruthValue truth = solution.truth;
        if (problem.getOccurenceTime() != solution.getOccurenceTime()) {
            //TODO avoid creating entire Sentence; 
            //only calculate TruthValue which is all that is useful here
            Sentence cloned = solution.projection(problem.getOccurenceTime(), memory.time());
            truth = cloned.truth;
        }
        if (problem.containQueryVar()) {
            return truth.getExpectation() / solution.content.getComplexity();
        } else {
            return truth.getConfidence();
        }
    }


    /* ----- Functions used both in direct and indirect processing of tasks ----- */
    /**
     * Evaluate the quality of a belief as a solution to a problem, then reward
     * the belief and de-prioritize the problem
     *
     * @param problem The problem (question or goal) to be solved
     * @param solution The belief as solution
     * @param task The task to be immediately processed, or null for continued
     * process
     * @return The budget for the new task which is the belief activated, if
     * necessary
     */
    public static BudgetValue solutionEval(final Sentence problem, final Sentence solution, Task task, final NAL nal) {
        BudgetValue budget = null;
        boolean feedbackToLinks = false;
        if (task == null) {
            task = nal.getCurrentTask();
            feedbackToLinks = true;
        }
        boolean judgmentTask = task.sentence.isJudgment();
        final float quality = TemporalRules.solutionQuality(problem, solution, nal.mem());
        if (judgmentTask) {
            task.incPriority(quality);
        } else {
            float taskPriority = task.getPriority();
            budget = new BudgetValue(UtilityFunctions.or(taskPriority, quality), task.getDurability(), BudgetFunctions.truthToQuality(solution.truth));
            task.setPriority(Math.min(1 - quality, taskPriority));
        }
        if (feedbackToLinks) {
            TaskLink tLink = nal.getCurrentTaskLink();
            tLink.setPriority(Math.min(1 - quality, tLink.getPriority()));
            TermLink bLink = nal.getCurrentBeliefLink();
            bLink.incPriority(quality);
        }
        return budget;
    }
}
