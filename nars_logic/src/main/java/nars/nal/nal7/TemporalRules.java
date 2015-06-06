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
package nars.nal.nal7;


import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.*;
import nars.nal.concept.Concept;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal8.Operation;
import nars.nal.stamp.Stamp;
import nars.nal.stamp.Stamper;
import nars.nal.term.Compound;
import nars.nal.term.Statement;
import nars.nal.term.Term;
import nars.nal.term.Variable;
import nars.op.mental.Mental;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author peiwang
 */
public class TemporalRules {

    public static final int ORDER_NONE = 2;
    public static final int ORDER_FORWARD = 1;
    public static final int ORDER_CONCURRENT = 0;
    public static final int ORDER_BACKWARD = -1;
    public static final int ORDER_INVALID = -2;

    public final static int reverseOrder(final int order) {

        if (order == ORDER_INVALID) {
            throw new RuntimeException("ORDER_INVALID not handled here");
        }

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
        if ((order1 == order2) || (order2 == TemporalRules.ORDER_NONE)) {
            return order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            return order2;
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            return order1;
        }
        return ORDER_INVALID;
    }

    public final static int abdIndComOrder(final int order1, final int order2) {
        if (order2 == TemporalRules.ORDER_NONE) {
            return order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            return reverseOrder(order2);
        } else if ((order2 == TemporalRules.ORDER_CONCURRENT) || (order1 == -order2)) {
            return order1;
        }
        return ORDER_INVALID;
    }

    public final static int analogyOrder(final int order1, final int order2, final int figure) {

        if ((order2 == TemporalRules.ORDER_NONE) || (order2 == TemporalRules.ORDER_CONCURRENT)) {
            return order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            return (figure < 20) ? order2 : reverseOrder(order2);
        } else if (order1 == order2) {
            if ((figure == 12) || (figure == 21)) {
                return order1;
            }
        } else if ((order1 == -order2)) {
            if ((figure == 11) || (figure == 22)) {
                return order1;
            }
        }
        return ORDER_INVALID;
    }

