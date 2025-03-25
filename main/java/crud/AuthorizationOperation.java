package crud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.ConstraintViolationException;
import exception.InvalidException;
import helper.Helper;
import helper.Validator;
import mapping.Mapper;
import pojo.Authorization;
import pojo.Condition;
import pojo.Order;

public class AuthorizationOperation 
{
	private static final String tableName= "Authorization";
	private static final String pk= "authId";
	private static final Class<Authorization> pojo= Authorization.class;
	private static Mapper newMap= new Mapper();
	
	public static Authorization createAuthEntry(Authorization auth) throws InvalidException
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
	
	public static Authorization getAuthorization(String authCode) throws InvalidException
	{
		Validator.checkForNull(authCode, "authCode");
		
		Authorization auth= new Authorization();
		List<String> requiredFields= Helper.getAllFields(pojo);
		Map<Authorization,List<String>> objects= new HashMap<Authorization, List<String>>();
		objects.put(auth, requiredFields);
		
		Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, "authCode", " = ", authCode, "");
		conditions.put(1, newCondition);
		
		Order order= new Order();
		
		return Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);
	}

}
