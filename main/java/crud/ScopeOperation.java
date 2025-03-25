package crud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.ConstraintViolationException;
import exception.InvalidException;
import helper.Helper;
import helper.Validator;
import mapping.Mapper;
import pojo.Condition;
import pojo.Order;
import pojo.Scope;

public class ScopeOperation 
{
	private static final String tableName= "Scope";
	private static final String field= "authId";
	private static final Class<Scope> pojo= Scope.class;
	private static final List<String> orderBy= new ArrayList<String>();
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
	
	public static boolean checkForScope(int authId, String scope) throws InvalidException
	{
		Scope obj= new Scope();
		List<String> requiredFields= Helper.getAllFields(pojo);
		Map<Scope, List<String>> objects= new HashMap<Scope, List<String>>();
		objects.put(obj, requiredFields);
		
		Map<Integer,Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, field, " = ", authId, "");
		conditions.put(1, newCondition);
		
		newCondition= Helper.prepareCondition(tableName, "scope", " = ", scope, "AND");
		conditions.put(2, newCondition);
		
		Order order= Helper.prepareOrder(obj, orderBy, false, 1, 1);
		
		obj= Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);
		System.out.println("Scope checked for "+ obj.getScope());
		return true;
	}
	
	public static List<String> getScopes(int authId) throws InvalidException
	{
		Scope obj= new Scope();
		List<String> requiredFields= Helper.getAllFields(pojo);
		Map<Scope, List<String>> objects= new HashMap<Scope, List<String>>();
		objects.put(obj, requiredFields);
		
		Map<Integer,Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, field, " = ", authId, "");
		conditions.put(1, newCondition);
		
		Order order= new Order();
		
		List<Scope> scopes= Helper.getListOfPojo(newMap.read(objects, conditions, order), pojo);
		return convertScopes(scopes);
	}
	
	private static List<String> convertScopes(List<Scope> scopes)
	{
		List<String> listOfScopes= new ArrayList<String>();
		
		for(Scope iter: scopes)
		{
			listOfScopes.add(iter.getScope());
		}
		
		return listOfScopes;
	}
}
