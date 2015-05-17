package nars.nal;

import nars.NAR;
import nars.util.event.Reaction;

/**
 * Created by me on 5/1/15.
 */
public interface DerivationFilter  {


    /**
     * returns null if allowed to derive, or a String containing a short rejection rule for logging
     */
    public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask);

}
