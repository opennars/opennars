package nars.meta;

import nars.Global;
import nars.Op;
import nars.meta.pre.*;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.nal.nal4.ProductN;
import nars.premise.Premise;
import nars.task.Task;
import nars.term.*;
import nars.term.transform.CompoundTransform;
import nars.term.transform.VariableNormalization;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A rule which produces a Task
 * contains: preconditions, predicates, postconditions, post-evaluations and metainfo
 */
public class TaskRule extends Rule<Premise, Task> {

    //match first rule pattern with task


    public PreCondition[] preconditions;
    //private final Term[] preconditions; //the terms to match

    public PostCondition[] postconditions;
    public PairMatchingProduct pattern;
    private int numPatternVar;
    //it has certain pre-conditions, all given as predicates after the two input premises


    public Product getPremises() {
        return (Product) term(0);
    }

    public ProductN conclusion() {
        return (ProductN) term(1);
    }

    public TaskRule(Product premises, Product result) {
        super(premises, result);
    }


    protected void ensureValid() {
        if (!Variable.hasPatternVariable(getTask()))
            throw new RuntimeException("rule's task term pattern has no pattern variable");
        if (!Variable.hasPatternVariable(getBelief()))
            throw new RuntimeException("rule's task belief pattern has no pattern variable");
        if (!Variable.hasPatternVariable(getResult()))
            throw new RuntimeException("rule's conclusion belief pattern has no pattern variable");
    }


    public ProductN premise() {
        return (ProductN) term(0);
    }

//    public Product result() {
//        return (Product) term(1);
//    }



    public static final Set<Atom> reservedMetaInfoCategories = new HashSet(6);

    static {
        reservedMetaInfoCategories.add(Atom.the("Truth"));
        reservedMetaInfoCategories.add(Atom.the("Stamp"));
        reservedMetaInfoCategories.add(Atom.the("Desire"));
        reservedMetaInfoCategories.add(Atom.the("Order"));
        reservedMetaInfoCategories.add(Atom.the("Info"));
        reservedMetaInfoCategories.add(Atom.the("Event"));
    }

    /**
     * non-null;
     * if it returns Op.VAR_PATTERN this means that any type can apply
     */
    public final Op getTaskTermType() {
        return getTask().op();
    }

    protected final Term getTask() {
        return getPremises().term(0);
    }


    /**
     * returns Op.NONE if there is no belief term type;
     * if it returns Op.VAR_PATTERN this means that any type can apply
     */
    public Op getBeliefTermType() {
        return getBelief().op();
    }

    protected Term getBelief() {
        return getPremises().term(1);
    }

    protected Term getResult() {
        return conclusion().term(0);
    }


    /**
     * test applicability of this rule with a specific maximum NAL level
     */
    public boolean levelValid(final int nalLevel) {
        return Terms.levelValid(getTask(), nalLevel) &&
                Terms.levelValid(getBelief(), nalLevel) &&
                Terms.levelValid(getResult(), nalLevel);
    }

    public boolean isReversible() {
        //TEST
        if (toString().contains("shift_occurrence"))
            return false;
        if (toString().contains("substitute"))
            return false;
        return true;
    }

    /** how many unique pattern variables are present */
    public int numPatternVariables() {
        return numPatternVar;
    }

    @Override
    protected void init(Term... term) {
        super.init(term);


        final Set<Term> patternVars = new HashSet();
        recurseTerms((v,p) -> {
            if (v.op() == Op.VAR_PATTERN)
                patternVars.add(v);
        });
        this.numPatternVar = patternVars.size();
    }

    static class UppercaseAtomsToPatternVariables implements CompoundTransform<Compound, Term> {


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
            if ((containingCompound instanceof Inheritance)
                    && reservedMetaInfoCategories.contains(
                    ((Inheritance) containingCompound).getPredicate()))
                return v;

