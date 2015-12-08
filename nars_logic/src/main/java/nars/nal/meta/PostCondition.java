package nars.nal.meta;

import nars.Symbols;
import nars.nal.Level;
import nars.nal.TaskRule;
import nars.nal.nal1.Inheritance;
import nars.term.Term;
import nars.term.Terms;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.truth.Truth;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;

import static nars.$.the;

/**
 * Describes a derivation postcondition
 * Immutable
 */
public class PostCondition implements Serializable, Level //since there can be multiple tasks derived per rule
{

    public static final float HALF = 0.5f;

    public PostCondition(Term term, PreCondition[] afterConclusions, BinaryOperator<Truth> truth, BinaryOperator<Truth> desire) {
        this.term = term;
        //this.modifiers = modifiers;
        this.afterConclusions = afterConclusions;
        this.truth = truth;
        this.desire = desire;
        minNAL = Terms.maxLevel(term);// term.op().minLevel;
    }

    public final Term term;
    //public final Term[] modifiers;

    /** steps to apply before initially forming the derived task's term */
    //public final PreCondition[] beforeConclusions;

    /**
     * steps to apply after forming the goal. after each of these steps, the term will be re-resolved
     */
    public final PreCondition[] afterConclusions;

    public final BinaryOperator<Truth> truth;
    public final BinaryOperator<Truth> desire;
    //public boolean negate = false;


    /**
     * minimum NAL level necessary to involve this postcondition
     */
    public final int minNAL;

    public static final Set<Atom> reservedMetaInfoCategories = new HashSet<Atom>() {{
        this.add(the("Truth"));
        this.add(the("Stamp"));
        this.add(the("Desire"));
        this.add(the("Order"));
        this.add(the("Derive"));
        this.add(the("Info"));
        this.add(the("Event"));
        this.add(the("Punctuation"));
        this.add(the("SequenceIntervals"));
        this.add(the("Eternalize"));
    }};


    static final Atom
            /*negation = the("Negation"),
            conversion = the("Conversion"),
            contraposition = the("Contraposition"),
            identity = the("Identity"),*/
            allowBackward = the("AllowBackward"),
            fromTask = the("FromTask"),
            fromBelief = the("FromBelief"),
            anticipate = the("Anticipate"),
            immediate = the("Immediate");

    /**
     * if puncOverride == 0 (unspecified), then the default punctuation rule determines the
     * derived task's punctuation.  otherwise, its punctuation will be set to puncOverride's value
     */
    public transient char puncOverride;


    /**
     * @param rule             rule which contains and is constructing this postcondition
     * @param term
     * @param afterConclusions
     * @param modifiers
     * @throws RuntimeException
     */
    public static PostCondition make(TaskRule rule, Term term,
                                     PreCondition[] afterConclusions,
                                     Term... modifiers) throws RuntimeException {


        BinaryOperator<Truth> judgmentTruth = null,goalTruth = null;

        //boolean negate = false;
        char puncOverride = 0;

        for (Term m : modifiers) {
            if (!(m instanceof Inheritance)) {
                throw new RuntimeException("Unknown postcondition format: " + m);
            }

            Inheritance<Term, Atom> i = (Inheritance) m;

            if (i.getPredicate() == null) {
                throw new RuntimeException("Unknown postcondition format (predicate must be atom): " + m);
            }

            Term type = i.getPredicate();
            Term which = i.getSubject();


            //negate = type.equals(negation);


            switch (type.toString()) {

                case "Punctuation":
                    switch (which.toString()) {
                        case "Question":
                            puncOverride = Symbols.QUESTION;
                            break;
                        case "Goal":
                            puncOverride = Symbols.GOAL;
                            break;
                        case "Judgment":
                            puncOverride = Symbols.JUDGMENT;
                            break;
                        case "Quest":
                            puncOverride = Symbols.QUEST;
                            break;
                        default:
                            throw new RuntimeException("unknown punctuation: " + which);
                    }
                    break;
                case "Truth":
                    BinaryOperator<Truth> tm = BeliefFunction.Helper.apply(which);
                    if (tm != null) {
                        if (judgmentTruth != null) //only allow one
                            throw new RuntimeException("beliefTruth " + judgmentTruth + " already specified; attempting to set to " + tm);
                        judgmentTruth = tm;
                    } else {
                        throw new RuntimeException("unknown TruthFunction " + which);
                    }
                    break;

                case "Desire":
                    BinaryOperator<Truth> dm = BeliefFunction.Helper.apply(which);
                    if (dm != null) {
                        if (goalTruth != null) //only allow one
                            throw new RuntimeException("goalTruth " + goalTruth + " already specified; attempting to set to " + dm);
                        goalTruth = dm;
                    } else {
                        throw new RuntimeException("unknown DesireFunction " + which);
                    }
                    break;

                case "Derive":
                    if (which.equals(PostCondition.allowBackward))
                        rule.setAllowBackward(true);
                    break;

                case "Order":
                    //ignore, because this only affects at TaskRule construction
                    break;

                case "Event":
                    if (which.equals(PostCondition.anticipate)) {
                        rule.anticipate = true;
                    }
                    //ignore, because this only affects at TaskRule construction
                    break;

                case "Eternalize":
                    if (which.equals(PostCondition.immediate)) {
                        rule.immediate_eternalize = true;
                    }
                    //ignore, because this only affects at TaskRule construction
                    break;

                case "SequenceIntervals":
                    if (which.equals(PostCondition.fromBelief)) {
                        rule.sequenceIntervalsFromBelief = true;
                    } else if (which.equals(PostCondition.fromTask)) {
                        rule.sequenceIntervalsFromTask = true;
                    }
                    break;

                default:
                    throw new RuntimeException("Unhandled postcondition: " + type + ':' + which);
            }


        }


        PostCondition pc = new PostCondition(term, afterConclusions, judgmentTruth, goalTruth);
        //pc.negate = negate;
        pc.puncOverride = puncOverride;
        if (pc.valid(rule)) {
            return pc;
        }


        return null;


    }

    @Override
    public int nal() {
        return this.minNAL;
    }



    boolean valid(TaskRule rule) {
        Term term = this.term;

        if (!this.modifiesPunctuation() && term instanceof Compound) {
            if (rule.getTaskTermPattern().equals(term) ||
                    rule.getBeliefTermPattern().equals(term))
                return false;
        }

        //assign the lowest non-zero, because non-zero will try them all anyway
        /*if (rule.minNAL == 0)
            rule.minNAL = minNAL;
        else*/
        if (this.minNAL != 0)
            rule.minNAL = Math.min(rule.minNAL, this.minNAL);

        return true;
    }

    public final boolean modifiesPunctuation() {
        return this.puncOverride > 0;
    }


//    @Override
//    public String toString() {
//        return term + "(" + Arrays.toString(modifiers) + ")";
//    }

    @Override
    public String toString() {
        return "PostCondition{" +
                "term=" + this.term +
                ", afterConc=" + Arrays.toString(this.afterConclusions) +
                //", modifiers=" + Arrays.toString(modifiers) +
                ", truth=" + this.truth +
                ", desire=" + this.desire +
                '}';
    }



}
