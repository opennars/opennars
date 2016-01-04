package nars.nal;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import nars.$;
import nars.Global;
import nars.Op;
import nars.nal.meta.AtomicBooleanCondition;
import nars.nal.meta.BooleanCondition;
import nars.nal.meta.PostCondition;
import nars.nal.meta.TaskBeliefPair;
import nars.nal.meta.op.Solve;
import nars.nal.meta.pre.*;
import nars.nal.op.*;
import nars.term.Term;
import nars.term.TermVector;
import nars.term.Terms;
import nars.term.atom.Atom;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.constraint.MatchConstraint;
import nars.term.constraint.NoCommonSubtermsConstraint;
import nars.term.constraint.NotEqualsConstraint;
import nars.term.constraint.NotOpConstraint;
import nars.term.match.Ellipsis;
import nars.term.transform.CompoundTransform;
import nars.term.transform.MapSubst;
import nars.term.transform.VariableNormalization;
import nars.term.variable.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static nars.term.Terms.concat;

/**
 * A rule which matches a Premise and produces a Task
 * contains: preconditions, predicates, postconditions, post-evaluations and metainfo
 */
public class PremiseRule extends GenericCompound implements Level {


    public static final Class<? extends ImmediateTermTransform>[] Operators = new Class[] {
        intersect.class,
        differ.class,
        union.class,
        substitute.class,
        substituteIfUnifies.class,
        occurrsForward.class,
        occurrsBackward.class
    };

    /** blank marker trie node indicating the derivation and terminating the branch */
    public static final BooleanCondition END = new AtomicBooleanCondition<PremiseMatch>() {


        @Override
        public String toJavaConditionString() {
            return "true";
        }

        @Override
        public boolean booleanValueOf(PremiseMatch versioneds) {
            return true;
        }

        @Override public String toString() {
            return "End";
        }
    };

    public boolean immediate_eternalize = false;

    public boolean anticipate = false;
    public boolean sequenceIntervalsFromTask = false;
    public boolean sequenceIntervalsFromBelief = false;

    /** conditions which can be tested before term matching */
    public BooleanCondition[] prePreconditions;

    /** conditions which are tested after term matching, including term matching itself */
    public BooleanCondition[] postPreconditions;

    public PostCondition[] postconditions;

    public PatternCompound pattern;

    //it has certain pre-conditions, all given as predicates after the two input premises


    boolean allowBackward = false;

    /** maximum of the minimum NAL levels involved in the postconditions of this rule */
    public int minNAL;

    private final String str;
    protected String source;
    public MatchTaskBelief match;

    public final Compound getPremise() {
        return (Compound) term(0);
    }

    public final Compound getConclusion() {
        return (Compound) term(1);
    }

    PremiseRule(Compound premisesResultProduct) {
        this((Compound)premisesResultProduct.term(0), (Compound)premisesResultProduct.term(1));
    }

    public PremiseRule(Compound premises, Compound result) {
        super(Op.PRODUCT, new TermVector(premises, result) );
        str = super.toString();
    }


//    public final boolean validTaskPunctuation(final char p) {
//        if ((p == Symbols.QUESTION) && !allowQuestionTask)
//            return false;
//        return true;
//    }

    protected final void ensureValid() {

        if (getConclusionTermPattern().containsTemporal()) {
            if ((!getTaskTermPattern().containsTemporal())
                    &&
                    (!getBeliefTermPattern().containsTemporal())) {
                //if conclusion is temporal term but the premise has none:

                String s = toString();
                if ((!s.contains("after")) && (!s.contains("concurrent") && (!s.contains("measure")))) {
                    //System.err.println
                  throw new RuntimeException
                            ("Possibly invalid temporal rule from atemporal premise: " + this);

                }
            }
        }


        if (postconditions.length == 0)
            throw new RuntimeException(this + " has no postconditions");
        if (!Variable.hasPatternVariable(getTask()))
            throw new RuntimeException("rule's task term pattern has no pattern variable");
        if (!Variable.hasPatternVariable(getBelief()))
            throw new RuntimeException("rule's task belief pattern has no pattern variable");
        if (!Variable.hasPatternVariable(getConclusionTermPattern()))
            throw new RuntimeException("rule's conclusion belief pattern has no pattern variable");
    }






