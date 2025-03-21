package builder;

import org.json.JSONException;
import org.json.JSONObject;

import crud.ClientOperation;
import exception.InvalidException;
import pojo.Authorization;
import pojo.Client;
import pojo.User;

public class ObjectBuilder {
	
	public static User buildUserFromJSON(JSONObject json, boolean isSignUpReq) throws InvalidException
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
			throw new InvalidException("Error: Building User object", error);
		}
	}
	
	public static Client buildClientFromParam(String name, String url, int userId) throws InvalidException
	{
		return new Client()
		.setCreatedBy(userId)
		.setClientName(name)
		.setRedirectUrl(url)
		.setClientSecret(ClientOperation.getClientSecret(url));
	}
	
	public static Authorization buildAuthorization(int userId, int clientRowId)
	{
		return new Authorization()
		.setClientRowId(clientRowId)
		.setUserId(userId);
	}
}
