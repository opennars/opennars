package nars.budget;

/**
 * indicates an implementation has, or is associated with a specific BudgetValue
 */
public interface Budgeted  {

    public Budget getBudget();

    default public float getPriority() {
        return getBudget().getPriority();
    }

    default public float getDurability() {
        return getBudget().getDurability();
    }

    default public float getQuality() {
        return getBudget().getQuality();
    }

    default public long getLastForgetTime() {
        return getBudget().getLastForgetTime();
    }



    default Object[] toBudgetArray() {
        return new Object[]{
                getPriority(), getDurability(), getQuality()
        };
    }


}
