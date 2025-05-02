package servlet;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import crud.AccessTokenOperation;
import crud.ClientOperation;
import crud.RefreshTokenOperation;
import crud.ScopeOperation;
import crud.UserOperation;
import exception.InternalException;
import exception.InvalidException;
import helper.Validator;
import pojo.AccessToken;
import pojo.Client;
import pojo.RefreshToken;
import pojo.User;

@SuppressWarnings("serial")
public class TokenIntrospection extends HttpServlet{
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String clientId= req.getParameter("client_id");
		String clientSecret= req.getParameter("client_secret");

		try
		{
			if(clientId==null || clientSecret==null)
			{
				System.out.println("Client credentials are expected to be sent via Header.");
				String token= req.getHeader("Authorization");
				Validator.validate(token, "token");
				
				if(!token.startsWith("Basic"))
				{
					throw new InvalidException("invalid_token_type");
				}
				System.out.println("Authorization Header: "+ token);
				String clientCred= new String(Base64.getDecoder().decode(token.split(" ")[1]));
				
				String[] credentials= clientCred.split(":");
				clientId= credentials[0];
				clientSecret= credentials[1];
			}
			
			Client client=null;
			client= ClientOperation.getClientById(clientId);

			JSONObject response= new JSONObject();
			String tokenType= req.getParameter("token_type_hint");
			String token= req.getParameter("token");
			
			Validator.validate(token, "token");
			Validator.validate(tokenType, "token_type_hint");
			
			try
			{
				response= fetchTokenData(token, tokenType, response, client.getClientRowId());
				response.put("active", true);
				response.put("aud", client.getClientId());
				response.put("iss", "http://localhost:8081/OurAuth");
			}
			catch(InvalidException error)
			{
				System.out.println("Error: "+ error.getMessage());
				response.put("active", false);
			}

			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(response.toString());
		}
		catch(InvalidException error)
		{
			error.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().write("{\"error\": \"" + error.getMessage() + "\"}");
		}
		catch(InternalException | JSONException error)
		{
			error.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private JSONObject fetchTokenData(String token, String tokenType, JSONObject response, int clientRowId) throws InvalidException, InternalException, JSONException
	{
		int userId=0, tokenClientId=0;
		String scopes=null;
		long issuedAt= 0;
		if(tokenType.equals("access_token"))
		{
			AccessToken accessToken= AccessTokenOperation.getAT(token);
			tokenClientId= accessToken.getClientRowId();
			issuedAt= accessToken.getCreatedTime();
			
			if(Validator.isExpired(issuedAt, "token", 3600))	//Token expiration 1 hour
			{
				System.out.println("The token seems to be expired! Updating in DB...");
				AccessTokenOperation.deactivateAT(accessToken.getAccessTokenId());
				throw new InvalidException("token_expired");
			}
			response.put("exp", (issuedAt/1000)+ 3600);
			
			userId= accessToken.getUserId();
			scopes= ScopeOperation.getScopes(accessToken.getAuthId(), accessToken.getAccessTokenId()).toString();
		}
		else if(tokenType.equals("refresh_token"))
		{
			RefreshToken refreshToken= RefreshTokenOperation.getRT(token);
			tokenClientId= refreshToken.getClientRowId();
			issuedAt= refreshToken.getCreatedTime();
			userId= refreshToken.getUserId();
			scopes= ScopeOperation.getScopes(refreshToken.getAuthId(), 0).toString();
		}

		if(tokenClientId != clientRowId)
		{
			throw new InvalidException("invalid_token");
		}
		
		User user= UserOperation.getUser(userId);
		response.put("username", user.getFirstName()+" "+user.getLastName());
		response.put("scope", scopes);
		response.put("iat", issuedAt/1000);

		return response;
	}
}
