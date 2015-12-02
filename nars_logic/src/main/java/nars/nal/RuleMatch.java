package nars.nal;

import nars.Global;
import nars.Op;
import nars.Premise;
import nars.Symbols;
import nars.budget.Budget;
import nars.nal.meta.PostCondition;
import nars.nal.meta.TaskBeliefPair;
import nars.nal.meta.TermPattern;
import nars.nal.meta.TruthFunction;
import nars.task.PreTask;
import nars.task.Task;
import nars.term.Term;
import nars.term.transform.FindSubst;
import nars.term.transform.Subst;
import nars.term.transform.Substitution;
import nars.truth.Stamp;
import nars.truth.Truth;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;


/**
 * rule matching context, re-recyclable as thread local
 */
public class RuleMatch {

    /**
     * thread-specific pool of RuleMatchers
     * this pool is local to this deriver
     */
    public static final ThreadLocal<RuleMatch> matchers = ThreadLocal.withInitial(() -> {
        //TODO use the memory's RNG for complete deterministic reproducibility
        return new RuleMatch(new XorShift1024StarRandom(1));
    });


    /**
     * Global Context
     */
    public Consumer<Task> receiver;


    @Deprecated //reference to the rule should not be necessary when complete
    public TaskRule rule;


    /**
     * Premise Context
     */
    public Premise premise;
    @Deprecated public enum MatchStage {
        Pre, Pattern, Post
    }

    //MatchStage stage = MatchStage.Pre;




    /** MUTABLE DATA */
    /**
     * stage S: primary pattern substutitions
     */
    public Subst subst;

    public static final class PostMods {
        public Truth truth;
        public Term derivedTerm;
        public long occurence_shift;
        public char punct;

        public void clear() {
            truth = null;
            derivedTerm = null;
            occurence_shift = Stamp.TIMELESS;
            punct = 0;
        }

        public void copyTo(PostMods m) {
            m.occurence_shift = occurence_shift;
            m.truth = truth;
            m.derivedTerm = derivedTerm;
            m.punct = punct;
        }

        @Override
        public String toString() {
            return "PostMods{" +
                    "truth=" + truth +
                    ", derivedTerm=" + derivedTerm +
                    ", occurence_shift=" + occurence_shift +
                    ", punct=" + punct +
                    '}';
        }
    }

    /**
     * stage P: postcondition modifiers
     */
    public PostMods post;

    public static final class SecondarySubs {

        /**
         * pair of maps available for temporary use by conditions and modifiers
         * NOTE: if you use this in a Precondition, make sure to clear() it before it exits
         * ie. return it in the condition you took it, empty
         */
        public final Map<Term, Term> left = Global.newHashMap(0);
        public final Map<Term, Term> right = Global.newHashMap(0);

        /**
         * 'outp' used by substitute to hold the current proposed / candidate
         * additional substitutions. if the modifier doesn't
         * succeed, the values it contains will have no effect
         * before it is cleared.
         * <p>
         * temporary, re-cycled immediately
         */
        public final Map<Term, Term> outp = Global.newHashMap();

        public void clear() {
            left.clear(); right.clear();
            outp.clear(); // necessary?
        }

        public void copyTo(SecondarySubs m) {
            m.clear();
            m.left.putAll(left);
            m.right.putAll(right);
            m.outp.putAll(outp); // necessary?
        }

        @Override
        public String toString() {
            return "SecondarySubs{" +
                    "left=" + left +
                    ", right=" + right +
                    ", outp=" + outp +
                    '}';
        }
    }

    /**
     * stage S2: secondary substutitions
     */
    public SecondarySubs sub2;

    /**
     * if no occurrence is stipulated, this value will be Stamp.STAMP_TIMELESS as initialized in reset
     */


//    @Override
//    public String toString() {
//        return taskBelief.toString() + ":<" + super.toString() + ">:";
//    }


    @Override
    public String toString() {
        return "RuleMatch{" +
                "premise=" + premise +
                ", subst=" + subst +
                ", post=" + post +
                ", sub2=" + sub2 +
                '}';

    }
    public RuleMatch(Random r) {
        this.subst = new FindSubst(Op.VAR_PATTERN,
                Global.newHashMap(0),
                Global.newHashMap(0),
                r);
        this.sub2 = new SecondarySubs();
        this.post = new PostMods();
    }

