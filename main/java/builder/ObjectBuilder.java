package builder;

import org.json.JSONException;
import org.json.JSONObject;

import crud.ClientOperation;
import exception.InternalException;
import exception.InvalidException;
import pojo.AccessToken;
import pojo.Authorization;
import pojo.Client;
import pojo.RefreshToken;
import pojo.Status;
import pojo.User;

public class ObjectBuilder {
	
	public static User buildUserFromJSON(JSONObject json, boolean isSignUpReq) throws InvalidException, InternalException
	{
		try
		{
			User user= new User();
			if(isSignUpReq)
			{
				user.setName(json.getString("name"))
				.setFirstName(json.getString("firstName"))
				.setLastName(json.getString("lastName"))
				.setGender(json.getString("gender"));
			}
			user.setEmail(json.getString("email"))
			.setPassword(json.getString("password"));

			return user;
		}
		catch(JSONException error)
		{
			throw new InternalException("Error: Building User object", error);
		}
	}
	
	public static Client buildClientFromParam(String name, String url, int userId) throws InternalException
	{
		return new Client()
		.setCreatedBy(userId)
		.setClientName(name)
		.setClientSecret(ClientOperation.getClientSecret(url));
	}
	
	public static Authorization buildAuthorization(int userId, int clientRowId)
	{
		return new Authorization()
		.setClientRowId(clientRowId)
		.setUserId(userId)
		.setStatus(Status.ACTIVE.name());
	}
	
	public static RefreshToken buildRefreshToken(int userId, int clientRowId, int authId)
	{
		return new RefreshToken()
		.setUserId(userId)
		.setClientRowId(clientRowId)
		.setAuthId(authId)
		.setStatus(Status.ACTIVE.name());
		
	}
	
	public static AccessToken buildAccessToken(int rtId, int authId, int userId, int clientRowId)
	{
		return new AccessToken()
		.setRefreshTokenId(rtId)
		.setAuthId(authId)
		.setUserId(userId)
		.setClientRowId(clientRowId)
		.setStatus(Status.ACTIVE.name());
	}
	
	public static Client buildRegistration(JSONObject param) throws InvalidException, JSONException
	{
		String grantType= null, responseType= null;
		try
		{
			grantType= param.getString("grant_type");
			responseType= param.getString("response_type");		
		}
		catch(JSONException error)
		{
			System.out.println(error.getMessage());
		}
		
		grantType= validateGrantType(grantType);
		//Currently not needed other than validation
		responseType= validateRespType(grantType, responseType);
		return new Client()
				.setClientName(param.getString("client_name"))
				.setGrantType(grantType);
	}
	
	private static String validateGrantType(String grantType) throws InvalidException
	{
		if(grantType == null)
		{
			grantType= "authorization_code";
		}
		
		if(!grantType.equals("authorization_code") && !grantType.equals("client_credentials"))
		{
			throw new InvalidException("invalid_grant_type");
		}
		
		return grantType;
	}
	
	private static String validateRespType(String grantType, String respType) throws InvalidException
	{
		if(grantType.equals("authorization_code"))
		{
			if(respType == null)
			{
				respType= "code";	
			}
			else if(!respType.equals("code"))
			{
				throw new InvalidException("invalid_response_type");
			}
		}
		else
		{
			return null;
		}
		//No validation for client_credential flow;
		return respType;
	}
	
}
