package nars.nal.meta;

import nars.Premise;
import nars.Symbols;
import nars.nal.Level;
import nars.nal.RuleMatch;
import nars.nal.TaskRule;
import nars.nal.nal1.Inheritance;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;
import nars.truth.Truth;

import java.io.Serializable;
import java.util.*;

import static nars.$.the;

/**
 * Describes a derivation postcondition
 * Immutable
 */
public class PostCondition implements Serializable, Level //since there can be multiple tasks derived per rule
{

    public static final float HALF = 0.5f;

    public PostCondition(Term term, PreCondition[] afterConclusions, BeliefFunction truth, DesireFunction desire) {
        this.term = term;
        //this.modifiers = modifiers;
        this.afterConclusions = afterConclusions;
        this.truth = truth;
        this.desire = desire;
        this.minNAL = Terms.maxLevel(term);// term.op().minLevel;
    }

    public final Term term;
    //public final Term[] modifiers;

    /** steps to apply before initially forming the derived task's term */
    //public final PreCondition[] beforeConclusions;

    /**
     * steps to apply after forming the goal. after each of these steps, the term will be re-resolved
     */
    public final PreCondition[] afterConclusions;

    public final BeliefFunction truth;
    public final DesireFunction desire;
    //public boolean negate = false;


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


    final static Atom
            negation = the("Negation"),
            conversion = the("Conversion"),
            contraposition = the("Contraposition"),
            identity = the("Identity"),
            allowBackward = the("AllowBackward"),
            fromTask = the("FromTask"),
            fromBelief = the("FromBelief"),
            anticipate = the("Anticipate"),
            immediate = the("Immediate");

    /**
     * if puncOverride == 0 (unspecified), then the default punctuation rule determines the
     * derived task's punctuation.  otherwise, its punctuation will be set to puncOverride's value
     */
    transient public char puncOverride = (char) 0;


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


        BeliefFunction judgmentTruth = null;
        DesireFunction goalTruth = null;

        //boolean negate = false;
        char puncOverride = 0;

        for (final Term m : modifiers) {
            if (!(m instanceof Inheritance)) {
                throw new RuntimeException("Unknown postcondition format: " + m);
            }

            final Inheritance<Term, Atom> i = (Inheritance) m;

            if (!(i.getPredicate() != null)) {
                throw new RuntimeException("Unknown postcondition format (predicate must be atom): " + m);
            }

            final Term type = i.getPredicate();
            final Term which = i.getSubject();


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
                    BeliefFunction tm = BeliefFunction.get(which);
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
                    if (which.equals(allowBackward))
                        rule.setAllowBackward(true);
                    break;

                case "Order":
                    //ignore, because this only affects at TaskRule construction
                    break;

                case "Event":
                    if (which.equals(anticipate)) {
                        rule.anticipate = true;
                    }
                    //ignore, because this only affects at TaskRule construction
                    break;

                case "Eternalize":
                    if (which.equals(immediate)) {
                        rule.immediate_eternalize = true;
                    }
                    //ignore, because this only affects at TaskRule construction
                    break;

                case "SequenceIntervals":
                    if (which.equals(fromBelief)) {
                        rule.sequenceIntervalsFromBelief = true;
                    } else if (which.equals(fromTask)) {
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
        return minNAL;
    }

    boolean valid(final TaskRule rule) {
        final Term term = this.term;

        if (!modifiesPunctuation() && term instanceof Compound) {
            if (rule.getTaskTermPattern().equals(term) ||
                    (rule.getBeliefTermPattern().equals(term)))
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


//    @Override
//    public String toString() {
//        return term + "(" + Arrays.toString(modifiers) + ")";
//    }

    @Override
    public String toString() {
        return "PostCondition{" +
                "term=" + term +
                ", afterConc=" + Arrays.toString(afterConclusions) +
                //", modifiers=" + Arrays.toString(modifiers) +
                ", truth=" + truth +
                ", desire=" + desire +
                '}';
    }

    public static class GetTruth extends PreCondition {
        public final BeliefFunction belief;
        public final DesireFunction desire;
        public final char puncOverride;

        public GetTruth(BeliefFunction belief, DesireFunction desire, char puncOverride) {
            this.belief = belief;
            this.desire = desire;
            this.puncOverride = puncOverride;
        }

        @Override
        public String toString() {
            if (puncOverride == 0)
                return getClass().getSimpleName() + "[" + belief + "," + desire + "]";
            else
                return getClass().getSimpleName() + "[" + belief + "," + desire + "," + puncOverride + "]";
        }

        TruthFunction getTruth(char punc) {

            switch (punc) {

                case Symbols.JUDGMENT:
                    return belief;

                case Symbols.GOAL:
                    return desire;

                /*case Symbols.QUEST:
                case Symbols.QUESTION:
                */

                default:
                    return null;
            }

        }

        @Override
        public boolean test(RuleMatch match) {

            Premise premise = match.premise;

            final Task task = premise.getTask();

            /** calculate derived task truth value */


            Task belief = premise.getBelief();


            final Truth T = task.getTruth();
            final Truth B = belief == null ? null : belief.getTruth();


            /** calculate derived task punctuation */
            char punct = puncOverride;
            if (punct == 0) {
                /** use the default policy determined by parent task */
                punct = task.getPunctuation();
            }


            final Truth truth;
            TruthFunction tf;

            if (punct == Symbols.JUDGMENT || punct == Symbols.GOAL) {
                tf = getTruth(punct);
                if (tf == null)
                    return false;

                truth = tf.get(T, B);

                if (truth == null) {
                    //no truth value function was applicable but it was necessary, abort
                    return false;
                }
            } else {
                //question or quest, no truth is involved
                truth = null;
                tf = null;
            }

            /** eliminate cyclic double-premise results
             *  TODO move this earlier to precondition check, or change to altogether new policy
             */
            final boolean single = (belief == null);
            if ((!single) && (match.cyclic(tf, premise))) {
//                if (Global.DEBUG && Global.DEBUG_REMOVED_CYCLIC_DERIVATIONS) {
//                    match.removeCyclic(outcome, premise, truth, punct);
//                }
                return false;
            }

            match.truth = truth;
            match.punct = punct;

            return true;
        }
    }

    /**
     * first resolution of the conclusion's pattern term
     */
    public static class Resolve extends PreCondition {

        public final Term term;

        public Resolve(Term term) {
            this.term = term;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + term + "]";
        }

        @Override
        public boolean test(RuleMatch match) {


            Term derivedTerm;

            if(null==(derivedTerm=match.resolve(term)))
                return false;

            match.derivedTerm = derivedTerm;

            return true;
        }


    }



    public void addConditions(List<PreCondition> l) {

        l.add(new GetTruth(truth, desire, puncOverride));
        l.add(new Resolve(term));

        Collections.addAll(l, afterConclusions);

        //super.addConditions(l);
    }

}
