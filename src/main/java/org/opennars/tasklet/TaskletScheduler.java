package org.opennars.tasklet;

import org.opennars.language.Term;
import org.opennars.main.Parameters;
import org.opennars.storage.Bag;

public class TaskletScheduler {
    public Bag<Tasklet, Term> primary;

    public void x(Parameters reasonerParameters) {
        int levels = 50;
        int bagSize = 1000;

        primary = new Bag<>(levels, bagSize, reasonerParameters);
    }
}
