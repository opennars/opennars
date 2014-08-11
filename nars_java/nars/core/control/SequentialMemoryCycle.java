package nars.core.control;

import nars.storage.Memory;

/**
 * A deterministic memory cycle implementation that is used for development and testing.
 */
public class SequentialMemoryCycle implements Memory.MemoryCycle {

    @Override
    public void cycle(Memory m) {
        m.processNewTask();

        if (m.noResult()) {       // necessary?
            m.processNovelTask();
        }

        if (m.noResult()) {       // necessary?
            m.processConcept();
        }

    }
    
}
