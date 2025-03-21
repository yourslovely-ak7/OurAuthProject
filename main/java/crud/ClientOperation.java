package crud;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.ConstraintViolationException;
import exception.InvalidException;
import helper.Helper;
import helper.Validator;
import mapping.Mapper;
import pojo.Client;
import pojo.Condition;
import pojo.Order;

public class ClientOperation {
	
	private static final String tableName= "Client";
	private static final String pk= "clientRowId";
	private static final Class<Client> pojo= Client.class;
	private static Mapper newMap= new Mapper();
	
	public static int createClient(Client newClient) throws InvalidException
	{
			try
			{
				String clientId= Helper.getRandomString();
				newClient.setClientId(clientId);
				
				return (int) newMap.create(newClient, true);
			}
			catch (ConstraintViolationException error) 
			{
				String message= error.getMessage();
				if(message.contains("redirect_url"))
				{
					throw new InvalidException("Client with the given url already exists!");
				}
				else
				{
					return 0;					
				}
			}
	}
	
	public static Client getClient(int clientRowId) throws InvalidException
	{
		Client newClient= new Client();
		List<String> requiredFields= Helper.getAllFields(pojo);
		Map<Client, List<String>> objects= new HashMap<Client, List<String>>();
		objects.put(newClient, requiredFields);
		
		Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, pk, " = ", clientRowId, "");
		conditions.put(1, newCondition);
		
		Order order= new Order();
		
		return Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);
	}
	
	public static List<Client> getAllClients(int userId) throws InvalidException
	{
		Client newClient= new Client();
		List<String> requiredFields= Helper.getAllFields(pojo);
		Map<Client, List<String>> objects= new HashMap<Client, List<String>>();
		objects.put(newClient, requiredFields);
		
		Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, "createdBy", " = ", userId, "");
		conditions.put(1, newCondition);
		
		Order order= new Order();
		
		return Helper.getListOfPojo(newMap.read(objects, conditions, order), pojo);
	}
	
	public static String getClientSecret(String url) throws InvalidException
	{
		try
		{
			return Helper.getMD5Hash(url);
		}
		catch(NoSuchAlgorithmException error)
		{
			throw new InvalidException("Error occurred while generating client secret.", error);
		}
	}
	
	public static Client validateClient(String clientId, String redirectUrl) throws InvalidException
	{
		Validator.checkForNull(clientId, "clientId");
		Validator.checkForNull(redirectUrl, "redirectUrl");
		
		Client newClient= new Client();
		List<String> requiredFields= Helper.getAllFields(pojo);
		Map<Client, List<String>> objects= new HashMap<Client, List<String>>();
		objects.put(newClient, requiredFields);
		
		Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, "clientId", " = ", clientId, "");
		conditions.put(1, newCondition);
		
		newCondition= Helper.prepareCondition(tableName, "redirectUrl", " = ", redirectUrl, "AND");
		conditions.put(2, newCondition);
		
		Order order= new Order();
		
		return Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);
	}
}
