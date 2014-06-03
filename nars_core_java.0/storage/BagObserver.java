package nars.storage;

public interface BagObserver {

	public abstract void setTitle(String title);

	public abstract void setBag( Bag<?> concepts);
	
	/**
	 * Post the bag content
	 * @param str The text
	 */
	public abstract void post(String str);

	public abstract void refresh(String string);

	public abstract void stop();

}