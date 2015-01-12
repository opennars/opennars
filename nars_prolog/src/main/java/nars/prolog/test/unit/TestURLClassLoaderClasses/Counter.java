package TestURLClassLoaderClasses;

public class Counter {
	public String name;
	private long value = 0;
	public Counter() {}
	public Counter(String aName) { name = aName; }
	public void setValue(long val) { value=val; }
	public long getValue() { return value; }
	public void inc() { value++; }
	static public String getVersion() { return "1.0"; }
}