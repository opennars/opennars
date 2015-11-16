package nars.nal.nal7;

import nars.Memory;
import nars.Premise;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.UtilityFunctions;
import nars.nal.nal8.Operation;
import nars.op.mental.Mental;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Termed;
import nars.truth.Stamp;
import nars.truth.Truth;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Tense  {

    Eternal(":-:"),
    Past(":\\:"),
    Present(":|:"),
    Future(":/:");

    public static final Tense Unknown = null;

    /** none=eternal*/
    public static final int ORDER_NONE = 2;
    /** forward = sequential */
    public static final int ORDER_FORWARD = 1;
    /** concurrent = parallel */
    public static final int ORDER_CONCURRENT = 0;
    public static final int ORDER_BACKWARD = -1;

    @Deprecated public static final int ORDER_INVALID = -2;

    public final String symbol;

    Tense(String string) {
        this.symbol = string;
    }

    public static boolean matchingOrder(final Termed a, final Termed b) {
        return matchingOrder(a.getTerm().getTemporalOrder(), b.getTerm().getTemporalOrder());
    }

    public static boolean matchingOrder(final int order1, final int order2) {

        return (order1 == order2) || (order1 == ORDER_NONE) || (order2 == ORDER_NONE);
    }

    public static boolean containsMentalOperator(final Task t) {
        return containsMentalOperator(t.getTerm(), true);
        /*
        if(!(t.term instanceof Operation))
            return false;

        Operation o= (Operation)t.term;
        return (o.getOperator() instanceof Mental);
        */
    }

    public static boolean containsMentalOperator(Term t, boolean recurse) {
        if (t instanceof Operation) {
            Operation o = (Operation) t;
            if (o.getOperatorTerm() instanceof Mental) return true;
        }
        if ((recurse) && (t instanceof Compound)) {
            Compound ct = (Compound)t;
            int l = ct.size();
            for (int i = 0; i < l; i++) {
                Term s = ct.term(i);
                if (containsMentalOperator(s, true)) return true;
            }
        }

        return false;
    }

    /**
     * Evaluate the quality of the judgment as a solution to a problem
     *
     * @param problem A goal or question
     * @param solution The solution to be evaluated
     * @return The quality of the judgment as the solution
     */
    public static float solutionQuality(final Sentence problem, final Sentence solution, long time) {

        if (!matchingOrder(problem, solution)) {
            return 0;
        }

        return solutionQualityMatchingOrder(problem, solution, time);
    }

    public static float solutionQualityMatchingOrder(final Sentence problem, final Sentence solution, final long time) {
        return solutionQualityMatchingOrder(problem, solution, time, problem.hasQueryVar() );
    }

    /**
        this method is used if the order is known to be matching, so it is not checked
     */
    public static float solutionQualityMatchingOrder(final Sentence problem, final Sentence solution, final long time, final boolean hasQueryVar) {

        /*if ((problem == null) || (solution == null)) {
            throw new RuntimeException("problem or solution is null");
        }*/

        Truth truth;
        long poc = problem.getOccurrenceTime();
        if (poc != solution.getOccurrenceTime()) {
            //TODO avoid creating new Truth instances
            truth = solution.projection(poc, time);
        }
        else {
            truth = solution.getTruth();
        }

        //if (problem.hasQueryVar()) {
        if (hasQueryVar) {
            return truth.getExpectation() / solution.getTerm().complexity();
        } else {
            return truth.getConfidence();
        }
    }

    public static float solutionQuality(boolean hasQueryVar, long occTime, final Sentence solution, final Truth projectedTruth, long time) {
        return solution.projectionTruthQuality(projectedTruth, occTime, time, hasQueryVar);
    }

    public static float solutionQuality(final Sentence problem, final Sentence solution, Truth truth, long time) {
        return solutionQuality(problem.hasQueryVar(), problem.getOccurrenceTime(), solution, truth, time);
    }

    /**
     * Evaluate the quality of a belief as a solution to a problem, then reward
     * the belief and de-prioritize the problem
     *
     * @param task  The problem (question or goal) to be solved
     * @param solution The belief as solution
     * @param task     The task to be immediately processed, or null for continued
     *                 process
     * @return The budget for the new task which is the belief activated, if
     * necessary
     */
    public static Budget solutionEval(final Task task, final Sentence solution, final Premise p) {
        //boolean feedbackToLinks = false;
        /*if (task == null) {
            task = nal.getCurrentTask();
            feedbackToLinks = true;
        }*/
        boolean judgmentTask = task.isJudgment();
        final float quality = solutionQuality(task, solution, p.time());
        if (quality <= 0)
            return null;

        Budget budget = null;
        if (judgmentTask) {
            task.getBudget().orPriority(quality);
        } else {
            float taskPriority = task.getPriority();
            budget = new Budget(UtilityFunctions.or(taskPriority, quality), task.getDurability(), BudgetFunctions.truthToQuality(solution.getTruth()));
            task.getBudget().setPriority(Math.min(1 - quality, taskPriority));
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
    public static boolean concurrent(final long a, final long b, final int perceptualDuration) {
        //since Stamp.ETERNAL is Integer.MIN_VALUE,
        //avoid any overflow errors by checking eternal first

        if (a == Stamp.ETERNAL) {
            //if both are eternal, consider concurrent.  this is consistent with the original
            //method of calculation which compared equivalent integer values only
            return (b == Stamp.ETERNAL);
        } else if (b == Stamp.ETERNAL) {
            return false; //a==b was compared above
        } else {
            return order(a, b, perceptualDuration) == ORDER_CONCURRENT;
        }
    }

    public static boolean before(long a, long b, int perceptualDuration) {
        return after(b, a, perceptualDuration);
    }

    /** true if B is after A */
    public static boolean after(long a, long b, int perceptualDuration) {
        if (a == Stamp.ETERNAL || b == Stamp.ETERNAL)
            return false;
        return order(a, b, perceptualDuration) == ORDER_FORWARD;
    }

    public static boolean isEternal(final long t)  {
        return t <= Stamp.TIMELESS; /* includes ETERNAL */
    }

    public static long getOccurrenceTime(final Tense tense, Memory m) {
        return getOccurrenceTime(m.time(), tense, m.duration());
    }

    public static long getOccurrenceTime(long creationTime, final Tense tense, Memory m) {
        return getOccurrenceTime(creationTime, tense, m.duration());
    }

    public static long getOccurrenceTime(long creationTime, final Tense tense, final int duration) {

        if (creationTime == Stamp.TIMELESS) {
            //in this case, occurenceTime must be considered relative to whatever creationTime will be set when perceived
            //so we base it at zero to make this possible
            creationTime = 0;
        }

        switch (tense) {
            case Present:
                return creationTime;
            case Past:
                return creationTime - duration;
            case Future:
                return creationTime + duration;
            default:
            //case Unknown:
            //case Eternal:
                return Stamp.ETERNAL;
        }
    }

    public static void appendInterval(Appendable p, long iii) throws IOException {
        p.append(Symbols.INTERVAL_PREFIX);
        p.append(Long.toString(iii));
    }

    /** inner between: time difference of later.start() - earlier.end() */
    public static int between(Task task, Task belief) {
        long tStart = task.start();
        long bStart = belief.start();

        Task earlier = tStart <= bStart ? task : belief;
        Task later = earlier == task ? belief : task;

        long a = earlier.end();
        long b = later.start();

        return (int)(b-a);
    }

    /** true if there is a non-zero overlap interval of the tasks */
    public static boolean overlaps(Task a, Task b) {
        return overlaps(a.start(), a.end(), b.start(), b.end());
    }

    public static boolean overlaps(long xStart, long xEnd, long yStart, long yEnd) {
        return Math.max(xStart,yStart) <= Math.min(xEnd,yEnd);
    }

    @Override
    public String toString() {
        return symbol;
    }
    
    protected static final Map<String, Tense> stringToTense;
    
    static {
        Map<String, Tense> stt = new HashMap(Tense.values().length*2);
        for (final Tense t : Tense.values()) {
            stt.put(t.toString(), t);
        }
        stringToTense = Collections.unmodifiableMap( stt );
    }

    public static Tense tense(final String s) {
        return stringToTense.get(s);
    }

    public static String tenseRelative(long then, long now) {
        long dt = then - now;
        if (dt < 0) return "[" + dt + ']';
        else return "[+" + dt + ']';
    }
}

//TODO make an enum for these Orders


//    static int reverseOrder(final int order) {
//
//        if (order == ORDER_INVALID) {
//            throw new RuntimeException("ORDER_INVALID not handled here");
//        }
//
//        if (order == ORDER_NONE) {
//            return ORDER_NONE;
//        } else {
//            return -order;
//        }
//    }


//    static int dedExeOrder(final int order1, final int order2) {
//        if ((order1 == order2) || (order2 == Temporal.ORDER_NONE)) {
//            return order1;
//        } else if ((order1 == Temporal.ORDER_NONE) || (order1 == Temporal.ORDER_CONCURRENT)) {
//            return order2;
//        } else if (order2 == Temporal.ORDER_CONCURRENT) {
//            return order1;
//        }
//        return ORDER_INVALID;
//    }

//    static int abdIndComOrder(final int order1, final int order2) {
//        if (order2 == Temporal.ORDER_NONE) {
//            return order1;
//        } else if ((order1 == Temporal.ORDER_NONE) || (order1 == Temporal.ORDER_CONCURRENT)) {
//            return reverseOrder(order2);
//        } else if ((order2 == Temporal.ORDER_CONCURRENT) || (order1 == -order2)) {
//            return order1;
//        }
//        return ORDER_INVALID;
//    }

//    static int analogyOrder(final int order1, final int order2, final int figure) {
//
//        if ((order2 == Temporal.ORDER_NONE) || (order2 == Temporal.ORDER_CONCURRENT)) {
//            return order1;
//        } else if ((order1 == Temporal.ORDER_NONE) || (order1 == Temporal.ORDER_CONCURRENT)) {
//            return (figure < 20) ? order2 : reverseOrder(order2);
//        } else if (order1 == order2) {
//            if ((figure == 12) || (figure == 21)) {
//                return order1;
//            }
//        } else if ((order1 == -order2)) {
//            if ((figure == 11) || (figure == 22)) {
//                return order1;
//            }
//        }
//        return ORDER_INVALID;
//    }

//    final int resemblanceOrder(final int order1, final int order2, final int figure) {
//        if ((order2 == Temporal.ORDER_NONE)) {
//            return (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
//        } else if ((order1 == Temporal.ORDER_NONE) || (order1 == Temporal.ORDER_CONCURRENT)) {
//            return (figure % 10 == 1) ? order2 : reverseOrder(order2); // switch when 12 or 22
//        } else if (order2 == Temporal.ORDER_CONCURRENT) {
//            return (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
//        } else if (order1 == order2) {
//            return (figure == 21) ? order1 : -order1;
//        }
//        return ORDER_INVALID;
//    }

//    final int composeOrder(final int order1, final int order2) {
//        if (order2 == Temporal.ORDER_NONE) {
//            return order1;
//        } else if (order1 == Temporal.ORDER_NONE) {
//            return order2;
//        } else if (order1 == order2) {
//            return order1;
//        }
//        return ORDER_INVALID;
//    }

//    /**
//     * whether temporal induction can generate a task by avoiding producing wrong terms; only one temporal operate is allowed
//     */
//    public final static boolean tooMuchTemporalStatements(final Term t, int maxTemporalRelations) {
//        return (t == null) || (t.containedTemporalRelations() > maxTemporalRelations);
//    }
//
//    //is input or by the system triggered operation
//    public static boolean isInputOrTriggeredOperation(final Task newEvent) {
//        if (newEvent.isInput()) return true;
//        if (containsMentalOperator(newEvent)) return true;
//        if (newEvent.getCause() != null) return true;
//        return false;
//    }


//
//    public static long applyExpectationOffset(final Term temporalStatement, final long occurrenceTime) {
//        if (occurrenceTime == Stamp.ETERNAL) return Stamp.ETERNAL;
//
//        if (temporalStatement != null && temporalStatement instanceof Implication) {
//            Implication imp = (Implication) temporalStatement;
//            if (imp.getSubject() instanceof Conjunction && imp.getTemporalOrder() == Temporal.ORDER_FORWARD) {
//                Conjunction conj = (Conjunction) imp.getSubject();
//                if (conj.term[conj.term.length - 1] instanceof Interval) {
//                    Interval intv = (Interval) conj.term[conj.term.length - 1];
//                    long time_offset = intv.duration();
//                    return (occurrenceTime + time_offset);
//                }
//            }
//        }
//        return Stamp.ETERNAL;
//    }


//    /**
//     * whether a term can be used in temoralInduction(,,)
//     */
//    static boolean termForTemporalInduction(final Term t) {
//        /*//
//                //if(t1 instanceof Operation && t2 instanceof Operation) {
//        //   return; //maybe too restrictive
//        //}
//        if(((t1 instanceof Implication || t1 instanceof Equivalence) && t1.getTemporalOrder()!=TemporalRules.ORDER_NONE) ||
//           ((t2 instanceof Implication || t2 instanceof Equivalence) && t2.getTemporalOrder()!=TemporalRules.ORDER_NONE)) {
//            return; //better, if this is fullfilled, there would be more than one temporal operate in the statement, return
//        }
//        */
//
//        //return (t instanceof Inheritance) || (t instanceof Similarity);
//        return (t instanceof Statement);
//    }

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


//    public static void temporalInduction(final Task snext, final Task sprev, final NAL nal) {
//        temporalInduction(snext, sprev, nal, nal.getTask(), true);
//    }
//
//    final static Variable var1 = new Variable("$0");
//    final static Variable v91 = new Variable("$91");
//    final static Variable v92 = new Variable("$92");
//
//    public static void temporalInduction(final Task snext, final Task sprev, final NAL nal, Task subbedTask, boolean SucceedingEventsInduction) {
//
//        if (!snext.isJudgment() || !sprev.isJudgment())
//            return;
//
//        Term t1 = snext.getTerm();
//        Term t2 = sprev.getTerm();
//
//        if (Statement.invalidStatement(t1, t2))
//            return;
//
//        Term t11 = null;
//        Term t22 = null;
//
//        if (termForTemporalInduction(t1) && termForTemporalInduction(t2)) {
//
//            Statement ss1 = (Statement) t1;
//            Statement ss2 = (Statement) t2;
//
//
//            final Variable var2 = var1;
//
//
//            if (ss1.getSubject().equals(ss2.getSubject())) {
//                t11 = Terms.makeStatement(ss1, var1, ss1.getPredicate());
//                t22 = Terms.makeStatement(ss2, var2, ss2.getPredicate());
//            } else if (ss1.getPredicate().equals(ss2.getPredicate())) {
//                t11 = Terms.makeStatement(ss1, ss1.getSubject(), var1);
//                t22 = Terms.makeStatement(ss2, ss2.getSubject(), var2);
//            }
//
//
//            Map<Term, Term> subs = null;
//
//            if (ss2.containsTermRecursively(ss1.getSubject())) {
//                subs = Global.newHashMap(1);
//                subs.put(ss1.getSubject(), var1);
//                if (ss2.containsTermRecursively(ss1.getPredicate())) {
//                    subs.put(ss1.getPredicate(), var2);
//                }
//
//                Substitution sub = new Substitution(subs);
//                t11 = ss1.applySubstitute(sub);
//                t22 = ss2.applySubstitute(sub);
//            }
//
//            if (ss1.containsTermRecursively(ss2.getSubject())) {
//                if (subs == null) subs = Global.newHashMap(1); else subs.clear();
//
//                subs.put(ss2.getSubject(), var1);
//
//                if (ss1.containsTermRecursively(ss2.getPredicate())) {
//                    subs.put(ss2.getPredicate(), var2);
//                }
//
//                Substitution sub = new Substitution(subs);
//                t11 = ss1.applySubstitute(sub);
//                t22 = ss2.applySubstitute(sub);
//            }
//
//            //TODO combine the below blocks they are similar
//
//            //allow also temporal induction on operation arguments:
//            if (ss2 instanceof Operation ^ ss1 instanceof Operation) {
//                if (ss2 instanceof Operation && !(ss2.getSubject() instanceof Variable)) {//it is an operation, let's look if one of the arguments is same as the subject of the other term
//                    Term comp = ss1.getSubject();
//                    Term ss2_term = ss2.getSubject();
//
//                    boolean applicableVariableType = !(comp instanceof Variable && comp.hasVarIndep());
//
//                    if (ss2_term instanceof Product) {
//                        Product ss2_prod = (Product) ss2_term;
//
//                        if (applicableVariableType && Terms.contains(ss2_prod.terms(), comp)) { //only if there is one and it isnt a variable already
//                            Term[] ars = ss2_prod.cloneTermsReplacing(comp, var1);
//
//                            t11 = Terms.makeStatement(ss1, var1, ss1.getPredicate());
//
//                            Operation op = (Operation) Operation.make(
//                                    ss2.getPredicate(),
//                                    Product.make(ars)
//                            );
//
//                            t22 = op;
//                        }
//                    }
//                }
//                if (ss1 instanceof Operation && !(ss1.getSubject() instanceof Variable)) {//it is an operation, let's look if one of the arguments is same as the subject of the other term
//                    Term comp = ss2.getSubject();
//                    Term ss1_term = ss1.getSubject();
//
//                    boolean applicableVariableType = !(comp instanceof Variable && comp.hasVarIndep());
//
//                    if (ss1_term instanceof Product) {
//                        Product ss1_prod = (Product) ss1_term;
//
//                        if (applicableVariableType && Terms.contains(ss1_prod.terms(), comp)) { //only if there is one and it isnt a variable already
//
//                            Term[] ars = ss1_prod.cloneTermsReplacing(comp, var1);
//
//
//                            t22 = Terms.makeStatement(ss2, var1, ss2.getPredicate());
//
//                            Operation op = (Operation) Operation.make(
//                                    ss1.getPredicate(),
//                                    Product.make(ars)
//                            );
//
//                            t11 = op;
//                        }
//                    }
//                }
//            }
//        }
//
//
//        int durationCycles = nal.memory().duration();
//
//        long time1 = snext.getOccurrenceTime();
//        long time2 = sprev.getOccurrenceTime();
//
//        final long timeDiff;
//        if ((time1 == Stamp.ETERNAL) || (time2 == Stamp.ETERNAL))
//            throw new RuntimeException("induction on eternal terms"); //timeDiff = 0;
//
//        timeDiff = time2 - time1;
//
//        if (!concurrent(time1, time2, durationCycles)) {
//
//            AbstractInterval interval = nal.newInterval(Math.abs(timeDiff));
//
//            if (timeDiff > 0) {
//                t1 = Conjunction.make(ORDER_FORWARD, t1, interval);
//                if (t11 != null) {
//                    t11 = Conjunction.make(ORDER_FORWARD, t11, interval);
//                }
//            } else {
//                t2 = Conjunction.make(ORDER_FORWARD, t2, interval);
//                if (t22 != null) {
//                    t22 = Conjunction.make(ORDER_FORWARD, t22, interval);
//                }
//            }
//        }
//
//
//        int order = order(timeDiff, durationCycles);
//        Truth givenTruth1 = snext.getTruth();
//        Truth givenTruth2 = sprev.getTruth();
//        //   TruthFunctions.
//        Truth truth1 = TruthFunctions.induction(givenTruth1, givenTruth2);
//        Budget budget1 = truth1!=null ? BudgetFunctions.forward(truth1, nal) : null;
//        Statement statement1 = truth1!=null ? Implication.make(t1, t2, order) : null;
//
//        Truth truth2 = TruthFunctions.induction(givenTruth2, givenTruth1);
//        Budget budget2 = truth2!=null ? BudgetFunctions.forward(truth2, nal) : null;
//        Statement statement2 = truth2!=null ? Implication.make(t2, t1, reverseOrder(order)) : null;
//
//        Truth truth3 = TruthFunctions.comparison(givenTruth1, givenTruth2);
//        Budget budget3 = truth3!=null ? BudgetFunctions.forward(truth3, nal) : null;
//        Statement statement3 = truth3!=null ? Equivalence.make(t1, t2, order) : null;
//
//        //https://groups.google.com/forum/#!topic/open-nars/0k-TxYqg4Mc
//        if (!SucceedingEventsInduction) { //reduce priority according to temporal distance
//            //it was not "semantically" connected by temporal succession
//            int tt1 = (int) snext.getOccurrenceTime();
//            int tt2 = (int) sprev.getOccurrenceTime();
//            float d = Math.abs(tt1 - tt2) / ((float)nal.memory().duration.get());
//            if (d != 0) {
//                float mul = 1.0f / d;
//                if (budget1!=null)  budget1.mulPriority(mul);
//                if (budget2!=null)  budget2.mulPriority(mul);
//                if (budget3!=null)  budget3.mulPriority(mul);
//            }
//        }
//
//
//
//
//
//        //maybe this way is also the more flexible and intelligent way to introduce variables for the case above
//        //TODO: rethink this for 1.6.3
//        //"Perception Variable Introduction Rule" - https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
//        if (statement2 != null) { //there is no general form
//            //ok then it may be the (&/ =/> case which
//            //is discussed here: https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
//            Statement st = statement2;
//            if (st.getPredicate() instanceof Inheritance && (st.getSubject() instanceof Conjunction || st.getSubject() instanceof Operation)) {
//                Term precon = st.getSubject();
//                Inheritance consequence = (Inheritance) st.getPredicate();
//                Term pred = consequence.getPredicate();
//                Term sub = consequence.getSubject();
//                //look if subject is contained in precon:
//                boolean SubsSub = precon.containsTermRecursivelyOrEquals(sub);
//                boolean SubsPred = precon.containsTermRecursivelyOrEquals(pred);
//                HashMap<Term, Term> app = new HashMap<Term, Term>();
//                if (SubsSub || SubsPred) {
//                    if (SubsSub)
//                        app.put(sub, v91);
//                    if (SubsPred)
//                        app.put(pred, v92);
//                    Term res = statement2.applySubstitute(app);
//                    if (res != null) { //ok we applied it, all we have to do now is to use it
//                        t22 = ((Statement) res).getSubject();
//                        t11 = ((Statement) res).getPredicate();
//                    }
//                }
//            }
//        }
//
//
//        final int inductionLimit = nal.memory().temporalRelationsMax.get();
//
//        //List<Task> success = new ArrayList<Task>();
//        if (t11 != null && t22 != null) {
//            Statement statement11 = Implication.make(t11, t22, order);
//            Statement statement22 = Implication.make(t22, t11, reverseOrder(order));
//            Statement statement33 = Equivalence.make(t11, t22, order);
//            if (!tooMuchTemporalStatements(statement11, inductionLimit)) {
//                Task t = nal.deriveDoubleTemporal(statement11, truth1, budget1, subbedTask, sprev);
//            }
//            if (!tooMuchTemporalStatements(statement22, inductionLimit)) {
//                Task t = nal.deriveDoubleTemporal(statement22, truth2, budget2, subbedTask, sprev);
//            }
//            if (!tooMuchTemporalStatements(statement33, inductionLimit)) {
//                Task t = nal.deriveDoubleTemporal(statement33, truth3, budget3, subbedTask, sprev);
//            }
//        }
//        if (!tooMuchTemporalStatements(statement1, inductionLimit)) {
//            Task t = nal.deriveDoubleTemporal(statement1, truth1, budget1, subbedTask, sprev);
//        }
//        if (!tooMuchTemporalStatements(statement2, inductionLimit)) {
//
//            /*  =/>  */
//
//            Task task = nal.deriveDoubleTemporal(statement2, truth2, budget2, subbedTask, sprev);
//
//            if (task!=null) {
//
//                deriveCompiledInferenceHelper(snext, sprev, nal, task);
//            }
//
//
//        }
//        if (!tooMuchTemporalStatements(statement3, inductionLimit)) {
//            nal.deriveDoubleTemporal(statement3, truth3, budget3, subbedTask, sprev);
//        }
//
//    }

//    /** //micropsi inspired strive for knowledge
//     //get strongest belief of that concept and use the revison truth, if there is no, use this truth */
//    protected static void deriveCompiledInferenceHelper(Sentence snext, Sentence sprev, NAL nal, Task task) {
//
//        desireUpdateCompiledInferenceHelper(snext, task, nal, sprev);
//
//        double conf = task.getTruth().getConfidence();
//        Concept C = nal.nar.concept(task.getTerm());
//        if (C != null && C.hasBeliefs()) {
//            Task bel = C.getBeliefs().top();
//            Truth cur = bel.getTruth();
//            conf = Math.max(cur.getConfidence(), conf); //no matter if revision is possible, it wont be below max
//            //if there is no overlapping evidental base, use revision:
//            boolean revisable;
//            revisable = !Stamp.overlapping(bel, task);
//            if (revisable) {
//                conf = TruthFunctions.revision(task.getTruth(), bel.getTruth()).getConfidence();
//            }
//        }
//
//        questionFromLowConfidenceHighPriorityJudgement(task, conf, nal);
//    }
//
//    static Task desireUpdateCompiledInferenceHelper(final Sentence s1, Task task, final NAL nal, final Sentence s2) {
//        /*
//        IN <SELF --> [good]>! %1.00;0.90%
//        IN (^pick,left). :|: %1.00;0.90%
//        IN  PauseInput(3)
//        IN <SELF --> [good]>. :|: %0.00;0.90%
//        <(&/,(^pick,left,$1),+3) =/> <$1 --> [good]>>. :|: %0.00;0.45%
//        <(&/,(^pick,left,$1),+3) =/> <$1 --> [good]>>. %0.00;0.31%
//        <(&/,(^pick,left,$1),+3) </> <$1 --> [good]>>. :|: %0.00;0.45%
//        <(&/,(^pick,left,$1),+3) </> <$1 --> [good]>>. %0.00;0.31%
//        <(&/,(^pick,left),+3) =/> <SELF --> [good]>>. :|: %0.00;0.45%
//        <(&/,(^pick,left),+3) =/> <SELF --> [good]>>. %0.00;0.31%
//        <(&/,(^pick,left),+3) </> <SELF --> [good]>>. :|: %0.00;0.45%
//        <(&/,(^pick,left),+3) </> <SELF --> [good]>>. %0.00;0.31%
//
//        It takes the system sometimes like 1000 steps to go from
//        "(^pick,left) leads to SELF not being good"
//        to
//        "since <SELF --> good> is a goal, (^pick,left) is not desired"
//        making it bad for RL tasks but this will change, maybe with the following principle:
//
//
//        task: <(&/,(^pick,left,$1),+3) =/> <$1 --> [good]>>.
//        belief: <SELF --> [good]>!
//        |-
//        (^pick,left)! (note the change of punctuation, it needs the punctuation of the belief here)
//
//        */
//        if (s1.isJudgment()) { //necessary check?
//            Sentence belief=task;
//            Concept S1_State_C=nal.nar.concept(s1.getTerm());
//            if(S1_State_C != null && S1_State_C.hasGoals() &&
//                    !(((Statement) belief.getTerm()).getPredicate() instanceof Operation)) {
//                Task a_desire = S1_State_C.getGoals().top();
//
////                Sentence g = new Sentence(S1_State_C.getTerm(),Symbols.JUDGMENT,
////                        new DefaultTruth(1.0f,0.99f), a_desire);
////
////
////                g.setOccurrenceTime(s1.getOccurrenceTime());
//
//                final long now = nal.time();
//
//                //strongest desire for that time is what we want to know
//                Task strongest_desireT = S1_State_C.getGoals().top(S1_State_C.getTerm().hasVarQuery(), now, s1.getOccurrenceTime(), s1.getTruth());
//
//                Task strongest_desire = strongest_desireT.projectTask(s1.getOccurrenceTime(), strongest_desireT.getOccurrenceTime());
//                Truth T=TruthFunctions.desireDed(belief.getTruth(), strongest_desire.getTruth());
//
//                //Stamp st=new Stamp(strongest_desire.stamp.clone(),belief.stamp, nal.memory.time());
//
//                final long occ;
//
//                if(strongest_desire.isEternal()) {
//                    occ = Stamp.ETERNAL;
//                } else {
//                    long shift=0;
//                    if(task.getTerm().getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
//                        shift=nal.memory().duration();
//                    }
//                    occ = (strongest_desire.getOccurrenceTime()-shift);
//                }
//
//
//
//                //nal.setCurrentBelief(belief);
//
//                //Sentence W=new Sentence(s2.term,Symbols.GOAL,T, belief).setOccurrenceTime(occ);
//
//                Budget val=BudgetFunctions.forward(T, nal);
//
//                return nal.derive(nal.newTask(s2.getTerm()).goal().truth(T).budget(val)
//                                .parent(task, strongest_desireT)
//                                .occurr(occ).temporalInductable(false)
//                );
//
//            }
//        }
//
//        //PRINCIPLE END
//        return null;
//    }
//
//
//
//    private static Task questionFromLowConfidenceHighPriorityJudgement(Task task, double conf, final NAL nal) {
//        if(!(task.getTerm() instanceof Implication)) return null;
//
//        if(nal.memory().emotion.busy()<Global.CURIOSITY_BUSINESS_THRESHOLD
//                && Global.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF
//                && task.isJudgment()
//                && conf<Global.CURIOSITY_CONFIDENCE_THRESHOLD
//                && task.getPriority()>Global.CURIOSITY_PRIORITY_THRESHOLD) {
//
//                boolean valid=false;
//
//                Implication equ=(Implication) task.getTerm();
//                if(equ.getTemporalOrder()!=TemporalRules.ORDER_NONE) {
//                    valid=true;
//                }
//
//                if(valid) {
//                    return nal.derive(nal.newTask(task.getTerm())
//                                    .question()
//                                    .parent(task)
//                                    .occurrNow()
//                                    .budget(task.getBudget(), Global.CURIOSITY_DESIRE_PRIORITY_MUL, Global.CURIOSITY_DESIRE_DURABILITY_MUL)
//                    );
//                }
//
//        }
//        return null;
//    }
//

//    /**
//     * Evaluate the quality of a truth judgment with a new projected turth value as a solution to a problem
//     *
//     * @param problem  A goal or question
//     * @param solution The solution to be evaluated
//     * @return The quality of the judgment as the solution
//     */
//    public static float solutionQuality(final Sentence problem, final Sentence solution, final Truth projectedTruth, long time) {
//
//        return solution.projectionTruthQuality(projectedTruth, problem.getOccurrenceTime(), time, problem.hasQueryVar());
//
////        if (!matchingOrder(problem, solution)) {
////            return 0.0F;
////        }
//
////        Truth truth;
////        if (ptime!=solution.getOccurrenceTime())
////            truth = solution.projectionTruth(ptime, memory.time());
////        else
////            truth = solution.truth;
////
////        if (problem.hasQueryVar()) {
////            return truth.getExpectation() / solution.term.getComplexity();
////        } else {
////            return truth.getConfidence();
////        }
//
//    }


    /* ----- Functions used both in direct and indirect processing of tasks ----- */