    /**
     * set the next premise
     */
    public final void start(Premise p, Consumer<Task> receiver) {
        clear();

        this.premise = p;
        this.receiver = receiver;

        this.subst.parent = new TaskBeliefPair(
            p.getTask().getTerm(),
            p.getTermLink().getTerm()
        );

        //scale unification power according to premise's mean priority linearly between min and max
        this.subst.power =
            (int) ((p.getMeanPriority() * (Global.UNIFICATION_POWER - Global.UNIFICATION_POWERmin))
                    + Global.UNIFICATION_POWERmin);

    }


//    @Override protected void putCommon(Variable a, Variable b) {
//        //no common variables; use the variable from term1 as the unification target
//        map1.put(a, a);
//        map2.put(b, a);
//    }



//    public static final class Stage extends PreCondition {
//        public final MatchStage s;
//
//        public Stage(MatchStage nextStage) {
//            this.s = nextStage;
//        }
//
//        @Override
//        public boolean test(RuleMatch ruleMatch) {
//            //ruleMatch.stage = s;
//            return true;
//        }
//
//        @Override
//        public String toString() {
//            return "Stage{" + s + "}";
//        }
//    }

    public void copyTo(RuleMatch target ) {

        target.premise = premise;
        target.receiver = receiver;

        subst.copyTo(target.subst);
        post.copyTo(target.post);
        sub2.copyTo(target.sub2);


//        if (Global.DEBUG) {
//            //  FOR EXTREME EQUALITY TESTING
//            if (!target.toString().equals(toString()))
//                throw new RuntimeException("invalid copy");
//        }

    }


