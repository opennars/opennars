package org.opennars.tasklet;

import org.opennars.control.DerivationContext;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.inference.TemporalRules;
import org.opennars.language.Term;
import org.opennars.main.Parameters;
import org.opennars.storage.Bag;

import java.util.ArrayList;
import java.util.List;

public class TaskletScheduler {
    public Bag<Tasklet, Term> primary;

    public void x(Parameters reasonerParameters) {
        int levels = 50;
        int bagSize = 1000;

        primary = new Bag<>(levels, bagSize, reasonerParameters);
    }

    // combines and infers two tasklets
    private void combine(Tasklet a, Tasklet b) {
        if (a.isTask() && b.isBelief()) {
            combine2(a.task.sentence, b.belief);
        }
        else if(a.isBelief() && b.isTask()) {
            combine2(b.belief, a.task.sentence);
        }
        else if(a.isTask() && b.isTask()) {
            combine2(a.task.sentence, b.task.sentence);
        }
        else {
            combine2(a.belief, b.belief);
        }
    }

    private void combine2(Sentence a, Sentence b) {
        // TODO< order a and b by time >

        System.out.println("combine2()");
        System.out.println("    " + a);
        System.out.println("    " + b);

        List<Task> derivedTasks = TemporalRules.temporalInduction(a, b, nal, SucceedingEventsInduction, true, true);

        // create new tasklets from derived ones
        List<Tasklet> derivedTasklets = new ArrayList<>();
        for(Task iDerivedTask : derivedTasks) {
            derivedTasklets.add(new Tasklet(iDerivedTask));
        }

        // TODO< rework to use a table with a utility function >
        // TODO< add tasklet to bag >
        for(Tasklet iDerivedTask : derivedTasklets) {
            primary.putIn(iDerivedTask);
        }
    }

    private DerivationContext nal;
}
