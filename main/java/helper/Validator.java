package helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

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
		for(String iter: scopes)
		{
			if(!scopeSet.contains(iter))
			{
				throw new InvalidException("Invalid Scope identified: "+iter);				
			}
		}
		return true;
	}

	public static JSONObject getApiScopes()
	{
		return apiScopes;
	}
}
