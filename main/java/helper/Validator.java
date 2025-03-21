package helper;

import exception.InvalidException;
import pojo.Scopes;

public class Validator {
	
	private static Class<Scopes> enumClass= Scopes.class;
	
	public static void checkForNull(Object obj) throws InvalidException
	{
		if(obj== null)
		{
			throw new InvalidException("Value cannot be null!");
		}
	}
	
	public static void checkForNull(Object obj, String name) throws InvalidException
	{
		if(obj== null)
		{
			throw new InvalidException(name+" is invalid!");
		}
	}
	
	public static void checkForZero(long value, String name) throws InvalidException
	{
		if(value== 0)
		{
			throw new InvalidException(name+" cannot be ZERO!");
		}
	}
	
	public static boolean isValidScope(String scopes[]) throws InvalidException
	{
		try
		{
			for(String iter: scopes)
			{
				Enum.valueOf(enumClass, iter);
			}
			return true;
		}
		catch(IllegalArgumentException error)
		{
			System.out.println(error.getMessage());
			throw new InvalidException("Invalid Scope identified!");
		}
	}
}
