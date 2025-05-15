package filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import crud.AccessTokenOperation;
import crud.ScopeOperation;
import exception.InternalException;
import exception.InvalidException;
import helper.Validator;
import pojo.AccessToken;

public class AuthorizationFilter implements Filter{
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req= (HttpServletRequest) request;
		HttpServletResponse resp= (HttpServletResponse) response;
		
		try
		{
			String authToken= req.getHeader("Authorization");
			Validator.checkForNull(authToken, "token");
			
			if(!authToken.startsWith("Bearer"))
			{
				throw new InvalidException("invalid_token_type");
			}
			
			String aToken= authToken.split(" ")[1];
			AccessToken token= AccessTokenOperation.getAT(aToken);
			if(Validator.isExpired(token.getCreatedTime(), "token", 3600))	//Token expiration 1 hour
			{
				AccessTokenOperation.deactivateAT(token.getAccessTokenId());
				throw new InvalidException("token_expired");
			}
			
			String endPoint= req.getRequestURI();
			System.out.println("Resource request received for: "+endPoint);
			
			if(hasAuthority(endPoint, token.getAuthId(), token.getAccessTokenId()))
			{
				System.out.println("Successfully passed Authorization filter!");
				chain.doFilter(req, resp);
			}
			else
			{
				throw new InvalidException("no_access");
			}
		}
		catch(InvalidException error)
		{
			error.printStackTrace();
			resp.getWriter().write("{\"error\": \"" + error.getMessage() + "\"}");
		}
		catch(InternalException error)
		{
			error.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	public static boolean hasAuthority(String endPoint, int authId, int atId) throws InternalException, InvalidException
	{
		System.out.println("Endpoint: "+endPoint);
		try
		{
			JSONArray scopes= Validator.getApiScopes().getJSONArray("scopes");
			int len= scopes.length();
			JSONArray requiredScopes=null;
			for(int i=0;i<len;i++)
			{
				JSONObject iter= scopes.getJSONObject(i);
//				System.out.println("Path: "+iter.getString("path"));

				if(iter.getString("path").equals(endPoint))
				{
					requiredScopes= iter.getJSONArray("requirement");
					break;
				}
			}
			
			List<String> providedScopes= ScopeOperation.getScopes(authId, atId);
			System.out.println("Provided Scopes: "+ providedScopes);
			
			Validator.checkForNull(requiredScopes);
			int scopeCount= requiredScopes.length();
			boolean verifyFlag= true;
			
			for(int i=0;i<scopeCount;i++)
			{
				String iter= requiredScopes.getString(i);
				System.out.println((i+1)+") "+iter);
				
				if(iter.contains("."))
				{
					String scopeWithoutOptType= iter.split("\\.")[0];
					if(providedScopes.contains(iter) || providedScopes.contains(scopeWithoutOptType+".all"))
					{
						continue;
					}
					else
					{
						verifyFlag= false;
						break;
					}					
				}
			}
			return verifyFlag;
		}
		catch(JSONException error)
		{
			System.out.println(error.getMessage());
			throw new InternalException("Error while checking authority.", error);
		}
	}

}
