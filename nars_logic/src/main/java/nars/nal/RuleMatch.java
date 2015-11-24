package nars.nal;

import nars.Global;
import nars.Op;
import nars.Premise;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.meta.PostCondition;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TaskBeliefPair;
import nars.nal.meta.TruthFunction;
import nars.nal.nal7.Sequence;
import nars.op.mental.Anticipate;
import nars.task.FluentTask;
import nars.task.PreTask;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Statement;
import nars.term.Term;
import nars.term.Variable;
import nars.term.transform.FindSubst;
import nars.term.transform.Substitution;
import nars.truth.Stamp;
import nars.truth.Truth;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;


/**
 * rule matching context, re-recyclable as thread local
 */
public class RuleMatch extends FindSubst {

    /** thread-specific pool of RuleMatchers
        this pool is local to this deriver */
    public static final ThreadLocal<RuleMatch> matchers = ThreadLocal.withInitial(() -> {
        //TODO use the memory's RNG for complete deterministic reproducibility
        return new RuleMatch(new XorShift1024StarRandom(1));
    });
    /**
     * if no occurrence is stipulated, this value will be Stamp.STAMP_TIMELESS as initialized in reset
     */
    public long occurence_shift;


    public TaskRule rule;

    public Premise premise;


    final public TaskBeliefPair taskBelief = new TaskBeliefPair();

    /**
     * used by substitute to hold the current proposed / candidate
     * additional substitutions. if the modifier doesn't
     * succeed, the values it contains will have no effect
     * before it is cleared.
     */
    public final Map<Term, Term> outp = Global.newHashMap();

    /** pair of maps available for temporary use by conditions and modifiers
     *  NOTE: if you use this in a Precondition, make sure to clear() it before it exits
     *  ie. return it in the condition you took it, empty
     * */
    public final Map<Term, Term> left = Global.newHashMap(0);
    public final Map<Term, Term> right = Global.newHashMap(0);
    public final Set<Term> tmpSet = Global.newHashSet(0);
    public TaskRule prevRule;

    public final Map<Term, Term> prevXY = Global.newHashMap(0);
    public final Map<Term, Term> prevYX = Global.newHashMap(0);
    public int unificationPower;

    @Override
    public String toString() {
        return taskBelief.toString() + ":<" + super.toString() + ">:";
    }

    public RuleMatch(Random random) {
        super(Op.VAR_PATTERN,
                Global.newHashMap(0),
                Global.newHashMap(0),
                random);
    }

    /**
     * set the next premise
     */
    public final void start(Premise p) {
        this.premise = p;
        this.prevRule = null;
        this.prevXY.clear(); this.prevYX.clear();


        //scale unification power according to premise's mean priority linearly between min and max
        this.unificationPower =
                (int)((p.getMeanPriority() * (Global.UNIFICATION_POWER - Global.UNIFICATION_POWERmin))
                    + Global.UNIFICATION_POWERmin);

        taskBelief.set(
                p.getTask().getTerm(),
                p.getTermLink().getTerm()
        );
    }


//    @Override protected void putCommon(Variable a, Variable b) {
//        //no common variables; use the variable from term1 as the unification target
//        map1.put(a, a);
//        map2.put(b, a);
//    }


    /**
     * clear and re-use with a next rule
     */
    public final void start(TaskRule nextRule) {

        this.prevRule = this.rule;

        clear();

        occurence_shift = Stamp.TIMELESS;

        this.rule = nextRule;
    }

