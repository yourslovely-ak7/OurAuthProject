package filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import exception.InvalidException;
import helper.Helper;
import helper.Validator;

public class AuthenticationFilter implements Filter{
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req= (HttpServletRequest) request;
		HttpServletResponse resp= (HttpServletResponse) response;
		
		String path= Helper.getPath(req);
		System.out.println("Filter invoked for: "+path);
		
		//Check for active session.
		HttpSession session= req.getSession(false);
		if(session==null && !checkForGrantType(req))
		{
			resp.sendRedirect("/OurAuth/login.html?serviceUrl="+path);
		}
		else
		{
			chain.doFilter(req, resp);
		}
	}
	
	private static boolean checkForGrantType(HttpServletRequest req)
	{
		String grantType= req.getParameter("grant_type");
		try
		{
			Validator.validate(grantType);
			return grantType.equals("client_credentials");
		}
		catch(InvalidException error)
		{
			System.out.println("Filter shouldn't be skipped.");
			return false;
		}
	}
}
