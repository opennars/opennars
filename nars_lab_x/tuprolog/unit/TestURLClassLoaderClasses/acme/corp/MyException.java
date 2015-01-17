package acme.corp;

@SuppressWarnings("serial")
public class MyException extends Exception {
	
	String error = null;
	public MyException()
	{
		super();            
		error = "unknown";
	}

	public MyException(String err)
	{
		super(err);     
		error = err; 
	}

	public String getError()
	{
		return error;
	}
}
