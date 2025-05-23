package exception;

@SuppressWarnings("serial")
public class InternalException extends Exception
{
	public InternalException(String message)
	{
		super(message);
	}
	
	public InternalException(String message, Throwable cause)
	{
		super(message,cause);
	}
}