    /** add the sequence of involved conditions to a list, for one given postcondition (ex: called for each this.postconditions)  */
    public List<Term> getConditions(PostCondition post) {

        int n = prePreconditions.length + postPreconditions.length;

        List<Term> l = Global.newArrayList(n+4 /* estimate */);

        ///--------------
        for (BooleanCondition p : prePreconditions)
            p.addConditions(l);

        match.addPreConditions(l); //pre-conditions

        Solve truth = Solve.the(post,
            this, anticipate, immediate_eternalize,  postPreconditions
        );

        truth.addConditions(l);

        match.addConditions(l); //the match itself

        { /* FOR EACH MATCH */
            l.add(truth.getDerive()); //will be linked to and invoked by match callbacks
        }


        l.add(END);

        return l;
    }




    public void setSource(String source) {
        this.source = source;
    }

    /** source string that generated this rule (for debugging) */
    public String getSource() {
        return source;
    }

    protected final Term getTask() {
        return getPremise().term(0);
    }




    protected final Term getBelief() {
        return getPremise().term(1);
    }

    protected final Term getConclusionTermPattern() {
        return getConclusion().term(0);
    }



    @Override
    public final String toString() {
        return str;
    }

    public final Term task() {
        return pattern.term(0);
    }
    public final Term belief() {
        return pattern.term(1);
    }

    /** deduplicate and generate match-optimized compounds for rules */
    public void compile(TermIndex index) {
        Term[] premisePattern = ((Compound) term(0)).terms();
        premisePattern[0] = index.theTerm(premisePattern[0]); //task pattern
        premisePattern[1] = index.theTerm(premisePattern[1]); //belief pattern
    }

    static final class UppercaseAtomsToPatternVariables implements CompoundTransform<Compound, Term> {


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
            if ((containingCompound.op() == Op.INHERIT)
                    && PostCondition.reservedMetaInfoCategories.contains(
                    ((Compound) containingCompound).term(1)))
                return v;

