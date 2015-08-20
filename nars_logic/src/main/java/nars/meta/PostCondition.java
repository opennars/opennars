package nars.meta;

import nars.Global;
import nars.nal.nal1.Inheritance;
import nars.term.Atom;
import nars.term.Term;

import java.util.Arrays;
import java.util.List;

/**
 * Describes a derivation postcondition
 * Immutable
 */
public class PostCondition //since there can be multiple tasks derived per rule
{

    public static final float HALF = 0.5f;



    public final Term term;
    public final Term[] modifiers;

    /** late preconditions, which must be applied during a PostCondition attempt */
    public final PreCondition[] precond;

    public final TruthFunction truth;
    public final DesireFunction desire;
    public boolean single_premise = false;
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

    final static Atom
        negation = Atom.the("Negation"),
        conversion = Atom.the("Conversion"),
        contraposition = Atom.the("Contraposition"),
        identity = Atom.the("Identity");


    public PostCondition(Term term, PreCondition[] latePreconditions, Term... modifiers) {
        this.term = term;

        @Deprecated List<Term> mods = Global.newArrayList();
        TruthFunction beliefTruth = null;
        DesireFunction goalTruth = null;

        for (final Term m : modifiers) {
            if (!(m instanceof Inheritance)) {
                System.err.println("Unknown postcondition format: " + m);
                continue;
            }

            final Inheritance<Term,Atom> i = (Inheritance) m;

            if (!(i.getPredicate() instanceof Atom)) {
                System.err.println("Unknown postcondition format (predicate must be atom): " + m);
            }

            final Atom type = i.getPredicate();
            final Term which = i.getSubject();


            negate = which.equals(negation);


            switch (type.toString()) {

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

                case "Order":
                    //ignore, because this only affects at TaskRule constructoin
                    break;

                default:
                    System.err.println("Unhandled postcondition: " + type + ":" + which);
                    //mods.add(m);
                    break;
            }


        }



        this.truth = beliefTruth;
        this.desire = goalTruth;

        this.modifiers = mods.toArray(new Term[mods.size()]);
        this.precond = latePreconditions;

        if (beliefTruth == null) {
            System.err.println("missing truth function: " + this);
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
                ", precon=" + Arrays.toString(precond) +
                ", modifiers=" + Arrays.toString(modifiers) +
                ", truth=" + truth +
                ", desire=" + desire +
                ", negation=" + negate +
                '}';
    }
}
