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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.entity.TruthValue;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Equivalence;
import nars.language.Implication;
import nars.language.Inheritance;
import nars.language.Interval;
import nars.language.Product;
import nars.language.Similarity;
import nars.language.Statement;
import nars.language.Term;
import nars.language.Terms;
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
        int order1Reverse = reverseOrder(order1);
        
        if ((order2 == TemporalRules.ORDER_NONE)) {
            order = (figure > 20) ? order1 : order1Reverse; // switch when 11 or 12
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = (figure % 10 == 1) ? order2 : reverseOrder(order2); // switch when 12 or 22
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            order = (figure > 20) ? order1 : order1Reverse; // switch when 11 or 12
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
    
    /** whether temporal induction can generate a task by avoiding producing wrong terms; only one temporal operator is allowed */
    public final static boolean tooMuchTemporalStatements(final Term t) {
        return (t == null) || (t.containedTemporalRelations() > 1);
    }
      
    // { A =/> B, B =/> C } |- (&/,A,B) =/> C
    // { A =/> B, (&/,B,...) =/> C } |-  (&/,A,B,...) =/> C
    //https://groups.google.com/forum/#!topic/open-nars/L1spXagCOh4
    public static boolean temporalInductionChain(final Sentence s1, final Sentence s2, final nars.core.control.NAL nal) {
        
        //prevent trying question sentences, causes NPE
        if ((s1.truth == null) || (s2.truth == null))
            return false;
        
        //try if B1 unifies with B2, if yes, create new judgement
        Implication S1=(Implication) s1.term;
        Implication S2=(Implication) s2.term;
        Term A=S1.getSubject();
        Term B1=S1.getPredicate();
        Term B2=S2.getSubject();
        Term C=S2.getPredicate();
        ArrayList<Term> args=null;
        
        int beginoffset=0;
        if(B2 instanceof Conjunction) {
            Conjunction CB2=((Conjunction)B2);
            if(CB2.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {       
                if(A instanceof Conjunction && ((Conjunction)A).getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
                    Conjunction ConjA=(Conjunction) A;
                    args=new ArrayList(CB2.term.length+ConjA.term.length);
                    beginoffset=ConjA.size();
                    
                    for(final Term t: ConjA.term) args.add(t);
                } else {
                    args = new ArrayList(CB2.term.length + 1);
                    args.add(A);
                    beginoffset=1;
                }
                for (final Term t : CB2.term) args.add(t);
            }
        }
        else {
            args=Lists.newArrayList(A, B1);
        }
        
        if(args==null)
            return false;
                
        //ok we have our B2, no matter if packed as first argument of &/ or directly, lets see if it unifies
        Term[] term = args.toArray(new Term[args.size()]);
        Term realB2 = term[beginoffset];
        HashMap<Term, Term> res1 = new HashMap<>();
        HashMap<Term, Term> res2 = new HashMap<>();

        if(Variables.findSubstitute(Symbols.VAR_INDEPENDENT, B1, realB2, res1,res2)) {
            //ok it unifies, so lets create a &/ term
            for(int i=0;i<term.length;i++) {
                if(term[i] instanceof CompoundTerm) {
                    term[i]=((CompoundTerm) term[i]).applySubstitute(res1);
                    if(term[i]==null) { 
                        //it resulted in invalid term for example <a --> a>, so wrong
                        return false;
                    }
                }
            }
            int order1=s1.getTemporalOrder();
            int order2=s2.getTemporalOrder();
            Term S = Conjunction.make(term,order1);
            //check if term has a element which is equal to C
            for(Term t : term) {
                if(Terms.equalSubTermsInRespectToImageAndProduct(t, C)) {
                    return false;
                }
                for(Term u : term) {
                    if(u!=t) { //important: checking reference here is as it should be!
                        if(Terms.equalSubTermsInRespectToImageAndProduct(t, u)) {
                            return false;
                        }
                    }
                }
            }
            Implication whole=Implication.make(S, C,order2);
            
            if(whole!=null) {
                TruthValue truth = TruthFunctions.induction(s1.truth, s2.truth);
                BudgetValue budget = BudgetFunctions.compoundForward(truth,whole, nal);
                budget.setPriority((float) Math.min(0.99, budget.getPriority()));
                
                return nal.doublePremiseTask(whole, truth, budget, true, false)!=null;
            }
        }
        return false;
    }
    
    /** whether a term can be used in temoralInduction(,,) */
    protected static boolean termForTemporalInduction(final Term t) {
        return (t instanceof Inheritance) || (t instanceof Similarity);
    }
    
    public static List<Task> temporalInduction(final Sentence s1, final Sentence s2, final nars.core.control.NAL nal, boolean SucceedingEventsInduction) {
        
        if ((s1.truth==null) || (s2.truth==null))
            return Collections.EMPTY_LIST;
        
        Term t1 = s1.term;
        Term t2 = s2.term;
                
        if (Statement.invalidStatement(t1, t2))
            return Collections.EMPTY_LIST;
        
        Term t11=null;
        Term t22=null;
        
        if (termForTemporalInduction(t1) && termForTemporalInduction(t2)) {
            
            Statement ss1 = (Statement) t1;
            Statement ss2 = (Statement) t2;

            Variable var1 = new Variable("$0");
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
                    Term comp=ss1.getSubject();
                    Term ss2_term = ((Operation)ss2).getSubject();
                    
                    boolean applicableVariableType = !(comp instanceof Variable && ((Variable)comp).hasVarIndep());
                    
                    if(ss2_term instanceof Product) {
                        Product ss2_prod=(Product) ss2_term;
                        
                        if(applicableVariableType && Terms.contains(ss2_prod.term, comp)) { //only if there is one and it isnt a variable already
                            Term[] ars = ss2_prod.cloneTermsReplacing(comp, var1);

                            t11 = Statement.make(ss1, var1, ss1.getPredicate());
                            
                            Operation op=(Operation) Operation.make(
                                    new Product(ars), 
                                    ss2.getPredicate()
                            );
                            
                            t22 = op;
                        }
                    }
                }
                if(ss1 instanceof Operation && !(ss1.getSubject() instanceof Variable)) {//it is an operation, let's look if one of the arguments is same as the subject of the other term
                    Term comp=ss2.getSubject();
                    Term ss1_term = ((Operation)ss1).getSubject();
                    
                    boolean applicableVariableType = !(comp instanceof Variable && ((Variable)comp).hasVarIndep());
                    
                    if(ss1_term instanceof Product) {
                        Product ss1_prod=(Product) ss1_term;
                                               
                        if(applicableVariableType && Terms.contains(ss1_prod.term, comp)) { //only if there is one and it isnt a variable already
                            
                            Term[] ars = ss1_prod.cloneTermsReplacing(comp, var1);
                            

                            t22 = Statement.make(ss2, var1, ss2.getPredicate());
                            
                            Operation op=(Operation) Operation.make(
                                    new Product(ars), 
                                    ss1.getPredicate()
                            );
                            
                            t11 = op;
                        }
                    }
                }
            }
        }

        
        final Interval.AtomicDuration duration = nal.memory.param.duration;
        int durationCycles = duration.get();
        
        long time1 = s1.getOccurenceTime();
        long time2 = s2.getOccurenceTime();
        
        long timeDiff = time2 - time1;
        
        List<Interval> interval;
        
        if (!concurrent(time1, time2, durationCycles)) {
            
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
        int order = order(timeDiff, durationCycles);
        TruthValue givenTruth1 = s1.truth;
        TruthValue givenTruth2 = s2.truth;
     //   TruthFunctions.
        TruthValue truth1 = TruthFunctions.induction(givenTruth1, givenTruth2);
        TruthValue truth2 = TruthFunctions.induction(givenTruth2, givenTruth1);
        TruthValue truth3 = TruthFunctions.comparison(givenTruth1, givenTruth2);
        BudgetValue budget1 = BudgetFunctions.forward(truth1, nal);
        BudgetValue budget2 = BudgetFunctions.forward(truth2, nal);
        BudgetValue budget3 = BudgetFunctions.forward(truth3, nal);
        
        //https://groups.google.com/forum/#!topic/open-nars/0k-TxYqg4Mc
        if(!SucceedingEventsInduction) { //reduce priority according to temporal distance
            //it was not "semantically" connected by temporal succession
            int tt1=(int) s1.getOccurenceTime();
            int tt2=(int) s1.getOccurenceTime();
            int d=Math.abs(tt1-tt2)/nal.memory.param.duration.get();
            if(d!=0) {
                double mul=1.0/((double)d);
                budget1.setPriority((float) (budget1.getPriority()*mul));
                budget2.setPriority((float) (budget2.getPriority()*mul));
                budget3.setPriority((float) (budget3.getPriority()*mul));
            }
        }
        
        Statement statement1 = Implication.make(t1, t2, order);
        Statement statement2 = Implication.make(t2, t1, reverseOrder(order));
        Statement statement3 = Equivalence.make(t1, t2, order);
        
        //maybe this way is also the more flexible and intelligent way to introduce variables for the case above
        //TODO: rethink this for 1.6.3
        //"Perception Variable Introduction Rule" - https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
        if(statement2!=null) { //there is no general form
            //ok then it may be the (&/ =/> case which 
            //is discussed here: https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
            Statement st=statement2;
            if(st.getPredicate() instanceof Inheritance && (st.getSubject() instanceof Conjunction || st.getSubject() instanceof Operation)) {
                Term precon=(Term) st.getSubject();
                Inheritance consequence=(Inheritance) st.getPredicate();
                Term pred=consequence.getPredicate();
                Term sub=consequence.getSubject();
                //look if subject is contained in precon:
                boolean SubsSub=precon.containsTermRecursively(sub);
                boolean SubsPred=precon.containsTermRecursively(pred);
                Variable v1=new Variable("$91");
                Variable v2=new Variable("$92");
                HashMap<Term,Term> app=new HashMap<Term,Term>();
                if(SubsSub || SubsPred) {
                    if(SubsSub)
                        app.put(sub, v1);
                    if(SubsPred)
                        app.put(pred,v2);
                    Term res=((CompoundTerm) statement2).applySubstitute(app);
                    if(res!=null) { //ok we applied it, all we have to do now is to use it
                        t22=((Statement)res).getSubject();
                        t11=((Statement)res).getPredicate();
                    }
                }
             }
        } 
        
       
        List<Task> success=new ArrayList<Task>();
        if(t11!=null && t22!=null) {
            Statement statement11 = Implication.make(t11, t22, order);
            Statement statement22 = Implication.make(t22, t11, reverseOrder(order));
            Statement statement33 = Equivalence.make(t11, t22, order);
            if(!tooMuchTemporalStatements(statement11)) {
                Task t=nal.doublePremiseTask(statement11, truth1, budget1,true, false);
                if(t!=null) {
                    success.add(t);
                }
            }
            if(!tooMuchTemporalStatements(statement22)) {
               Task t=nal.doublePremiseTask(statement22, truth2, budget2,true, false);
                if(t!=null) {
                    success.add(t);
                }
            }
            if(!tooMuchTemporalStatements(statement33)) {
                Task t=nal.doublePremiseTask(statement33, truth3, budget3,true, false);
                if(t!=null) {
                    success.add(t);
                }
            }
        }
        if(!tooMuchTemporalStatements(statement1)) {
            Task t=nal.doublePremiseTask(statement1, truth1, budget1,true, false);
            if(t!=null) {
                    success.add(t);
                }
        }
        if(!tooMuchTemporalStatements(statement2)) {
            Task t=nal.doublePremiseTask(statement2, truth2, budget2,true, false);
                 if(t!=null) {
                    success.add(t);
                }
            }
        if(!tooMuchTemporalStatements(statement3)) {
            Task t=nal.doublePremiseTask(statement3, truth3, budget3,true, false);
            if(t!=null) {
                    success.add(t);
                }
        }
        return success;
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
        if (problem.getOccurenceTime()!=solution.getOccurenceTime()) {
            truth = solution.projectionTruth(problem.getOccurenceTime(), memory.time());            
        }
        
        if (problem.containQueryVar()) {
            return truth.getExpectation() / solution.term.getComplexity();
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
    public static BudgetValue solutionEval(final Sentence problem, final Sentence solution, Task task, final nars.core.control.NAL nal) {
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
            float taskPriority = task.getPriority(); //+goal satisfication is a matter of degree - https://groups.google.com/forum/#!topic/open-nars/ZfCM416Dx1M
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

    public static int order(final long timeDiff, final int durationCycles) {
        final int halfDuration = durationCycles/2;
        if (timeDiff > halfDuration) {
            return ORDER_FORWARD;
        } else if (timeDiff < -halfDuration) {
            return ORDER_BACKWARD;
        } else {
            return ORDER_CONCURRENT;
        }
    }
    /** if (relative) event B after (stationary) event A then order=forward;
     *                event B before       then order=backward
     *                occur at the same time, relative to duration: order = concurrent
     */
    public static int order(final long a, final long b, final int durationCycles) {        
        if ((a == Stamp.ETERNAL) || (b == Stamp.ETERNAL))
            throw new RuntimeException("order() does not compare ETERNAL times");
        
        return order(b - a, durationCycles);
    }
    
    public static boolean concurrent(final long a, final long b, final int durationCycles) {        
        //since Stamp.ETERNAL is Integer.MIN_VALUE, 
        //avoid any overflow errors by checking eternal first
        
        if (a == Stamp.ETERNAL) {
            //if both are eternal, consider concurrent.  this is consistent with the original
            //method of calculation which compared equivalent integer values only
            return (b == Stamp.ETERNAL);
        }
        else if (b == Stamp.ETERNAL) {
            return false; //a==b was compared above
        }
        else {        
            return order(a, b, durationCycles) == ORDER_CONCURRENT;
        }
    }
    
}
