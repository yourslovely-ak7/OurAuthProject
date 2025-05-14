package builder;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import crud.ClientOperation;
import exception.InternalException;
import exception.InvalidException;
import helper.Helper;
import pojo.AccessToken;
import pojo.Authorization;
import pojo.Client;
import pojo.ClientRegister;
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
	
	public static ClientRegister buildRegistration(JSONObject param) throws InvalidException, JSONException
	{
		JSONArray redirectUris= param.getJSONArray("redirect_uris");
		List<String> uriList= Helper.convertJSONArrayToList(redirectUris, "redirect_uris");
		
		if(uriList.size()==0)
		{
			throw new InvalidException("minimum redirect_uri required = one");
		}
		
		String grantType= validateGrantType(param.getString("grant_type"));
		String responseType= validateRespType(grantType, param.getString("response_type"));
		
		return new ClientRegister()
				.setClientName(param.getString("client_name"))
				.setRedirectUris(uriList)
				.setGrantType(grantType)
				.setResponseType(responseType);
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
			else if(respType!= "code")
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
