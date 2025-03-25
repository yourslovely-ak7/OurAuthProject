package crud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.ConstraintViolationException;
import exception.InvalidException;
import helper.Helper;
import mapping.Mapper;
import pojo.Condition;
import pojo.Key;
import pojo.Order;

public class KeyOperation {
	
	private static Mapper newMap= new Mapper();
	private static final Class<Key> pojo= Key.class;
	
	public static void createKeyEntry(Key key) throws InvalidException
	{
		try
		{
			newMap.create(key, false);
		}
		catch(ConstraintViolationException error)
		{
			System.out.println(error.getMessage());
			throw new InvalidException("No handling for this case.", error);
		}
	}
	
	public static Key getKey(String keyId) throws InvalidException
	{
		Key key= new Key();
		List<String> requiredFields= Helper.getAllFields(pojo);
		Map<Key, List<String>> objects= new HashMap<>();
		objects.put(key, requiredFields);
		
		Map<Integer, Condition> conditions= new HashMap<>();
		Condition newCondition= Helper.prepareCondition("Key", "keyId", " = ", keyId, "");
		conditions.put(1, newCondition);
		
		Order order= new Order();
		
		return Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);
	}
}
