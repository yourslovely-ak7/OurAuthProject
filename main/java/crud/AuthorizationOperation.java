package crud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.ConstraintViolationException;
import exception.InternalException;
import exception.InvalidException;
import helper.Helper;
import helper.Validator;
import mapping.Mapper;
import pojo.Authorization;
import pojo.Condition;
import pojo.Order;
import pojo.Status;

public class AuthorizationOperation 
{
	private static final String tableName= "Authorization";
	private static final String pk= "authId";
	private static final Class<Authorization> pojo= Authorization.class;
	private static Mapper newMap= new Mapper();
	
	public static Authorization createAuthEntry(Authorization auth) throws InternalException
	{
		try
		{
			String authCode= Helper.generateCode();
			long millis= System.currentTimeMillis();
			
			auth.setAuthCode(authCode)
			.setCreatedTime(millis);
			
			int authId= (int) newMap.create(auth, true);
			
			auth.setAuthId(authId);
			return auth;
		}
		catch (ConstraintViolationException error) 
		{
			return null;
		}
	}
	
	public static Authorization getAuthorization(String authCode, int clientRowId) throws InternalException, InvalidException
	{
		try
		{
			Validator.validate(authCode, "code");
			
			Authorization auth= new Authorization();
			List<String> requiredFields= Helper.getAllFields(pojo);
			Map<Authorization,List<String>> objects= new HashMap<Authorization, List<String>>();
			objects.put(auth, requiredFields);
			
			Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
			Condition newCondition= Helper.prepareCondition(tableName, "authCode", " = ", authCode, "");
			conditions.put(1, newCondition);
			
			newCondition= Helper.prepareCondition(tableName, "status", " = ", Status.ACTIVE.name(), "AND");
			conditions.put(2, newCondition);
			
			newCondition= Helper.prepareCondition(tableName, "clientRowId", " = ", clientRowId, "AND");
			conditions.put(3, newCondition);
			
			Order order= new Order();
			
			return Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);			
		}
		catch(InvalidException error)
		{
			System.out.println(error.getMessage());
			throw new InvalidException("invalid_code");
		}
	}

	public static boolean deactivateToken(int authId) throws InternalException
	{
		try
		{
			Authorization auth= new Authorization();
			auth.setStatus(Status.INACTIVE.name());
			
			List<Object> objects= new ArrayList<>();
			objects.add(auth);
			
			Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
			Condition newCondition= Helper.prepareCondition(tableName, pk, " = ", authId, "");
			conditions.put(1, newCondition);
			
			return newMap.update(objects, conditions) == 1;
		}
		catch(ConstraintViolationException error)
		{
			System.out.println("No handling required for this method...");
			return false;
		}
	}
}
