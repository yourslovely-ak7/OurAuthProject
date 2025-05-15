package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import builder.ObjectBuilder;
import crud.UserOperation;
import exception.InternalException;
import exception.InvalidException;
import helper.Helper;
import pojo.User;

@SuppressWarnings("serial")
public class UserServlet extends HttpServlet{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		System.out.println("Request received at User Servlet!");
		String requestType= req.getParameter("type");
		try
		{
			switch(requestType)
			{
			case "signup":
				signUpUser(req, resp);
				break;
				
			case "login":
				loginUser(req, resp);
				break;
			
			case "logout":
				logoutUser(req, resp);
				break;
			}			
		}
		catch(JSONException | InternalException error)
		{
			error.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(InvalidException error)
		{
			error.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().write("{\"error\": \"" + error.getMessage() + "\"}");
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String requestType= req.getParameter("type");
		try
		{
			switch(requestType)
			{
			case "logout":
				logoutUser(req, resp);
				break;
			}			
		}
		catch(JSONException | InvalidException error)
		{
			error.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private void signUpUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, JSONException, InternalException, InvalidException
	{
		JSONObject requestBody= Helper.getJsonRequest(req);
		
		User user= ObjectBuilder.buildUserFromJSON(requestBody, true);
		
		int rowsAffected= UserOperation.addUser(user);
		
		if(rowsAffected == 1)
		{
			resp.setStatus(HttpServletResponse.SC_OK);
		}
		else
		{
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	private void loginUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, JSONException, InternalException, InvalidException
	{
		JSONObject requestBody= Helper.getJsonRequest(req);
		
		User fetchedUser= UserOperation.getUser(requestBody.getString("email"));
		String password= requestBody.getString("password");
		
		if(fetchedUser.getPassword().equals(password))
		{
			System.out.println("Password Matched and initiated redirecting...");
			HttpSession session= req.getSession(true);
			session.setAttribute("userId", fetchedUser.getUserId());
			
			String serviceUrl= req.getParameter("serviceUrl");
			if(serviceUrl==null || serviceUrl.equals("null"))
			{
				serviceUrl= "http://localhost:8081/OurAuth/welcome.html";
			}
			System.out.println("Redirect to: "+serviceUrl);
			JSONObject responseJson= new JSONObject();
			responseJson.put("serviceUrl", serviceUrl);
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(responseJson.toString());
		}
		else
		{
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}
	
	private void logoutUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, JSONException, InvalidException
	{
		HttpSession session= req.getSession(false);
		if(session!=null)
		{
			session.invalidate();	
		}
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.sendRedirect("/OurAuth/login.html");
	}
}
