package nars.budget;

/**
 * indicates an implementation has, or is associated with a specific BudgetValue
 */
public interface Budgeted  {

    UnitBudget getBudget();

    default boolean isDeleted() {
        return Budget.isDeleted(getPriority());
    }

    default float getPriority() {
        return getBudget().getPriority();
    }

    default float getDurability() {
        return getBudget().getDurability();
    }

    default float getQuality() {
        return getBudget().getQuality();
    }

    default long getLastForgetTime() {
        return getBudget().getLastForgetTime();
    }


    default Object[] toBudgetArray() {
        return new Object[]{
                getPriority(), getDurability(), getQuality()
        };
    }


}
