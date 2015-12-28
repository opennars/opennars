package nars.nal.meta;

import nars.Op;
import nars.Symbols;
import nars.nal.Level;
import nars.nal.PremiseRule;
import nars.term.Term;
import nars.term.Terms;
import nars.term.atom.Atom;
import nars.term.compound.Compound;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static nars.$.the;

/**
 * Describes a derivation postcondition
 * Immutable
 */
public class PostCondition implements Serializable, Level //since there can be multiple tasks derived per rule
{

    public static final float HALF = 0.5f;
    public final Term beliefTruth;
    public final Term goalTruth;

    public PostCondition(Term term, Term beliefTruth, Term goalTruth, char puncOverride) {
        this.term = term;
        //this.modifiers = modifiers;
        this.beliefTruth = beliefTruth;
        this.goalTruth = goalTruth;
        this.puncOverride = puncOverride;
        minNAL = Terms.maxLevel(term);// term.op().minLevel;
    }

    public final Term term;
    //public final Term[] modifiers;

    /** steps to apply before initially forming the derived task's term */
    //public final PreCondition[] beforeConclusions;

    /**
     * minimum NAL level necessary to involve this postcondition
     */
    public final int minNAL;

    public static final Set<Atom> reservedMetaInfoCategories = new HashSet<Atom>() {{
        add(the("Truth"));
        add(the("Stamp"));
        add(the("Desire"));
        add(the("Order"));
        add(the("Derive"));
        add(the("Info"));
        add(the("Event"));
        add(the("Punctuation"));
        add(the("SequenceIntervals"));
        add(the("Eternalize"));
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
    public transient char puncOverride = 0;


    /**
     * @param rule             rule which contains and is constructing this postcondition
     * @param term
     * @param modifiers
     * @throws RuntimeException
     */
    public static PostCondition make(PremiseRule rule, Term term,
                                     Term... modifiers) throws RuntimeException {


        //TruthOperator judgmentTruth = null,goalTruth = null;
        Term beliefTruth = null, goalTruth = null;

        //boolean negate = false;
        char puncOverride = 0;

        for (Term m : modifiers) {
            if (m.op() != Op.INHERIT) {
                throw new RuntimeException("Unknown postcondition format: " + m);
            }

            Compound i = (Compound) m;

            Term type = i.term(1);
            if (type == null) {
                throw new RuntimeException("Unknown postcondition format (predicate must be atom): " + m);
            }

            Term which = i.term(0);


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

                //TODO rename to: 'Belief'
                case "Truth":
                    beliefTruth = which;
                    break;

                case "Desire":
                    goalTruth = which;
                    break;

                case "Derive":
                    if (which.equals(PostCondition.allowBackward))
                        rule.setAllowBackward();
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


        PostCondition pc = new PostCondition(term,
                beliefTruth, goalTruth, puncOverride);

        //pc.negate = negate;
        pc.puncOverride = puncOverride;
        if (pc.valid(rule)) {
            return pc;
        }


        return null;


    }

    @Override
    public int nal() {
        return minNAL;
    }



    boolean valid(PremiseRule rule) {
        Term term = this.term;

        if (!modifiesPunctuation() && term instanceof Compound) {
            if (rule.getTaskTermPattern().equals(term) ||
                    rule.getBeliefTermPattern().equals(term))
                return false;
        }

        //assign the lowest non-zero, because non-zero will try them all anyway
        /*if (rule.minNAL == 0)
            rule.minNAL = minNAL;
        else*/
        if (minNAL != 0)
            rule.minNAL = Math.min(rule.minNAL, minNAL);

        return true;
    }

    public final boolean modifiesPunctuation() {
        return puncOverride > 0;
    }



    @Override
    public String toString() {
        return "PostCondition{" +
                "term=" + term +
                //", modifiers=" + Arrays.toString(modifiers) +
                ", beliefTruth=" + beliefTruth +
                ", goalTruth=" + goalTruth +
                ", puncOverride=" + puncOverride +
                '}';
    }



}