            return Variable.v(Op.VAR_PATTERN, v.toString());
        }
    }

    static final UppercaseAtomsToPatternVariables UppercaseAtomsToPatternVariables = new UppercaseAtomsToPatternVariables();


    public final PremiseRule normalizeRule(PatternIndex index) {
        return new PremiseRule(
            index.theCompound(
                $.terms.transform(
                    $.terms.transform(this, UppercaseAtomsToPatternVariables),
                new PremiseRuleVariableNormalization()) ) );
    }



    public final PremiseRule setup(PatternIndex index) /* throws PremiseRuleException */ {

        compile(index);

        //1. construct precondition term array
        //Term[] terms = terms();

        Term[] precon = ((Compound) term(0)).terms();
        Term[] postcons = ((Compound) term(1)).terms();


        List<BooleanCondition> prePreConditionsList = Global.newArrayList(precon.length);
        List<BooleanCondition> preConditionsList = Global.newArrayList(precon.length);


        Term taskTermPattern = getTaskTermPattern();
        Term beliefTermPattern = getBeliefTermPattern();

        if (beliefTermPattern.op(Op.ATOM)) {
            throw new RuntimeException("belief term must contain no atoms: " + beliefTermPattern);
        }

        //if it contains an atom term, this means it is a modifier,
        //and not a belief term pattern
        //(which will not reference any particular atoms)




        pattern = new PatternCompound((Compound)$.p(taskTermPattern, beliefTermPattern));


        ListMultimap<Term, MatchConstraint> constraints = MultimapBuilder.treeKeys().arrayListValues().build();

        //additional modifiers: either preConditionsList or beforeConcs, classify them here
        for (int i = 2; i < precon.length; i++) {
//            if (!(precon[i] instanceof Inheritance)) {
//                System.err.println("unknown precondition type: " + precon[i] + " in rule: " + this);
//                continue;
//            }

            Compound predicate = (Compound) precon[i];
            Term predicate_name = predicate.term(1);

            String predicateNameStr = predicate_name.toString().substring(1);//.replace("^", "");

            BooleanCondition next = null, preNext = null;

            Term[] args;
            Term arg1, arg2;

            //if (predicate.getSubject() instanceof SetExt) {
                //decode precondition predicate arguments
            args = ((Compound)(predicate.term(0))).terms();
            arg1 = args[0];
            arg2 = (args.length > 1) ? args[1] : null;
            /*} else {
                throw new RuntimeException("invalid arguments");*/
                /*args = null;
                arg1 = arg2 = null;*/
            //}

            switch (predicateNameStr) {

                case "neq":
                    constraints.put(arg1, new NotEqualsConstraint(arg2));
                    constraints.put(arg2, new NotEqualsConstraint(arg1));

                    //TODO eliminate need for:
                    //next = NotEqual.make(arg1, arg2);

                    break;

                case "no_common_subterm":
                    constraints.put(arg1, new NoCommonSubtermsConstraint(arg2));
                    constraints.put(arg2, new NoCommonSubtermsConstraint(arg1));

                    //next = NoCommonSubterm.make(arg1, arg2);
                    break;

                //postcondition test
                case "not_equal":
                    next = NotEqual.make(arg1, arg2);
                    break;


                case "notSet":
                    constraints.put( arg1, new NotOpConstraint(Op.SetsBits) );
                    break;


                case "notConjunction":
                    constraints.put(arg1, new NotOpConstraint(Op.ConjunctivesBits));
                    break;


                case "notImplicationOrEquivalence":
                    constraints.put(arg1, new NotOpConstraint(Op.ImplicationOrEquivalenceBits));
                    break;



//                case "event":
//                    preNext = Temporality.both;
//                    break;

//                case "temporal":
//                    preNext = Temporality.either;
//                    break;

                case "after":
                    preNext = After.the;
                    break;

                case "concurrent":
                    preNext = Concurrent.the;
                    break;


                case "measure_time":
                    if (args.length!=1)
                        throw new RuntimeException("measure_time requires 1 component");

                    preNext = Temporality.both;
                    next = new MeasureTime(arg1);
                    break;



                case "substitute":
                case "substitute_if_unifies":
                    throw new RuntimeException("depr");
                    //afterConcs.add(new Substitute(arg1, (Variable)arg2));
                    //break;

                    //afterConcs.add(new SubstituteIfUnified(arg1, arg2, args[2]));
                    //break;

//                case "intersection":
//                    afterConcs.add(new Intersect(arg1, arg2, args[2]));
//                    break;
//
//                case "union":
//                    afterConcs.add(new Unite(arg1, arg2, args[2]));
//                    break;
//
//                case "difference":
//                    afterConcs.add(new Differ(arg1, arg2, args[2]));
//                    break;

                case "task":
                    switch (arg1.toString()) {
                        case "negative":
                            preNext = TaskNegative.the;
                            break;
                        case "\"?\"":
                            preNext = TaskPunctuation.TaskQuestion;
                            break;
                        case "\".\"":
                            preNext = TaskPunctuation.TaskJudgment;
                            break;
                        case "\"!\"":
                            preNext = TaskPunctuation.TaskGoal;
                            break;
                        default:
                            throw new RuntimeException("Unknown task punctuation type: " + predicate.term(0));
                    }
                    break;


                case "not_conjunction":
                case "not_set":
                case "not_implication_or_equivalence":
                case "shift_occurrence_forward":
                case "shift_occurrence_backward":
                    throw new RuntimeException("depr");

                default:
                    throw new RuntimeException("unhandled postcondition: " + predicateNameStr + " in " + this + "");

            }


            if (preNext!=null) {
                if (!prePreConditionsList.contains(preNext)) //unique
                    prePreConditionsList.add(preNext);
            }
            if (next != null)
                preConditionsList.add(next);
        }


        this.match = new MatchTaskBelief(
                            new TaskBeliefPair(pattern.term(0), pattern.term(1)), //HACK
                            constraints);


        //store to arrays
        prePreconditions = prePreConditionsList.toArray(new BooleanCondition[prePreConditionsList.size()]);
        postPreconditions = preConditionsList.toArray(new BooleanCondition[preConditionsList.size()]);


        List<PostCondition> postConditions = Global.newArrayList();

        for (int i = 0; i < postcons.length; ) {
            Term t = postcons[i++];
            if (i >= postcons.length)
                throw new RuntimeException("invalid rule: missing meta term for postcondition involving " + t);


            Term[] modifiers = ((Compound) postcons[i++]).terms();

            PostCondition pc = PostCondition.make(this, t,
                    Terms.toSortedSetArray(modifiers));

            if (pc!=null)
                postConditions.add( pc );
        }

        if (Sets.newHashSet(postConditions).size()!=postConditions.size())
            throw new RuntimeException("postcondition duplicates:\n\t" + postConditions);

        postconditions = postConditions.toArray( new PostCondition[postConditions.size() ] );


        //TODO add modifiers to affect minNAL (ex: anything temporal set to 7)
        //this will be raised by conclusion postconditions of higher NAL level
        minNAL =
                Math.max(minNAL,
                    Math.max(
                            Terms.maxLevel(pattern.term(0)),
                            Terms.maxLevel(pattern.term(1)
                            )));


        ensureValid();

        return this;
    }

    public final Term getTaskTermPattern() {
        return ((Compound) term(0)).terms()[0];
    }
    public final Term getBeliefTermPattern() {
        return ((Compound) term(0)).terms()[1];
    }

    public final void setAllowBackward() {
        this.allowBackward = true;
    }


    /**
     * for each calculable "question reverse" rule,
     * supply to the consumer
     */
    public final void forEachQuestionReversal(BiConsumer<PremiseRule,String> w) {

        //String s = w.toString();
        /*if(s.contains("task(\"?") || s.contains("task(\"@")) { //these are backward inference already
            return;
        }
        if(s.contains("substitute(")) { //these can't be reversed
            return;
        }*/

//        if(!allowBackward) { //explicitely stated in the rules now
//            return;
//        }

        // T, B, [pre] |- C, [post] ||--

        Term T = getTaskTermPattern();
        Term B = getBeliefTermPattern();
        Term C = getConclusionTermPattern();

        //      C, B, [pre], task_is_question() |- T, [post]
        PremiseRule clone1 = clone(C, B, T, true);
        w.accept(clone1, "C,B,[pre],question |- T,[post]");

        //      C, T, [pre], task_is_question() |- B, [post]
        PremiseRule clone2 = clone(C, T, B, true);
        w.accept(clone2, "C,T,[pre],question |- B,[post]");

    }



