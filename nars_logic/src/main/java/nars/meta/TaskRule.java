package nars.meta;

import nars.Global;
import nars.Op;
import nars.meta.pre.*;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.nal.nal7.Interval;
import nars.nal.nal8.Operation;
import nars.premise.Premise;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.term.transform.CompoundTransform;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A rule which produces a Task
 * contains: preconditions, predicates, postconditions, post-evaluations and metainfo
 */
public class TaskRule extends Rule<Premise,Task> {

    //match first rule pattern with task


    public final PreCondition[] preconditions;
    //private final Term[] preconditions; //the terms to match

    public final PostCondition[] postconditions;
    //it has certain pre-conditions, all given as predicates after the two input premises


    public static long timeOffsetForward(Term arg, Premise nal) {
        if (arg instanceof Interval) {
            return ((Interval) arg).cycles(nal.getMemory().param.duration);
        }
        int duration = nal.getMemory().param.duration.get();
        String a = arg.toString();
        if (a.equals("\"=/>\"")) {
            return duration;
        }
        if (a.equals("\"=\\>\"")) {
            return -duration;
        }
        return 0;
    }

    public Product getPremises() {
        return (Product)term(0);
    }

    public TaskRule(Product premises, Product result) {
        super(premises, result);

        //The last entry is the postcondition
        normalizeDestructively();

        //1. construct precondition term array
        //Term[] terms = terms();

        Term[] precon = premises.terms();
        Term[] postcons = result.terms();

        postconditions = new PostCondition[postcons.length / 2]; //term_1 meta_1 ,..., term_2 meta_2 ...

        //extract preconditions
        List<PreCondition> early = Global.newArrayList(precon.length);
        List<PreCondition> late = Global.newArrayList(precon.length);


        Term taskTermMatch = precon[0];
        early.add(new MatchTaskTerm(taskTermMatch));
        //if (precon.length > 1) {
        early.add(new MatchBeliefTerm(precon[1]));
        //}


        //additional modifiers: either early or late, classify them here
        for (int i = 2; i < precon.length; i++) {
            Inheritance predicate = (Inheritance) precon[i];
            Term predicate_name = predicate.getPredicate();
            Term[] args = ((Product) (((SetExt) predicate.getSubject()).term(0))).terms();
            final String predicateNameStr = predicate_name.toString().substring(1);//.replace("^", "");

            PreCondition next = null;

            final Term arg1 = args[0];
            final Term arg2 = (args.length > 1) ? args[1] : null;

            switch (predicateNameStr) {
                case "not_equal":
                    next = new NotEqual(arg1, arg2);
                    break;
                case "event":
                    next = new IsEvent(arg1, arg2);
                    break;
                case "negative":
                    next = new IsNegative(arg1, arg2);
                    break;
                case "no_common_subterm":
                    next = new NoCommonSubterm(arg1, arg2);
                    break;
                case "measure_time":
                    next = new MeasureTime(arg1, arg2, args[2]);
                    break;
                case "after":
                    next = new After(arg1/*, arg2*/);
                    break;
                case "not_implication_or_equivalence":
                    next = new NotImplicationOrEquivalence(arg1);
                    break;
                case "concurrent":
                    next = new Concurrent(arg1);
                    break;
                case "substitute":
                    next = new Substitute(arg1, arg2);
                    break;
                case "shift_occurrence_forward":
                    next = new TimeOffset(arg1, arg2, true);
                    break;
                case "shift_occurrence_backward": {
                    next = new TimeOffset(arg1, arg2, false);
                    break;
                }

            }

            if (next != null)
                early.add(next);
        }

        //store as arrays
        this.preconditions = early.toArray(new PreCondition[early.size()]);
        final PreCondition[] _late = late.toArray(new PreCondition[late.size()]);

        int k = 0;
        for (int i = 0; i < postcons.length; ) {
            Term t = postcons[i++];
            if (i >= postcons.length)
                throw new RuntimeException("invalid rule: missing meta term for postcondition involving " + t);

            postconditions[k++] = new PostCondition(t,
                    _late,
                    ((Product)postcons[i++]).terms() );
        }

    }

    public Product premise() {
        return (Product)term(0);
    }

    public Product result() {
        return (Product) term(1);
    }

    public int premiseCount() {
        return premise().length();
    }


    public static final Set<Atom> reservedPostconditions = new HashSet(6);
    static {
        reservedPostconditions.add(Atom.the("Truth"));
        reservedPostconditions.add(Atom.the("Stamp"));
        reservedPostconditions.add(Atom.the("Desire"));
        reservedPostconditions.add(Atom.the("Order"));
        reservedPostconditions.add(Atom.the("Info"));
        reservedPostconditions.add(Atom.the("Event"));
    }

    /** non-null;
     *  if it returns Op.VAR_PATTERN this means that any type can apply */
    public Op getTaskTermType() {
        return getPremises().term(0).operator();
    }

    /** returns Op.NONE if there is no belief term type;
     if it returns Op.VAR_PATTERN this means that any type can apply */
    public Op getBeliefTermType() {
//        if (getPremises().length() < 2)
//            return Op.NONE;
        return getPremises().term(1).operator();
    }


    public static class TaskRuleNormalization implements CompoundTransform<Compound,Term> {


        @Override
        public boolean test(Term term) {
            if (term instanceof Atom) {
                String name = term.toString();
                return (Character.isUpperCase(name.charAt(0)));
            }
            return false;
        }

        @Override
        public Term apply(Compound containingCompound, Term v, int depth) {

            //do not alter postconditions
            if ((containingCompound instanceof Inheritance) && reservedPostconditions.contains(((Inheritance)containingCompound).getPredicate()))
                return v;

            return new Variable("%" + v.toString());
        }
    }

    final static TaskRuleNormalization taskRuleNormalization = new TaskRuleNormalization();

    @Override
    public TaskRule normalizeDestructively() {
        this.transform(taskRuleNormalization);
        this.invalidate();
        return this;
    }

    public TaskRule normalize() {
        return this;
    }

    public void run(RuleMatch m) {

        m.start(this);

        for (final PreCondition p : preconditions) {
            if (!p.test(m))
                return;
        }

        //if preconditions are met:
        for (final PostCondition p : postconditions)
            m.apply(p);
    }


    //TEMPORARY for testing, to make sure the postcondition equality guarantees rule equality
    boolean deepEquals(Object obj) {
        /*
        the precondition uniqueness is guaranted because they exist as the terms of the rule meta-term which equality is already tested for
         */
        if (super.equals(obj)) {
            if (!Arrays.equals(postconditions, ((TaskRule)obj).postconditions)) {
                throw new RuntimeException(this + " and " + obj + " have equal Rule Product but inequal postconditions");
            }

            return true;
        }
        return false;
    }

}



