package crud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import exception.ConstraintViolationException;
import exception.InternalException;
import exception.InvalidException;
import helper.Helper;
import mapping.Mapper;
import pojo.Condition;
import pojo.Order;
import pojo.Uri;

public class UriOperation {
	
	private static final String tableName= "Uri";
	private static final Class<Uri> pojo= Uri.class;
	private static Mapper newMap= new Mapper();
	
	public static boolean addUris(int clientRowId, List<String> uriList) throws InternalException, InvalidException
	{
		try
		{
//			List<String> uriList= Arrays.asList(redirectUri);
			List<Uri> records= new ArrayList<>();
			
			for(String iter: uriList)
			{
				records.add(new Uri().setClientRowId(clientRowId).setRedirectUri(iter));
			}
			
			int rowsAffected= (int) newMap.createBatch(records.get(0), records);
			
			return rowsAffected!=0;
		}
		catch(ConstraintViolationException error)
		{
			ClientOperation.deleteClient(clientRowId);

			System.out.println(error.getMessage());
			throw new InvalidException("Client with the given url already exists!", error);
		}
	}
	
	
	public static List<String> getUris(int clientRowId) throws InternalException, InvalidException
	{
			Uri obj= new Uri();
			List<String> requiredFields= Helper.getAllFields(pojo);
			Map<Uri, List<String>> objects= new HashMap<>();
			objects.put(obj, requiredFields);
			
			Map<Integer,Condition> conditions= new HashMap<>();
			Condition newCondition= Helper.prepareCondition(tableName, "clientRowId", " = ", clientRowId, "");
			conditions.put(1, newCondition);
			
			Order order= new Order();
			
			List<Uri> uriList= Helper.getListOfPojo(newMap.read(objects, conditions, order), pojo);
			return convertUris(uriList);
	}
	
	private static List<String> convertUris(List<Uri> uriList)
	{
		List<String> listOfUris= uriList.stream()
								.map(Uri::getRedirectUri)
								.collect(Collectors.toList());
		return listOfUris;
	}

	public static boolean isValidUri(String uriFromParam, int clientRowId) throws InvalidException, InternalException
	{
			List<String> uriList= getUris(clientRowId);
			
			for(String iter: uriList)
			{
				if(uriFromParam.contains(iter))
				{
					return true;
				}
			}
			throw new InvalidException("invalid_redirect_uri");			
	}
}

//	public static boolean checkForUri(int clientRowId, String redirectUri) throws InternalException
//	{
//		try
//		{
//			Uri obj= new Uri();
//			List<String> requiredFields= Helper.getAllFields(pojo);
//			Map<Uri, List<String>> objects= new HashMap<>();
//			objects.put(obj, requiredFields);
//			
//			Map<Integer,Condition> conditions= new HashMap<>();
//			Condition newCondition= Helper.prepareCondition(tableName, "clientRowId", " = ", clientRowId, "");
//			conditions.put(1, newCondition);
//			
//			newCondition= Helper.prepareCondition(tableName, "redirectUri", " LIKE ", redirectUri, "AND");
//			conditions.put(2, newCondition);
//			
//			Order order= Helper.prepareOrder(obj, orderBy, false, 1, 1);
//			
//			obj= Helper.getSingleElePojo(newMap.read(objects, conditions, order), pojo);
//			System.out.println("Scope checked for "+ obj.getRedirectUri());
//			return true;			
//		}
//		catch(InternalException error)
//		{
//			System.out.println("Error: "+ error.getMessage());
//			throw new InternalException("redirectUri not found");
//		}
//	}