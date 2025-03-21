package exception;

@SuppressWarnings("serial")
public class ConstraintViolationException extends Exception
{
	public ConstraintViolationException(String message)
	{
		super(message);
	}
	
	public ConstraintViolationException(String message, Throwable cause)
	{
		super(message,cause);
	}
}