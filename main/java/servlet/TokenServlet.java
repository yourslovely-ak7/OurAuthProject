package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import crud.AuthorizationOperation;
import crud.ClientOperation;
import exception.InvalidException;
import pojo.Authorization;
import pojo.Client;

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
			Client client= ClientOperation.validateClient(clientId, redirectUrl);
			Authorization auth= AuthorizationOperation.getAuthorization(code);
			
			if(client.getClientSecret().equals(clientSecret))
			{
				
			}
		}
		catch(InvalidException error)
		{
			error.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
