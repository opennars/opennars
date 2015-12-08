package nars.nal.meta.op;

import nars.Op;
import nars.Premise;
import nars.Symbols;
import nars.nal.RuleMatch;
import nars.nal.TaskRule;
import nars.nal.meta.BeliefFunction;
import nars.nal.meta.DesireFunction;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TruthFunction;
import nars.nal.nal7.Sequence;
import nars.task.Task;
import nars.term.Statement;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.variable.Variable;

/**
 * first resolution of the conclusion's pattern term
 */
public final class Solve extends PreCondition {

    public final Term term;
    @Deprecated public final TaskRule rule;

    private transient final String id;
    private final boolean continueIfIncomplete;

    public Solve(Term term, TaskRule rule, boolean continueIfIncomplete) {
        this.term = term;
        this.rule = rule;
        this.continueIfIncomplete = continueIfIncomplete;
        this.id = getClass().getSimpleName() + ':' + term;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean test(RuleMatch match) {

        Term derivedTerm;

        if(null==(derivedTerm=match.apply(term, !continueIfIncomplete)))
            return false;

        if (!continueIfIncomplete && Variable.hasPatternVariable(derivedTerm)) {
            return false;
        }

        match.derived.set(derivedTerm);


        final Compound pattern = (Compound) rule.term(0);
        final Term taskpart = pattern.term(0);
        final Term beliefpart = pattern.term(1);

        Term possibleSequenceHolder = null;

        if (rule.sequenceIntervalsFromBelief) {
            possibleSequenceHolder = beliefpart;
        }
        if (rule.sequenceIntervalsFromTask) {
            possibleSequenceHolder = taskpart;
        }

        if (possibleSequenceHolder!=null && possibleSequenceHolder.hasAny(Op.SEQUENCE)) {
            processSequence(match, derivedTerm, possibleSequenceHolder);

        }

        return true;
    }

    public void processSequence(RuleMatch match, Term derivedTerm, Term toInvestigate) {
        int TermIsSequence = 1;
        int TermSubjectIsSequence = 2;
        int TermPredicateIsSequence = 3;

        int mode = 0; //nothing
        //int sequence_term_amount = 0;


        if (rule.sequenceIntervalsFromBelief || rule.sequenceIntervalsFromTask) {
            if (toInvestigate instanceof Sequence) {
                //sequence_term_amount = ((Sequence) toInvestigate).terms().length;
                mode = TermIsSequence;
            } else if (toInvestigate instanceof Statement) {
                Statement st = (Statement) toInvestigate;
                if (st.getSubject() instanceof Sequence) {
                    //sequence_term_amount = ((Sequence) st.getSubject()).terms().length;
                    mode = TermSubjectIsSequence;
                } else if (st.getPredicate() instanceof Sequence) {
                    //sequence_term_amount = ((Sequence) st.getPredicate()).terms().length;
                    mode = TermPredicateIsSequence;
                }
            }
        }

        int Nothing = 0;
        if (mode != Nothing) {

            Sequence paste = null; //where to paste it to

            //TODO: THIS CODE EXISTS TWICE WITH DIFFERENT PARAMETERS, PLACE1
            if (mode == TermIsSequence && derivedTerm instanceof Sequence) {
                paste = (Sequence) derivedTerm;
            } else if (mode == TermSubjectIsSequence && derivedTerm instanceof Statement && ((Statement) derivedTerm).getSubject() instanceof Sequence) {
                paste = (Sequence) ((Statement) derivedTerm).getSubject();
            } else if (mode == TermPredicateIsSequence && derivedTerm instanceof Statement && ((Statement) derivedTerm).getPredicate() instanceof Sequence) {
                paste = (Sequence) ((Statement) derivedTerm).getPredicate();
            }
            //END CODE

            Term lookat = null;
            Premise premise = match.premise;

            if (rule.sequenceIntervalsFromTask) {
                lookat = premise.getTask().getTerm();
            } else if (rule.sequenceIntervalsFromBelief) {
                lookat = premise.getBelief().getTerm();
            }

            //TODO: THIS CODE EXISTS TWICE WITH DIFFERENT PARAMETERS, PLACE2
            Sequence copy = null; //where to copy the interval data from
            if (mode == TermIsSequence && lookat instanceof Sequence) {
                copy = (Sequence) lookat;
            } else if (mode == TermSubjectIsSequence && lookat instanceof Statement && ((Statement) lookat).getSubject() instanceof Sequence) {
                copy = (Sequence) ((Statement) lookat).getSubject();
            } else if (mode == TermPredicateIsSequence && lookat instanceof Statement && ((Statement) lookat).getPredicate() instanceof Sequence) {
                copy = (Sequence) ((Statement) lookat).getPredicate();
            }
            //END CODE

            //ok now we can finally copy the intervals.

            if (copy!=null) {

                int[] copyIntervals = copy.intervals();

                if (paste != null) {



                    int a = copy.terms().length;
                    int b = paste.terms().length;
                    boolean sameLength = a == b;
                    boolean OneLess = a - 1 == b;

                    if (!sameLength && !OneLess) {
                        System.err.println("result Sequence insufficient elements; rule:" + rule);
                    }

                    int[] pasteIntervals = paste.intervals();

                    if (OneLess) {
                        match.occurrenceShift.set(copyIntervals[1]); //we shift according to first interval
                        System.arraycopy(copyIntervals, 2, pasteIntervals, 1, copyIntervals.length - 2);
                    } else if (sameLength) {
                        System.arraycopy(copyIntervals, 0, pasteIntervals, 0, copyIntervals.length);
                    }
                } else /* if (paste == null)  */ {
                    //ok we reduced to a single element, so its a one less case
                    match.occurrenceShift.set(copyIntervals[1]);
                }
            }
        }
    }

    public final static class Truth extends PreCondition {
        public final BeliefFunction belief;
        public final DesireFunction desire;
        public final char puncOverride;

        transient private final String id;

        public Truth(BeliefFunction belief, DesireFunction desire, char puncOverride) {
            this.belief = belief;
            this.desire = desire;
            this.puncOverride = puncOverride;

            String beliefLabel = belief==null ? "_" : belief.toString();
            String desireLabel = desire==null ? "_" : desire.toString();

            this.id = (puncOverride == 0) ?
                    (getClass().getSimpleName() + ":(" + beliefLabel + ", " + desireLabel + ")")  :
                    (getClass().getSimpleName() + ":(" + beliefLabel + ", " + desireLabel + ", \"" + puncOverride + "\")");

        }

        @Override
        public String toString() {
            return id;
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
        public boolean test(RuleMatch m) {

            Premise premise = m.premise;

            final Task task = premise.getTask();

            /** calculate derived task truth value */


            Task belief = premise.getBelief();


            final nars.truth.Truth T = task.getTruth();
            final nars.truth.Truth B = belief == null ? null : belief.getTruth();


            /** calculate derived task punctuation */
            char punct = puncOverride;
            if (punct == 0) {
                /** use the default policy determined by parent task */
                punct = task.getPunctuation();
            }


            final nars.truth.Truth truth;
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


            /** filter cyclic double-premise results  */
            if (tf != null && !tf.allowOverlap()) {
                if (m.premise.isCyclic()) {
                    //                if (Global.DEBUG && Global.DEBUG_REMOVED_CYCLIC_DERIVATIONS) {
                    //                    match.removeCyclic(outcome, premise, truth, punct);
                    //                }
                    return false;
                }
            }

            m.truth.set(truth);
            m.punct.thenSet(punct);

            return true;
        }
    }

}
