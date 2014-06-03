package nars.entity;

import nars.storage.BagObserver;

/**
 * Observer for a {@link Concept} object; similar to Observer design pattern, except that here we have a single observer;
 * NOTE: very similar to interface {@link nars.storage.BagObserver}
 */
public interface EntityObserver {

	/**
	 * Display the content of the concept
	 * @param str The text to be displayed
	 */
	public abstract void post(String str);

	/** create a {@link BagObserver} of the right type (Factory design pattern) */
	@SuppressWarnings("rawtypes")
	public abstract BagObserver createBagObserver();

    /**
     * Set the observed Concept
     * @param showLinks unused : TODO : is this forgotten ?
     */
	public abstract void startPlay(Concept concept, boolean showLinks);

    /**
     * put in non-showing state
     */
	public abstract void stop();

    /**
     * Refresh display if in showing state
     */
	void refresh(String message);

}