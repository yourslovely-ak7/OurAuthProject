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
import crud.UserOperation;
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
		
		String type= req.getParameter("responseType");
		System.out.println("Token request received for "+type);

		switch(type)
		{
			case "token":
				getTokens(req, resp);
				break;
			
			case "refresh":
				refreshToken(req, resp);
				break;
		}
	}
	
	private void getTokens(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		String clientId= req.getParameter("clientId");
		String clientSecret= req.getParameter("clientSecret");
		String redirectUrl= req.getParameter("redirectUrl");
		String code= req.getParameter("code");
		
		try
		{
			Client client=null;
			try
			{
				client= ClientOperation.validateClientByIdAndUrl(clientId, redirectUrl);
			}
			catch(InvalidException error)
			{
				System.out.println(error.getMessage());
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().write("{\"error\": \"" + "Invalid client!" + "\"}");
			}
			
			Authorization auth= AuthorizationOperation.getAuthorization(code);
			Validator.checkForNull(client);
			
			if(client.getClientSecret().equals(clientSecret))
			{
				int userId= auth.getUserId();
				int clientRowId= client.getClientRowId();
				int authId= auth.getAuthId();
				JSONObject response= new JSONObject();
				
				RefreshToken rToken= generateRefreshToken(userId, clientRowId, authId);
				String aToken= generateAccessToken(rToken.getRefreshTokenId(), authId);
				
				response.put("refreshToken", rToken.getRefreshToken());
				response.put("accessToken", aToken);
				response.put("expiresIn", 3600);
				
				List<String> scopes= ScopeOperation.getScopes(authId);
				System.out.println("Scopes granted: "+scopes);
				
				if(Helper.hasOIDCScopes(scopes))
				{
					String idToken= generateIdToken(userId, scopes, clientId);
					response.put("idToken", idToken);
				}

				resp.setStatus(HttpServletResponse.SC_OK);
				resp.getWriter().write(response.toString());
			}
		}
		catch(InvalidException | JSONException error)
		{
			error.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void refreshToken(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		String clientId= req.getParameter("clientId");
		String clientSecret= req.getParameter("clientSecret");
		String refreshToken= req.getParameter("refreshToken");
		
		Client client=null;
		try
		{
			client= ClientOperation.validateClientById(clientId);
			if(!(client.getClientSecret().equals(clientSecret)))
			{
				throw new InvalidException("Client secret is invalid!");
			}
		}
		catch(InvalidException error)
		{
			error.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().write("{\"error\": \"" + "Invalid client!" + "\"}");
		}

		try
		{
			Validator.checkForNull(refreshToken);
			RefreshToken rToken= RefreshTokenOperation.getRT(refreshToken);
			String aToken= generateAccessToken(rToken.getRefreshTokenId(), rToken.getAuthId());
			
			RefreshToken newToken= new RefreshToken();
			String token= null;
			do {
				token= RefreshTokenOperation.updateRT(newToken, rToken.getRefreshTokenId());
			}
			while(token==null);
			
			JSONObject jsonResponse= new JSONObject();
			jsonResponse.put("refreshToken", token);
			jsonResponse.put("accessToken", aToken);
			
			try
			{
				String scope= req.getParameter("scope");
				Validator.checkForNull(scope);
				
				List<String> scopes= Arrays.asList(scope.split(" "));
				if(Helper.hasOIDCScopes(scopes))
				{
					String idToken= generateIdToken(rToken.getUserId(), scopes, clientId);
					jsonResponse.put("idToken", idToken);
				}
			}
			catch(InvalidException error)
			{
				System.out.println("Scopes not present! Sending only access token!");
			}
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(jsonResponse.toString());
		}
		catch(InvalidException | JSONException error)
		{
			error.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().write("{\"error\": \"" + "Process interrupted!" + "\"}");
		}
	}

	private RefreshToken generateRefreshToken(int userId, int clientRowId, int authId) throws InvalidException
	{
		RefreshToken rToken= ObjectBuilder.buildRefreshToken(userId, clientRowId, authId);
		do
		{
			rToken= RefreshTokenOperation.createRTEntry(rToken);					
		}
		while(rToken==null);
		
		return rToken;
	}
	
	private String generateAccessToken(int rtId, int authId) throws InvalidException
	{
		AccessToken aToken= ObjectBuilder.buildAccessToken(rtId, authId);
		
		do {
			aToken= AccessTokenOperation.createATEntry(aToken);
		}
		while(aToken==null);
		
		return aToken.getAccessToken();
	}
	
	private String generateIdToken(int userId, List<String> scopes, String clientId) throws JSONException, InvalidException
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
			throw new InvalidException("Error while generating ID token", error);
		}
	}
}
