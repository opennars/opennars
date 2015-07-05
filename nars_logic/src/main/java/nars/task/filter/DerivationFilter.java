package nars.task.filter;

import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;


@FunctionalInterface
public interface DerivationFilter  {

    /** use this when returning null to elucidate that the filter allows it */
    public static final String VALID = null;

    /**
     * returns null if allowed to derive, or a String containing a short rejection rule for logging
     */
    String reject(NAL nal, TaskSeed task, boolean solution, boolean revised);

}
