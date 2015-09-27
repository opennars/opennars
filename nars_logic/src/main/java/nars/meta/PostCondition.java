package nars.meta;

import nars.Global;
import nars.Symbols;
import nars.nal.nal1.Inheritance;
import nars.term.Atom;
import nars.term.Term;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes a derivation postcondition
 * Immutable
 */
public class PostCondition implements Serializable //since there can be multiple tasks derived per rule
{

    public static final float HALF = 0.5f;



    public final Term term;
    public final Term[] modifiers;

    /** steps to apply before initially forming the derived task's term */
    public final PreCondition[] beforeConclusions;

    /** steps to apply after forming the goal. after each of these steps, the term will be re-resolved */
    public final PreCondition[] afterConclusions;

    public final TruthFunction truth;
    public final DesireFunction desire;
    public boolean negate = false;

    /* high-speed adaptive RETE-like precondition filtering:

            sort all unique preconditions by a hueristic value:

                # of applications (across all appearances in rules) divided by an estimated computational cost,
                since some preconditions are less expensive to test than others.
                this value represents the discriminatory power to computational cost ratio
                of the precondition, and the higher the value, the earlier this condition
                should be tested to eliminate the most possibilities as soon as possible.

            the remaining preconditions to test in each iteration only need to be those
            which will discriminate the remaining eligible rules, and eliminating
            these sooner will require less necessary precondition tests.

            if no tests remain, the process terminates without any derivation.  this
            is the ideal result becaues a brute-force approach (as originally implemented here)
            requires the slower traversal of all rules, regardless.

     */

//    public static int totalPostconditionsRequested = 0;
//    public static final Map<Pair<Term,Term[]>, PostCondition> postconditions = Global.newHashMap();
//
//    /** this allows all unique postconditions to be stored and re-used by multiple rules */
//    public static PostCondition get(Term term, Term... modifiers) {
//        totalPostconditionsRequested++;
//        return postconditions.computeIfAbsent(Tuples.pair(term, modifiers), t -> {
//            return new PostCondition(t.getOne(), t.getTwo());
//        });
//    }

    public static final Set<Atom> reservedMetaInfoCategories = new HashSet(6);

    static {
        reservedMetaInfoCategories.add(Atom.the("Truth"));
        reservedMetaInfoCategories.add(Atom.the("Stamp"));
        reservedMetaInfoCategories.add(Atom.the("Desire"));
        reservedMetaInfoCategories.add(Atom.the("Order"));
        reservedMetaInfoCategories.add(Atom.the("Derive"));
        reservedMetaInfoCategories.add(Atom.the("Info"));
        reservedMetaInfoCategories.add(Atom.the("Event"));
        reservedMetaInfoCategories.add(Atom.the("Punctuation"));
    }

    final static Atom
        negation = Atom.the("Negation"),
        conversion = Atom.the("Conversion"),
        contraposition = Atom.the("Contraposition"),
        identity = Atom.the("Identity"),
        allowBackward = Atom.the("AllowBackward");

    public char custom_punctuation = '0';

    /**
     *
     * @param rule rule which contains and is constructing this postcondition
     * @param term
     * @param beforeConclusions
     * @param afterConclusions
     * @param modifiers
     * @throws RuntimeException
     */
    public PostCondition(TaskRule rule, Term term,
                         PreCondition[] beforeConclusions,
                         PreCondition[] afterConclusions,
                         Term... modifiers) throws RuntimeException {

        this.term = term;

        @Deprecated List<Term> mods = Global.newArrayList();
        TruthFunction beliefTruth = null;
        DesireFunction goalTruth = null;

        for (final Term m : modifiers) {
            if (!(m instanceof Inheritance)) {
                throw new RuntimeException("Unknown postcondition format: " + m);
            }

            final Inheritance<Term,Atom> i = (Inheritance) m;

            if (!(i.getPredicate() instanceof Atom)) {
                throw new RuntimeException("Unknown postcondition format (predicate must be atom): " + m);
            }

            final Atom type = i.getPredicate();
            final Term which = i.getSubject();


            negate = which.equals(negation);


            switch (type.toString()) {

                case "Punctuation":
                    if(which.toString().equals("Question")) {
                        custom_punctuation = Symbols.QUESTION;
                    }
                    //for completeness
                    if(which.toString().equals("Goal")) {
                        custom_punctuation = Symbols.GOAL;
                    }
                    if(which.toString().equals("Judgement")) {
                        custom_punctuation = Symbols.JUDGMENT;
                    }
                    if(which.toString().equals("Quest")) {
                        custom_punctuation = Symbols.QUEST;
                    }

                    break;
                case "Truth":
                    TruthFunction tm = TruthFunction.get(which);
                    if (tm != null) {
                        if (beliefTruth != null) //only allow one
                            throw new RuntimeException("beliefTruth " + beliefTruth + " already specified; attempting to set to " + tm);
                        beliefTruth = tm;
                    } else {
                        throw new RuntimeException("unknown TruthFunction " + which);
                    }
                    break;

                case "Desire":
                    DesireFunction dm = DesireFunction.get(which);
                    if (dm != null) {
                        if (goalTruth != null) //only allow one
                            throw new RuntimeException("goalTruth " + goalTruth + " already specified; attempting to set to " + dm);
                        goalTruth = dm;
                    } else {
                        throw new RuntimeException("unknown DesireFunction " + which);
                    }
                    break;

                case "Derive":
                    if (which.equals(allowBackward));
                        rule.setAllowBackward(true);
                    break;

                case "Order":
                    //ignore, because this only affects at TaskRule construction
                    break;

                default:
                    throw new RuntimeException("Unhandled postcondition: " + type + ":" + which);
            }


        }



        this.truth = beliefTruth;
        this.desire = goalTruth;

        this.modifiers = mods.toArray(new Term[mods.size()]);
        this.beforeConclusions = beforeConclusions;
        this.afterConclusions = afterConclusions;

        if (beliefTruth == null) {
            //System.err.println("missing truth function: " + this);
        }
        /*if (goalTruth == null) {
            System.err.println("missing desire function: " + this);
        }*/
    }




//    @Override
//    public String toString() {
//        return term + "(" + Arrays.toString(modifiers) + ")";
//    }

    @Override
    public String toString() {
        return "PostCondition{" +
                "term=" + term +
                ", precon=" + Arrays.toString(beforeConclusions) +
                ", modifiers=" + Arrays.toString(modifiers) +
                ", truth=" + truth +
                ", desire=" + desire +
                ", negation=" + negate +
                '}';
    }
}
