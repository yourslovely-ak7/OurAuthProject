package servlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;

import javax.servlet.ServletException;
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
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String clientId= req.getParameter("clientId");
		String clientSecret= req.getParameter("clientSecret");
		String redirectUrl= req.getParameter("redirectUrl");
		String code= req.getParameter("code");
		
		try
		{
			Client client=null;
			try
			{
				client= ClientOperation.validateClient(clientId, redirectUrl);
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
				
				RefreshToken rToken= ObjectBuilder.buildRefreshToken(userId, clientRowId, authId);
				do
				{
					rToken= RefreshTokenOperation.createRTEntry(rToken);					
				}
				while(rToken==null);
				
				AccessToken aToken= ObjectBuilder.buildAccessToken(rToken.getRefreshTokenId(), authId);
				
				do {
					aToken= AccessTokenOperation.createATEntry(aToken);
				}
				while(aToken==null);
				
				response.put("refreshToken", rToken.getRefreshToken());
				response.put("accessToken", aToken.getAccessToken());
				
				List<String> scopes= ScopeOperation.getScopes(authId);
				if(scopes.contains(Scopes.email.name()) || scopes.contains(Scopes.profile.name()))
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
		payloadJson.put("iss", "http://localhost/OurAuth");
		payloadJson.put("iat", timeInSec);
		payloadJson.put("exp", timeInSec+ 3600);
		
		User user= UserOperation.getUser(userId);
		if(scopes.contains(Scopes.profile.name()))
		{
			payloadJson.put("name", user.getName());
			payloadJson.put("first_name", user.getFirstName());
			payloadJson.put("last_name", user.getLastName());
			payloadJson.put("gender", user.getGender());			
		}
		
		if(scopes.contains(Scopes.email.name()))
		{
			payloadJson.put("email", user.getEmail());			
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