            return Variable.make(Op.VAR_PATTERN, v.bytes());
        }
    }

    final static UppercaseAtomsToPatternVariables uppercaseAtomsToPatternVariables = new UppercaseAtomsToPatternVariables();

    @Override
    public TaskRule normalizeDestructively() {

        this.transform(uppercaseAtomsToPatternVariables);

        rehash();

        return this;
    }

    public TaskRule normalizeRule() {

        TaskRule tr = (TaskRule) new VariableNormalization(this, false) {

            @Override
            public boolean testSuperTerm(Compound t) {
                //descend all, because VAR_PATTERN is not yet always considered a variable
                return true;
            }
        }.getResult();

        if (tr == null) {
            return null;
        }

        tr.rehash();

        return tr.setup();
    }


    @Override
    public TaskRule clone(Term[] replaced) {
        return new TaskRule((Product) replaced[0], (Product) replaced[1]);
    }

    public TaskRule setup() {


        //1. construct precondition term array
        //Term[] terms = terms();

        Term[] precon = ((Product) term(0)).terms();
        Term[] postcons = ((Product) term(1)).terms();

        postconditions = new PostCondition[postcons.length / 2]; //term_1 meta_1 ,..., term_2 meta_2 ...

        //extract preconditions
        List<PreCondition> early = Global.newArrayList(precon.length);

        List<PreCondition> beforeConcs = Global.newArrayList(precon.length);

        List<PreCondition> afterConcs = Global.newArrayList(0);


        Term taskTermPattern = precon[0];


        Term beliefTermPattern = precon[1];
        if (beliefTermPattern.has(Op.ATOM)) {
            throw new RuntimeException("belief term must be a pattern");
        }

        //if it contains an atom term, this means it is a modifier,
        //and not a belief term pattern
        //(which will not reference any particular atoms)


        this.pattern = new PairMatchingProduct(taskTermPattern, beliefTermPattern);

        final MatchTaskBeliefPattern matcher = new MatchTaskBeliefPattern(pattern);
        early.add(matcher);


        //additional modifiers: either early or beforeConcs, classify them here
        for (int i = 2; i < precon.length; i++) {
            if (!(precon[i] instanceof Inheritance)) {
                System.err.println("unknown precondition type: " + precon[i] + " in rule: " + this);
                continue;
            }

            Inheritance predicate = (Inheritance) precon[i];
            Term predicate_name = predicate.getPredicate();

            final String predicateNameStr = predicate_name.toString().substring(1);//.replace("^", "");

            PreCondition next = null;

            final Term[] args;
            final Term arg1, arg2;

            if (predicate.getSubject() instanceof SetExt) {
                //decode precondition predicate arguments
                args = ((Product) (((SetExt) predicate.getSubject()).term(0))).terms();
                arg1 = args[0];
                arg2 = (args.length > 1) ? args[1] : null;
            } else {
                throw new RuntimeException("invalid arguments");
                /*args = null;
                arg1 = arg2 = null;*/
            }

            switch (predicateNameStr) {
                case "not_equal":
                    next = new NotEqual(arg1, arg2);
                    break;
                case "set_ext":
                    next = new ExtSet(arg1);
                    break;
                case "set_int":
                    next = new IntSet(arg1);
                    break;
                case "not_set":
                    next = new NotSet(arg1);
                    break;
                case "event":
                    next = new IsEvent(arg1, arg2);
                    break;
                case "no_common_subterm":
                    next = new NoCommonSubterm(arg1, arg2);
                    break;


                case "after":
                    if ((next = after(arg1, arg2)) == null)
                        return null; //this rule is not valid, probably from a rearrangement of terms which invalidates the pattern -> task relationship this needs to test
                    break;
                case "concurrent":
                    if ((next = concurrent(arg1, arg2)) == null)
                        return null; //this rule is not valid, probably from a rearrangement of terms which invalidates the pattern -> task relationship this needs to test
                    break;

                //TODO apply similar pattern to these as after and concurrent
                case "shift_occurrence_forward":
                    next = new TimeOffset(arg1, arg2, true);
                    break;
                case "shift_occurrence_backward":
                    next = new TimeOffset(arg1, arg2, false);
                    break;
                case "measure_time":
                    if (args.length>2)
                        next = new MeasureTime(arg1, arg2, args[2]);
                    break;

                case "substitute":
                    afterConcs.add(new Substitute(arg1, arg2));
                    break;

                case "intersection":
                    afterConcs.add(new Intersection(arg1, arg2, args[2]));
                    break;

                case "union":
                    afterConcs.add(new Union(arg1, arg2, args[2]));
                    break;

                case "difference":
                    afterConcs.add(new Difference(arg1, arg2, args[2]));
                    break;

                case "not_implication_or_equivalence":
                    next = new NotImplicationOrEquivalence(arg1);
                    break;

                case "task":
                    switch (arg1.toString()) {
                        case "negative":
                            next = new TaskNegative();
                            break;
                        case "\"?\"":
                            next = TaskPunctuation.TaskQuestion;
                            break;
                        case "\".\"":
                            next = TaskPunctuation.TaskJudgment;
                            break;
                        case "\"!\"":
                            next = TaskPunctuation.TaskGoal;
                            break;
                        default:
                            throw new RuntimeException("Unknown task punctuation type: " + predicate.getSubject());
                    }
                    break;


            }

            if (next != null)
                early.add(next);
        }

        //store as arrays
        this.preconditions = early.toArray(new PreCondition[early.size()]);


        int k = 0;
        for (int i = 0; i < postcons.length; ) {
            Term t = postcons[i++];
            if (i >= postcons.length)
                throw new RuntimeException("invalid rule: missing meta term for postcondition involving " + t);

            postconditions[k++] = new PostCondition(t,
                    beforeConcs.toArray(new PreCondition[beforeConcs.size()]),
                    afterConcs.toArray(new PreCondition[afterConcs.size()]),
                    ((Product) postcons[i++]).terms());
        }

        ensureValid();


        return this;
    }



