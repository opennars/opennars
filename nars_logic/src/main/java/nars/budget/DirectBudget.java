package nars.budget;

/** budget subclass which allows out-of-range values for its components.
 * ex: NaN can be used to indicate a missing value
 */
public class DirectBudget extends Budget {

    /** initializes with all values NaN */
    public DirectBudget() {
        super(Float.NaN, Float.NaN, Float.NaN);
    }

    @Override public boolean setDurability(float d) {
        this.durability = d;
        return true;
    }
    @Override public boolean setPriority(float p) {
        this.priority = p;
        return true;
    }
    @Override public boolean setQuality(float q) {
        this.quality = q;
        return true;
    }

    @Override
    public boolean requireValidStoredValues() {
        return false;
    }

    /** true if all the components are finite and within 0..1.0 range */
    public boolean isBudgetValid() {
        return validRange(priority) && validRange(durability) && validRange(quality);
    }

    public static final boolean validRange(final float x) {
        return Float.isFinite(x) && x >= 0 && x <= 1f;
    }
}