    public final Task apply(final PostCondition outcome) {

        Premise premise = this.premise;

        final Task task = premise.getTask();


        /** calculate derived task truth value */


        Task belief = premise.getBelief();


        final Truth T = task.getTruth();
        final Truth B = belief == null ? null : belief.getTruth();


        /** calculate derived task punctuation */
        char punct = outcome.puncOverride;
        if (punct == 0) {
            /** use the default policy determined by parent task */
            punct = task.getPunctuation();
        }
        final Truth truth;
        if (punct == Symbols.JUDGMENT || punct == Symbols.GOAL) {
            truth = getTruth(outcome, punct, T, B);
            if (truth == null) {
                //no truth value function was applicable but it was necessary, abort
                return null;
            }
        } else {
            //question or quest, no truth is involved
            truth = null;
        }

        /** eliminate cyclic double-premise results
         *  TODO move this earlier to precondition check, or change to altogether new policy
         */
        final boolean single = (belief == null);
        if ((!single) && (cyclic(outcome, premise))) {
            if (Global.DEBUG && Global.DEBUG_REMOVED_CYCLIC_DERIVATIONS) {
                removeCyclic(outcome, premise, truth, punct);
            }
            return null;
        }

        Term derivedTerm;

        if (null == (derivedTerm = resolve(outcome.term)))
            return null;

        Term taskpart = rule.terms()[0].term(0);
        Term beliefpart = rule.terms()[0].term(1);

        Term toInvestigate = null;

        if(rule.sequenceIntervalsFromBelief) {
            toInvestigate = beliefpart;
        }
        if(rule.sequenceIntervalsFromTask) {
            toInvestigate = taskpart;
        }

        int Nothing = 0;
        int TermIsSequence = 1;
        int TermSubjectIsSequence = 2;
        int TermPredicateIsSequence = 3;

        int mode = 0; //nothing
        int sequence_term_amount = 0;
        if(rule.sequenceIntervalsFromBelief || rule.sequenceIntervalsFromTask) {
            if(toInvestigate instanceof Sequence) {
                sequence_term_amount = ((Sequence)toInvestigate).terms().length;
                mode = TermIsSequence;
            }
            else
            if(toInvestigate instanceof Statement) {
                Statement st = (Statement) toInvestigate;
                if(st.getSubject() instanceof Sequence) {
                    sequence_term_amount = ((Sequence)st.getSubject()).terms().length;
                    mode = TermSubjectIsSequence;
                }
                else
                if(st.getPredicate() instanceof Sequence) {
                    sequence_term_amount = ((Sequence)st.getPredicate()).terms().length;
                    mode = TermPredicateIsSequence;
                }
            }
        }

        if(mode != Nothing) {

            Sequence copy = null; //where to copy the interval data from
            Sequence paste = null; //where to paste it to

            //TODO: THIS CODE EXISTS TWICE WITH DIFFERENT PARAMETERS, PLACE1
            if(mode == TermIsSequence && derivedTerm instanceof Sequence) {
                paste = (Sequence) derivedTerm;
            }
            else
            if(mode == TermSubjectIsSequence && derivedTerm instanceof Statement && ((Statement)derivedTerm).getSubject() instanceof Sequence) {
                paste = (Sequence) ((Statement)derivedTerm).getSubject();
            }
            else
            if(mode == TermPredicateIsSequence && derivedTerm instanceof Statement && ((Statement)derivedTerm).getPredicate() instanceof Sequence) {
                paste = (Sequence) ((Statement)derivedTerm).getPredicate();
            }
            //END CODE

            Term lookat = null;
            if(rule.sequenceIntervalsFromTask) {
                lookat = task.getTerm();
            }
            else
            if(rule.sequenceIntervalsFromBelief) {
                lookat = belief.getTerm();
            }

            //TODO: THIS CODE EXISTS TWICE WITH DIFFERENT PARAMETERS, PLACE2
            if(mode == TermIsSequence && lookat instanceof Sequence) {
                copy = (Sequence) lookat;
            }
            else
            if(mode == TermSubjectIsSequence && lookat instanceof Statement && ((Statement)lookat).getSubject() instanceof Sequence) {
                copy = (Sequence) ((Statement)lookat).getSubject();
            }
            else
            if(mode == TermPredicateIsSequence && lookat instanceof Statement && ((Statement)lookat).getPredicate() instanceof Sequence) {
                copy = (Sequence) ((Statement)lookat).getPredicate();
            }
            //END CODE

            //ok now we can finally copy the intervals.

            if(copy!=null && paste!=null) {
                int a = copy.terms().length;
                int b = paste.terms().length;
                boolean sameLength = a == b;
                boolean OneLess = a - 1 == b;

                if (!sameLength && !OneLess) {
                    System.out.println("the case where the resulting sequence has less elements should not happen and needs to be analyzed!!");
                }

                if (OneLess) {
                    occurence_shift = copy.intervals()[1]; //we shift according to first interval
                    for (int i = 2; i < copy.intervals().length; i++) { //and copy the rest into the conclusion
                        paste.intervals()[i - 1] = copy.intervals()[i];
                    }
                } else if (sameLength) {
                    for (int i = 0; i < copy.intervals().length; i++) {
                        paste.intervals()[i] = copy.intervals()[i];
                    }
                }
            }
            else
            if(copy!=null && paste==null) { //ok we reduced to a single element, so its a one less case
                occurence_shift = copy.intervals()[1];
            }
        }




        final Map<Term, Term> Outp = this.outp;

        for (final PreCondition c : outcome.afterConclusions) {
            if (!c.test(this)) {
                return null;
            }
        }


        if (!Outp.isEmpty()) {
            Term rederivedTerm = derivedTerm.substituted(Outp);
            Outp.clear();

            //its possible that the substitution produces an invalid term, ex: an invalid statement
            if (rederivedTerm == null)
                return null;

            derivedTerm = rederivedTerm;
        }

        //the apply substitute will invoke clone which invokes normalized, so its not necessary to call it here
        derivedTerm = derivedTerm.normalized();

        if (!(derivedTerm instanceof Compound))
            return null;


        //test for reactor leak
        // TODO prevent this from happening
        if (Variable.hasPatternVariable(derivedTerm)) {
            //if (Global.DEBUG) {
                //String leakMsg = "reactor leak: " + derivedTerm;
                //throw new RuntimeException(leakMsg);
                //System.err.println(leakMsg);
            //}
            return null;

//
//            System.out.println(premise + "   -|-   ");
//
//            map1.entrySet().forEach(x -> System.out.println("  map1: " + x ));
//            map2.entrySet().forEach(x -> System.out.println("  map2: " + x ));
//            resolutions.entrySet().forEach(x -> System.out.println("  reso: " + x ));
//
//            resolver.apply(t);
            //return null;
        }
//        else {
//            if (rule.numPatternVariables() > map1.size()) {
//                System.err.println("predicted reactor leak FAIL: " + derive);
//                System.err.println("  " + map1);
//                System.err.println("  " + rule);
//            }
//        }





//        if (punct == task.getPunctuation() && derive.equals(task.getTerm())) {
//            //this revision-like consequence is an artifact of rule term pattern simplifications which can distort a rule into producing derivatives of the input task (and belief?) with unsubstantiatedly different truth values
//            //ideally this type of rule would be detected sooner and eliminated
//            //for now this hack will at least prevent the results
//            throw new RuntimeException(rule + " has a possibly BAD rule / postcondition");
//            //return null;
//        }


//        //check if this is redundant
//        Term nextDerived = derive.substituted(assign); //at first M -> #1 for example (rule match), then #1 -> test (var elimination)
//        if (nextDerived == null)
//            return false;
//        derive = nextDerived;
//        if (!precondsubs.isEmpty())
//            derive = derive.substituted(precondsubs);

//                //ok apply substitution to both elements in args
//
//                final Term arg1 = args[0].substituted(assign);
//
//                final Term arg2;
//                //arg2 is optional
//                if (args.length > 1 && args[1] != null)
//                    arg2 = args[1].substituted(assign);
//                else
//                    arg2 = null;


        //}


        //TODO also allow substituted evaluation on output side (used by 2 rules I think)

        //TODO on occurenceDerive, for example consider ((&/,<a --> b>,+8) =/> (c --> k)), (a --> b) |- (c --> k)
        // or ((a --> b) =/> (c --> k)), (a --> b) |- (c --> k) where the order makes a difference,
        //a difference in occuring, not a difference in matching
        //CALCULATE OCCURENCE TIME HERE AND SET DERIVED TASK OCCURENCE TIME ACCORDINGLY!


        final Budget budget;
        if (truth != null) {
            budget = BudgetFunctions.compoundForward(truth, derivedTerm, premise);
            //budget = BudgetFunctions.forward(truth, premise);
        } else {
            budget = BudgetFunctions.compoundBackward(derivedTerm, premise);
        }

        if (!premise.validateDerivedBudget(budget)) {
            if (Global.DEBUG && Global.DEBUG_REMOVED_INSUFFICIENT_BUDGET_DERIVATIONS) {
                removeInsufficientBudget(premise, new PreTask(derivedTerm, punct, truth, budget, occurence_shift, premise));
            }
            return null;
        }


        FluentTask deriving = premise.newTask((Compound) derivedTerm); //, task, belief, allowOverlap);
        if (deriving != null) {

            //TODO ANTICIPATE IF IN FUTURE AND Event:Anticipate is given

            final long now = premise.time();
            final long occ;

            if (occurence_shift > Stamp.TIMELESS) {
                occ = task.getOccurrenceTime() + occurence_shift;
            }
            else {
                occ = task.getOccurrenceTime(); //inherit premise task's
            }


            if (occ != Stamp.ETERNAL && premise.isEternal() && !premise.nal(7)) {
                throw new RuntimeException("eternal premise " + premise + " should not result in non-eternal occurence time: " + deriving + " via rule " + rule);
            }

            final Task derived = premise.validate(deriving
                    .punctuation(punct)
                    .truth(truth)
                    .budget(budget)
                    .time(now, occ)
                    .parent(task, belief /* null if single */)
            );

            if (derived != null) {
                if(premise.nal(7) && rule.anticipate && task.isInput()) { //the prediction needs to be based on a observation
                    premise.memory().the(Anticipate.class).anticipate(derived); //else the system can anticipate things it can not measure
                }                    //thus these anticipations would fail, leading the system thinking that this did not happen altough it was
                if (Global.DEBUG && Global.DEBUG_LOG_DERIVING_RULE) { //just not able to measure it, closed world assumption gone wild.
                    derived.log(rule.toString());
                    //t.log(premise + "," + rule);
                }

                return derived;
            }
        }

        return null;
    }

