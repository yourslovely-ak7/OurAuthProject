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
import pojo.AccessToken;
import pojo.Condition;
import pojo.Order;
import pojo.Status;

public class AccessTokenOperation {

	private static final String tableName= "AccessToken";
	private static final String pk= "accessTokenId";
	private static final Class<AccessToken> pojo= AccessToken.class;
	private static Mapper newMap= new Mapper();
	
	public static AccessToken createATEntry(AccessToken token) throws InternalException
	{
		try
		{
			String accessToken= Helper.generateCode();
			long millis= System.currentTimeMillis();
			
			token.setAccessToken(accessToken)
			.setCreatedTime(millis)
			.setStatus(Status.ACTIVE.name());
			
			int authId= (int) newMap.create(token, true);
			
			token.setAccessTokenId(authId);
			return token;
		}
		catch (ConstraintViolationException error) 
		{
			System.out.println(tableName+" entry already exists!");
			return null;
		}
	}
	
	public static AccessToken getAT(String aToken) throws InternalException, InvalidException
	{
		try
		{
			Validator.checkForNull(aToken);
			
			AccessToken token= new AccessToken();
			Map<AccessToken, List<String>> objects= new HashMap<>();
			objects.put(token, Helper.getAllFields(pojo));
			
			Map<Integer, Condition> conditions= new HashMap<>();
			Condition newCondition= Helper.prepareCondition(tableName, "accessToken", " = ", aToken, "");
			conditions.put(1, newCondition);
			
			Order order= new Order();
			
			return Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);			
		}
		catch(InternalException error)
		{
			throw new InvalidException("invalid_token", error);
		}
	}
	
	public static boolean deactivateAT(int atId) throws InternalException
	{
		try
		{
			AccessToken token= new AccessToken();
			
			token.setStatus(Status.INACTIVE.name());

			List<Object> objects= new ArrayList<>();
			objects.add(token);
			
			Map<Integer, Condition> conditions= new HashMap<>();
			Condition newCondition= Helper.prepareCondition(tableName, pk, " = ", atId, "");
			conditions.put(1, newCondition);
			
			int result= newMap.update(objects, conditions);
			if (result != 1) {
				return false;
			} else {
				System.out.println("Message: AccessToken deactivated!");
				return true ;
			}
		}
		catch (ConstraintViolationException error) 
		{
			throw new InternalException("No handling required for this exception...");
		}
	}
}