    public static final int resemblanceOrder(final int order1, final int order2, final int figure) {
        if ((order2 == TemporalRules.ORDER_NONE)) {
            return (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            return (figure % 10 == 1) ? order2 : reverseOrder(order2); // switch when 12 or 22
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            return (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
        } else if (order1 == order2) {
            return (figure == 21) ? order1 : -order1;
        }
        return ORDER_INVALID;
    }

    public static final int composeOrder(final int order1, final int order2) {
        if (order2 == TemporalRules.ORDER_NONE) {
            return order1;
        } else if (order1 == TemporalRules.ORDER_NONE) {
            return order2;
        } else if (order1 == order2) {
            return order1;
        }
        return ORDER_INVALID;
    }

    /**
     * whether temporal induction can generate a task by avoiding producing wrong terms; only one temporal operate is allowed
     */
    public final static boolean tooMuchTemporalStatements(final Term t, int maxTemporalRelations) {
        return (t == null) || (t.containedTemporalRelations() > maxTemporalRelations);
    }

    //is input or by the system triggered operation
    public static boolean isInputOrTriggeredOperation(final Task newEvent, Memory mem) {
        if (newEvent.isInput()) return true;
        if (containsMentalOperator(newEvent)) return true;
        if (newEvent.getCause() != null) return true;
        return false;
    }


    public static boolean containsMentalOperator(final Task t) {
        return containsMentalOperator(t.getTerm(), true);
        /*
        if(!(t.sentence.term instanceof Operation))
            return false;

        Operation o= (Operation)t.sentence.term;
        return (o.getOperator() instanceof Mental);
        */
    }

    public static boolean containsMentalOperator(Term t, boolean recurse) {
        if (t instanceof Operation) {
            Operation o = (Operation) t;
            if (o.getOperator() instanceof Mental) return true;
        }
        if ((recurse) && (t instanceof Compound)) {
            for (Term s : ((Compound) t)) {
                if (containsMentalOperator(s, true)) return true;
            }
        }

        return false;
    }


    public static long applyExpectationOffset(final Memory memory, final Term temporalStatement, final long occurrenceTime) {
        if (occurrenceTime == Stamp.ETERNAL) return Stamp.ETERNAL;

        if (temporalStatement != null && temporalStatement instanceof Implication) {
            Implication imp = (Implication) temporalStatement;
            if (imp.getSubject() instanceof Conjunction && imp.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
                Conjunction conj = (Conjunction) imp.getSubject();
                if (conj.term[conj.term.length - 1] instanceof Interval) {
                    Interval intv = (Interval) conj.term[conj.term.length - 1];
                    long time_offset = intv.durationCycles(memory);
                    return (occurrenceTime + time_offset);
                }
            }
        }
        return Stamp.ETERNAL;
    }


    /**
     * whether a term can be used in temoralInduction(,,)
     */
    protected static boolean termForTemporalInduction(final Term t) {
        /*
                //if(t1 instanceof Operation && t2 instanceof Operation) {
        //   return; //maybe too restrictive
        //}
        if(((t1 instanceof Implication || t1 instanceof Equivalence) && t1.getTemporalOrder()!=TemporalRules.ORDER_NONE) ||
           ((t2 instanceof Implication || t2 instanceof Equivalence) && t2.getTemporalOrder()!=TemporalRules.ORDER_NONE)) {
            return; //better, if this is fullfilled, there would be more than one temporal operate in the statement, return
        }
        */

        return (t instanceof Inheritance) || (t instanceof Similarity);
    }

    /*
    public static void applyExpectationOffset(Memory memory, Term temporalStatement, Stamp stamp) {
        if(temporalStatement!=null && temporalStatement instanceof Implication) {
            Implication imp=(Implication) temporalStatement;
            if(imp.getSubject() instanceof Conjunction && imp.getTemporalOrder()==TemporalRules.ORDER_FORWARD)  {
                Conjunction conj=(Conjunction) imp.getSubject();
                if(conj.term[conj.term.length-1] instanceof Interval) {
                    Interval intv=(Interval) conj.term[conj.term.length-1];
                    long time_offset=intv.durationCycles(memory);
                    stamp.setOccurrenceTime(stamp.getOccurrenceTime()+time_offset);
                }
            }
        }
    }*/


    public static void temporalInduction(final Sentence s1, final Sentence s2, Stamper stamp, final NAL nal) {
        temporalInduction(s1, s2, stamp, nal, nal.getCurrentTask(), true);
    }

    final static Variable var1 = new Variable("$0");
    final static Variable v91 = new Variable("$91");
    final static Variable v92 = new Variable("$92");

    public static void temporalInduction(final Sentence s1, final Sentence s2, Stamper stamp, final NAL nal, Task subbedTask, boolean SucceedingEventsInduction) {

        if ((s1.truth==null) || (s2.truth==null) || s1.punctuation!=Symbols.JUDGMENT || s2.punctuation!=Symbols.JUDGMENT)
            return;

        Term t1 = s1.term;
        Term t2 = s2.term;

        if (Statement.invalidStatement(t1, t2))
            return;

        Term t11 = null;
        Term t22 = null;

        if (termForTemporalInduction(t1) && termForTemporalInduction(t2)) {

            Statement ss1 = (Statement) t1;
            Statement ss2 = (Statement) t2;


            final Variable var2 = var1;


            if (ss1.getSubject().equals(ss2.getSubject())) {
                t11 = Terms.makeStatement(ss1, var1, ss1.getPredicate());
                t22 = Terms.makeStatement(ss2, var2, ss2.getPredicate());
            } else if (ss1.getPredicate().equals(ss2.getPredicate())) {
                t11 = Terms.makeStatement(ss1, ss1.getSubject(), var1);
                t22 = Terms.makeStatement(ss2, ss2.getSubject(), var2);
            }


            Map<Term, Term> subs = null;

            if (ss2.containsTermRecursively(ss1.getSubject())) {
                subs = Global.newHashMap(1);
                subs.put(ss1.getSubject(), var1);
                if (ss2.containsTermRecursively(ss1.getPredicate())) {
                    subs.put(ss1.getPredicate(), var2);
                }
                t11 = ss1.applySubstitute(subs);
                t22 = ss2.applySubstitute(subs);
            }

            if (ss1.containsTermRecursively(ss2.getSubject())) {
                if (subs == null) subs = Global.newHashMap(1); else subs.clear();

                subs.put(ss2.getSubject(), var1);

                if (ss1.containsTermRecursively(ss2.getPredicate())) {
                    subs.put(ss2.getPredicate(), var2);
                }
                t11 = ss1.applySubstitute(subs);
                t22 = ss2.applySubstitute(subs);
            }

            //TODO combine the below blocks they are similar

            //allow also temporal induction on operation arguments:
            if (ss2 instanceof Operation ^ ss1 instanceof Operation) {
                if (ss2 instanceof Operation && !(ss2.getSubject() instanceof Variable)) {//it is an operation, let's look if one of the arguments is same as the subject of the other term
                    Term comp = ss1.getSubject();
                    Term ss2_term = ss2.getSubject();

                    boolean applicableVariableType = !(comp instanceof Variable && comp.hasVarIndep());

                    if (ss2_term instanceof Product) {
                        Product ss2_prod = (Product) ss2_term;

                        if (applicableVariableType && Terms.contains(ss2_prod.terms(), comp)) { //only if there is one and it isnt a variable already
                            Term[] ars = ss2_prod.cloneTermsReplacing(comp, var1);

                            t11 = Terms.makeStatement(ss1, var1, ss1.getPredicate());

                            Operation op = (Operation) Operation.make(
                                    ss2.getPredicate(),
                                    Product.make(ars)
                            );

                            t22 = op;
                        }
                    }
                }
                if (ss1 instanceof Operation && !(ss1.getSubject() instanceof Variable)) {//it is an operation, let's look if one of the arguments is same as the subject of the other term
                    Term comp = ss2.getSubject();
                    Term ss1_term = ss1.getSubject();

                    boolean applicableVariableType = !(comp instanceof Variable && comp.hasVarIndep());

                    if (ss1_term instanceof Product) {
                        Product ss1_prod = (Product) ss1_term;

                        if (applicableVariableType && Terms.contains(ss1_prod.terms(), comp)) { //only if there is one and it isnt a variable already

                            Term[] ars = ss1_prod.cloneTermsReplacing(comp, var1);


                            t22 = Terms.makeStatement(ss2, var1, ss2.getPredicate());

                            Operation op = (Operation) Operation.make(
                                    ss1.getPredicate(),
                                    Product.make(ars)
                            );

                            t11 = op;
                        }
                    }
                }
            }
        }


        final Interval.AtomicDuration duration = nal.memory.param.duration;
        int durationCycles = duration.get();

        long time1 = s1.getOccurrenceTime();
        long time2 = s2.getOccurrenceTime();

        final long timeDiff;
        if ((time1 == Stamp.ETERNAL) || (time2 == Stamp.ETERNAL))
            throw new RuntimeException("induction on eternal terms"); //timeDiff = 0;

        timeDiff = time2 - time1;

        if (!concurrent(time1, time2, durationCycles)) {

            List<Interval> interval = Interval.intervalSequence(Math.abs(timeDiff), Global.TEMPORAL_INTERVAL_PRECISION, nal.memory);

            if (timeDiff > 0) {
                t1 = Conjunction.make(t1, interval, ORDER_FORWARD);
                if (t11 != null) {
                    t11 = Conjunction.make(t11, interval, ORDER_FORWARD);
                }
            } else {
                t2 = Conjunction.make(t2, interval, ORDER_FORWARD);
                if (t22 != null) {
                    t22 = Conjunction.make(t22, interval, ORDER_FORWARD);
                }
            }
        }


        int order = order(timeDiff, durationCycles);
        Truth givenTruth1 = s1.truth;
        Truth givenTruth2 = s2.truth;
        //   TruthFunctions.
        Truth truth1 = TruthFunctions.induction(givenTruth1, givenTruth2);
        Truth truth2 = TruthFunctions.induction(givenTruth2, givenTruth1);
        Truth truth3 = TruthFunctions.comparison(givenTruth1, givenTruth2);
        Budget budget1 = BudgetFunctions.forward(truth1, nal);
        Budget budget2 = BudgetFunctions.forward(truth2, nal);
        Budget budget3 = BudgetFunctions.forward(truth3, nal);

        //https://groups.google.com/forum/#!topic/open-nars/0k-TxYqg4Mc
        if (!SucceedingEventsInduction) { //reduce priority according to temporal distance
            //it was not "semantically" connected by temporal succession
            int tt1 = (int) s1.getOccurrenceTime();
            int tt2 = (int) s1.getOccurrenceTime();
            int d = Math.abs(tt1 - tt2) / nal.memory.param.duration.get();
            if (d != 0) {
                double mul = 1.0 / ((double) d);
                budget1.setPriority((float) (budget1.getPriority() * mul));
                budget2.setPriority((float) (budget2.getPriority() * mul));
                budget3.setPriority((float) (budget3.getPriority() * mul));
            }
        }
        Statement statement1 = Implication.make(t1, t2, order);
        Statement statement2 = Implication.make(t2, t1, reverseOrder(order));
        Statement statement3 = Equivalence.make(t1, t2, order);


        //maybe this way is also the more flexible and intelligent way to introduce variables for the case above
        //TODO: rethink this for 1.6.3
        //"Perception Variable Introduction Rule" - https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
        if (statement2 != null) { //there is no general form
            //ok then it may be the (&/ =/> case which
            //is discussed here: https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
            Statement st = statement2;
            if (st.getPredicate() instanceof Inheritance && (st.getSubject() instanceof Conjunction || st.getSubject() instanceof Operation)) {
                Term precon = st.getSubject();
                Inheritance consequence = (Inheritance) st.getPredicate();
                Term pred = consequence.getPredicate();
                Term sub = consequence.getSubject();
                //look if subject is contained in precon:
                boolean SubsSub = precon.containsTermRecursivelyOrEquals(sub);
                boolean SubsPred = precon.containsTermRecursivelyOrEquals(pred);
                HashMap<Term, Term> app = new HashMap<Term, Term>();
                if (SubsSub || SubsPred) {
                    if (SubsSub)
                        app.put(sub, v91);
                    if (SubsPred)
                        app.put(pred, v92);
                    Term res = statement2.applySubstitute(app);
                    if (res != null) { //ok we applied it, all we have to do now is to use it
                        t22 = ((Statement) res).getSubject();
                        t11 = ((Statement) res).getPredicate();
                    }
                }
            }
        }


        final int inductionLimit = nal.memory.param.temporalRelationsMax.get();

        //List<Task> success = new ArrayList<Task>();
        if (t11 != null && t22 != null) {
            Statement statement11 = Implication.make(t11, t22, order);
            Statement statement22 = Implication.make(t22, t11, reverseOrder(order));
            Statement statement33 = Equivalence.make(t11, t22, order);
            if (!tooMuchTemporalStatements(statement11, inductionLimit)) {
                Task t = nal.doublePremiseTask(statement11, truth1, budget1, stamp, true, subbedTask, false);
            }
            if (!tooMuchTemporalStatements(statement22, inductionLimit)) {
                Task t = nal.doublePremiseTask(statement22, truth2, budget2, stamp, true, subbedTask, false);
            }
            if (!tooMuchTemporalStatements(statement33, inductionLimit)) {
                Task t = nal.doublePremiseTask(statement33, truth3, budget3, stamp, true, subbedTask, false);
            }
        }
        if (!tooMuchTemporalStatements(statement1, inductionLimit)) {
            Task t = nal.doublePremiseTask(statement1, truth1, budget1, stamp, true, subbedTask, false);
        }
        if (!tooMuchTemporalStatements(statement2, inductionLimit)) {

            /*  =/>  */

            Task task = nal.doublePremiseTask(statement2, subbedTask.sentence.punctuation, truth2,
                    budget2, stamp, true, subbedTask, false);

            if (task!=null) {
            
                desireUpdateCompiledInferenceHelper(s1, task, nal, s2);

                //micropsi inspired strive for knowledge
                //get strongest belief of that concept and use the revison truth, if there is no, use this truth
                double conf = task.sentence.truth.getConfidence();
                Concept C = nal.memory.concept(task.sentence.term);
                if (C != null && C.hasBeliefs()) {
                    Sentence bel = C.getBeliefs().get(0).sentence;
                    Truth cur = bel.truth;
                    conf = Math.max(cur.getConfidence(), conf); //no matter if revision is possible, it wont be below max
                    //if there is no overlapping evidental base, use revision:
                    boolean revisable = true;
                    revisable = !Stamp.evidentialSetOverlaps(bel, task.sentence);
                    if (revisable) {
                        conf = TruthFunctions.revision(task.sentence.truth, bel.truth).getConfidence();
                    }
                }

                questionFromLowConfidenceHighPriorityJudgement(task, conf, nal);
            }


        }
        if (!tooMuchTemporalStatements(statement3, inductionLimit)) {
            nal.doublePremiseTask(statement3, truth3, budget3, stamp, true, subbedTask, false);
        }

    }

    private static void desireUpdateCompiledInferenceHelper(final Sentence s1, Task task, final NAL nal, final Sentence s2) {
        /*
        IN <SELF --> [good]>! %1.00;0.90%
        IN (^pick,left). :|: %1.00;0.90%
        IN  PauseInput(3)
        IN <SELF --> [good]>. :|: %0.00;0.90%
        <(&/,(^pick,left,$1),+3) =/> <$1 --> [good]>>. :|: %0.00;0.45%
        <(&/,(^pick,left,$1),+3) =/> <$1 --> [good]>>. %0.00;0.31%
        <(&/,(^pick,left,$1),+3) </> <$1 --> [good]>>. :|: %0.00;0.45%
        <(&/,(^pick,left,$1),+3) </> <$1 --> [good]>>. %0.00;0.31%
        <(&/,(^pick,left),+3) =/> <SELF --> [good]>>. :|: %0.00;0.45%
        <(&/,(^pick,left),+3) =/> <SELF --> [good]>>. %0.00;0.31%
        <(&/,(^pick,left),+3) </> <SELF --> [good]>>. :|: %0.00;0.45%
        <(&/,(^pick,left),+3) </> <SELF --> [good]>>. %0.00;0.31%
        
        It takes the system sometimes like 1000 steps to go from
        "(^pick,left) leads to SELF not being good"
        to
        "since <SELF --> good> is a goal, (^pick,left) is not desired"
        making it bad for RL tasks but this will change, maybe with the following principle:
        
        
        task: <(&/,(^pick,left,$1),+3) =/> <$1 --> [good]>>.
        belief: <SELF --> [good]>!
        |-
        (^pick,left)! (note the change of punctuation, it needs the punctuation of the belief here)
        
        */
        if (s1.isJudgment()) { //necessary check?
            Sentence belief=task.sentence;
            Concept S1_State_C=nal.memory.concept(s1.term);
            if(S1_State_C != null && S1_State_C.hasGoals() &&
                    !(((Statement)belief.term).getPredicate() instanceof Operation)) {
                Task a_desire = S1_State_C.getStrongestGoal(true, true);

                Sentence g = new Sentence(S1_State_C.getTerm(),Symbols.JUDGMENT,
                        new DefaultTruth(1.0f,0.99f), a_desire.sentence);

                g.setOccurrenceTime(s1.getOccurrenceTime()); //strongest desire for that time is what we want to know
                Task strongest_desireT=S1_State_C.getTask(g, S1_State_C.getGoals());
                Sentence strongest_desire=strongest_desireT.sentence.projectionSentence(s1.getOccurrenceTime(), strongest_desireT.getOccurrenceTime());
                Truth T=TruthFunctions.desireDed(belief.truth, strongest_desire.truth);

                //Stamp st=new Stamp(strongest_desire.sentence.stamp.clone(),belief.stamp, nal.memory.time());

                final long occ;
                
                if(strongest_desire.isEternal()) {
                    occ = Stamp.ETERNAL;
                } else {
                    long shift=0;
                    if(((Implication)task.sentence.term).getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
                        shift=nal.memory.duration();
                    }
                    occ = (strongest_desire.stamp.getOccurrenceTime()-shift);
                }
                

                
                nal.setCurrentBelief(belief);
                
                //Sentence W=new Sentence(s2.term,Symbols.GOAL,T, belief).setOccurrenceTime(occ);

                Budget val=BudgetFunctions.forward(T, nal);

                nal.doublePremiseTask(s2.term, Symbols.GOAL, T, val, nal.newStamp(belief, occ), false, strongest_desireT, true);

            }
        }
        
        //PRINCIPLE END
    }



    private static void questionFromLowConfidenceHighPriorityJudgement(Task task, double conf, final NAL nal) {
        if(!(task.sentence.term instanceof Implication)) return;

        if(nal.memory.emotion.busy()<Global.CURIOSITY_BUSINESS_THRESHOLD
                && Global.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF
                && task.sentence.punctuation==Symbols.JUDGMENT
                && conf<Global.CURIOSITY_CONFIDENCE_THRESHOLD
                && task.getPriority()>Global.CURIOSITY_PRIORITY_THRESHOLD) {

                boolean valid=false;

                Implication equ=(Implication) task.sentence.term;
                if(equ.getTemporalOrder()!=TemporalRules.ORDER_NONE) {
                    valid=true;
                }

                if(valid) {
                    nal.singlePremiseTask(task.sentence.term, Symbols.QUESTION, null,
                            null, nal.newStamp(task.sentence, nal.memory.time()),
                            Global.CURIOSITY_DESIRE_PRIORITY_MUL, Global.CURIOSITY_DESIRE_DURABILITY_MUL
                    );
                }

        }
    }

    /**
     * Evaluate the quality of the judgment as a solution to a problem
     *
     * @param problem  A goal or question
     * @param solution The solution to be evaluated
     * @return The quality of the judgment as the solution
     */
    public static float solutionQuality(final Sentence problem, final Sentence solution, long time) {

        return solution.projectionTruthQuality(problem.getOccurrenceTime(), time, problem.hasQueryVar());

//        if (!matchingOrder(problem, solution)) {
//            return 0.0F;
//        }

//        Truth truth;
//        if (ptime!=solution.getOccurrenceTime())
//            truth = solution.projectionTruth(ptime, memory.time());
//        else
//            truth = solution.truth;
//
//        if (problem.hasQueryVar()) {
//            return truth.getExpectation() / solution.term.getComplexity();
//        } else {
//            return truth.getConfidence();
//        }

    }


    /* ----- Functions used both in direct and indirect processing of tasks ----- */

    /**
     * Evaluate the quality of a belief as a solution to a problem, then reward
     * the belief and de-prioritize the problem
     *
     * @param problem  The problem (question or goal) to be solved
     * @param solution The belief as solution
     * @param task     The task to be immediately processed, or null for continued
     *                 process
     * @return The budget for the new task which is the belief activated, if
     * necessary
     */
    public static Budget solutionEval(final Sentence problem, final Sentence solution, Task task, final NAL nal) {
        Budget budget = null;
        boolean feedbackToLinks = false;
        /*if (task == null) {
            task = nal.getCurrentTask();
            feedbackToLinks = true;
        }*/
        boolean judgmentTask = task.sentence.isJudgment();
        final float quality = TemporalRules.solutionQuality(problem, solution, nal.time());
        if (judgmentTask) {
            task.orPriority(quality);
        } else {
            float taskPriority = task.getPriority();
            budget = new Budget(UtilityFunctions.or(taskPriority, quality), task.getDurability(), BudgetFunctions.truthToQuality(solution.truth));
            task.setPriority(Math.min(1 - quality, taskPriority));
        }
        /*
        if (feedbackToLinks) {
            TaskLink tLink = nal.getCurrentTaskLink();
            tLink.setPriority(Math.min(1 - quality, tLink.getPriority()));
            TermLink bLink = nal.getCurrentBeliefLink();
            bLink.incPriority(quality);
        }*/
        return budget;
    }

    public static int order(final float timeDiff, final int durationCycles) {
        final float halfDuration = durationCycles / 2f;
        if (timeDiff >= halfDuration) {
            return ORDER_FORWARD;
        } else if (timeDiff <= -halfDuration) {
            return ORDER_BACKWARD;
        } else {
            return ORDER_CONCURRENT;
        }
    }

    /**
     * if (relative) event B after (stationary) event A then order=forward;
     * event B before       then order=backward
     * occur at the same time, relative to duration: order = concurrent
     */
    public static int order(final long a, final long b, final int durationCycles) {
        if ((a == Stamp.ETERNAL) || (b == Stamp.ETERNAL))
            throw new RuntimeException("order() does not compare ETERNAL times");

        return order(b - a, durationCycles);
    }

    public static boolean concurrent(Sentence a, Sentence b, final int durationCycles) {
        return concurrent(a.getOccurrenceTime(), b.getOccurrenceTime(), durationCycles);
    }

    /**
     * whether two times are concurrent with respect ao a specific duration ("present moment") # of cycles
     */
    public static boolean concurrent(final long a, final long b, final int durationCycles) {
        //since Stamp.ETERNAL is Integer.MIN_VALUE, 
        //avoid any overflow errors by checking eternal first

        if (a == Stamp.ETERNAL) {
            //if both are eternal, consider concurrent.  this is consistent with the original
            //method of calculation which compared equivalent integer values only
            return (b == Stamp.ETERNAL);
        } else if (b == Stamp.ETERNAL) {
            return false; //a==b was compared above
        } else {
            return order(a, b, durationCycles) == ORDER_CONCURRENT;
        }
    }

    public static boolean before(long a, long b, int duration) {
        return after(b, a, duration);
    }

    /** true if B is after A */
    public static boolean after(long a, long b, int duration) {
        if (a == Stamp.ETERNAL || b == Stamp.ETERNAL)
            return false;
        return order(a, b, duration) == TemporalRules.ORDER_FORWARD;
    }

    /** true if B is after A */
    public static boolean occurrsAfter(Stamp a, Stamp b) {
        return after(a.getOccurrenceTime(), b.getOccurrenceTime(), a.getDuration());
    }
}
