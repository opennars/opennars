/*
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.control;

import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.inference.RuleTables;
import org.opennars.interfaces.Timable;
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.Events;
import org.opennars.language.Statement;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.main.Parameters;
import org.opennars.storage.Memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.opennars.entity.TermLink.COMPOUND_STATEMENT;

public class ShardControl {
    public List<Shard> shards = new ArrayList<>();

    public void limitMemory(long time) {
        if ((time % 97) == 97-1) {
            Collections.sort(shards, (s1, s2) -> { return s1.calcMaxUtility(time) > s2.calcMaxUtility(time) ? 1 : (s1.calcMaxUtility(time) == s2.calcMaxUtility(time)) ? 0 : -1;});

            while (shards.size() >= 80) {
                int here = 5;

                shards.remove(80-1);
            }
        }
    }

    /**
     * "cluster" of common goals
     */
    public static class Shard {
        public void addIfNotExists(Sentence sentence, long creationTime) {
            if (existsInShard(sentence)) {
                return;
            }

            TaskWithMetaInfo taskWMeta = new TaskWithMetaInfo(sentence);
            taskWMeta.creationTime = creationTime;
            tasksWithMeta.add(taskWMeta);
        }

        public boolean existsInShard(Sentence s) {
            for(Shard.TaskWithMetaInfo iOther : tasksWithMeta) {
                if (iOther.s.stamp.equals(s.stamp,  false, true, true)) {
                    return true;
                }
            }
            return false;
        }

        public static class TaskWithMetaInfo {
            public Sentence s;
            public long creationTime;

            public TaskWithMetaInfo(Sentence s) {
                this.s = s;
            }

            public double calcUtility(long t, double scale) {
                long dt = t - creationTime;
                return Math.exp(-dt * 0.1/* * scale*/) * s.truth.getExpectation();
            }

            public double calcExp() {
                return s.truth.getExpectation();
            }
        }

        public List<TaskWithMetaInfo> tasksWithMeta = new ArrayList<>();

        public List<Integer> lastDerivationsEvidentialHashes = new ArrayList<>(); // used to reduce overwhelming memory with derivations

        public double calcMaxUtility(long t) {
            double u = 0.0;

            for(TaskWithMetaInfo iTaskWithMeta : tasksWithMeta) {
                u = Math.max(iTaskWithMeta.calcUtility(t, tasksWithMeta.size()), u);
            }

            return u;
        }

        // selects any premise and consolts the memory for a related belief
        public void consultAny(Memory mem) {
            // TODO< select by mass of utility >
            int idx = mem.randomNumber.nextInt(tasksWithMeta.size());

            TaskWithMetaInfo taskWMeta = tasksWithMeta.get(idx);


            Term selSubterm = selectSubterm(taskWMeta.s.term, mem.randomNumber); // select random subterm

            Concept selConcept = mem.concept(selSubterm);

            Task selBeliefFromConcept = selConcept.beliefs.get(mem.randomNumber.nextInt(selConcept.beliefs.size()));

            // TODO< check for duplicates >

            // put it into tasks
            TaskWithMetaInfo createdTaskWMeta = new TaskWithMetaInfo(selBeliefFromConcept.sentence);
            createdTaskWMeta.creationTime = selBeliefFromConcept.getCreationTime();
            tasksWithMeta.add(createdTaskWMeta);
        }

        public List<Task> inference(Sentence a, Sentence b, Nar n, Parameters narParameters, Timable time) {
            Sentence taskSentence, beliefSentence;
            if (a.isGoal()) {
                taskSentence = a;
                beliefSentence = b;
            }
            else if (b.isGoal()) {
                taskSentence = b;
                beliefSentence = a;
            }
            else { // doesn't matter
                taskSentence = a;
                beliefSentence = b;
            }


            DerivationContext ctx = new DerivationContext(n.memory, narParameters, time);
            ctx.addToMemory = false; // don't want to spam memory

            { // build tasklink
                ctx.currentTaskLink = new TaskLink(new Task(taskSentence, new BudgetValue(0.5f,0.5f,0.5f, narParameters), Task.EnumType.DERIVED), null, new BudgetValue(0.5f,0.5f,0.5f, narParameters), 0);

                if (taskSentence.term.hasVar()) {
                    if (taskSentence.term instanceof Statement) {
                        Statement taskStmt = (Statement)taskSentence.term;

                        Term dummy = new Term("X");

                        TermLink template;
                        // select any side
                        //if (n.memory.randomNumber.nextInt(2) == 0) {
                        //    template = new TermLink(dummy, COMPOUND_STATEMENT, (short)1);
                        //}
                        //else

                        if (!taskStmt.getSubject().hasVar()) {
                            template = new TermLink(dummy, COMPOUND_STATEMENT, (short)0); // just pick subj
                        }
                        else {//if(!taskStmt.getPredicate().hasVar()) {
                            template = new TermLink(dummy, COMPOUND_STATEMENT, (short)1); // just pick pred
                        }



                        ctx.currentTaskLink = new TaskLink(new Task(taskSentence, new BudgetValue(0.5f,0.5f,0.5f, narParameters), Task.EnumType.DERIVED), template, new BudgetValue(0.5f,0.5f,0.5f, narParameters), 0);

                    }
                }

            }


            // HACK to put "a" into belief
            if (beliefSentence.term instanceof Statement) {
                //ctx.currentBeliefLink = new TermLink(beliefSentence.term, COMPOUND_STATEMENT, (short)1);

                Term dummy = new Term("X");

                TermLink template;
                // select any side
                //if (n.memory.randomNumber.nextInt(2) == 0) {
                //    template = new TermLink(dummy, COMPOUND_STATEMENT, (short)1);
                //}
                //else
                {
                    {
                        Statement s1 = (Statement)beliefSentence.term;

                        template = new TermLink(dummy, COMPOUND_STATEMENT, (short)1); // just pick pred

                        if (s1.getPredicate().hasVar()) {
                            if (s1.getPredicate() instanceof Statement) {
                                Statement s2 = (Statement)s1.getPredicate();

                                if (s2.getSubject().hasVar()) {
                                    template = new TermLink(dummy, COMPOUND_STATEMENT, (short)1, (short)1); // pick pred pred which is not var
                                }
                                else {
                                    template = new TermLink(dummy, COMPOUND_STATEMENT, (short)1, (short)0); // pick pred sub which is hopefully not var
                                }
                            }
                            else {
                                int here = 5;// fallback, not handled
                            }
                        }

                    }


                }
                ctx.currentBeliefLink = new TermLink(beliefSentence.term, template, new BudgetValue(0.5f, 0.5f, 0.5f, narParameters));

                int here = 5;
            }
            else {
                int x = 5;
                //throw new Exception("TODO");
            }

            if (ctx.currentBeliefLink != null) { // can be null if it is not handled

                ctx.setCurrentBelief(beliefSentence);
                BudgetValue taskBudget = BudgetFunctions.forward(taskSentence.truth, ctx);
                ctx.setCurrentTask(new Task(taskSentence, taskBudget, Task.EnumType.DERIVED));

                //System.out.println(beliefSentence);
                //System.out.println(taskSentence);

                List<Task> derivedTasks;
                { // reason and listen for derivations
                    TaskDeriveRecorder rec = new TaskDeriveRecorder();
                    n.on(Events.TaskDerive.class, rec);

                    RuleTables.reason(ctx.currentTaskLink, ctx.currentBeliefLink, ctx);

                    n.off(Events.TaskDerive.class, rec);

                    derivedTasks = rec.derivedTasks;
                }

                return derivedTasks;
            }
            else {
                return new ArrayList<>();
            }
        }

        private Term selectSubterm(Term t, Random rng) {
            // TODO

            return t.cloneDeep();
        }
    }

    private static boolean existsInShard(Shard shard, Sentence s) {
        return shard.existsInShard(s);
    }

    public void addNewShardIfNotExist(Sentence sentence, long creationTime, Parameters narParameters) {
        //return; /*

        for (Shard iShard : shards) {
            for (Shard.TaskWithMetaInfo iTaskWMeta : iShard.tasksWithMeta) {
                if (iTaskWMeta.s.term.equals(sentence.term) && Stamp.baseOverlap(iTaskWMeta.s.stamp, sentence.stamp)) {
                    return;
                }
            }
        }

        addNewShard(sentence, creationTime); // add it if it doesn't exist already

        //*/
    }

    // adds a new shard without checking for duplicates
    public void addNewShard(Sentence sentence, long creationTime) {
        if(false) System.out.println("Shard: addNewShard for s = "+sentence);

        Shard.TaskWithMetaInfo taskWMeta = new Shard.TaskWithMetaInfo(sentence);
        taskWMeta.creationTime = creationTime;

        Shard iShard = new Shard();
        iShard.tasksWithMeta.add(taskWMeta);

        // add shard
        shards.add(iShard);

        int here = 5;
    }

    public void selectShardAndDoInference(Nar n, Memory mem, Parameters narParameters, Timable timable) {
        Shard selectedShard = null;
        { // select random shard by priority
            double shardMass = calcShardUtilityMass(timable.time());
            double chosenShardMass = mem.randomNumber.nextDouble() * shardMass;
            double accuMass = 0.0;
            for(Shard iShard : shards) {
                accuMass += iShard.calcMaxUtility(timable.time());
                if (accuMass >= chosenShardMass) {
                    selectedShard = iShard;
                    break;
                }
            }
        }

        if (selectedShard == null) {
            return; // no reason to continue
        }

        { // "pull in"(consult) new knowledge
            Sentence selSentence;
            {
                int idx = mem.randomNumber.nextInt(selectedShard.tasksWithMeta.size());
                selSentence = selectedShard.tasksWithMeta.get(idx).s;
            }


            Term iSelSelTerm = selSentence.term; // current selected term for the loop

            for(int i=0;i<2;i++) { // we need to loop to select related terms

                Concept selConcept = mem.concept(iSelSelTerm);
                if (selConcept == null) {
                    break; // give up to walk related terms
                }

                // we need to select random term
                TermLink selTermLink = selConcept.termLinks.takeOut();
                if (selTermLink == null) {
                    break; // give up to walk related terms
                }
                selConcept.termLinks.putIn(selTermLink);

                Term targetTerm = selTermLink.target;
                Concept targetC = mem.concept(targetTerm);

                iSelSelTerm = targetTerm;

                // select random belief as premise
                Task selBelief = null;
                if (targetC != null && targetC.beliefs.size() > 0) {
                    int beliefIdx = mem.randomNumber.nextInt(targetC.beliefs.size());
                    selBelief = targetC.beliefs.get(beliefIdx);

                    if(false) System.out.println("Shard: selected secondary belief = " + selBelief.sentence);
                }

                if (selBelief != null) {
                    int here = 6;

                    if (!existsInShard(selectedShard, selBelief.sentence)) {
                        // add to shard to "pull it in"
                        Shard.TaskWithMetaInfo createdTaskWithMeta = new Shard.TaskWithMetaInfo(selBelief.sentence);
                        createdTaskWithMeta.creationTime = selBelief.getCreationTime();
                        selectedShard.tasksWithMeta.add(createdTaskWithMeta);
                    }
                }

                int here6 = 6;
            }



            int here = 5;
        }

        if(selectedShard.tasksWithMeta.size() >= 2) { // do inference for shard and store results into shard

            // select two premises by random
            int beliefAIdx = n.memory.randomNumber.nextInt(selectedShard.tasksWithMeta.size());
            int beliefBIdx = 0;
            for(;;) {
                beliefBIdx = n.memory.randomNumber.nextInt(selectedShard.tasksWithMeta.size());
                if (beliefBIdx != beliefAIdx) {
                    break;
                }
            }

            Sentence a = selectedShard.tasksWithMeta.get(beliefAIdx).s;
            Sentence b = selectedShard.tasksWithMeta.get(beliefBIdx).s;

            // TODO< put goal into special storage >
            if (!a.isGoal() && !b.isGoal()) {
                return; // one must be goal
            }

            List<Task> inferenceResultsOfShard = selectedShard.inference(a, b, n, narParameters, timable);

            for (Task iTask : inferenceResultsOfShard) {
                int evidentialHash = iTask.sentence.stamp.evidentialHash();
                if (selectedShard.lastDerivationsEvidentialHashes.contains(evidentialHash)) {
                    continue; // if was already derived
                }

                selectedShard.lastDerivationsEvidentialHashes.add(evidentialHash);
                if (selectedShard.lastDerivationsEvidentialHashes.size() > 10) {
                    selectedShard.lastDerivationsEvidentialHashes.remove(0);
                }

                if (false)  System.out.println("drved = "+iTask.sentence);

                mem.addNewTask(iTask, "Derived");
            }

            // add results to shard
            for (Task iTask : inferenceResultsOfShard) {
                if (iTask.sentence.isJudgment()) { // only allow judgement because every shard is for every goal/question, quest
                    selectedShard.addIfNotExists(iTask.sentence, iTask.getCreationTime());
                }
            }

            int shardItemsMax = 25;
            { // sort shard and keep under AIKR
                Collections.sort(selectedShard.tasksWithMeta, (s1, s2) -> { return s1.calcExp() > s2.calcExp() ? -1 : (s1.calcExp() == s2.calcExp() ? 0 : 1); });

                int here = 5;

                while(selectedShard.tasksWithMeta.size() > shardItemsMax) {
                    selectedShard.tasksWithMeta.remove(shardItemsMax);
                }
            }


            // create new shards
            //commented because new version MUST implicitly create new shards when something is derived!
            //for (Task iTask : inferenceResultsOfShard) {
            //    addNewShard(iTask.sentence, iTask.getCreationTime());
            //}

            int here = 5;
        }
    }

    private double calcShardUtilityMass(long t) {
        double mass = 0;
        for(Shard iShard : shards) {
            mass += iShard.calcMaxUtility(t);
        }
        return mass;
    }

    // consults the memory for a related task in the shard
    public void consult(Shard shard, Memory mem) {
        shard.consultAny(mem);
    }

    private static class TaskDeriveRecorder implements EventEmitter.EventObserver {
        public List<Task> derivedTasks = new ArrayList<>();

        @Override
        public void event(Class event, Object[] args) {
            Task task = (Task)args[0];
            derivedTasks.add(task);

            if(false) System.out.println("derived = " + task.sentence);
        }
    }
}
