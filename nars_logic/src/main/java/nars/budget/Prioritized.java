package nars.budget;

/**
 * Essentially the methods involved with the priority component of a Budget
 */
public interface Prioritized {

	float getPriority();

	void setPriority(float p);

	// void addPriority(float v);

	// void merge(Prioritized that);

	long setLastForgetTime(long currentTime);

	long getLastForgetTime();

	void mulPriority(float factor);

}
