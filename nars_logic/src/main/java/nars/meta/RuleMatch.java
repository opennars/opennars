package nars.meta;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nars.Global;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.premise.Premise;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.FindSubst;
import nars.truth.Truth;

import java.util.Collections;
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




    /**
     * @param type
     * @param map0
     * @param map1
     * @param random
     */
    public RuleMatch(Random random) {
        super(Symbols.VAR_PATTERN, Global.newHashMap(8), Global.newHashMap(8), random);
    }

    /** set the next premise */
    public void start(Premise nal) {
        this.premise = nal;
    }

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

        final Truth T = task.getTruth();
        final Truth B = belief == null ? null : belief.getTruth();


        if (p.negate && task.getFrequency() >= PostCondition.HALF) { //its negation, it needs this additional information to be useful
            return false;
        }



        final Truth truth;

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
            /*if (Global.DEBUG) {
                System.err.println("truth rule missing: " + this);
            }*/
            return false; //not specified!!
        }

        //test and apply late preconditions
        for (final PreCondition c : p.precond) {

            if (!c.test(this))
                return false;

            //now we have to apply this to the derive term
        }

        Term derive = resolve(p.term);
        if (derive == null)
            return false;
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

        /*if (derive != null && derive.toString().contains("%")) {
            System.err.println("Reactor leak - Pattern variable detected in output");
        }*/

        //TODO also allow substituted evaluation on output side (used by 2 rules I think)

        //TODO on occurenceDerive, for example consider ((&/,<a --> b>,+8) =/> (c --> k)), (a --> b) |- (c --> k)
        // or ((a --> b) =/> (c --> k)), (a --> b) |- (c --> k) where the order makes a difference,
        //a difference in occuring, not a difference in matching
        //CALCULATE OCCURENCE TIME HERE AND SET DERIVED TASK OCCURENCE TIME ACCORDINGLY!

        boolean allowOverlap = false; //to be refined




        TaskSeed<Compound> t = premise.newTask((Compound)derive, task, belief, allowOverlap);
        if (t != null) {

            Budget budget = BudgetFunctions.compoundForward(truth, derive, premise);

            t.punctuation(task.getPunctuation()).truth(truth).budget(budget);

            if (t.getOccurrenceTime() != Stamp.ETERNAL) {
                t.occurr(t.getOccurrenceTime() + occurence_shift);
            }

            //TODO ANTICIPATE IF IN FUTURE AND Event:Anticipate is given

            if (belief!=null) {
                premise.deriveDouble(t);
            } else {
                premise.deriveSingle(t);
            }

            return true;

        }

        return false;
    }

    final Function<Term,Term> resolver = k -> {
        return k.substituted(map0);
    };

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
    public Term resolve(final Term t) {
        if (t == null) return null;

        return resolutions.computeIfAbsent(t, resolver);
    }




    public final void run(final List<TaskRule> u) {

        for (int i = 0; i < u.size(); i++) {
            final TaskRule uu = u.get(i);
            uu.run(this);
        }
    }

}