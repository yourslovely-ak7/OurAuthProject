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
import pojo.Condition;
import pojo.Order;
import pojo.RefreshToken;
import pojo.Status;

public class RefreshTokenOperation {

	private static final String tableName= "RefreshToken";
	private static final String pk= "refreshTokenId";
	private static final Class<RefreshToken> pojo= RefreshToken.class;
	private static Mapper newMap= new Mapper();
	
	public static RefreshToken createRTEntry(RefreshToken token) throws InternalException
	{
		try
		{
			String refreshToken= Helper.generateCode();
			long millis= System.currentTimeMillis();
			
			token.setRefreshToken(refreshToken)
			.setCreatedTime(millis)
			.setStatus(Status.ACTIVE.name());
			
			int rtId= (int) newMap.create(token, true);
			
			token.setRefreshTokenId(rtId);
			return token;
		}
		catch (ConstraintViolationException error)
		{
			System.out.println(tableName+" entry already exists!");
			return null;
		}
	}
	
	public static RefreshToken getRT(String rToken) throws InternalException, InvalidException
	{
		try
		{
			Validator.validate(rToken, "token");
			
			RefreshToken token= new RefreshToken();
			Map<RefreshToken, List<String>> objects= new HashMap<>();
			objects.put(token, Helper.getAllFields(pojo));
			
			Map<Integer, Condition> conditions= new HashMap<>();
			Condition newCondition= Helper.prepareCondition(tableName, "refreshToken", " = ", rToken, "");
			conditions.put(1, newCondition);
			
			Order order= new Order();
			
			return Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);
		}
		catch(InvalidException error)
		{
			throw new InvalidException("invalid_token", error);
		}
	}
	
	public static String updateRT(RefreshToken token, int rtId) throws InternalException
	{
		try
		{
			String refreshToken= Helper.generateCode();
			long millis= System.currentTimeMillis();

			token.setRefreshToken(refreshToken)
			.setCreatedTime(millis);

			List<Object> objects= new ArrayList<>();
			objects.add(token);
			
			Map<Integer, Condition> conditions= new HashMap<>();
			Condition newCondition= Helper.prepareCondition(tableName, pk, " = ", rtId, "");
			conditions.put(1, newCondition);
			
			int result= newMap.update(objects, conditions);
			if (result != 1) {
				return null;
			} else {
				return refreshToken;
			}
		}
		catch (ConstraintViolationException error) 
		{
			System.out.println(tableName+" entry already exists!");
			return null;
		}
	}
	
	public static boolean deactivateRT(int rtId) throws InternalException
	{
		try
		{
			RefreshToken token= new RefreshToken();
			
			token.setStatus(Status.INACTIVE.name());

			List<Object> objects= new ArrayList<>();
			objects.add(token);
			
			Map<Integer, Condition> conditions= new HashMap<>();
			Condition newCondition= Helper.prepareCondition(tableName, pk, " = ", rtId, "");
			conditions.put(1, newCondition);
			
			int result= newMap.update(objects, conditions);
			return result==1;
		}
		catch (ConstraintViolationException error) 
		{
			throw new InternalException("No handling required for this exception...");
		}
	}
}
