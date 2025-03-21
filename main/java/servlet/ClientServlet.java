package servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import builder.ObjectBuilder;
import crud.ClientOperation;
import exception.InvalidException;
import helper.Helper;
import pojo.Client;

@SuppressWarnings("serial")
public class ClientServlet extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		String name= req.getParameter("name");
		String url= req.getParameter("redirectUrl");
		
		try
		{
			Client newClient= ObjectBuilder.buildClientFromParam(name, url, Helper.getUserId(req));
			int clientRowId;
			
			do {
				clientRowId= ClientOperation.createClient(newClient);
			}
			while(clientRowId==0);
			
			newClient= ClientOperation.getClient(clientRowId);
			
			JSONObject json= new JSONObject();
			json.put("name", newClient.getClientName());
			json.put("clientId", newClient.getClientId());
			json.put("clientSecret", newClient.getClientSecret());
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(json.toString());
		}
		catch(InvalidException | JSONException error)
		{
			error.printStackTrace();
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, error.getMessage());
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		int userId= Helper.getUserId(req);
		
		try
		{
			List<Client> clients= ClientOperation.getAllClients(userId);
			
			JSONObject responseJson= new JSONObject();
			JSONArray jsonClients= new JSONArray();
			
			for(Client iter: clients)
			{
				JSONObject json= new JSONObject();
				json.put("clientName", iter.getClientName());
				json.put("clientId", iter.getClientId());
				json.put("clientSecret", iter.getClientSecret());
				json.put("redirectUrl", iter.getRedirectUrl());
				
				jsonClients.put(json);
			}
			
			responseJson.put("clientData", jsonClients);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(responseJson.toString());
		}
		catch(InvalidException | JSONException error)
		{
			error.printStackTrace();
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, error.getMessage());
		}
	}
}
