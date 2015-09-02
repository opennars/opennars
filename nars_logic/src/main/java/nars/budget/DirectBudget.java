//package nars.budget;
//
///** budget subclass which allows out-of-range values for its components.
// * ex: NaN can be used to indicate a missing value
// */
//public class DirectBudget extends Budget {
//
///**
// * whether the 3 component values stored must be valid (finite, 0 <= x <= 1).
// * may be overridden in subclasses
// */
//public boolean requireValidStoredValues() {
//        return true;
//        }

//    /** initializes with all values NaN */
//    public DirectBudget() {
//        super(Float.NaN, Float.NaN, Float.NaN);
//    }
//
//    @Override public void setDurability(float d) {
//        this.durability = d;
//    }
//    @Override public void setPriority(float p) {
//        this.priority = p;
//    }
//    @Override public void setQuality(float q) {
//        this.quality = q;
//    }
//
//    @Override
//    public boolean requireValidStoredValues() {
//        return false;
//    }
//
//    /** true if all the components are finite and within 0..1.0 range */
//    public boolean isBudgetValid() {
//        return validRange(priority) && validRange(durability) && validRange(quality);
//    }
//
//    public static final boolean validRange(final float x) {
//        return Float.isFinite(x) && x >= 0 && x <= 1f;
//    }
//}
