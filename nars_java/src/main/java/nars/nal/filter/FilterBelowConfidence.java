package nars.nal.filter;

import nars.nal.DerivationFilter;
import nars.nal.NAL;
import nars.nal.Sentence;
import nars.nal.Task;

/**
* Created by me on 2/9/15.
*/
public class FilterBelowConfidence implements DerivationFilter {

    @Override public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        if (task.sentence.truth != null) {
            float conf = task.sentence.truth.getConfidence();
            if (conf < nal.memory.param.confidenceThreshold.get()) {
                //no confidence - we can delete the wrongs out that way.
                return "Insufficient confidence";
            }
        }
        return null;
    }
}
