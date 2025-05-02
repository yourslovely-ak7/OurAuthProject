package helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import exception.InternalException;
import exception.InvalidException;
import pojo.Scopes;

public class Validator {
	
	private static final Set<String> scopeSet= Arrays.stream(Scopes.values()).map(Scopes::getName).collect(Collectors.toSet());
	private static final String apiScopesPath= "/scopes.json";
	private static JSONObject apiScopes= null;
	
	static 
	{
		try(InputStream scopesStream= Validator.class.getClassLoader().getResourceAsStream(apiScopesPath);
				Scanner sc= new Scanner(scopesStream, "UTF-8"))
		{
			String data= sc.useDelimiter("\\A").next();
			apiScopes= new JSONObject(data);
		}
		catch (IOException | JSONException e) {
			e.printStackTrace();
		}		
	}
	
	public static void checkForNull(Object obj) throws InternalException
	{
		if(obj== null)
		{
			throw new InternalException("Value cannot be null!");
		}
	}
	
	public static void validate(Object obj) throws InvalidException
	{
		if(obj== null)
		{
			throw new InvalidException("Value cannot be null!");
		}
	}
	
	public static void checkForNull(Object obj, String message) throws InternalException
	{
		if(obj== null)
		{
			throw new InternalException("Invalid "+ message);
		}
	}
	
	public static void validate(Object obj, String message) throws InvalidException
	{
		if(obj== null)
		{
			throw new InvalidException("invalid_"+ message);
		}
	}
	
	public static void checkForZero(long value, String name) throws InternalException
	{
		if(value== 0)
		{
			throw new InternalException(name+" cannot be ZERO!");
		}
	}
	
	public static boolean isValidScope(String scopes[]) throws InvalidException
	{
		for(String iter: scopes)
		{
			if(!scopeSet.contains(iter))
			{
				System.out.println("Invalid Scope identified: "+iter);
				throw new InvalidException("invalid_scope");
			}
		}
		return true;
	}
	
	public static JSONObject getApiScopes()
	{
		return apiScopes;
	}
	
	public static boolean isExpired(long time, String type, long validityInSec) throws InvalidException
	{
		long inSec= (time/1000) + validityInSec, currentTimeInSec= System.currentTimeMillis()/1000;
		
		System.out.println("Expiration Time: "+inSec+" ; Current Time: "+ currentTimeInSec);
		if(inSec < currentTimeInSec)
		{
			return true;
		}
		return false;
	}
	
	public static boolean isEqual(List<String> list1, List<String> list2)
	{
		Collections.sort(list1);
		Collections.sort(list2);
		
		return list1.equals(list2);
	}
}
