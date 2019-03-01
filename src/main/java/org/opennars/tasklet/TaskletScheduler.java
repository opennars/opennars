package org.opennars.tasklet;

import org.opennars.control.DerivationContext;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.inference.TemporalRules;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.language.Term;
import org.opennars.main.Parameters;
import org.opennars.storage.Bag;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskletScheduler {
    public Bag<Tasklet, Term> primary;
    public List<Tasklet> secondary;

    private Random rng = new Random(43); // with seed for debugging and testing of core - we hit a lot of unsupported cases in temporal induction because the preconditions are loosened

    public TaskletScheduler(Parameters reasonerParameters) {
        int levels = 50;
        int bagSize = 1000;

        primary = new Bag<>(levels, bagSize, reasonerParameters);
        secondary = new ArrayList<>();
    }

    // /param addedToMemory was the task added to memory?
    public void addTaskletByTask(Task task, EnumAddedToMemory addedToMemory, Timable timable) {
        secondary.add(0, new Tasklet(task, timable));
    }

    public void iterate(DerivationContext nal) {
        for(int iteration=0;iteration < 20; iteration++) {
            sample(nal);
        }

        // TODO< recalc utilities >
        // TODO< find items which utility is not sorted and inseert them again in the right places >

        // TODO< limit size of secondary >

        int debugHere = 5;
    }

    public void sample(DerivationContext nal) {
        if(secondary.size() < 2) {
            return;
        }

        int idxA, idxB;
        for(;;) {
            idxA = rng.nextInt(secondary.size());
            idxB = rng.nextInt(secondary.size());

            if(idxA != idxB) {
                break;
            }
        }

        Tasklet taskletA = secondary.get(idxA);
        Tasklet taskletB = secondary.get(idxB);

        combine(taskletA, taskletB, nal);

    }

    // combines and infers two tasklets
    private void combine(Tasklet a, Tasklet b, DerivationContext nal) {
        if (a.isTask() && b.isBelief()) {
            combine2(a.task.sentence, a.task.isInput(), b.belief, false/* don't know */, nal);
        }
        else if(a.isBelief() && b.isTask()) {
            combine2(b.belief, false/* don't know */, a.task.sentence, b.task.isInput(), nal);
        }
        else if(a.isTask() && b.isTask()) {
            combine2(a.task.sentence, a.task.isInput(), b.task.sentence, b.task.isInput(), nal);
        }
        else {
            combine2(a.belief, false /* don't know */, b.belief, false /* don't know */, nal);
        }
    }

    private void combine2(Sentence a, boolean aIsInput, Sentence b, boolean bIsInput, DerivationContext nal) {
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

        if (a.stamp.isEternal() || b.stamp.isEternal()) {
            return; // we can't combine eternal beliefs/tasks
        }

        // b must be input
        if (!bIsInput) {
            return;
        }

        if( Stamp.baseOverlap(a.stamp, b.stamp)) {
            return; // we can't combine the two sentences of the tasklets!
        }


        nal.setTheNewStamp(a.stamp, b.stamp, nal.time.time());
        nal.setCurrentBelief(b);

        // addToMemory is a misnomer - should be renamed
        List<Task> derivedTasks = TemporalRules.temporalInduction(b, a, nal, true, false, true);

        if (derivedTasks.size() > 0) {
            System.out.println("combine2()");
            System.out.println("    " + a + " occTime=" + a.stamp.getOccurrenceTime());
            System.out.println("    " + b + " occTime=" + b.stamp.getOccurrenceTime());
        }


        //System.out.println("=====");
        for(Task iDerivedTask : derivedTasks) {
            System.out.println("derived " + iDerivedTask.toString());
        }

        // create new tasklets from derived ones
        List<Tasklet> derivedTasklets = new ArrayList<>();
        for(Task iDerivedTask : derivedTasks) {
            derivedTasklets.add(new Tasklet(iDerivedTask, nal.time));
        }

        // TODO< rework to use a table with a utility function >
        // TODO< add tasklet to bag >
        for(Tasklet iDerivedTask : derivedTasklets) {
            secondary.add(0, iDerivedTask);
        }
    }

    public enum EnumAddedToMemory {
        YES,
        NO
    }
}
