package servlet;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import builder.ObjectBuilder;
import crud.AuthorizationOperation;
import crud.ClientOperation;
import crud.ScopeOperation;
import exception.InvalidException;
import helper.Helper;
import helper.Validator;
import pojo.Authorization;
import pojo.Client;

@SuppressWarnings("serial")
public class OurAuth extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String type= req.getParameter("response_type");
		System.out.println("OAuth request received for "+type);

		switch(type)
		{
			case "code":
				requestValidate(req, resp);
				break;
			
			case "consent":
				generateCode(req, resp);
				break;
		}
	}
	
	private void requestValidate(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		String clientId= req.getParameter("client_id");
		String redirectUrl= req.getParameter("redirect_uri");
		String scope= req.getParameter("scope");
		String [] scopes= scope.split(" ");
		
		try
		{
			Client client= ClientOperation.validateClientByIdAndUrl(clientId, redirectUrl);
			Validator.isValidScope(scopes);
			
			HttpSession session= req.getSession(false);
			session.setAttribute("clientRowId", client.getClientRowId());
			
//			String path= Helper.getPath(req);
//			resp.sendRedirect("/OurAuth/consent.html?serviceUrl="+path+"&scopes="+scope+"&name="+client.getClientName());
			resp.sendRedirect("/OurAuth/consent.html?scopes="+scope+"&name="+client.getClientName());
		}
		catch(InvalidException error)
		{
			System.out.println(error.getMessage());
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().write("{\"message\": \"" + error.getMessage() + "\"}");
		}	
	}
	
	private void generateCode(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
//		String clientId= req.getParameter("clientId");
		String redirectUrl= req.getParameter("redirect_uri");
		String scope= req.getParameter("scope");
		String [] reqScopes= scope.split(" ");
		String agreedScopes= req.getParameter("agreed_scopes");
		String [] allowableScopes= agreedScopes.split(" ");
		
		System.out.println("Scopes req: "+scope+"\nScopes approved: "+agreedScopes);
		
		int userId= Helper.getUserId(req);
		int clientRowId= Helper.getClientRowId(req);
		
		try
		{
			Authorization auth= ObjectBuilder.buildAuthorization(userId, clientRowId);
			
			do {
				auth= AuthorizationOperation.createAuthEntry(auth);
			}
			while(auth == null);
			ScopeOperation.addScopes(auth.getAuthId(), allowableScopes);

			StringBuilder responseBuilder= new StringBuilder(redirectUrl);
			responseBuilder.append("?code="+auth.getAuthCode())
			.append("&api_domain=http://localhost:8081/OurAuth");
			
			String revokedScopes= checkForRejectedScopes(reqScopes, allowableScopes);
			
			if(revokedScopes != null)
			{
				responseBuilder.append("&scope="+revokedScopes);
			}
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.sendRedirect(responseBuilder.toString());
		}
		catch(InvalidException error)
		{
			System.out.println(error.getMessage());
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private static String checkForRejectedScopes(String reqScopes[], String allowedScopes[])
	{
		StringBuilder rejectedScopes= new StringBuilder();
		
		int lenRS= reqScopes.length, lenAS= allowedScopes.length;
		
		if(lenRS == lenAS)
		{
			return null;
		}
		
		Arrays.sort(reqScopes);
		Arrays.sort(allowedScopes);
		
		int iter=0;
		for(int i=0;i<lenRS;i++)
		{
			if(!reqScopes[i].equals(allowedScopes[iter]))
			{
				rejectedScopes.append(reqScopes[i])
				.append(" ");
			}
			else
			{
				iter++;
			}
		}
		return rejectedScopes.toString();
	}
}
