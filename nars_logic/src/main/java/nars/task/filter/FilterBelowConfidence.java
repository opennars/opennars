package nars.task.filter;

import nars.premise.Premise;
import nars.task.TaskSeed;
import nars.truth.Truth;

/**
* Created by me on 2/9/15.
*/
public class FilterBelowConfidence implements DerivationFilter {

    @Override public final String reject(Premise nal, TaskSeed task, boolean solution, boolean revised) {
        Truth t = task.getTruth();
        if (t != null) {
            float conf = t.getConfidence();
            if (conf < nal.param().confidenceThreshold.get()) {
                //no confidence - we can delete the wrongs out that way.
                return "Insufficient confidence";
            }
        }
        return null;
    }
}
