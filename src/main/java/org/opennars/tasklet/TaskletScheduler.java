package org.opennars.tasklet;

import javassist.CannotCompileException;
import org.opennars.derivation.DerivationCompiler;
import org.opennars.derivation.DerivationProcessor;
import org.opennars.derivation.Helpers;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.inference.TemporalRules;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.language.Conjunction;
import org.opennars.language.Inheritance;
import org.opennars.language.Similarity;
import org.opennars.language.Term;
import org.opennars.main.Parameters;
import org.opennars.storage.Memory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class TaskletScheduler {
    private boolean verbose = false;

    private List<Tasklet> secondary; // sequences and other compositions
    private Map<Tasklet, Boolean> secondaryMap; // used to check if we already derived a conclusion
    private List<Tasklet> secondarySingleEvents;

    private Random rng = new Random(43); // with seed for debugging and testing of core - we hit a lot of unsupported cases in temporal induction because the preconditions are loosened

    private long step = 0;

    private Class compiledProgramCombineSequenceAndEvent;
    private Class compiledProgramCombineEventAndEvent;
    private Class compiledProgramTranslateSequenceToWindowedSequence;

    public TaskletScheduler(Parameters reasonerParameters) throws CannotCompileException {
        secondary = new ArrayList<>();
        secondarySingleEvents = new ArrayList<>();
        secondaryMap = new HashMap<>();

        compileDerivationPrograms();
    }

    private void compileDerivationPrograms() throws CannotCompileException {
        compiledProgramCombineSequenceAndEvent = DerivationCompiler.compile(DerivationProcessor.programCombineSequenceAndEvent);
        compiledProgramCombineEventAndEvent = DerivationCompiler.compile(DerivationProcessor.programCombineEventAndEvent);
        compiledProgramTranslateSequenceToWindowedSequence = DerivationCompiler.compile(DerivationProcessor.programTranslateSequenceToWindowedSequence);
    }

    private static boolean isEvent(Sentence sentence) {
        final Term term = sentence.term;

        if(sentence.isEternal()) {
            return false;
        }

        return term instanceof Similarity || term instanceof Inheritance;
    }

    // /param addedToMemory was the task added to memory?
    public void addTaskletByTask(Task task, EnumAddedToMemory addedToMemory, Timable timable) {
        addTasklet(new Tasklet(task, timable));
    }

    private void addTasklet(Tasklet tasklet) {
        Sentence sentence = tasklet.isBelief() ? tasklet.belief : tasklet.task.sentence;

        if (isEvent(sentence)) {
            secondarySingleEvents.add(0, tasklet);
        }
        else {
            if (!secondaryMap.containsKey(tasklet)) {
                secondary.add(0, tasklet);
                secondaryMap.put(tasklet, true);
            }
        }
    }

    public void iterate(Timable timable, Memory memory, Parameters narParameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if(verbose) {
            System.out.println("step=" + step);
        }

        indicesAlreadySampled.clear();
        for(int iteration=0;iteration < 50; iteration++) {
            sample(secondarySingleEvents, secondarySingleEvents, timable, memory, narParameters);
        }

        indicesAlreadySampled.clear();
        for(int iteration=0;iteration < 10; iteration++) {
            sample(secondary, secondarySingleEvents, timable, memory, narParameters);
        }

        if (((step+1) % 50) == 0) {
            sortByUtilityAndLimitSize(secondary, secondaryMap, timable);
            sortByUtilityAndLimitSize(secondarySingleEvents, null, timable);
        }

        int debugHere = 5;

        step++;
    }

    private void sortByUtilityAndLimitSize(List<Tasklet> tasklets, Map<Tasklet, Boolean> hashmap, Timable timable) {
        // recalc utilities
        for(int idx=0;idx<tasklets.size();idx++) {
            tasklets.get(idx).calcUtility(timable);
        }

        // TODO< find items which utility is not sorted and insert them again in the right places >
        Collections.sort(tasklets, (s1, s2) -> { return s1.cachedUtility == s2.cachedUtility ? 0 : ( s1.cachedUtility < s2.cachedUtility ? 1 : -1 ); });

        while (tasklets.size() > 20000) {
            tasklets.remove(20000-1);

            // TODO< remove from hashmap !!! >
        }
    }

    private Map<Tuple, Boolean> indicesAlreadySampled = new HashMap<>();

    private void sample(List<Tasklet> taskletsA, List<Tasklet> taskletsB, Timable timable, Memory memory, Parameters narParameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final boolean areSameTaskletLists = taskletsA == taskletsB;

        if(areSameTaskletLists && taskletsA.size() < 2) {
            return;
        }
        else if(taskletsA.size() < 1 || taskletsB.size() < 1) {
            return;
        }

        int idxA, idxB;
        for(;;) {
            idxA = rng.nextInt(taskletsA.size());
            idxB = rng.nextInt(taskletsB.size());

            if(areSameTaskletLists) {
                if(idxA != idxB) {
                    break;
                }
            }
            else {
                break;
            }
        }

        // avoid sampling the same multiple times
        if(indicesAlreadySampled.containsKey(new Tuple(idxA, idxB))) {
            return; // ignore sample
        }
        if(areSameTaskletLists && indicesAlreadySampled.containsKey(new Tuple(idxB, idxA))) {
            return; // ignore sample
        }
        indicesAlreadySampled.put(new Tuple(idxA, idxB), true);

        Tasklet taskletA = taskletsA.get(idxA);
        Tasklet taskletB = taskletsB.get(idxB);

        combine(taskletA, taskletB, timable, memory, narParameters);

    }

    // combines and infers two tasklets
    private void combine(Tasklet a, Tasklet b, Timable timable, Memory memory, Parameters narParameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (a.isTask() && b.isBelief()) {
            combine2(a.task.sentence, a.task.isInput(), b.belief, false/* don't know */, timable, memory, narParameters);
        }
        else if(a.isBelief() && b.isTask()) {
            combine2(a.belief, false/* don't know */, b.task.sentence, b.task.isInput(), timable, memory, narParameters);
        }
        else if(a.isTask() && b.isTask()) {
            combine2(a.task.sentence, a.task.isInput(), b.task.sentence, b.task.isInput(), timable, memory, narParameters);
        }
        else {
            combine2(a.belief, false /* don't know */, b.belief, false /* don't know */, timable, memory, narParameters);
        }
    }

    private void combine2(Sentence a, boolean aIsInput, Sentence b, boolean bIsInput, Timable timable, Memory memory, Parameters narParameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (a.stamp.isEternal() || b.stamp.isEternal()) {
            return; // we can't combine eternal beliefs/tasks
        }

        // order a and b by time
        if(a.stamp.getOccurrenceTime() > b.stamp.getOccurrenceTime()) {
            Sentence temp = a;
            boolean tempInput = aIsInput;
            a = b;
            aIsInput = bIsInput;
            b = temp;
            bIsInput = tempInput;
        }

        if(b.punctuation!= Symbols.JUDGMENT_MARK && b.punctuation!=Symbols.GOAL_MARK) {
            return; // succeeding can be a judgement or goal
        }

        if(a.punctuation!= Symbols.JUDGMENT_MARK) {
            return;// temporal inductions for judgements  only
        }


        // b must be input
        if (!bIsInput) {
            return;
        }

        if( Stamp.baseOverlap(a.stamp, b.stamp)) {
            return; // we can't combine the two sentences of the tasklets!
        }


        //nal.setTheNewStamp(a.stamp, b.stamp, nal.time.time());
        //nal.setCurrentBelief(b);

        // addToMemory is a misnomer - should be renamed
        List<Sentence> derivedSentences = new ArrayList<>();

        {
            Sentence derivedSentence = DerivationProcessor.processCompiledProgram("S", "E", compiledProgramCombineSequenceAndEvent, a, b, derivedSentences, timable, narParameters);
            if (derivedSentence != null) {
                derivedSentence = DerivationProcessor.processCompiledProgram("S", "", compiledProgramTranslateSequenceToWindowedSequence, derivedSentence, null, derivedSentences, timable, narParameters);
                if (derivedSentence != null) {
                    int debugHere = 1;
                }
            }

            derivedSentence = DerivationProcessor.processCompiledProgram("S", "E", compiledProgramCombineSequenceAndEvent, b, a, derivedSentences, timable, narParameters);
            if (derivedSentence != null) {
                derivedSentence = DerivationProcessor.processCompiledProgram("S", "", compiledProgramTranslateSequenceToWindowedSequence, derivedSentence, null, derivedSentences, timable, narParameters);
                if (derivedSentence != null) {
                    int debugHere = 1;
                }
            }

            derivedSentence = DerivationProcessor.processCompiledProgram("E", "E", compiledProgramCombineEventAndEvent, a, b, derivedSentences, timable, narParameters);
            if (derivedSentence != null) {
                int debugHere = 1;
            }
        }

        { // abbreviation
            // we abbreviate by counting how often sub sequences (with different occurence times) get derived
            // The most frequent occuring ones get abbreviated

            {
                for(Sentence iDerivedSentence : derivedSentences) {
                    if(!(iDerivedSentence.term instanceof Conjunction) || ((Conjunction) iDerivedSentence.term).temporalOrder != TemporalRules.ORDER_FORWARD) {
                        continue; // only sequences are possible candidates for abbreviation
                    }

                    Conjunction iDerivedSequence = (Conjunction)iDerivedSentence.term;

                    Term termWithoutIntervals = org.opennars.tasklet.Helpers.removeIntervals(iDerivedSequence);

                    // TODO< sample sub-sequences >

                    // TODO< count and register sub-sequences >

                    // TODO< abbreviate sub-sequences if the occur frequently enough >
                }
            }
        }

        { // convert to =/> when ever possible

            // TODO< split up case when predicate of the result impl is a parallel conj >

            List<Sentence> derivedSentence2 = new ArrayList<>();
            for(Sentence iSentence : derivedSentences) {
                Term transformedTerm = Helpers.convertFromSeqToSeqImpl(iSentence.term);
                if (transformedTerm == null) {
                    continue;
                }
                Sentence s = new Sentence(transformedTerm, iSentence.punctuation, iSentence.truth, iSentence.stamp);
                derivedSentence2.add(s);
            }
            derivedSentences.addAll(derivedSentence2);
        }


        // TODO< introduce variables (after abbreviation got implemented) >

        if (derivedSentences.size() > 0 && verbose) {
            System.out.println("combine2()");
            System.out.println("    " + a + " occTime=" + a.stamp.getOccurrenceTime());
            System.out.println("    " + b + " occTime=" + b.stamp.getOccurrenceTime());
        }


        if (verbose) {
            //System.out.println("=====");
            //for(Task iDerivedTask : derivedTasks) {
            //    System.out.println("derived " + iDerivedTask.toString());
            //}
            for(Sentence iDerivedSentence : derivedSentences) {
                System.out.println("derived " + iDerivedSentence);
            }
        }


        // create new tasklets from derived ones
        List<Tasklet> derivedTasklets = new ArrayList<>();
        for(Sentence iDerivedSentence : derivedSentences) {
            derivedTasklets.add(new Tasklet(iDerivedSentence, timable));
        }

        // TODO< rework to use a table with a utility function >
        for(Tasklet iDerivedTasklet : derivedTasklets) {
            addTasklet(iDerivedTasklet);

            { // feed back the derived result
                Task task = new Task(iDerivedTasklet.belief, new BudgetValue(1.0f, 0.95f, 0.95f, narParameters), Task.EnumType.DERIVED);
                memory.addNewTask(task, "tasklet", timable);
            }

        }
    }

    public enum EnumAddedToMemory {
        YES,
        NO
    }

    private static class Tuple {
        public int a, b;

        public Tuple(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public int hashCode() {
            return a + b;
        }
    }

}
