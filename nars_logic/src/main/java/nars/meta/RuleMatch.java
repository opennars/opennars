package nars.meta;

import nars.Global;
import nars.Op;
import nars.Symbols;
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
import java.util.stream.Stream;


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

    public Task apply(final PostCondition p) {
        final Task task = premise.getTask();
        final Task belief = premise.getBelief();


        if (task == null)
            throw new RuntimeException("null task");

        if(task.isQuestion() && !rule.allowQuestionTask) {
            return null;
        }

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
        if (!single && (p.truth!=null && !p.truth.allowOverlap) && premise.isCyclic()) {
            return null;
        }


        final Truth T = task.getTruth();
        final Truth B = belief == null ? null : belief.getTruth();

        char punct = p.custom_punctuation;
        if(punct == '0') {
            punct = task.getPunctuation();
        }

        final Truth truth;
        {

            if (punct == Symbols.JUDGMENT) {
                truth = p.truth.get(T, B);

                if (truth == null) {
                    return null;
                }

            } else if (punct == Symbols.GOAL) {
                if (p.desire != null)
                    truth = p.desire.get(T, B);
                else {
                    truth = null;
                    System.err.println(p + " has null desire function");
                }

                if (truth == null) {
                    return null; //truth = null;
                }
            } else {
                //question or quest, truth should be null
                truth = null;
            }

        }

        //test and apply late preconditions
        for (final PreCondition c : p.beforeConclusions) {
            if (!c.test(this))
                return null;
        }

        Term derive;

        if ((derive = resolve(p.term)) == null) return null;

        for (final PreCondition c : p.afterConclusions) {

            if (!c.test(this))
                return null;

            if ((derive = resolve(derive)) == null) return null;
        }

        if (!(derive instanceof Compound)) return null;



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
                .punctuation(punct)
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


            if (!single) {
                t.parent(task,belief);
            } else {
                t.parent(task);
            }

            return premise.validDerivation(t);
        }

        return null;
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

       //There are after-preconditions which bind a pattern variable
      /*  if (rule.numPatternVariables() > map1.size()) {
            //System.err.println("predicted reactor leak");
            return null;
        }*/

        //cached:
        Term derive = resolutions.computeIfAbsent(t, resolver);



//        //TODO prevent this from happening
//        if (Variable.hasPatternVariable(derive)) {
//            String leakMsg = "reactor leak: " + derive;
//            //throw new RuntimeException(leakMsg);
//            System.err.println(leakMsg);
//
//            System.out.println(premise + "   -|-   ");
//
//            map1.entrySet().forEach(x -> System.out.println("  map1: " + x ));
//            map2.entrySet().forEach(x -> System.out.println("  map2: " + x ));
//            resolutions.entrySet().forEach(x -> System.out.println("  reso: " + x ));
//
//            resolver.apply(t);
//            return null;
//        }
//        else {
//            if (rule.numPatternVariables() > map1.size()) {
//                System.err.println("predicted reactor leak FAIL: " + derive);
//                System.err.println("  " + map1);
//                System.err.println("  " + rule);
//            }
//        }

        return derive;

        //uncached:
        //return resolver.apply(t);
    }


    public Stream<Task> run(final List<TaskRule> u) {
        return run(u.stream());
    }

    public Stream<Task> run(final Stream<TaskRule> trs) {
        return trs.map(r -> run(r)).flatMap(p ->
                Stream.of(p)).map(p -> apply(p)).filter(t->t!=null);
    }

    final private static PostCondition[] abortDerivation = new PostCondition[0];

    public PostCondition[] run(TaskRule rule) {

        start(rule);

        for (final PreCondition p : rule.preconditions) {
            if (!p.test(this))
                return abortDerivation;
        }
        return rule.postconditions;
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
