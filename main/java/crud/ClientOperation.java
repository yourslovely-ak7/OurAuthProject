package crud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.ConstraintViolationException;
import exception.InternalException;
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
	
	public static int createClient(Client newClient) throws InternalException, InvalidException
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
				if(message.contains("client_id"))
				{
					return 0;					
				}
				else
				{
					throw new InvalidException("Client with the url expected to be already registered.", error);
				}
			}
	}
	
	public static Client getClient(int clientRowId) throws InternalException, InvalidException
	{		
		Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, pk, " = ", clientRowId, "");
		conditions.put(1, newCondition);
		
		return getClient(conditions);
	}
	
	public static List<Client> getAllClients(int userId) throws InternalException, InvalidException
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
	
	public static String getClientSecret(String url) throws InternalException
	{
		return Helper.getMD5Hash(url);
	}
	
	
	public static Client getClientById(String clientId) throws InternalException, InvalidException
	{
		Validator.validate(clientId, "client");
		
		Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, "clientId", " = ", clientId, "");
		conditions.put(1, newCondition);
		
		return getClient(conditions);
	}
	
	private static Client getClient(Map<Integer,Condition> conditions) throws InternalException, InvalidException
	{
		try
		{
			Client newClient= new Client();
			List<String> requiredFields= Helper.getAllFields(pojo);
			Map<Client, List<String>> objects= new HashMap<Client, List<String>>();
			objects.put(newClient, requiredFields);
			
			Order order= new Order();
			
			return Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);			
		}
		catch(InvalidException error)
		{
			System.out.println("Error: "+error.getMessage());
			throw new InvalidException("invalid_client", error);
		}
	}
	
	public static int deleteClient(int clientRowId) throws InternalException
	{
		Client newClient= new Client();
		
		Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
		Condition newCondition= Helper.prepareCondition(tableName, pk, " = ", clientRowId, "");
		conditions.put(1, newCondition);
		
		return newMap.delete(newClient, conditions);
	}
}

//	public static Client validateClientByIdAndUrl(String clientId, String redirectUrl) throws InternalException
//	{
//		try
//		{
//			Validator.checkForNull(clientId, "client");
//			Validator.checkForNull(redirectUrl, "redirectUri");
//			
//			Map<Integer, Condition> conditions= new HashMap<Integer, Condition>();
//			Condition newCondition= Helper.prepareCondition(tableName, "clientId", " = ", clientId, "");
//			conditions.put(1, newCondition);
//			
//			
//			
//			return getClient(conditions);			
//		}
//		catch(InternalException error)
//		{
//			System.out.println("Error: "+error.getMessage());
//			throw new InternalException("invalid_client");
//		}
//	}