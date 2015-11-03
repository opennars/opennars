package nars.meta;

import nars.Global;
import nars.Op;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.meta.pre.PairMatchingProduct;
import nars.premise.Premise;
import nars.task.FluentTask;
import nars.task.PreTask;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.term.transform.FindSubst;
import nars.truth.Truth;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;


/**
 * rule matching context, re-recyclable as thread local
 */
public class RuleMatch extends FindSubst {

    /** thread-specific pool of RuleMatchers
        this pool is local to this deriver */
    public static final transient ThreadLocal<RuleMatch> matchers = ThreadLocal.withInitial(() -> {
        //TODO use the memory's RNG for complete deterministic reproducibility
        return new RuleMatch(new XorShift1024StarRandom(1));
    });
    /**
     * if no occurrence is stipulated, this value will be Stamp.STAMP_TIMELESS as initialized in reset
     */
    public long occurence_shift;


    public TaskRule rule;

    public Premise premise;


    final public PairMatchingProduct taskBelief = new PairMatchingProduct();

    /**
     * used by substitute:
     */
    public final Map<Term, Term> Outp = Global.newHashMap();

    @Override
    public String toString() {
        return taskBelief.toString() + ":<" + super.toString() + ">:";
    }

    public RuleMatch(Random random) {
        super(Op.VAR_PATTERN,
                Global.newHashMap(8),
                Global.newHashMap(8),
                random);
    }

    /**
     * set the next premise
     */
    public void start(Premise p) {
        this.premise = p;
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
     * clear and re-use with a new rule
     */
    public RuleMatch start(TaskRule rule) {

        super.clear();

        occurence_shift = Stamp.TIMELESS;

        this.rule = rule;
        return this;
    }

    public Task apply(final PostCondition outcome) {

        Premise premise = this.premise;

        final Task task = premise.getTask();

        if (!rule.validTaskPunctuation(task.getPunctuation())) {
            return null;
        }

        /** calculate derived task truth value */
        final Task belief = premise.getBelief();
        final Truth T = task.getTruth();
        final Truth B = belief == null ? null : belief.getTruth();
        final Truth truth;

        /** calculate derived task punctuation */
        char punct = outcome.puncOverride;
        if (punct == 0) {
            /** use the default policy determined by parent task */
            punct = task.getPunctuation();
        }
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
            if (Global.DEBUG) {
                removeCyclic(outcome, premise, truth, punct);
            }
            return null;
        }


        Term derivedTerm;

        if (null == (derivedTerm = resolve(outcome.term)))
            return null;

        final Map<Term, Term> Outp = this.Outp;

        for (final PreCondition c : outcome.afterConclusions) {
            if (!c.test(this)) {
                //Outp.clear();
                return null;
            }
        }


        if (!Outp.isEmpty()) {
            Term rederivedTerm = ((Compound)derivedTerm).applySubstitute(Outp);
            Outp.clear();

            //its possible that the substitution produces an invalid term, ex: an invalid statement
            if (rederivedTerm == null)
                return null;

            //the apply substitute will invoke clone which invokes normalized, so its not necessary to call it here
            derivedTerm = rederivedTerm.normalized();
        }
        else {
            //necessary?
            derivedTerm = derivedTerm.normalized();
        }

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
        } else {
            budget = BudgetFunctions.compoundBackward(derivedTerm, premise);
        }

        if (!premise.validateDerivedBudget(budget)) {
            if (Global.DEBUG) {
                removeInsufficientBudget(premise, new PreTask(derivedTerm, punct, truth, budget, occurence_shift, premise));
            }
            return null;
        }


        FluentTask deriving = premise.newTask((Compound) derivedTerm); //, task, belief, allowOverlap);
        if (deriving != null) {

            //TODO ANTICIPATE IF IN FUTURE AND Event:Anticipate is given

            final long now = premise.time();
            final long occ;

            if (occurence_shift > Stamp.TIMELESS) {//!t.isEternal()) {


                //verify some conditions which should not produce a temporal task
//                /*if (Global.DEBUG) */        {
//
//                    if ((single && task.isEternal()) ||
//                            (!single && (task.isEternal() && belief.isEternal())
//                            )) {
//                        throw new RuntimeException("derived temporal task from eternal parents: " + task);
//                    }
//                }

                if (!premise.nal(7)) {
                    if (Global.DEBUG) {
                        throw new RuntimeException("temporal change with lower than NAL7");
                    }
                    return null;
                }

                occ = task.getOccurrenceTime() + occurence_shift;
            }
            else {
                occ = task.getOccurrenceTime();
            }



            final Task derived = premise.validate(deriving
                    .punctuation(punct)
                    .truth(truth)
                    .budget(budget)
                    .time(now, occ)
                    .parent(task, single ? null : belief)
            );

            if (derived != null) {
                if (Global.DEBUG) {
                    derived.log(rule.toString());
                    //t.log(premise + "," + rule);
                }

                return derived;
            }
        }

        return null;
    }

    /** for debugging */
    private void removeInsufficientBudget(Premise premise, PreTask task) {
        premise.memory().remove(task, "Insufficient Derivation Budget");
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


        final TruthOrDesireFunction f = getTruthFunction(punc, outcome);
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

    static TruthOrDesireFunction getTruthFunction(char punc, PostCondition outcome) {

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

    /**
     * provides the cached result if it exists, otherwise computes it and adds to cache
     */
    public final Term resolve(final Term t) {

//       //There are after-preconditions which bind a pattern variable
//        if (rule.numPatternVariables() > map1.size()) {
//            System.err.println("predicted reactor leak");
//            //return null;
//        }

        return t.substituted(xy);
    }





    /** return null if no postconditions match (equivalent to an empty array)
     *  or an array of matching PostConditions to apply */
    public PostCondition[] run(TaskRule rule) {

        start(rule);

        for (final PreCondition p : rule.preconditions) {
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
