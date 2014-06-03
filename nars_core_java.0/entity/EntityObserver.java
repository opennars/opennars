package nars.entity;

import nars.storage.BagObserver;

public interface EntityObserver {

	/**
	 * Display the content of the concept
	 * @param str The text to be displayed
	 */
	public abstract void post(String str);

	public abstract BagObserver createBagObserver();

	public abstract void startPlay(Concept concept, boolean showLinks);

	public abstract void stop();

	void refresh(String message);

}