//    //TEMPORARY for testing, to make sure the postcondition equality guarantees rule equality
//    boolean deepEquals(Object obj) {
//        /*
//        the precondition uniqueness is guaranted because they exist as the terms of the rule meta-term which equality is already tested for
//         */
//        if (super.equals(obj)) {
//            if (!Arrays.equals(postconditions, ((TaskRule)obj).postconditions)) {
//                throw new RuntimeException(this + " and " + obj + " have equal Rule Product but inequal postconditions");
//            }
//
//            return true;
//        }
//        return false;
//    }


    /**
     * for each calculable "question reverse" rule,
     * supply to the consumer
     */
    public void forEachQuestionReversal(Consumer<TaskRule> w) {

        // %T, %B, [pre] |- %C, [post] ||--

        Term T = this.getTask();
        Term B = this.getBelief();
        Term C = this.getResult();

        //      %C, %B, [pre], task_is_question() |- %T , [post]
        w.accept(clone(C, B, T));

        //      %T, %C, [pre], task_is_question() |- %B, [post]
        w.accept(clone(C, T, B));

    }

    private TaskRule clone(final Term newT, final Term newB, final Term newR) {


        final ProductN newPremise =
                (ProductN) Product.make(premise().cloneTerms(TaskPunctuation.TaskQuestionTerm));
        newPremise.term[0] = newT;
        newPremise.term[1] = newB;

        final Product newConclusion = Product.make(conclusion().cloneTermsReplacing(0, newR));

        return new TaskRule(newPremise, newConclusion);
    }

    /**
     * returns +1 if first arg=task, second arg = belief, -1 if opposite, and 0 if there was an incomplete match
     *
     * @param arg1
     * @param arg2
     * @param rule
     * @return
     */
    public int getTaskOrder(Term arg1, Term arg2) {

        Term taskPattern = getPremises().term(0);
        Term beliefPattern = getPremises().term(1);
        if (arg2.equals(taskPattern) && arg1.equals(beliefPattern)) {
            return -1;
        } else if (arg1.equals(taskPattern) && arg2.equals(beliefPattern)) {
            return 1;
        } else {
            //throw new RuntimeException("after(%X,%Y) needs to match both taks and belief patterns, in one of 2 orderings");
            return 0;
        }

    }


    public After after(Term arg1, Term arg2) {
        int order = getTaskOrder(arg1, arg2);
        if (order == 0) return null;
        return new After(order == 1);
    }

    public Concurrent concurrent(Term arg1, Term arg2) {
        int order = getTaskOrder(arg1, arg2);
        if (order == 0)
            return null;
        return new Concurrent();
    }

}




