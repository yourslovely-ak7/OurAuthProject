package filter;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import crud.AccessTokenOperation;
import crud.UserOperation;
import exception.InternalException;
import exception.InvalidException;
import helper.Validator;
import pojo.AccessToken;
import pojo.User;

public class RegisterFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req= (HttpServletRequest) request;
		HttpServletResponse resp= (HttpServletResponse) response;
		
		try
		{
			String authToken= req.getHeader("Authorization");
			Validator.checkForNull(authToken, "token");
			
			if(authToken.startsWith("Bearer"))
			{
				String aToken= authToken.split(" ")[1];
				AccessToken token= AccessTokenOperation.getAT(aToken);
				if(Validator.isExpired(token.getCreatedTime(), "token", 3600))	//Token expiration 1 hour
				{
					AccessTokenOperation.deactivateAT(token.getAccessTokenId());
					throw new InvalidException("token_expired");
				}
				
				String endPoint= req.getRequestURI();
				System.out.println("Resource request received for: "+endPoint);
				
				if(AuthorizationFilter.hasAuthority(endPoint, token.getAuthId(), token.getAccessTokenId()))
				{
					System.out.println("Successfully passed Register filter!");
					HttpSession session= req.getSession(true);
					session.setAttribute("userId", token.getUserId());
					
					chain.doFilter(req, resp);
				}
				else
				{
					throw new InvalidException("no_access");
				}
			}
			else if(authToken.startsWith("Basic"))
			{
				String userCred= new String(Base64.getDecoder().decode(authToken.split(" ")[1]));
				
				String[] credentials= userCred.split(":");
				String email= credentials[0];
				String password= credentials[1];
				
				User fetchedUser= UserOperation.getUser(email);

				if(fetchedUser.getPassword().equals(password))
				{
					HttpSession session= req.getSession(true);
					session.setAttribute("userId", fetchedUser.getUserId());

					chain.doFilter(req, resp);
				}
				else
				{
					throw new InvalidException("invalid_credentials");
				}
			}
			else
			{
				throw new InvalidException("invalid_token_type");
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
}
