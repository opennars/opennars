package nars.logic.reason.filter;

import nars.logic.NAL;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;

/**
* Created by me on 2/9/15.
*/
public class FilterBelowConfidence implements NAL.DerivationFilter {

    @Override public String reject(NAL nal, Task task, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
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
