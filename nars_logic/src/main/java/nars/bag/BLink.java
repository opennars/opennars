package nars.bag;

import nars.budget.Budget;
import nars.data.BudgetedStruct;
import nars.budget.UnitBudget;
import nars.nal.nal7.Tense;

import java.util.function.Supplier;

import static nars.util.data.Util.clamp;

/**
 * Budgeted Link (an entry in a bag)
 * equalsTo/hashCode proxies to the wrapped element, X id
 *
 * Acts as a "budget vector" containing an accumulating delta
 * that can be commit()'d on the next udpate
 */
public final class BLink<X> extends Budget implements Supplier<X> {

    private final float[] b = new float[6];
    public final X id;
    private long lastForget = Tense.TIMELESS;

    public BLink(X id) {
        this.id = id;
    }

    public BLink(X id, float p, float d, float q) {
        this(id);
        init(p, d, q);
    }

    public BLink(X id, BudgetedStruct b) {
        this(id);
        init(b, 1f);
    }

    public BLink(X id, BudgetedStruct b, float scale) {
        this(id);
        init(b, scale);
    }

    @Override
    public X get() {
        return id;
    }

    private void init(BudgetedStruct c, float scale) {
        //this.lastForget = c.getLastForgetTime();
        this.lastForget = Tense.TIMELESS;


        init(c.getPriority() * scale, c.getDurability(), c.getQuality());
    }

    public void init(float p, float d, float q) {
        float[] b = this.b;
        b[0] = clamp(p);
        b[1] = clamp(d);
        b[2] = clamp(q);
    }

    private void clearDelta() {
        float[] b = this.b;
        b[3] = b[4] = b[5] = 0;
    }

    public void commit() {
        float[] b = this.b;
        b[0] = clamp(b[0] + b[3]);
        b[1] = clamp(b[1] + b[4]);
        b[2] = clamp(b[2] + b[5]);
        clearDelta();
    }

    @Override
    public float getPriority() {
        return b[0];
    }

    @Override
    public void setPriority(float p) {
        float[] b = this.b;
        b[3] += (clamp(p)-b[0]);
    }

    @Override
    public float getDurability() {
        return b[1];
    }

    @Override
    public void setDurability(float d) {
        float[] b = this.b;
        b[4] += (clamp(d)-b[1]);
    }

    @Override
    public float getQuality() {
        return b[2];
    }

    @Override
    public void setQuality(float q) {
        float[] b = this.b;
        b[5] += (clamp(q)-b[2]);
    }

    @Override
    public long setLastForgetTime(long currentTime) {
        long lastForget = this.lastForget;
        long diff;
        if (lastForget == Tense.TIMELESS) {
            diff = 0;
        } else {
            diff = currentTime - lastForget;
            if (diff == 0) return 0;
        }
        this.lastForget = currentTime;
        return diff;
    }

    @Override
    public long getLastForgetTime() {
        return lastForget;
    }


    @Override
    public UnitBudget clone() {
        return new UnitBudget(this);
    }


    @Override public boolean equals(Object obj) {
//        /*if (obj instanceof Budget)*/ {
//            return equalsBudget((Budget) obj);
//        }
//        return id.equals(((BagBudget)obj).id);
        return obj == this;
    }

    @Override public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id + "=" + getBudgetString();
    }

    public boolean hasDelta() {
        float[] b = this.b;
        return nonZero(b[3]) || nonZero(b[4]) || nonZero(b[5]);
    }

    static boolean nonZero(float x) {
        //return (Math.abs(x) > Global.BUDGET_EPSILON);
        return x!=0f;
    }
}
