package nars.control;

import nars.entity.*;
import nars.inference.TemporalRules;
import nars.inference.TruthFunctions;
import nars.io.Symbols;
import nars.io.events.Events;
import nars.language.*;
import nars.main.Parameters;
import nars.operator.FunctionOperator;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.plugin.mental.InternalExperience;

import java.util.HashSet;

import static nars.control.ConceptProcessing.generatePotentialNegConfirmation;
import static nars.control.ConceptProcessing.questionFromGoal;

public class DecisionMaking {
    // checks if the task is firing for decision making
    public static boolean isFiring(Task task) {
        return task.aboveThreshold();
    }

    public static boolean fire(Concept concept, DerivationContext nal, Task task, Sentence goal, Task oldGoalT, Task beliefT) {
        double AntiSatisfaction = 0.5f; //we dont know anything about that goal yet, so we pursue it to remember it because its maximally unsatisfied
        if (beliefT != null) {
            Sentence belief = beliefT.sentence;
            Sentence projectedBelief = belief.projection(task.sentence.getOccurenceTime(), Parameters.DURATION);
            AntiSatisfaction = task.sentence.truth.getExpDifAbs(projectedBelief.truth);
        }

        double Satisfaction=1.0-AntiSatisfaction;
        task.setPriority(task.getPriority()* (float)AntiSatisfaction);
        if (!task.aboveThreshold()) {
            return false;
        }
        TruthValue T=goal.truth.clone();

        T.setFrequency((float) (T.getFrequency()-Satisfaction)); //decrease frequency according to satisfaction value

        boolean fullfilled = AntiSatisfaction < Parameters.SATISFACTION_TRESHOLD;

        Sentence projectedGoal = goal.projection(nal.memory.time(),nal.memory.time());

        if (projectedGoal != null && task.aboveThreshold() && !fullfilled) {

            bestReactionForGoal(concept, nal, projectedGoal, task);

            questionFromGoal(task, nal);

            concept.addToTable(task, false, concept.desires, Parameters.CONCEPT_GOALS_MAX, Events.ConceptGoalAdd.class, Events.ConceptGoalRemove.class);

            InternalExperience.InternalExperienceFromTask(concept.memory,task,false);

            if(projectedGoal.truth.getExpectation() > nal.memory.param.decisionThreshold.get() && nal.memory.time() >= concept.memory.decisionBlock) {
                //see whether the goal evidence is fully included in the old goal, if yes don't execute
                //as execution for this reason already happened (or did not since there was evidence against it)
                HashSet<Long> oldEvidence = new HashSet<Long>();
                boolean Subset=false;
                if(oldGoalT != null) {
                    Subset = true;
                    for(Long l: oldGoalT.sentence.stamp.evidentialBase) {
                        oldEvidence.add(l);
                    }
                    for(Long l: task.sentence.stamp.evidentialBase) {
                        if(!oldEvidence.contains(l)) {
                            Subset = false;
                            break;
                        }
                    }
                }
                if(!Subset && !executeDecision(nal, task)) {
                    concept.memory.emit(Events.UnexecutableGoal.class, task, concept, nal);
                    return true; //it was made true by itself
                }
            }
            return false;
        }
        return fullfilled;
    }