//    @Override
//    public Term clone(TermContainer subs) {
//        return null;
//    }

    //    @Override
//    public Term clone(Term[] x) {
//        return new TaskRule((Compound)x[0], (Compound)x[1]);
//    }


    /**
     * for each calculable "question reverse" rule,
     * supply to the consumer
     */
    public final PremiseRule forwardPermutation() {

        // T, B, [pre] |- C, [post] ||--

        Term T = getTaskTermPattern();
        Term B = getBeliefTermPattern();
        Term C = getConclusionTermPattern();

        //      B, T, [pre], task_is_question() |- T, [post]

        return clone(B, T, C, false);
    }

    private PremiseRule clone(Term newT, Term newB, Term newR, boolean question) {

        Map<Term,Term> m = new HashMap(3);
        m.put(getTaskTermPattern(), newT);
        m.put(getBeliefTermPattern(), newB);
        m.put(getConclusionTermPattern(), newR);

        Compound remapped = (Compound)$.terms.transform(this, new MapSubst(m), false);

        //Append taskQuestion
        Compound pc = (Compound) remapped.term(0);
        Term[] pp = pc.terms(); //premise component
        Compound newPremise = question ?
                $.p(concat(pp, TaskPunctuation.TaskQuestionTerm) ) :
                pc;

        return new PremiseRule(newPremise, (Compound)remapped.term(1));


//
//        /*if (StringUtils.countMatches(newPremise.toString(), "task(\"") > 1) {
//            System.err.println(newPremise);
//        }*/
//
//        newPremise.terms()[0] = newT;
//        newPremise.terms()[1] = newB;
//
//        Term[] newConclusion = getConclusion().terms().clone();
//        newConclusion[0] = newR;
//
//
//        return new PremiseRule(newPremise, $.p( newConclusion ));
    }

//    /**
//     * -1 or +1 depending on how arg1 and arg2 match either Task/Belief of the premise
//     * @return +1 if first arg=task, second arg = belief, -1 if opposite,
//     * throws exception if incomplete match
//     */
//    public final int getTaskOrder(Term arg1, Term arg2) {
//
//        Product p = getPremises();
//        Term taskPattern = p.term(0);
//        Term beliefPattern = p.term(1);
//        if (arg2.equals(taskPattern) && arg1.equals(beliefPattern)) {
//            return -1;
//        } else if (arg1.equals(taskPattern) && arg2.equals(beliefPattern)) {
//            return 1;
//        } else {
//            throw new RuntimeException("after(X,Y) needs to match both taks and belief patterns, in one of 2 orderings");
//        }
//
//    }

    public final int nal() { return minNAL; }

    public static final class PremiseRuleVariableNormalization extends VariableNormalization {


        int offset = 0;

        @Override protected Variable newVariable(Variable v, int serial) {


            if (v instanceof Ellipsis) {
                Ellipsis e = (Ellipsis)v;
                Variable r = e.clone($.varPattern(serial+offset), this);
                if (e.toString().contains("..+..+"))
                    throw new RuntimeException("x");
                offset = 0; //return to zero
                return r;
            }
            else {
                Variable newVar = v.normalize(serial+offset);
                return newVar;
            }
        }

        @Override
        public final boolean testSuperTerm(Compound t) {
            //descend all, because VAR_PATTERN is not yet always considered a variable
            return true;
        }

        public Term applyAfter(Variable secondary) {
            offset++;
            return apply(null, secondary, -1);
        }
    }
}