    /** for debugging */
    private static void removeInsufficientBudget(Premise premise, PreTask task) {
        premise.memory().remove(task, "Insufficient Derived Budget");
    }

    /** for debugging */
    private void removeCyclic(PostCondition outcome, Premise premise, Truth truth, char punct) {
        Term termm = resolve(outcome.term);
        if (termm != null) {
            premise.memory().remove(
                    new PreTask(termm, punct, truth,
                            Budget.zero, occurence_shift, premise
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

    protected static boolean cyclic(PostCondition outcome, Premise premise) {
        return (outcome.truth != null && !outcome.truth.allowOverlap) && premise.isCyclic();
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

    /** reusable instance */
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

        final Substitution s = substituter;

        Term ret = t.substituted(s, xy);
        if(ret != null) {
            ret = ret.substituted(s, yx);
        }
        return ret;
    }


    /** return null if no postconditions match (equivalent to an empty array)
     *  or an array of matching PostConditions to apply */
    public PostCondition[] run(TaskRule rule) {

        start(rule);

        //stage 1 (early)
        for (final PreCondition p : rule.prePreconditions) {
            if (!p.test(this))
                return null;
        }

        //stage 2 (task/belief term match + late)
        for (final PreCondition p : rule.postPreconditions) {
            if (!p.test(this))
                return null;
        }

        return rule.postconditions;
    }

    public final void occurrenceAdd(final long cyclesDelta) {
        long oc = this.occurence_shift;
        if (oc == Stamp.TIMELESS)
            oc = 0;
        oc += cyclesDelta;
        this.occurence_shift = oc;
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
