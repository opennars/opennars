package nars.nal.rule;

import nars.nal.*;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.nal1.Negation;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal5.SyllogisticRules;
import nars.nal.term.Compound;
import nars.nal.term.Term;

import static nars.io.Symbols.VAR_INDEPENDENT;
import static nars.nal.RuleTables.goalFromQuestion;


public class TableDerivations extends ConceptFireTaskTerm {

    @Override
    public boolean apply(final ConceptProcess f, final TaskLink tLink, final TermLink bLink) {

        final Sentence taskSentence = tLink.getSentence();
        final Term taskTerm = tLink.getTerm();
        final Sentence belief = f.getCurrentBelief();
        final Term beliefTerm = bLink.getTerm();

        final short tIndex = tLink.getIndex(0);
        short bIndex = bLink.getIndex(0);
        switch (tLink.type) {          // dispatch first by TaskLink type
            case TermLink.SELF:
                switch (bLink.type) {
                    case TermLink.COMPONENT:
                        RuleTables.compoundAndSelf((Compound) taskTerm, beliefTerm, true, bIndex, f);
                        break;
                    case TermLink.COMPOUND:
                        RuleTables.compoundAndSelf((Compound) beliefTerm, taskTerm, false, bIndex, f);
                        break;
                    case TermLink.COMPONENT_STATEMENT:
                        if (belief != null) {
                            if (taskTerm instanceof Statement) {
                                SyllogisticRules.detachment(taskSentence, belief, bIndex, f);
                            }
                        } else {
                            goalFromQuestion(f.getCurrentTask(), taskTerm, f);
                        }
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            if (belief.getTerm() instanceof Statement)
                                SyllogisticRules.detachment(belief, taskSentence, bIndex, f);
                            else {
                                new RuntimeException(belief + " not a statement via termlink " + tLink).printStackTrace();
                            }
                        }
                        break;
                    case TermLink.COMPONENT_CONDITION:
                        if ((belief != null) && (taskTerm instanceof Implication)) {
                            bIndex = bLink.getIndex(1);
                            SyllogisticRules.conditionalDedInd((Implication) taskTerm, bIndex, beliefTerm, tIndex, f);
                        }
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if ((belief != null) && (taskTerm instanceof Implication) && (beliefTerm instanceof Implication)) {
                            bIndex = bLink.getIndex(1);
                            SyllogisticRules.conditionalDedInd((Implication) beliefTerm, bIndex, taskTerm, tIndex, f);
                        }
                        break;
                }
                break;
            case TermLink.COMPOUND:
                switch (bLink.type) {
                    case TermLink.COMPOUND:
                        RuleTables.compoundAndCompound((Compound) taskTerm, (Compound) beliefTerm, bIndex, f);
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        RuleTables.compoundAndStatement((Compound) taskTerm, tIndex, (Statement) beliefTerm, bIndex, beliefTerm, f);
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if (belief != null) {
                            if (beliefTerm instanceof Implication) {
                                Term[] u = new Term[] { beliefTerm, taskTerm };
                                if (Variables.unify(VAR_INDEPENDENT, ((Statement) beliefTerm).getSubject(), taskTerm, u)) {
                                    Sentence<Statement> newBelief = belief.clone(u[0], Statement.class);
                                    if (newBelief!=null) {
                                        Sentence newTaskSentence = taskSentence.clone(u[1]);
                                        if (newTaskSentence!=null) {
                                            RuleTables.detachmentWithVar(newBelief, newTaskSentence, bIndex, f);
                                        }
                                    }
                                } else {
                                    SyllogisticRules.conditionalDedInd((Implication) beliefTerm, bIndex, taskTerm, -1, f);
                                }

                            } else if (beliefTerm instanceof Equivalence) {
                                SyllogisticRules.conditionalAna((Equivalence) beliefTerm, bIndex, taskTerm, -1, f);
                            }
                        }
                        break;
                }
                break;
            case TermLink.COMPOUND_STATEMENT:
                switch (bLink.type) {
                    case TermLink.COMPONENT:
                        if (taskTerm instanceof Statement) {
                            RuleTables.componentAndStatement((Compound) f.getCurrentTerm(), bIndex, (Statement) taskTerm, tIndex, f);
                        }
                        break;
                    case TermLink.COMPOUND:
                        if (taskTerm instanceof Statement) {
                            RuleTables.compoundAndStatement((Compound) beliefTerm, bIndex, (Statement) taskTerm, tIndex, beliefTerm, f);
                        }
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            RuleTables.syllogisms(tLink, bLink, taskTerm, beliefTerm, f);
                        }
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if (belief != null) {
                            bIndex = bLink.getIndex(1);
                            if ((taskTerm instanceof Statement) && (beliefTerm instanceof Implication)) {

                                RuleTables.conditionalDedIndWithVar((Implication) beliefTerm, bIndex, (Statement) taskTerm, tIndex, f);
                            }
                        }
                        break;
                }
                break;
            case TermLink.COMPOUND_CONDITION:
                switch (bLink.type) {
                    case TermLink.COMPOUND:
                        if (belief != null) {
                            RuleTables.detachmentWithVar(taskSentence, belief, tIndex, f);
                        }
                        break;

                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            if (taskTerm instanceof Implication) // TODO maybe put instanceof test within conditionalDedIndWithVar()
                            {
                                Term subj = ((Statement) taskTerm).getSubject();
                                if (subj instanceof Negation) {
                                    if (taskSentence.isJudgment()) {
                                        RuleTables.componentAndStatement((Compound) subj, bIndex, (Statement) taskTerm, tIndex, f);
                                    } else {
                                        RuleTables.componentAndStatement((Compound) subj, tIndex, (Statement) beliefTerm, bIndex, f);
                                    }
                                } else {
                                    RuleTables.conditionalDedIndWithVar((Implication) taskTerm, tIndex, (Statement) beliefTerm, bIndex, f);
                                }
                            }
                            break;

                        }
                        break;
                }
        }

        return true;
    }
}
