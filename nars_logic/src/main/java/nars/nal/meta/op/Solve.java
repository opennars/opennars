package nars.nal.meta.op;

import nars.Op;
import nars.Premise;
import nars.Symbols;
import nars.nal.PremiseRule;
import nars.nal.RuleMatch;
import nars.nal.meta.BeliefFunction;
import nars.nal.meta.CanCycle;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TruthOperator;
import nars.nal.nal7.Sequence;
import nars.task.Task;
import nars.term.Statement;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.variable.Variable;

import static nars.term.Statement.pred;
import static nars.term.Statement.subj;

/**
 * first resolution of the conclusion's pattern term
 */
public final class Solve extends PreCondition {

    public final Term term;
    @Deprecated public final PremiseRule rule;

    private final transient String id;
    private final boolean continueIfIncomplete;

    public Solve(Term term, PremiseRule rule, boolean continueIfIncomplete) {
        this.term = term;
        this.rule = rule;
        this.continueIfIncomplete = continueIfIncomplete;
        id = getClass().getSimpleName() + ':' + term;
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


        Compound pattern = (Compound) rule.term(0);
        Term taskpart = pattern.term(0);
        Term beliefpart = pattern.term(1);

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
            } else if (toInvestigate.op().isStatement()) {

                if (subj(toInvestigate) instanceof Sequence) {
                    //sequence_term_amount = ((Sequence) st.getSubject()).terms().length;
                    mode = TermSubjectIsSequence;
                } else if (pred(toInvestigate) instanceof Sequence) {
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
            } else if (mode == TermSubjectIsSequence && derivedTerm instanceof Statement && subj(derivedTerm) instanceof Sequence) {
                paste = (Sequence) subj(derivedTerm);
            } else if (mode == TermPredicateIsSequence && derivedTerm instanceof Statement && pred(derivedTerm) instanceof Sequence) {
                paste = (Sequence) pred(derivedTerm);
            }
            //END CODE

            Term lookat = null;
            Premise premise = match.premise;

            if (rule.sequenceIntervalsFromTask) {
                lookat = premise.getTask().getTerm();
            } else if (rule.sequenceIntervalsFromBelief) {
                lookat = premise.getBelief()!=null ?
                        premise.getBelief().getTerm() : null;
            }

            //TODO: THIS CODE EXISTS TWICE WITH DIFFERENT PARAMETERS, PLACE2
            Sequence copy = null; //where to copy the interval data from
            if (mode == TermIsSequence && lookat instanceof Sequence) {
                copy = (Sequence) lookat;
            } else if (lookat!=null && lookat.op().isStatement()) {

                if (mode == TermSubjectIsSequence && subj(lookat) instanceof Sequence) {
                    copy = (Sequence) subj(lookat);
                } else if (mode == TermPredicateIsSequence && pred(lookat) instanceof Sequence) {
                    copy = (Sequence) pred(lookat);
                }

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

    public static final class Truth extends PreCondition {
        public final Term beliefTerm, desireTerm;
        public final TruthOperator belief;
        public final TruthOperator desire;
        public final char puncOverride;

        private final transient String id;

        public Truth(Term beliefTerm, Term desireTerm, char puncOverride) {
            this.beliefTerm = beliefTerm;
            this.desireTerm = desireTerm;
            this.puncOverride = puncOverride;

            belief = BeliefFunction.the.belief(beliefTerm);
            if (belief == null)
                throw new RuntimeException("unknown belief function " + beliefTerm);

            desire = BeliefFunction.the.desire(desireTerm);


            String beliefLabel = belief==null ? "_" :
                    beliefTerm.toString();
            String desireLabel = desire==null ? "_" :
                    desireTerm.toString();

            String sn = getClass().getSimpleName();
            id = puncOverride == 0 ?
                    sn + ":(" + beliefLabel + ", " + desireLabel + ')' :
                    sn + ":(" + beliefLabel + ", " + desireLabel + ", \"" + puncOverride + "\")";
        }

        @Override
        public String toString() {
            return id;
        }

        final TruthOperator getTruth(char punc) {

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


            /** calculate derived task truth value */



            Task task = premise.getTask();

            /** calculate derived task punctuation */
            char punct = puncOverride;
            if (punct == 0) {
                /** use the default policy determined by parent task */
                punct = task.getPunctuation();
            }


            nars.truth.Truth truth;

            if (punct == Symbols.JUDGMENT || punct == Symbols.GOAL) {

                TruthOperator tf = getTruth(punct);
                if (tf == null)
                    return false;

                Task belief = premise.getBelief();
                nars.truth.Truth T = task.getTruth();
                nars.truth.Truth B = belief == null ? null : belief.getTruth();

                truth = tf.apply(T, B, premise.memory());
                if (truth == null)
                    //no truth value function was applicable but it was necessary, abort
                    return false;


                /** filter cyclic double-premise results  */
                if (!(tf instanceof CanCycle)) {
                    if (premise.isCyclic()) {
                        //                if (Global.DEBUG && Global.DEBUG_REMOVED_CYCLIC_DERIVATIONS) {
                        //                    match.removeCyclic(outcome, premise, truth, punct);
                        //                }
                        return false;
                    }
                }
            } else {
                //question or quest, no truth is involved
                truth = null;
            }




            m.truth.set(truth);
            m.punct.thenSet(punct);

            return true;
        }
    }

}
