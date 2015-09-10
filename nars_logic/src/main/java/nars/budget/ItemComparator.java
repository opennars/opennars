//package nars.budget;
//
//import com.gs.collections.api.block.procedure.Procedure2;
//
//import java.util.Comparator;
//
///**
// * Compares tasks, and accumulates their budget priority additively
// */
//public class ItemComparator<I extends Item> implements Comparator<I> {
//
//    public final Procedure2<Budget,Budget> merge;
//
//    protected ItemComparator(Procedure2<Budget, Budget> merge) {
//        this.merge = merge;
//    }
//
//    @Override
//    public final int compare(final I o1, final I o2) {
//
//        if (o1 == o2) return 0;
//
//        if (o1.equals(o2)) {
//
//            merge.value(o1, o2);
//
//            return 0;
//        }
//
//        //o2, o1 = highest first
//        final int priorityComparison = Float.compare(o2.getPriority(), o1.getPriority());
//        if (priorityComparison != 0)
//            return priorityComparison;
//
//        /*final int complexityComparison = Integer.compare(o1.getTerm().getComplexity(), o2.getTerm().getComplexity());
//        if (complexityComparison != 0)
//            return complexityComparison;
//        else*/
//        int hashCompare = Integer.compare(o1.hashCode(), o2.hashCode());
//        if (hashCompare == 0) {
//            //try to avoid this, identityHashcode is slow
//            return Integer.compare(
//                    System.identityHashCode(o1),
//                    System.identityHashCode(o2));
//        }
//        return hashCompare;
//    }
//
//
////    /**
////     * plus priority
////     */
////    public static final class Plus<I extends Item> extends ItemComparator<I> {
////
////        @Override
////        protected int compareBudget(I o1, I o2) {
////            o1.accumulate(o2);
////            //TODO return some other signal if the target budget is unaffected (ex: already maxed out 1.0)
////            return 0;
////        }
////    }
////
////    public static final class Or<I extends Item> extends ItemComparator<I> {
////
////        @Override
////        protected int compareBudget(I o1, I o2) {
////            o1.orPriority(o2.getPriority());
////            o1.maxDurability(o2.getPriority());
////            o1.maxQuality(o2.getPriority());
////            return 0;
////        }
////    }
////
////    /**
////     * allow duplicates
////     */
////    public static final class Duplicate<I extends Item> extends ItemComparator<I> {
////
////        @Override
////        protected int compareBudget(I o1, I o2) {
////            return Integer.compare(
////                    System.identityHashCode(o1), System.identityHashCode(o2));
////        }
////    }
////
//////        /** merges the tasks using OR budget formula */
//////        Or,
//////
//////        /** allows additional copies of equivalent tasks */
//////        Duplicate
//////    //}
//
//
//
//
////    /**
////     * comparison function when items have equal content.
////     *
////     * @return 0 if equal and merged
////     * -1 allow a duplicate
////     */
////    abstract protected int compareBudget(I o1, I o2);
//
////    if (o1.equals(o2)) {
////        switch (merge) {
////            case Duplicate:
////                return -1;
////            case Plus:
////                o1.accumulate(o2);
////                break;
////            case Or:
////                o1.orPriority(o2.getPriority());
////                o1.maxDurability(o2.getPriority());
////                o1.maxQuality(o2.getPriority());
////                break;
////        }
////        o2.merge(o1);
////        return 0;
////    }
//
//}
