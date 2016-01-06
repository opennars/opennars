package nars.budget;

/**
 * indicates an implementation has, or is associated with a specific BudgetValue
 */
public abstract class BudgetedHandle implements Budgeted {


    @Override
    public boolean getDeleted() {
        return Budget.getDeleted(getPriority());
    }

    @Override
    public float getPriority() {
        return getBudget().getPriority();
    }

    @Override
    public float getDurability() {
        return getBudget().getDurability();
    }

    @Override
    public float getQuality() {
        return getBudget().getQuality();
    }

    @Override
    public long getLastForgetTime() {
        return getBudget().getLastForgetTime();
    }

    public abstract void setPriority(float p);

    abstract long setLastForgetTime(long currentTime);
}
