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

import helper.Helper;

public class AuthenticationFilter implements Filter{
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req= (HttpServletRequest) request;
		HttpServletResponse resp= (HttpServletResponse) response;
		
		String path= Helper.getPath(req);
		System.out.println("Filter invoked for: "+path);
		
		HttpSession session= req.getSession(false);
		if(session==null)
		{
			resp.sendRedirect("/OurAuth/login.html?serviceUrl="+path);
		}
		else
		{
			chain.doFilter(req, resp);
		}
	}
}
