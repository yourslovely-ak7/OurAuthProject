package pojo;

import java.util.List;

import exception.InvalidException;
import helper.Validator;

public class ClientRegister {
	
	private List<String> redirectUris;
//	private String tokenEndpointAuthMethod;
	private String grantType;
	private String responseType;
	private String clientName;
	
	public List<String> getRedirectUris() {
		return redirectUris;
	}
//	public String getTokenEndpointAuthMethod() {
//		return tokenEndpointAuthMethod;
//	}
	public String getGrantType() {
		return grantType;
	}
	public String getResponseType() {
		return responseType;
	}
	public String getClientName() {
		return clientName;
	}
	public ClientRegister setRedirectUris(List<String> redirectUris) throws InvalidException {
		Validator.validate(redirectUris, "redirect_uris");
		this.redirectUris = redirectUris;
		return this;
	}
//	public ClientRegister setTokenEndpointAuthMethod(String tokenEndpointAuthMethod) {
//		this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
//		return this;
//	}
	public ClientRegister setGrantType(String grantType) {
		this.grantType = grantType;
		return this;
	}
	public ClientRegister setResponseType(String responseType) {
		this.responseType = responseType;
		return this;
	}
	public ClientRegister setClientName(String clientName) throws InvalidException {
		Validator.validate(clientName, "client_name");
		this.clientName = clientName;
		return this;
	}
}
