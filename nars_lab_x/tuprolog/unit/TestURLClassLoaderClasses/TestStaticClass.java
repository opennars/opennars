package TestURLClassLoaderClasses;

public class TestStaticClass {
	
	public static int id;
	
	public static String echo(String message) {
		return message;
	}
	
	public int getId(){return id;}
	
	public void testMyException() throws MyException
	{
		throw new MyException("Test MyException done!");
	}
}
