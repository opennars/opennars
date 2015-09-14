package nars.meta;

import nars.Global;
import nars.Op;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.meta.pre.PairMatchingProduct;
import nars.premise.Premise;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.FindSubst;
import nars.truth.Truth;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;


/** rule matching context, re-recyclable if thread local */
public class RuleMatch extends FindSubst {

    public long occurence_shift;
    public TaskRule rule;

    final Map<Term,Term> resolutions = Global.newHashMap();
    public Premise premise;



    final public PairMatchingProduct taskBelief = new PairMatchingProduct();

    @Override
    public String toString() {
        return taskBelief.toString() + ":<" + super.toString() + ">:" + resolutions;
    }

    /**
     * @param type
     * @param map0
     * @param map1
     * @param random
     */
    public RuleMatch(Random random) {
        super(Op.VAR_PATTERN,
                Global.newHashMap(8),
                Global.newHashMap(8),
                random);
    }

    /** set the next premise */
    public void start(Premise p) {
        this.premise = p;
        taskBelief.set(p.getTask(), p.getTermLink().getTerm());
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

        resolutions.clear();
        occurence_shift = 0;

        this.rule = rule;
        return this;
    }

    public boolean apply(final PostCondition p) {
        final Task task = premise.getTask();
        final Task belief = premise.getBelief();


        if (task == null)
            throw new RuntimeException("null task");


        //stamp cyclic filter
        final boolean single = (belief == null);

//        {
//
//            boolean allowOverlap = false; //to be refined
//            if (!allowOverlap) {
//
//                //determine cyclicity before creating task
//                boolean cyclic;
//                boolean allowed = true;
//                if (single) {
//                    cyclic = (task.isCyclic());
//                    //it will be cyclic but only make the task if it's parent is not also cyclic
//                    allowed = cyclic && (!task.isParentCyclic());
//                } else {
//                    cyclic = Stamp.overlapping(task, belief);
//                    allowed = cyclic && (!task.isParentCyclic() && !belief.isParentCyclic());
//                }
//
//                if (allowed) {
//                    //System.err.println( ": " + premise + " cyclic");
//                    return false;
//                }
//
//
//            }
//        }
        if (!single && !p.truth.allowOverlap && premise.isCyclic()) {
            return false;
        }


        final Truth T = task.getTruth();
        final Truth B = belief == null ? null : belief.getTruth();
        final Truth truth;
        {

            if (task.isJudgment()) {
                truth = p.truth.get(T, B);
            } else if (task.isGoal()) {
                if (p.desire != null)
                    truth = p.desire.get(T, B);
                else
                    truth = null;
            } else {
                truth = null;
            }

            if (truth == null && task.isJudgmentOrGoal()) {
                //if this happens it could have been known before any substitution/matching happened
                //set a precondition when a rule precludes certain punctuations */
                /*if (Global.DEBUG) {
                    System.err.println("truth rule missing: " + this);
                }*/
                return false; //not specified!!
            }
        }

        //test and apply late preconditions
        for (final PreCondition c : p.beforeConclusions) {
            if (!c.test(this))
                return false;
        }

        Term derive;

        if ((derive = resolve(p.term)) == null) return false;

        for (final PreCondition c : p.afterConclusions) {

            if (!c.test(this))
                return false;

            if ((derive = resolve(derive)) == null) return false;
        }

        if (!(derive instanceof Compound)) return false;


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

        if (derive != null && derive.toString().contains("%")) {
            System.err.println("Reactor leak - Pattern variable detected in output: " + derive);
            //System.err.println("  " + this);
            //System.err.println("  " + premise);
            return false;
        }

        //TODO also allow substituted evaluation on output side (used by 2 rules I think)

        //TODO on occurenceDerive, for example consider ((&/,<a --> b>,+8) =/> (c --> k)), (a --> b) |- (c --> k)
        // or ((a --> b) =/> (c --> k)), (a --> b) |- (c --> k) where the order makes a difference,
        //a difference in occuring, not a difference in matching
        //CALCULATE OCCURENCE TIME HERE AND SET DERIVED TASK OCCURENCE TIME ACCORDINGLY!




        TaskSeed t = premise.newTask((Compound)derive); //, task, belief, allowOverlap);
        if (t != null) {

            final Budget budget;
            if (truth!=null) {
                budget = BudgetFunctions.compoundForward(truth, derive, premise);
            }
            else {
                budget = BudgetFunctions.compoundBackward(derive, premise);
            }


            t
                .punctuation(task.getPunctuation())
                .truth(truth)
                .budget(budget);

            if (!t.isEternal()) {
                t.occurr(t.getOccurrenceTime() + occurence_shift);
            }

            //TODO ANTICIPATE IF IN FUTURE AND Event:Anticipate is given

            if (Global.DEBUG)  {
                t.log(rule.toString());
                //t.log(premise + "," + rule);
            }

            Task tt;
            if (!single) {
                tt = premise.deriveDouble(t.parent(task,belief));
            } else {
                tt = premise.deriveSingle(t.parent(task));
            }



            return true;

        }

        return false;
    }

    final Function<Term,Term> resolver = k ->
        k!=null ? k.substituted(map1) : null;

    public Term resolveTest(final Term t) {
        Term r = resolver.apply(t);

        /* temporary */
        Term existing = resolutions.put(t, r);
        if ((existing!=null) && (!existing.equals(r))) {
            throw new RuntimeException("inconsistent resolution: " + r + "!= existing" + existing + "... in " + this);
        }

        return r;
    }

    /** provides the cached result if it exists, otherwise computes it and adds to cache */
    public final Term resolve(final Term t) {
        //cached:
        return resolutions.computeIfAbsent(t, resolver);

        //uncached:
        //return resolver.apply(t);
    }


    public void run(final List<TaskRule> u) {

        final int size = u.size();
        for (int i = 0; i < size; i++)
            u.get(i).run(this);

    }

}
