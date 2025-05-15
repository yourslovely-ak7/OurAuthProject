package servlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import builder.ObjectBuilder;
import crud.AccessTokenOperation;
import crud.AuthorizationOperation;
import crud.ClientOperation;
import crud.KeyOperation;
import crud.RefreshTokenOperation;
import crud.ScopeOperation;
import crud.UriOperation;
import crud.UserOperation;
import exception.InternalException;
import exception.InvalidException;
import helper.Helper;
import helper.KeyConvertor;
import helper.Validator;
import pojo.AccessToken;
import pojo.Authorization;
import pojo.Client;
import pojo.Key;
import pojo.RefreshToken;
import pojo.Scopes;
import pojo.User;

@SuppressWarnings("serial")
public class TokenServlet extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String type= req.getParameter("grant_type");
		System.out.println("Token request received for "+type);

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

			JSONObject jsonResponse=null;
			switch(type)
			{
				case "authorization_code":
					String redirectUri= req.getParameter("redirect_uri");
					String code= req.getParameter("code");
				
					jsonResponse= grantByCode(clientId, clientSecret, redirectUri, code);
					break;
				
				case "client_credentials":
					String scope= req.getParameter("scope");
					jsonResponse= grantByClientCred(clientId, clientSecret, scope);
					break;
				
				case "refresh_token":
					String refreshToken= req.getParameter("refresh_token");
					scope= req.getParameter("scope");
					jsonResponse= refreshToken(clientId, clientSecret, refreshToken, scope);
					break;
					
				case "client_registration":
					jsonResponse= initialToken(clientId, clientSecret);
					break;
			}

			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(jsonResponse.toString());
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
	
	private JSONObject grantByCode(String clientId, String clientSecret, String redirectUri,  String code) throws IOException, JSONException, InternalException, InvalidException
	{
		Validator.validate(clientSecret, "client");
			
		Client client=null;
		client= ClientOperation.getClientById(clientId);
		int clientRowId= client.getClientRowId();
		UriOperation.isValidUri(redirectUri, clientRowId);
		
		Authorization auth=null;
		auth= AuthorizationOperation.getAuthorization(code, clientRowId);
		Validator.isExpired(auth.getCreatedTime(), "code", 2*60);	//Code expiration time is 2 minutes
		
		AuthorizationOperation.deactivateToken(auth.getAuthId());
		
		if(!client.getClientSecret().equals(clientSecret))
		{
			throw new InvalidException("invalid_client_secret");
		}
		
		int userId= auth.getUserId();
		int authId= auth.getAuthId();
		JSONObject response= produceTokenPair(userId, clientRowId, authId);
		
		List<String> scopes= ScopeOperation.getScopes(authId, 0);
		System.out.println("Scopes granted: "+scopes);

		if(Helper.hasOIDCScopes(scopes))
		{
			String idToken= generateIdToken(userId, scopes, clientId);
			response.put("id_token", idToken);
		}
		
		return response;
	}

	private JSONObject grantByClientCred(String clientId, String clientSecret, String scope) throws InvalidException, InternalException, JSONException
	{
		Validator.validate(clientSecret, "client");
		Validator.validate(scope, "scope");
		
		String[] scopes= scope.split(" ");
		Validator.isValidScope(scopes);

		Client client=null;
		client= ClientOperation.getClientById(clientId);
		
		if(!client.getClientSecret().equals(clientSecret))
		{
			throw new InvalidException("invalid_client_secret");
		}
		
		int userId= client.getCreatedBy();
		int clientRowId= client.getClientRowId();
		JSONObject response= new JSONObject();
		
		AccessToken token= generateAccessToken(0, 0, userId, clientRowId);
		ScopeOperation.addScopes(0, scopes, token.getAccessTokenId());
		
		response.put("access_token", token.getAccessToken());
		response.put("expires_in", 3600);
		
		return response;
	}

	private JSONObject refreshToken(String clientId, String clientSecret, String refreshToken, String scope) throws IOException, InternalException, InvalidException, JSONException
	{
		Client client=null;
		client= ClientOperation.getClientById(clientId);
		if(!(client.getClientSecret().equals(clientSecret)))
		{
			throw new InvalidException("invalid_client_secret.");
		}
		
		RefreshToken rToken= RefreshTokenOperation.getRT(refreshToken);
		
		int userId= rToken.getUserId();
		int clientRowId= client.getClientRowId();
		int authId= rToken.getAuthId();
		JSONObject jsonResponse= produceTokenPair(userId, clientRowId, authId);
		
		RefreshTokenOperation.deactivateRT(rToken.getRefreshTokenId());
		try
		{
			Validator.validate(scope);
			String[] scopes= scope.split(" ");
			Validator.isValidScope(scopes);
			
			AccessToken token= AccessTokenOperation.getAT(jsonResponse.getString("access_token"));
			int atId= token.getAccessTokenId();
			
			List<String> requestedScopes= Arrays.asList(scopes);
			List<String> grantedScopes= ScopeOperation.getScopes(rToken.getAuthId(), atId);
			if(!Validator.isEqual(requestedScopes, grantedScopes))
			{
				ScopeOperation.addScopes(token.getAuthId(), scopes, atId);
			}
			
			if(Helper.hasOIDCScopes(requestedScopes))
			{
				String idToken= generateIdToken(rToken.getUserId(), requestedScopes, clientId);
				jsonResponse.put("id_token", idToken);
			}
		}
		catch(InvalidException error)
		{
			System.out.println("Scopes not present! Sending only access token!");
		}
		
		return jsonResponse;
	}
	
	private JSONObject initialToken(String clientId, String clientSecret) throws InvalidException, InternalException, JSONException
	{
		Client client=null;
		client= ClientOperation.getClientById(clientId);
		if(!(client.getClientSecret().equals(clientSecret)))
		{
			throw new InvalidException("invalid_client_secret.");
		}
		
		int userId= client.getCreatedBy();
		int clientRowId= client.getClientRowId();

		JSONObject response= new JSONObject();

		RefreshToken rToken= generateRefreshToken(userId, clientRowId, 0);
		AccessToken aToken= generateAccessToken(rToken.getRefreshTokenId(), 0, userId, clientRowId);

		response.put("refresh_token", rToken.getRefreshToken());
		response.put("access_token", aToken.getAccessToken());
		response.put("expires_in", 3600);
		
		String[] scopes= {Scopes.REGISTER_CREATE.getName()};
		ScopeOperation.addScopes(0, scopes, aToken.getAccessTokenId());
		
		return response;
	}
	
	private RefreshToken generateRefreshToken(int userId, int clientRowId, int authId) throws InternalException
	{
		RefreshToken rToken= ObjectBuilder.buildRefreshToken(userId, clientRowId, authId);
		RefreshToken referer;
		do
		{
			referer= RefreshTokenOperation.createRTEntry(rToken);
		}
		while(referer==null);

		return referer;
	}

	private AccessToken generateAccessToken(int rtId, int authId, int userId, int clientRowId) throws InternalException
	{
		AccessToken aToken= ObjectBuilder.buildAccessToken(rtId, authId, userId, clientRowId);
		AccessToken referer;
		do {
			referer= AccessTokenOperation.createATEntry(aToken);
		}
		while(referer==null);

		return referer;
	}
	
	private JSONObject produceTokenPair(int userId, int clientRowId, int authId) throws JSONException, InternalException, InvalidException
	{
		JSONObject response= new JSONObject();
		
		RefreshToken rToken= generateRefreshToken(userId, clientRowId, authId);
		AccessToken aToken= generateAccessToken(rToken.getRefreshTokenId(), authId, userId, clientRowId);
		
		response.put("refresh_token", rToken.getRefreshToken());
		response.put("access_token", aToken.getAccessToken());
		response.put("expires_in", 3600);
		
		return response;
	}

	private String generateIdToken(int userId, List<String> scopes, String clientId) throws JSONException, InternalException, InvalidException
	{
		String kid= "f2d5ae4a1e67329f5aec223b7544d3dc";

		JSONObject headerJson = new JSONObject();
		headerJson.put("alg", "RS256");
		headerJson.put("typ", "JWT");
		headerJson.put("kid", kid);

		long timeInSec= System.currentTimeMillis()/1000;
		JSONObject payloadJson = new JSONObject();
		payloadJson.put("aud", clientId);
		payloadJson.put("azp", clientId);
		payloadJson.put("iss", "http://localhost:8081/OurAuth");
		payloadJson.put("iat", timeInSec);
		payloadJson.put("exp", timeInSec+ 3600);
		
		User user= UserOperation.getUser(userId);
		if(scopes.contains(Scopes.PROFILE.getName()))
		{
			payloadJson.put("name", user.getName());
			payloadJson.put("first_name", user.getFirstName());
			payloadJson.put("last_name", user.getLastName());
			payloadJson.put("gender", user.getGender());			
		}
		
		if(scopes.contains(Scopes.EMAIL.getName()))
		{
			payloadJson.put("email", user.getEmail());			
		}
		
		if(scopes.contains(Scopes.OPENID.getName()))
		{
			payloadJson.put("sub", Helper.getMD5Hash(userId+""));
		}
		
		String encodedHeader= Base64.getEncoder().withoutPadding().encodeToString(headerJson.toString().getBytes(StandardCharsets.UTF_8));
		String encodedPayload= Base64.getEncoder().withoutPadding().encodeToString(payloadJson.toString().getBytes(StandardCharsets.UTF_8));
		String data= encodedHeader+"."+encodedPayload;
		Key key= KeyOperation.getKey(kid);
		try
		{
			PrivateKey pvtKey= KeyConvertor.convertToPrivateKey(key.getPrivateKey());
			
			Signature signature= Signature.getInstance("SHA256withRSA");
			signature.initSign(pvtKey);
			signature.update(data.getBytes(StandardCharsets.UTF_8));
			
			String encodedSignature= Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
			
			return data+"."+encodedSignature;
		}
		catch(NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | SignatureException error)
		{
			System.out.println(error.getMessage());
			throw new InternalException("Error while generating ID token", error);
		}
	}
}
