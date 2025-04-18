package crud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.ConstraintViolationException;
import exception.InternalException;
import exception.InvalidException;
import helper.Helper;
import mapping.Mapper;
import pojo.Condition;
import pojo.Order;
import pojo.User;

public class UserOperation {
	
	private static final String tableName= "User";
	private static final String pk= "userId";
	private static final Class<User> pojo= User.class;
	private static Mapper newMap= new Mapper();
	
	public static int addUser(User user) throws InternalException
	{
		try
		{
			return (int) newMap.create(user, true);
		}
		catch(ConstraintViolationException error)
		{
			throw new InternalException("Error due to violation of Unique Constraint", error);
		}
	}
	
	public static User getUser(String email) throws InternalException, InvalidException
	{
		Map<User,List<String>> objects= new HashMap<User, List<String>>();
		User user= new User();
		List<String> requiredFields= Helper.getAllFields(pojo);
		objects.put(user, requiredFields);
		
		Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, "email", " = ", email, "");
		conditions.put(1, newCondition);
		
		Order order= new Order();
		
		return Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);
	}
	
	public static User getUser(int userId) throws InternalException, InvalidException
	{
		Map<User,List<String>> objects= new HashMap<User, List<String>>();
		User user= new User();
		List<String> requiredFields= Helper.getAllFields(pojo);
		objects.put(user, requiredFields);
		
		Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, pk, " = ", userId, "");
		conditions.put(1, newCondition);
		
		Order order= new Order();
		
		return Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);
	}
}
