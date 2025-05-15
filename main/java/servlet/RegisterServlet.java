package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import builder.ObjectBuilder;
import exception.InternalException;
import exception.InvalidException;
import helper.Helper;
import helper.Validator;
import pojo.Client;

@SuppressWarnings("serial")
public class RegisterServlet extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try
		{
			JSONObject jsonParam= Helper.getJsonRequest(req);
			Client newReg= ObjectBuilder.buildRegistration(jsonParam);
			
			String url= jsonParam.getString("redirect_uris");
			Validator.validate(url, "redirect_uris");
			
			JSONObject json= ClientServlet.clientCreation(newReg.getClientName(), url, Helper.getUserId(req));
			json.put("request_params", jsonParam);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(json.toString());
		}
		catch(InvalidException error)
		{
			error.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().write("{\"error\": \"" + error.getMessage() + "\"}");
		}
		catch(InternalException | JSONException error)
		{
			error.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
