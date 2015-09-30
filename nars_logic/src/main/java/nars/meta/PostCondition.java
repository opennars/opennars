package nars.meta;

import nars.Symbols;
import nars.nal.nal1.Inheritance;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes a derivation postcondition
 * Immutable
 */
public class PostCondition implements Serializable //since there can be multiple tasks derived per rule
{

    public static final float HALF = 0.5f;

    public PostCondition(Term term, Term[] modifiers, PreCondition[] beforeConclusions, PreCondition[] afterConclusions, TruthFunction truth, DesireFunction desire) {
        this.term = term;
        this.modifiers = modifiers;
        this.beforeConclusions = beforeConclusions;
        this.afterConclusions = afterConclusions;
        this.truth = truth;
        this.desire = desire;
    }

    public final Term term;
    public final Term[] modifiers;

    /** steps to apply before initially forming the derived task's term */
    public final PreCondition[] beforeConclusions;

    /** steps to apply after forming the goal. after each of these steps, the term will be re-resolved */
    public final PreCondition[] afterConclusions;

    public final TruthFunction truth;
    public final DesireFunction desire;
    public boolean negate = false;


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

    /** if puncOverride == 0 (unspecified), then the default punctuation rule determines the
     *  derived task's punctuation.  otherwise, its punctuation will be set to puncOverride's value */
    transient public char puncOverride = (char)0;

    /**
     *
     * @param rule rule which contains and is constructing this postcondition
     * @param term
     * @param beforeConclusions
     * @param afterConclusions
     * @param modifiers
     * @throws RuntimeException
     */
    public static PostCondition make(TaskRule rule, Term term,
                         PreCondition[] beforeConclusions,
                         PreCondition[] afterConclusions,
                         Term... modifiers) throws RuntimeException {



        TruthFunction judgmentTruth = null;
        DesireFunction goalTruth = null;

        boolean negate = false;
        char puncOverride = 0;

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
                    switch (which.toString()) {
                        case "Question": puncOverride = Symbols.QUESTION; break;
                        case "Goal": puncOverride = Symbols.GOAL; break;
                        case "Judgment": puncOverride = Symbols.JUDGMENT; break;
                        case "Quest": puncOverride = Symbols.QUEST; break;
                        default:
                            throw new RuntimeException("unknown punctuation: " + which);
                    }
                    break;
                case "Truth":
                    TruthFunction tm = TruthFunction.get(which);
                    if (tm != null) {
                        if (judgmentTruth != null) //only allow one
                            throw new RuntimeException("beliefTruth " + judgmentTruth + " already specified; attempting to set to " + tm);
                        judgmentTruth = tm;
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



        PostCondition pc = new PostCondition(term, modifiers, beforeConclusions, afterConclusions, judgmentTruth, goalTruth);
        pc.negate = negate;
        pc.puncOverride = puncOverride;
        if (pc.valid(rule))
            return pc;
        return null;


    }

    boolean valid(final TaskRule rule) {
        final Term term = this.term;

        if (!modifiesPunctuation() && term instanceof Compound) {
            if (rule.getTaskTermPattern().equals(term) ||
                (rule.getBeliefTermPattern().equals(term)))
                return false;
        }

        return true;
    }

    public final boolean modifiesPunctuation() {
        return puncOverride > 0;
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
