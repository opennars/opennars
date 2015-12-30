package nars.nal.meta.op;

import nars.Global;
import nars.Op;
import nars.Premise;
import nars.Symbols;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.nal.PremiseRule;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TruthOperator;
import nars.nal.nal7.Sequence;
import nars.nal.nal7.Tense;
import nars.process.ConceptProcess;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.Statement;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.variable.Variable;
import nars.truth.BeliefFunction;
import nars.truth.DesireFunction;
import nars.truth.Truth;

import java.util.Arrays;

import static nars.term.Statement.pred;
import static nars.term.Statement.subj;
import static nars.truth.TruthFunctions.eternalizedConfidence;

/**
 * Evaluates the truth of a premise
 */
public final class Solve extends PreCondition {
    public final TruthOperator belief;
    public final TruthOperator desire;
    public final char puncOverride;

    private final transient String id;

    public final PremiseRule rule;
    private final Derive derive;


    public Solve(Term beliefTerm, Term desireTerm, char puncOverride,
                 PremiseRule rule, boolean anticipate, boolean eternalize, Term term,

                 PreCondition[] postPreconditions
    ) {
        this.puncOverride = puncOverride;

        belief = BeliefFunction.get(beliefTerm);
//        if (belief == null &&
//                !((puncOverride==Symbols.GOAL) || (puncOverride==Symbols.QUEST) || (puncOverride==Symbols.QUESTION)))
//            throw new RuntimeException("unknown belief function " + beliefTerm);

        desire = DesireFunction.get(desireTerm);


        String beliefLabel = belief == null ? "_" :
                beliefTerm.toString();
        String desireLabel = desire == null ? "_" :
                desireTerm.toString();

        String sn = getClass().getSimpleName();
        String i = puncOverride == 0 ?
                sn + ":(" + beliefLabel + ',' + desireLabel  :
                sn + ":(" + beliefLabel + ',' + desireLabel + ",punc:\"" + puncOverride + '\"';

        i += ')';

        this.rule = rule;


        this.id = i;
        this.derive = new Derive(rule, term,
                postPreconditions,
                anticipate,
                eternalize);
    }

    @Override
    public String toString() {
        return id;
    }

//        final TruthOperator getTruth(char punc) {
//
//            switch (punc) {
//
//                case Symbols.JUDGMENT:
//                    return belief;
//
//                case Symbols.GOAL:
//                    return desire;
//
//            /*case Symbols.QUEST:
//            case Symbols.QUESTION:
//            */
//
//                default:
//                    return null;
//            }
//
//        }

    @Override
    public boolean eval(RuleMatch m) {

        Premise premise = m.premise;


        /** calculate derived task truth value */


        Task task = premise.getTask();

        /** calculate derived task punctuation */
        char punct = puncOverride;
        if (punct == 0) {
            /** use the default policy determined by parent task */
            punct = task.getPunctuation();
        }

        m.punct.set(punct);


        if (punct == Symbols.JUDGMENT || punct == Symbols.GOAL) {
            if (!measureTruth(m, punct))
                return false;
        }


        return true;
    }

    public boolean measureTruth(RuleMatch m, char punct) {
        TruthOperator tf = (punct == Symbols.JUDGMENT) ? belief : desire;
        if (tf == null)
            return false;

        /** filter cyclic double-premise results  */
        if (m.cyclic && !tf.allowOverlap()) {
            //                if (Global.DEBUG && Global.DEBUG_REMOVED_CYCLIC_DERIVATIONS) {
            //                    match.removeCyclic(outcome, premise, truth, punct);
            //                }
            return false;
        }

        return tf.apply(m);
    }

    public Derive getDerive() {
        return derive;
    }

    public static class Derive extends PreCondition {

        private final String id;

        private final boolean anticipate;
        private final boolean eternalize;
        private final PremiseRule rule;
        private final Term term;
        private final PreCondition[] postMatch;

