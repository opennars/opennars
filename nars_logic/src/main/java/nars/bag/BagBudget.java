package nars.bag;

import nars.Global;
import nars.budget.Budget;
import nars.budget.UnitBudget;
import nars.nal.nal7.Tense;

import java.util.function.Supplier;

/**
 * Created by me on 12/14/15.
 */
public final class BagBudget<X> implements Budget, Supplier<X> {

    private final float[] b = new float[6];
    public final X id;
    private long lastForget;

    protected BagBudget(X id) {
        this.id = id;
    }

    public BagBudget(X id, Budget b) {
        this(id);
        init(b, 1f);
    }

    public BagBudget(X id, Budget b, float scale) {
        this(id);
        init(b, scale);
    }

    @Override
    public final X get() {
        return id;
    }

    protected void init(Budget c, float scale) {
        //this.lastForget = c.getLastForgetTime();
        this.lastForget = Tense.TIMELESS;


        float[] b = this.b;
        b[0] = c.getPriority() * scale;
        b[1] = c.getDurability();
        b[2] = c.getQuality();
        clearDelta();
    }

    private void clearDelta() {
        float[] b = this.b;
        b[3] = b[4] = b[5] = 0;
    }

    public void commit() {
        float[] b = this.b;
        b[0] += b[3];
        b[1] += b[4];
        b[2] += b[5];
        clearDelta();
    }

    @Override
    public float getPriority() {
        return b[0];
    }

    @Override
    public void setPriority(float p) {
        b[3] += (p-b[0]);
    }

    @Override
    public float getDurability() {
        return b[1];
    }

    @Override
    public void setDurability(float d) {
        float[] b = this.b;
        b[4] = (d-b[1]);
    }

    @Override
    public float getQuality() {
        return b[2];
    }

    @Override
    public void setQuality(float q) {
        float[] b = this.b;
        b[5] = (q-b[2]);
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
    public Budget clone() {
        return new UnitBudget(this);
    }

    public void set(Budget b) {
        setPriority(b.getPriority());
        setDurability(b.getDurability());
        setQuality(b.getQuality());
    }

    @Override
    public boolean equals(Object obj) {
        /*if (obj instanceof Budget)*/ {
            return equalsBudget((Budget) obj);
        }
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
        return (Math.abs(x) > Global.BUDGET_EPSILON);
    }
}
