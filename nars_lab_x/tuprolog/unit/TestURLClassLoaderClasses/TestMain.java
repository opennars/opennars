package TestURLClassLoaderClasses;

public class TestMain{
	public static void main(String[] args) throws MyException
	{
		if(args.length == 0)
			throw new MyException("java TestMain <Message>");
		else
			System.out.println(TestStaticClass.echo(args[0]));
	}
}
