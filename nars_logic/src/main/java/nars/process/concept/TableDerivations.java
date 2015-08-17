package nars.process.concept;

import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nal.RuleTables;
import nars.nal.nal1.Negation;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal5.SyllogisticRules;
import nars.process.ConceptProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Statement;
import nars.term.Term;
import nars.term.Variables;

import static nars.Symbols.VAR_INDEPENDENT;
import static nars.nal.RuleTables.goalFromQuestion;


public class TableDerivations extends ConceptFireTaskTerm { //the real RuleTable, but since this one we will delete, the confusion will be gone


    @Override
    public final boolean apply(final ConceptProcess f, final TermLink bLink) {


        final Task task = f.getTask();
        final TaskLink tLink = f.getTaskLink();

        final Term taskTerm = tLink.getTerm();

        final Task beliefTask = f.getBelief();
        @Deprecated final Task belief = f.getBelief();
        final Term beliefTerm = bLink.getTerm();



//        if (belief == null) {
//            if (beliefTerm!=null) {
//                System.err.println("belief is null and beliefTerm = " + beliefTerm);
//            }
//        }
//        else if (!beliefTerm.equals(belief.getTerm())) {
//            System.err.println("beliefTerm " + beliefTerm + " is not equal to beliefLink term " + bLink);
//        }


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
                                SyllogisticRules.detachment(tLink.getTask(), belief, bIndex, f);
                            }
                        } else {
                            goalFromQuestion(f.getTask(), taskTerm, f);
                        }
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            if (belief.getTerm() instanceof Statement)
                                SyllogisticRules.detachment(beliefTask, task, bIndex, f);
                            /*else {
                                new RuntimeException(belief + " not a statement via termlink " + tLink).printStackTrace();
                            }*/
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
                    default:
                        none(tLink, bLink); break;
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
                                if (Variables.unify(VAR_INDEPENDENT, ((Statement) beliefTerm).getSubject(), taskTerm, u, f.memory.random)) {
                                    if (u[0] instanceof Compound) {
                                        Task<Statement> newBelief = beliefTask.clone((Compound) u[0]/*, Statement.class*/);
                                        if (newBelief != null) {
                                            Task newTaskSentence = task.clone((Compound) u[1]);
                                            if (newTaskSentence != null) {
                                                RuleTables.detachmentWithVar(newBelief, newTaskSentence, bIndex, f);
                                            }
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
                    default:
                        none(tLink, bLink); break;
                }
                break;
            case TermLink.COMPOUND_STATEMENT:
                switch (bLink.type) {
                    case TermLink.COMPONENT:
                        if (taskTerm instanceof Statement) {
                            RuleTables.componentAndStatement((Compound) f.getTerm(), bIndex, (Statement) taskTerm, tIndex, f);
                        }
                        break;
                    case TermLink.COMPOUND:
                        if (taskTerm instanceof Statement) {
                            RuleTables.compoundAndStatement((Compound) beliefTerm, bIndex, (Statement) taskTerm, tIndex, beliefTerm, f);
                        }
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if ((belief != null) && (taskTerm instanceof Statement) && (beliefTerm instanceof Statement)) {
                            RuleTables.syllogisms(tLink, bLink, tLink.getTask(), (Statement)beliefTerm, f);
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
                    default:
                        none(tLink, bLink); break;
                }
                break;
            case TermLink.COMPOUND_CONDITION:
                switch (bLink.type) {
                    case TermLink.COMPOUND:
                        if (belief != null) {
                            RuleTables.detachmentWithVar(tLink.getTask(), belief, tIndex, f);
                        }
                        break;

                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            if (taskTerm instanceof Implication) // TODO maybe put instanceof test within conditionalDedIndWithVar()
                            {
                                Term subj = ((Statement) taskTerm).getSubject();
                                if (subj instanceof Negation) {
                                    if (task.isJudgment()) {
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



                    default:
                        none(tLink, bLink); break;
                }
                break;
            default:
                none(tLink); break;
        }

        return true;
    }

    private void none(TaskLink tLink, TermLink bLink) {
        //System.err.println(this + " inactivity: " + tLink + "(" + tLink.type + ") &&& " + bLink + " (" + bLink.type + ")");
    }

    private void none(TaskLink tLink) {
        //System.err.println(this + " inactivity: " + tLink+ "(" + tLink.type + ")");
    }

}