        public Derive(PremiseRule rule, Term term, PreCondition[] postMatch, boolean anticipate, boolean eternalize ) {
            this.rule = rule;
            this.postMatch = postMatch;
            this.term = term;
            this.anticipate = anticipate;
            this.eternalize = eternalize;

            String i = "Derive:((";
            if (eternalize || anticipate) {
                if (eternalize && anticipate) {
                    i += "{eternalize,anticipate}, ";
                } else if (eternalize && !anticipate) {
                    i += "{eternalize}, ";
                } else if (anticipate && !eternalize) {
                    i += "{anticipate}, ";
                }
            }

            i += term.toString();

            if (postMatch.length > 0) {
                i += ", " + Arrays.toString(postMatch);
            }

            i += ")";
            this.id = i;
        }

        @Override
        public String toString() {
            return id;
        }

        @Override
        public boolean eval(RuleMatch m) {
            //set derivation handlers
            m.derived.set(this);
            return true;
        }

//        public void partial(RuleMatch match) {
//            Term dt = solve(match);
//            if (dt == null) return ;
//
//            //maybe this needs applied somewhre diferent
//            if (!post(match))
//                return ;
//
//            VarCachedVersionMap secondary = match.secondary;
//
//            if (!secondary.isEmpty()) {
//
//                Term rederivedTerm = dt.apply(secondary, true);
//
//                //its possible that the substitution produces an invalid term, ex: an invalid statement
//                dt = rederivedTerm;
//                if (dt == null) return;
//            }
//
//            dt = dt.normalized();
//            if (dt == null) return;
//
//
//            derive(match, dt);
//        }

        public Term solve(RuleMatch match) {

            Term derivedTerm = match.apply(term);

            if(null==derivedTerm)
                return null;

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


            return derivedTerm;
        }

        public boolean post(RuleMatch match) {

            for (PreCondition p : postMatch) {
                if (!p.eval(match))
                    return false;
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
                    lookat = premise.getTask().term();
                } else if (rule.sequenceIntervalsFromBelief) {
                    lookat = premise.getBelief()!=null ?
                            premise.getBelief().term() : null;
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

        public boolean derive(RuleMatch m, Term t) {

            if (t==null || Variable.hasPatternVariable(t))
                return false;

            Concept c = m.premise.memory().taskConcept(t);
            if (c == null)
                return false;

            derive(m, (Compound)c.term());

            return false; //match finish
        }

        private void derive(RuleMatch m, Compound c) {

            ConceptProcess premise = m.premise;

            Truth truth = m.truth.get();

            Budget budget = m.getBudget(truth, c);
            if (budget == null)
                return;


            Task task = premise.getTask();
            Task belief = premise.getBelief();


            char punct = m.punct.get();

            MutableTask deriving = new MutableTask(c);

            long now = premise.time();

            int occurence_shift = m.occurrenceShift.getIfAbsent(Tense.TIMELESS);
            long taskOcc = task.getOccurrenceTime();
            long occ = occurence_shift > Tense.TIMELESS ? taskOcc + occurence_shift : taskOcc;


            //just not able to measure it, closed world assumption gone wild.
            if (occ != Tense.ETERNAL && premise.isEternal() && !premise.nal(7)) {
                throw new RuntimeException("eternal premise " + premise + " should not result in non-eternal occurence time: " + deriving + " via rule " + rule);
            }

            if ((Global.DEBUG_DETECT_DUPLICATE_DERIVATIONS ||Global.DEBUG_LOG_DERIVING_RULE) && Global.DEBUG) {
                deriving.log(rule);
            }

            Task derived = deriving
                    .punctuation(punct)
                    .truth(truth)
                    .budget(budget)
                    .time(now, occ)
                    .parent(task, belief /* null if single */)
                    .anticipate(occ != Tense.ETERNAL && anticipate);

            if ((derived = m.derive(derived)) == null)
                return;

            //--------- TASK WAS DERIVED if it reaches here


            if (truth != null && eternalize && !derived.isEternal()) {

                m.derive(
                        new MutableTask(derived.term())
                                .punctuation(punct)
                                .truth(
                                        truth.getFrequency(),
                                        eternalizedConfidence(truth.getConfidence())
                                )
                                .budgetCompoundForward(premise)
                                .time(now, Tense.ETERNAL)
                                .parent(task, belief)
                );

            }

        }


    }
}
