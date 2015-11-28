package nars.nal.meta.op;

import nars.nal.RuleMatch;
import nars.nal.TaskRule;
import nars.nal.meta.PreCondition;
import nars.nal.nal7.Sequence;
import nars.term.Statement;
import nars.term.Term;

/**
 * first resolution of the conclusion's pattern term
 */
public class Resolve extends PreCondition {

    public final Term term;
    @Deprecated public final TaskRule rule;

    public Resolve(Term term, TaskRule rule) {
        this.term = term;
        this.rule = rule;
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

        match.post.derivedTerm = derivedTerm;

        Term taskpart = rule.terms()[0].term(0);
        Term beliefpart = rule.terms()[0].term(1);

        Term toInvestigate = null;

        if (rule.sequenceIntervalsFromBelief) {
            toInvestigate = beliefpart;
        }
        if (rule.sequenceIntervalsFromTask) {
            toInvestigate = taskpart;
        }

        int Nothing = 0;
        int TermIsSequence = 1;
        int TermSubjectIsSequence = 2;
        int TermPredicateIsSequence = 3;

        int mode = 0; //nothing
        int sequence_term_amount = 0;
        if (rule.sequenceIntervalsFromBelief || rule.sequenceIntervalsFromTask) {
            if (toInvestigate instanceof Sequence) {
                sequence_term_amount = ((Sequence) toInvestigate).terms().length;
                mode = TermIsSequence;
            } else if (toInvestigate instanceof Statement) {
                Statement st = (Statement) toInvestigate;
                if (st.getSubject() instanceof Sequence) {
                    sequence_term_amount = ((Sequence) st.getSubject()).terms().length;
                    mode = TermSubjectIsSequence;
                } else if (st.getPredicate() instanceof Sequence) {
                    sequence_term_amount = ((Sequence) st.getPredicate()).terms().length;
                    mode = TermPredicateIsSequence;
                }
            }
        }

        if (mode != Nothing) {

            Sequence copy = null; //where to copy the interval data from
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
            if (rule.sequenceIntervalsFromTask) {
                lookat = match.premise.getTask().getTerm();
            } else if (rule.sequenceIntervalsFromBelief) {
                lookat = match.premise.getBelief().getTerm();
            }

            //TODO: THIS CODE EXISTS TWICE WITH DIFFERENT PARAMETERS, PLACE2
            if (mode == TermIsSequence && lookat instanceof Sequence) {
                copy = (Sequence) lookat;
            } else if (mode == TermSubjectIsSequence && lookat instanceof Statement && ((Statement) lookat).getSubject() instanceof Sequence) {
                copy = (Sequence) ((Statement) lookat).getSubject();
            } else if (mode == TermPredicateIsSequence && lookat instanceof Statement && ((Statement) lookat).getPredicate() instanceof Sequence) {
                copy = (Sequence) ((Statement) lookat).getPredicate();
            }
            //END CODE

            //ok now we can finally copy the intervals.

            if (copy != null && paste != null) {
                int a = copy.terms().length;
                int b = paste.terms().length;
                boolean sameLength = a == b;
                boolean OneLess = a - 1 == b;

                if (!sameLength && !OneLess) {
                    System.out.println("the case where the resulting sequence has less elements should not happen and needs to be analyzed!!");
                }

                if (OneLess) {
                    match.post.occurence_shift = copy.intervals()[1]; //we shift according to first interval
                    for (int i = 2; i < copy.intervals().length; i++) { //and copy the rest into the conclusion
                        paste.intervals()[i - 1] = copy.intervals()[i];
                    }
                } else if (sameLength) {
                    for (int i = 0; i < copy.intervals().length; i++) {
                        paste.intervals()[i] = copy.intervals()[i];
                    }
                }
            } else if (copy != null && paste == null) { //ok we reduced to a single element, so its a one less case
                match.post.occurence_shift = copy.intervals()[1];
            }
        }


        return true;
    }


}