    /**
     * call at beginning to reset
     */
    public final void clear() {
        subst.clear();
        sub2.clear();
        post.clear();
        //stage = MatchStage.Pre;
    }




//    @Deprecated
//    public final ArrayList<Task> apply(final PostCondition outcome) {
//
//        Premise premise = this.premise;
//
//        final Task task = premise.getTask();
//
//        /** calculate derived task truth value */
//
//
//        Task belief = premise.getBelief();
//
//
//        final Truth T = task.getTruth();
//        final Truth B = belief == null ? null : belief.getTruth();
//
//
//        /** calculate derived task punctuation */
//        char punct = outcome.puncOverride;
//        if (punct == 0) {
//            /** use the default policy determined by parent task */
//            punct = task.getPunctuation();
//        }
//        final Truth truth;
//        if (punct == Symbols.JUDGMENT || punct == Symbols.GOAL) {
//            truth = getTruth(outcome, punct, T, B);
//            if (truth == null) {
//                //no truth value function was applicable but it was necessary, abort
//                return null;
//            }
//        } else {
//            //question or quest, no truth is involved
//            truth = null;
//        }
//
//        /** eliminate cyclic double-premise results
//         *  TODO move this earlier to precondition check, or change to altogether new policy
//         */
//        final boolean single = (belief == null);
//        if ((!single) && (cyclic(outcome, premise))) {
//            if (Global.DEBUG && Global.DEBUG_REMOVED_CYCLIC_DERIVATIONS) {
//                removeCyclic(outcome, premise, truth, punct);
//            }
//            return null;
//        }
//
//        Term derivedTerm;
//
//        if (null == (derivedTerm = resolve(outcome.term)))
//            return null;
//
//        Term taskpart = rule.terms()[0].term(0);
//        Term beliefpart = rule.terms()[0].term(1);
//
//        Term toInvestigate = null;
//
//        if (rule.sequenceIntervalsFromBelief) {
//            toInvestigate = beliefpart;
//        }
//        if (rule.sequenceIntervalsFromTask) {
//            toInvestigate = taskpart;
//        }
//
//        int Nothing = 0;
//        int TermIsSequence = 1;
//        int TermSubjectIsSequence = 2;
//        int TermPredicateIsSequence = 3;
//
//        int mode = 0; //nothing
//        int sequence_term_amount = 0;
//        if (rule.sequenceIntervalsFromBelief || rule.sequenceIntervalsFromTask) {
//            if (toInvestigate instanceof Sequence) {
//                sequence_term_amount = ((Sequence) toInvestigate).terms().length;
//                mode = TermIsSequence;
//            } else if (toInvestigate instanceof Statement) {
//                Statement st = (Statement) toInvestigate;
//                if (st.getSubject() instanceof Sequence) {
//                    sequence_term_amount = ((Sequence) st.getSubject()).terms().length;
//                    mode = TermSubjectIsSequence;
//                } else if (st.getPredicate() instanceof Sequence) {
//                    sequence_term_amount = ((Sequence) st.getPredicate()).terms().length;
//                    mode = TermPredicateIsSequence;
//                }
//            }
//        }
//
//        if (mode != Nothing) {
//
//            Sequence copy = null; //where to copy the interval data from
//            Sequence paste = null; //where to paste it to
//
//            //TODO: THIS CODE EXISTS TWICE WITH DIFFERENT PARAMETERS, PLACE1
//            if (mode == TermIsSequence && derivedTerm instanceof Sequence) {
//                paste = (Sequence) derivedTerm;
//            } else if (mode == TermSubjectIsSequence && derivedTerm instanceof Statement && ((Statement) derivedTerm).getSubject() instanceof Sequence) {
//                paste = (Sequence) ((Statement) derivedTerm).getSubject();
//            } else if (mode == TermPredicateIsSequence && derivedTerm instanceof Statement && ((Statement) derivedTerm).getPredicate() instanceof Sequence) {
//                paste = (Sequence) ((Statement) derivedTerm).getPredicate();
//            }
//            //END CODE
//
//            Term lookat = null;
//            if (rule.sequenceIntervalsFromTask) {
//                lookat = task.getTerm();
//            } else if (rule.sequenceIntervalsFromBelief) {
//                lookat = belief.getTerm();
//            }
//
//            //TODO: THIS CODE EXISTS TWICE WITH DIFFERENT PARAMETERS, PLACE2
//            if (mode == TermIsSequence && lookat instanceof Sequence) {
//                copy = (Sequence) lookat;
//            } else if (mode == TermSubjectIsSequence && lookat instanceof Statement && ((Statement) lookat).getSubject() instanceof Sequence) {
//                copy = (Sequence) ((Statement) lookat).getSubject();
//            } else if (mode == TermPredicateIsSequence && lookat instanceof Statement && ((Statement) lookat).getPredicate() instanceof Sequence) {
//                copy = (Sequence) ((Statement) lookat).getPredicate();
//            }
//            //END CODE
//
//            //ok now we can finally copy the intervals.
//
//            if (copy != null && paste != null) {
//                int a = copy.terms().length;
//                int b = paste.terms().length;
//                boolean sameLength = a == b;
//                boolean OneLess = a - 1 == b;
//
//                if (!sameLength && !OneLess) {
//                    System.out.println("the case where the resulting sequence has less elements should not happen and needs to be analyzed!!");
//                }
//
//                if (OneLess) {
//                    occurence_shift = copy.intervals()[1]; //we shift according to first interval
//                    for (int i = 2; i < copy.intervals().length; i++) { //and copy the rest into the conclusion
//                        paste.intervals()[i - 1] = copy.intervals()[i];
//                    }
//                } else if (sameLength) {
//                    for (int i = 0; i < copy.intervals().length; i++) {
//                        paste.intervals()[i] = copy.intervals()[i];
//                    }
//                }
//            } else if (copy != null && paste == null) { //ok we reduced to a single element, so its a one less case
//                occurence_shift = copy.intervals()[1];
//            }
//        }
//
//
//        final Map<Term, Term> Outp = this.outp;
//
//        for (final PreCondition c : outcome.afterConclusions) {
//            if (!c.test(this)) {
//                return null;
//            }
//        }
//
//
//        if (!Outp.isEmpty()) {
//            Term rederivedTerm = derivedTerm.substituted(Outp);
//            Outp.clear();
//
//            //its possible that the substitution produces an invalid term, ex: an invalid statement
//            if (rederivedTerm == null)
//                return null;
//
//            derivedTerm = rederivedTerm;
//        }
//
//
//        //the apply substitute will invoke clone which invokes normalized, so its not necessary to call it here
//        derivedTerm = derivedTerm.normalized();
//
//        if (!(derivedTerm instanceof Compound))
//            return null;
//
//
//        //test for reactor leak
//        // TODO prevent this from happening
//        if (Variable.hasPatternVariable(derivedTerm)) {
//            //if (Global.DEBUG) {
//            //String leakMsg = "reactor leak: " + derivedTerm;
//            //throw new RuntimeException(leakMsg);
//            //System.err.println(leakMsg);
//            //}
//            return null;
//
////
////            System.out.println(premise + "   -|-   ");
////
////            map1.entrySet().forEach(x -> System.out.println("  map1: " + x ));
////            map2.entrySet().forEach(x -> System.out.println("  map2: " + x ));
////            resolutions.entrySet().forEach(x -> System.out.println("  reso: " + x ));
////
////            resolver.apply(t);
//            //return null;
//        }
////        else {
////            if (rule.numPatternVariables() > map1.size()) {
////                System.err.println("predicted reactor leak FAIL: " + derive);
////                System.err.println("  " + map1);
////                System.err.println("  " + rule);
////            }
////        }
//
//
////        if (punct == task.getPunctuation() && derive.equals(task.getTerm())) {
////            //this revision-like consequence is an artifact of rule term pattern simplifications which can distort a rule into producing derivatives of the input task (and belief?) with unsubstantiatedly different truth values
////            //ideally this type of rule would be detected sooner and eliminated
////            //for now this hack will at least prevent the results
////            throw new RuntimeException(rule + " has a possibly BAD rule / postcondition");
////            //return null;
////        }
//
//
////        //check if this is redundant
////        Term nextDerived = derive.substituted(assign); //at first M -> #1 for example (rule match), then #1 -> test (var elimination)
////        if (nextDerived == null)
////            return false;
////        derive = nextDerived;
////        if (!precondsubs.isEmpty())
////            derive = derive.substituted(precondsubs);
//
////                //ok apply substitution to both elements in args
////
////                final Term arg1 = args[0].substituted(assign);
////
////                final Term arg2;
////                //arg2 is optional
////                if (args.length > 1 && args[1] != null)
////                    arg2 = args[1].substituted(assign);
////                else
////                    arg2 = null;
//
//
//        //}
//
//
//        //TODO also allow substituted evaluation on output side (used by 2 rules I think)
//
//        //TODO on occurenceDerive, for example consider ((&/,<a --> b>,+8) =/> (c --> k)), (a --> b) |- (c --> k)
//        // or ((a --> b) =/> (c --> k)), (a --> b) |- (c --> k) where the order makes a difference,
//        //a difference in occuring, not a difference in matching
//        //CALCULATE OCCURENCE TIME HERE AND SET DERIVED TASK OCCURENCE TIME ACCORDINGLY!
//
//
//        final Budget budget;
//        if (truth != null) {
//            budget = BudgetFunctions.compoundForward(truth, derivedTerm, premise);
//            //budget = BudgetFunctions.forward(truth, premise);
//        } else {
//            budget = BudgetFunctions.compoundBackward(derivedTerm, premise);
//        }
//
//        if (!premise.validateDerivedBudget(budget)) {
//            if (Global.DEBUG && Global.DEBUG_REMOVED_INSUFFICIENT_BUDGET_DERIVATIONS) {
//                removeInsufficientBudget(premise, new PreTask(derivedTerm, punct, truth, budget, occurence_shift, premise));
//            }
//            return null;
//        }
//
//        FluentTask deriving = premise.newTask((Compound) derivedTerm); //, task, belief, allowOverlap);
//        if (deriving != null) {
//
//            final long now = premise.time();
//            final long occ;
//
//            if (occurence_shift > Stamp.TIMELESS) {
//                occ = task.getOccurrenceTime() + occurence_shift;
//            } else {
//                occ = task.getOccurrenceTime(); //inherit premise task's
//            }
//
//
//            if (occ != Stamp.ETERNAL && premise.isEternal() && !premise.nal(7)) {
//                throw new RuntimeException("eternal premise " + premise + " should not result in non-eternal occurence time: " + deriving + " via rule " + rule);
//            }
//
//            final Task derived = premise.validate(deriving
//                    .punctuation(punct)
//                    .truth(truth)
//                    .budget(budget)
//                    .time(now, occ)
//                    .parent(task, belief /* null if single */)
//            );
//
//            if (derived != null) {
//                if (premise.nal(7) && rule.anticipate && task.isInput()) { //the prediction needs to be based on a observation
//                    premise.memory().the(Anticipate.class).anticipate(derived); //else the system can anticipate things it can not measure
//                }                    //thus these anticipations would fail, leading the system thinking that this did not happen altough it was
//                if (Global.DEBUG && Global.DEBUG_LOG_DERIVING_RULE) { //just not able to measure it, closed world assumption gone wild.
//                    derived.log(rule.toString());
//                }
//
//                ArrayList<Task> ret = new ArrayList<Task>();
//                ret.add(derived);
//
//                if (truth != null && rule.immediate_eternalize && !derived.isEternal()) {
//                    Truth et = TruthFunctions.eternalize(new DefaultTruth(truth.getFrequency(), truth.getConfidence()));
//                    FluentTask deriving2 = premise.newTask((Compound) derivedTerm);
//                    Budget budget2 = BudgetFunctions.compoundForward(et, derivedTerm, premise);
//
//                    final Task derivedEternal = premise.validate(deriving2
//                            .punctuation(punct)
//                            .truth(et)
//                            .budget(budget2)
//                            .time(now, Stamp.ETERNAL)
//                            .parent(task, belief // null if single
//                            )
//                    );
//
//                    if (derivedEternal != null) {
//                        ret.add(derivedEternal);
//                    }
//                }
//
//                return ret;
//            }
//        }
//
//        return null;
//    }

