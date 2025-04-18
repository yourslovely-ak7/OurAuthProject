package servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import builder.ObjectBuilder;
import crud.AuthorizationOperation;
import crud.ClientOperation;
import crud.ScopeOperation;
import crud.UriOperation;
import exception.InternalException;
import exception.InvalidException;
import helper.Helper;
import helper.Validator;
import pojo.Authorization;
import pojo.Client;

@SuppressWarnings("serial")
public class OurAuth extends HttpServlet
{
	private static final String defaultScopes= "openid profile email";
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
		
		if(scope==null)
		{
			scope= defaultScopes;
		}
		
		String responseType= req.getParameter("response_type");
		switch(responseType)
		{
			case "code":
				typeCode(clientId, redirectUrl, scope, req, resp);
				break;
		}
	}
	
	private void typeCode(String clientId, String redirectUri, String scope, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		try
		{
			String [] scopes= scope.split(" ");
			Client client= ClientOperation.getClientById(clientId);
			Validator.isValidScope(scopes);
			UriOperation.isValidUri(redirectUri, client.getClientRowId());
			
			HttpSession session= req.getSession(false);
			session.setAttribute("clientRowId", client.getClientRowId());
			
			scope= removeRepetition(scopes);
//			String path= Helper.getPath(req);
//			resp.sendRedirect("/OurAuth/consent.html?serviceUrl="+path+"&scopes="+scope+"&name="+client.getClientName());
			resp.sendRedirect("/OurAuth/consent.html?scope="+scope+"&name="+client.getClientName()+"&redirect_uri="+redirectUri);
		}
		catch(InvalidException error)
		{
			System.out.println(error.getMessage());
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().write("{\"message\": \"" + error.getMessage() + "\"}");
		}
		catch(InternalException error)
		{
			error.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
			ScopeOperation.addScopes(auth.getAuthId(), allowableScopes, 0);

			StringBuilder responseBuilder= new StringBuilder(redirectUrl);
			responseBuilder.append("?code="+auth.getAuthCode())
			.append("&api_domain=http://localhost:8081/OurAuth");
			
			String revokedScopes= checkForRejectedScopes(reqScopes, allowableScopes);
			System.out.println("Revoked Scopes : "+ revokedScopes);
			
			String grantedScopes= Helper.convertArrayToString(allowableScopes);
			if(grantedScopes!= null)
			{
				responseBuilder.append("&scope="+grantedScopes);
			}
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.sendRedirect(responseBuilder.toString());
		}
		catch(InternalException error)
		{
			error.printStackTrace();
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
	
	private String removeRepetition(String[] scopes)
	{
		String finalizedScopes= Arrays.stream(scopes)
										.distinct()
										.collect(Collectors.joining(" "));		
		return finalizedScopes;
	}
}
