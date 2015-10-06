//package nars.task.filter;
//
//import com.google.common.util.concurrent.AtomicDouble;
//import nars.premise.Premise;
//import nars.task.Task;
//import nars.truth.Truth;
//
///**
//* Created by me on 2/9/15.
//*/
//public class FilterBelowConfidence implements DerivationFilter {
//
//    final AtomicDouble confidenceThreshold = new AtomicDouble();
//
//    public FilterBelowConfidence(double thresh) {
//        confidenceThreshold.set(thresh);
//    }
//
//    @Override public final String reject(Premise nal, Task task, boolean solution, boolean revised) {
//        Truth t = task.getTruth();
//        if (t != null) {
//            float conf = t.getConfidence();
//            if (conf < confidenceThreshold.get()) {
//                //no confidence - we can delete the wrongs out that way.
//                return "Insufficient confidence";
//            }
//        }
//        return null;
//    }
//}