    /**
     * When a goal is processed, use the best memorized reaction
     * that is applicable to the current context (recent events) in case that it exists.
     * This is a special case of the choice rule and allows certain behaviors to be automated.
     */
    protected static void bestReactionForGoal(Concept concept, final DerivationContext nal, Sentence projectedGoal, final Task task) {
        try{
            Operation bestop = null;
            float bestop_truthexp = 0.0f;
            TruthValue bestop_truth = null;
            Task executable_precond = null;
            long mintime = -1;
            long maxtime = -1;
            for(Task t: concept.executable_preconditions) {
                Term[] prec = ((Conjunction) ((Implication) t.getTerm()).getSubject()).term;
                Term[] newprec = new Term[prec.length-3];
                System.arraycopy(prec, 0, newprec, 0, prec.length - 3);

                long add_tolerance = (long) (((Interval)prec[prec.length-1]).time*Parameters.ANTICIPATION_TOLERANCE);
                mintime = nal.memory.time();
                maxtime = nal.memory.time() + add_tolerance;

                Operation op = (Operation) prec[prec.length-2];
                Term precondition = Conjunction.make(newprec, TemporalRules.ORDER_FORWARD);

                Concept preconc = nal.memory.concept(precondition);
                long newesttime = -1;
                Task bestsofar = null;
                if(preconc != null) { //ok we can look now how much it is fullfilled

                    //check recent events in event bag
                    for(Task p : concept.memory.seq_current) {
                        if(p.sentence.term.equals(preconc.term) && p.sentence.isJudgment() && !p.sentence.isEternal() && p.sentence.getOccurenceTime() > newesttime  && p.sentence.getOccurenceTime() <= concept.memory.time()) {
                            newesttime = p.sentence.getOccurenceTime();
                            bestsofar = p; //we use the newest for now
                        }
                    }
                    if(bestsofar == null) {
                        continue;
                    }
                    //ok now we can take the desire value:
                    TruthValue A = projectedGoal.getTruth();
                    //and the truth of the hypothesis:
                    TruthValue Hyp = t.sentence.truth;
                    //overlap will almost never happen, but to make sure
                    if(Stamp.baseOverlap(projectedGoal.stamp.evidentialBase, t.sentence.stamp.evidentialBase)) {
                        continue; //base overlap
                    }
                    if(Stamp.baseOverlap(bestsofar.sentence.stamp.evidentialBase, t.sentence.stamp.evidentialBase)) {
                        continue; //base overlap
                    }
                    if(Stamp.baseOverlap(projectedGoal.stamp.evidentialBase, bestsofar.sentence.stamp.evidentialBase)) {
                        continue; //base overlap
                    }
                    //and the truth of the precondition:
                    Sentence projectedPrecon = bestsofar.sentence.projection(concept.memory.time() /*- distance*/, concept.memory.time());

                    if(projectedPrecon.isEternal()) {
                        continue; //projection wasn't better than eternalization, too long in the past
                    }
                    //debug start
                    //long timeA = memory.time();
                    //long timeOLD = bestsofar.sentence.stamp.getOccurrenceTime();
                    //long timeNEW = projectedPrecon.stamp.getOccurrenceTime();
                    //debug end
                    TruthValue precon = projectedPrecon.truth;
                    //and derive the conjunction of the left side:
                    TruthValue leftside = TruthFunctions.desireDed(A, Hyp);
                    //in order to derive the operator desire value:
                    TruthValue opdesire = TruthFunctions.desireDed(precon, leftside);

                    float expecdesire = opdesire.getExpectation();
                    if(expecdesire > bestop_truthexp) {
                        bestop = op;
                        bestop_truthexp = expecdesire;
                        bestop_truth = opdesire;
                        executable_precond = t;
                    }
                }
            }

            if(bestop != null && bestop_truthexp > concept.memory.param.decisionThreshold.get() /*&& Math.random() < bestop_truthexp */) {

                Sentence createdSentence = new Sentence(
                    bestop,
                    Symbols.JUDGMENT_MARK,
                    bestop_truth,
                    projectedGoal.stamp);

                Task t = new Task(createdSentence,
                    new BudgetValue(1.0f,1.0f,1.0f),
                    false);
                //System.out.println("used " +t.getTerm().toString() + String.valueOf(memory.randomNumber.nextInt()));
                if(!task.sentence.stamp.evidenceIsCyclic()) {
                    if(!executeDecision(nal, t)) { //this task is just used as dummy
                        concept.memory.emit(Events.UnexecutableGoal.class, task, concept, nal);
                    } else {
                        concept.memory.decisionBlock = concept.memory.time() + Parameters.AUTOMATIC_DECISION_USUAL_DECISION_BLOCK_CYCLES;
                        generatePotentialNegConfirmation(nal, executable_precond.sentence, executable_precond.budget, mintime, maxtime, 2);
                    }
                }
            }
        } catch(Exception ex) {
            System.out.println("Failure in operation choice rule, analyze!");
        }
    }


    /**
     * Entry point for all potentially executable tasks.
     * Returns true if the Task has a Term which can be executed
     */
    public static boolean executeDecision(DerivationContext nal, final Task t) {
        if(!nal.memory.allowExecution) {
            return false;
        }

        Term content = t.getTerm();

        if(content instanceof Operation) {

            Operation op=(Operation)content;
            Operator oper = op.getOperator();
            Product prod = (Product) op.getSubject();
            Term arg = prod.term[0];
            if(oper instanceof FunctionOperator) {
                for(int i=0;i<prod.term.length-1;i++) { //except last one, the output arg
                    if(prod.term[i].hasVarDep() || prod.term[i].hasVarIndep()) {
                        return false;
                    }
                }
            } else {
                if(content.hasVarDep() || content.hasVarIndep()) {
                    return false;
                }
            }
            if(!arg.equals(Term.SELF)) { //will be deprecated in the future
                return false;
            }

            op.setTask(t);
            if(!oper.call(op, nal.memory)) {
                return false;
            }
            System.out.println(t.toStringLong());
            //this.memory.sequenceTasks = new LevelBag<>(Parameters.SEQUENCE_BAG_LEVELS, Parameters.SEQUENCE_BAG_SIZE);
            return true;
        }
    }
}
