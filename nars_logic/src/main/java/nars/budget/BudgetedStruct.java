package nars.budget;

/**
 * Created by jim on 1/6/2016.
 */
public interface BudgetedStruct {
	boolean isDeleted();

	float getPriority();

	float getDurability();

	float getQuality();

	long getLastForgetTime();
}