    /**
     * for debugging
     */
    public static void removeInsufficientBudget(Premise premise, PreTask task) {
        premise.memory().remove(task, "Insufficient Derived Budget");
    }

    /**
     * for debugging
     */
    private void removeCyclic(PostCondition outcome, Premise premise, Truth truth, char punct) {
        Term termm = resolve(outcome.term);
        if (termm != null) {
            premise.memory().remove(
                    new PreTask(termm, punct, truth,
                            Budget.zero, post.occurence_shift, premise
                    ),
                    "Cyclic:" +
                            Arrays.toString(premise.getTask().getEvidence()) + ',' +
                            Arrays.toString(premise.getBelief().getEvidence())
            );
        }
    }


    static Truth getTruth(final PostCondition outcome, final char punc, final Truth T, final Truth B) {


        final TruthFunction f = getTruthFunction(punc, outcome);
        if (f == null) return null;


        final Truth truth = f.get(T, B);
//        if (T!=null && truth == T)
//            throw new RuntimeException("tried to steal Task's truth instance: " + f);
//        if (B!=null && truth == B)
//            throw new RuntimeException("tried to steal Belief's truth instance: " + f);


        //minConfidence pre-filter
        final float minConf = Global.DEBUG ? Global.CONFIDENCE_PREFILTER_DEBUG : Global.CONFIDENCE_PREFILTER;
        return (validJudgmentOrGoalTruth(truth, minConf)) ? truth : null;
    }

