package nars.nal;

import nars.nal.task.TaskSeed;

/**
 * Created by me on 5/1/15.
 */
public interface DerivationFilter  {


    /**
     * returns null if allowed to derive, or a String containing a short rejection rule for logging
     */
    String reject(NAL nal, TaskSeed task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask);

}
