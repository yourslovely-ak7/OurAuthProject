package crud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.ConstraintViolationException;
import exception.InternalException;
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
	private static final List<String> orderBy= new ArrayList<>();
	private static Mapper newMap= new Mapper();
	
	public static boolean addScopes(int authId, String scopes[], int at_id) throws InternalException
	{
		Validator.checkForZero(scopes.length, "Scope count");
		try
		{
			List<String> scopeList= Arrays.asList(scopes);
			List<Scope> records= new ArrayList<Scope>();
			
			for(String iter: scopeList)
			{
				records.add(new Scope().setAuthId(authId).setScope(iter).setAccessTokenId(at_id));
			}
			
			int rowsAffected= (int) newMap.createBatch(records.get(0), records);
			
			return rowsAffected!=0;
		}
		catch(ConstraintViolationException error)
		{
			throw new InternalException("No handling required for this exception...");
		}
	}
	
	public static boolean checkForScope(int authId, String scope) throws InternalException, InvalidException
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
	
	public static List<String> getScopes(int authId, int atId) throws InternalException, InvalidException
	{
		Scope obj= new Scope();
		List<String> requiredFields= Helper.getAllFields(pojo);
		Map<Scope, List<String>> objects= new HashMap<Scope, List<String>>();
		objects.put(obj, requiredFields);
		
		Map<Integer,Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, field, " = ", authId, "");
		conditions.put(1, newCondition);
		
		if(atId != 0)
		{
			newCondition= Helper.prepareCondition(tableName, "accessTokenId", " = ", atId, "OR");
			conditions.put(2, newCondition);			
		}
		
		Order order= new Order();
		
		List<Scope> scopes= Helper.getListOfPojo(newMap.read(objects, conditions, order), pojo);
		return extractScopes(scopes);
	}
	
	private static List<String> extractScopes(List<Scope> scopes)
	{
		List<String> allScopes= new ArrayList<>();		
		List<String> limitedScopes= new ArrayList<>();
		boolean limitedScopeFlag= false;
		
		for(Scope iter: scopes)
		{
			if(iter.getAuthId() !=0 && iter.getAccessTokenId() !=0)
			{
				limitedScopeFlag= true;
				limitedScopes.add(iter.getScope());
			}
			else
			{
				if(!limitedScopeFlag)
				{
					allScopes.add(iter.getScope());
				}
			}
		}

		return limitedScopeFlag ? limitedScopes : allScopes;
	}
}