    @Deprecated
    static TruthFunction getTruthFunction(char punc, PostCondition outcome) {

        switch (punc) {

            case Symbols.JUDGMENT:
                return outcome.truth;

            case Symbols.GOAL:
                if (outcome.desire == null) {
                    //System.err.println(outcome + " has null desire function");
                    return null; //no desire function specified for this rule
                } else {
                    return outcome.desire;
                }

            /*case Symbols.QUEST:
            case Symbols.QUESTION:
            */

            default:
                return null;
        }

    }


    static boolean validJudgmentOrGoalTruth(Truth truth, float minConf) {
        return !((truth == null) || (truth.getConfidence() < minConf));
    }

    @Deprecated
    protected static boolean cyclic(PostCondition outcome, Premise premise) {
        return (outcome.truth != null && !outcome.truth.allowOverlap) && premise.isCyclic();
    }

    public static boolean cyclic(TruthFunction f, Premise premise) {
        return (f != null && !f.allowOverlap()) && premise.isCyclic();
    }


//    public Term resolveTest(final Term t) {
//        Term r = resolver.apply(t);
//
//        /* temporary */
//        Term existing = resolutions.put(t, r);
//        if ((existing!=null) && (!existing.equals(r))) {
//            throw new RuntimeException("inconsistent resolution: " + r + "!= existing" + existing + "... in " + this);
//        }
//
//        return r;
//    }

    /**
     * reusable instance (local only)
     */
    final Substitution substituter = new Substitution(null);

    /**
     * provides the cached result if it exists, otherwise computes it and adds to cache
     */
    public final Term resolve(final Term t) {

//       //There are after-preconditions which bind a pattern variable
//        if (rule.numPatternVariables() > map1.size()) {
//            System.err.println("predicted reactor leak");
//            //return null;
//        }

        return subst.resolve(t, substituter);
    }


    public final void occurrenceAdd(final long cyclesDelta) {
        //TODO move to post
        long oc = this.post.occurence_shift;
        if (oc == Stamp.TIMELESS)
            oc = 0;
        oc += cyclesDelta;
        this.post.occurence_shift = oc;
    }

    @Deprecated public boolean next(Term x, Term y, int unificationPower) {
        return subst.next(x, y, unificationPower);
    }

    public boolean next(TermPattern x, Term y, int unificationPower) {
        return subst.next(x, y, unificationPower);
    }

//    public void run(TaskRule rule, Stream.Builder<Task> stream) {
//        //if preconditions are met:
//        for (final PostCondition p : rule.postconditions) {
//            Task t = apply(p);
//            if (t!=null)
//                stream.accept(t);
//        }
//
//    }

}
