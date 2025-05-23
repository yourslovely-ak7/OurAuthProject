package helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import exception.InternalException;
import exception.InvalidException;
import pojo.Condition;
import pojo.Order;
import pojo.Scopes;

public class Helper {
	
	public static JSONObject getJsonRequest(HttpServletRequest request) throws IOException, JSONException
	{
		StringBuilder jsonData = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonData.append(line);
            }
        }
        
        return new JSONObject(jsonData.toString());
	}
	
	public static Condition prepareCondition(String tableName, String fieldName, String operator, Object value, String conjOpt)
	{
		Condition condition= new Condition();
		condition.setTableName(tableName);
		condition.setFieldName(fieldName);
		condition.setOperator(operator);
		if(operator.equals(" LIKE "))
		{
			condition.setValue("%"+value+"%");
		}
		else
		{
			condition.setValue(value);			
		}
		condition.setConjuctiveOpt(conjOpt);
		
		return condition;
	}
	
	public static Order prepareOrder(Object pojo, List<String> orderBy, boolean desc, int limit, int page)
	{
		int offset= (page-1)*limit;
		
		Order order= new Order();
		order.setTableObject(pojo);
		order.setOrderBy(orderBy);
		order.setDesc(desc);
		order.setLimit(limit);
		order.setOffset(offset);
		
		return order;
	}
	
	public static List<String> getAllFields(Class<?> clazz)
	{
		List<String> requiredFields= new ArrayList<String>();
		Field[] declaredField= clazz.getDeclaredFields();
		
		for(Field currField: declaredField)
		{
			requiredFields.add(currField.getName());
		}
		return requiredFields;
	}
	
	public static <T> T getSingleElePojo(List<List<Object>> result, Class<T> clazz) throws InvalidException
	{
		Validator.validate(result);
		
	    if (result.isEmpty() || result.get(0).isEmpty()) {
	        throw new InvalidException("No data found");
	    }
	    
		Object obj= result.get(0).get(0);
		return clazz.cast(obj);
	}
	
	public static <T> List<T> getListOfPojo(List<List<Object>> result, Class<T> clazz) throws InvalidException
	{
		Validator.validate(result);
		
	    if (result.isEmpty() || result.get(0).isEmpty()) {
	        throw new InvalidException("No data found");
	    }
		
		List<T> list= new ArrayList<T>();
		for(List<Object> row: result)
		{
			list.add(clazz.cast(row.get(0)));
		}
		return list;
	}
	
	public static int getUserId(HttpServletRequest req)
	{
		return Integer.parseInt(req.getSession(false).getAttribute("userId").toString());
	}
	
	public static int getClientRowId(HttpServletRequest req)
	{
		return Integer.parseInt(req.getSession(false).getAttribute("clientRowId").toString());
	}
	
	public static String getPath(HttpServletRequest req)
	{
		StringBuffer reqUrl= req.getRequestURL();
		String params= req.getQueryString();
		String path;
		if(params!=null)
		{
			path= reqUrl.append("&").append(params).toString();
		}
		else
		{
			path= reqUrl.toString();
		}
		
		return path;
	}
	
	public static String getRandomString()
	{
		return UUID.randomUUID().toString();
	}
	
	public static int getRandomInt()
	{
		int min=1000, max=9999;
		
		return min + (int) (Math.random() * (max - min + 1));
	}
	
	public static String getSHAHash(String value) throws InternalException 
	{
		try
		{
			return hash(MessageDigest.getInstance("SHA-256"), value);
		}
		catch(NoSuchAlgorithmException error)
		{
			System.out.println(error.getMessage());
			throw new InternalException("Error getting hash value.", error);
		}
    }
	
	public static String getMD5Hash(String value) throws InternalException
	{
		try
		{
			return hash(MessageDigest.getInstance("MD5"), value);			
		}
		catch(NoSuchAlgorithmException error)
		{
			System.out.println(error.getMessage());
			throw new InternalException("Error getting hash value.", error);
		}
	}
	
	private static String hash(MessageDigest newMD, String value)
	{
		byte[] byteArr= newMD.digest(value.getBytes(StandardCharsets.UTF_8));
		
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArr) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
	}

	public static String capitalize(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	public static String generateCode() throws InternalException
	{
			StringBuilder sb= new StringBuilder();
			
			sb.append(getRandomInt())
			.append(".")
			.append(getMD5Hash(getRandomString()))
			.append(".")
			.append(getMD5Hash(getRandomString()));
			
			return sb.toString();
	}
	
	public static boolean hasOIDCScopes(List<String> scopes)
	{
		return (scopes.contains(Scopes.EMAIL.getName()) 
				|| scopes.contains(Scopes.PROFILE.getName()) 
				|| scopes.contains(Scopes.OPENID.getName()));
	}

	public static String convertArrayToString(String arr[])
	{
		StringBuilder sb= new StringBuilder();
		for(String iter: arr)
		{
			sb.append(iter+" ");
		}
		
		return sb.toString().trim();
	}
	
	public static List<String> convertJSONArrayToList(JSONArray uris, String name) throws JSONException, InvalidException
	{
		Validator.validate(uris, name);
		
		List<String> uriList= new ArrayList<>();
		int len= uris.length();
		
		for(int i=0;i<len;i++)
		{
			uriList.add(uris.getString(i));
		}
		
		return uriList;
	}
}
