package crud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import exception.ConstraintViolationException;
import exception.InvalidException;
import helper.Validator;
import mapping.Mapper;
import pojo.Scope;

public class ScopeOperation 
{
	private static final String tableName= "Scope";
	private static final String field= "authId";
	private static final Class<Scope> pojo= Scope.class;
	private static Mapper newMap= new Mapper();
	
	public static boolean addScopes(int authId, String scopes[]) throws InvalidException
	{
		Validator.checkForZero(scopes.length, "Scope count");
		try
		{
			List<String> scopeList= Arrays.asList(scopes);
			List<Scope> records= new ArrayList<Scope>();
			
			for(String iter: scopeList)
			{
				records.add(new Scope().setAuthId(authId).setScope(iter));
			}
			
			int rowsAffected= (int) newMap.createBatch(records.get(0), records);
			
			if(rowsAffected!=0)
			{
				return true;
			}
			else
			{
				throw new InvalidException("Error inserting mentioned scopes!");
			}
		}
		catch(ConstraintViolationException error)
		{
			System.out.println(error.getMessage());
			throw new InvalidException("No handling written for this case.", error);
		}
	}
	
	public static boolean checkForScope(int authId, String scope)
	{
		
	}
}
