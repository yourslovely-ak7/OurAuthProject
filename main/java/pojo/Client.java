package pojo;

public class Client {
	
	private int clientRowId;
	private String clientName;
	private String clientId;
	private String clientSecret;
	private String redirectUrl;
	private int createdBy;
	
	public int getClientRowId() {
		return clientRowId;
	}
	public String getClientName() {
		return clientName;
	}
	public String getClientId() {
		return clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public String getRedirectUrl() {
		return redirectUrl;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public Client setClientRowId(int clientRowId) {
		this.clientRowId = clientRowId;
		return this;
	}
	public Client setClientName(String clientName) {
		this.clientName = clientName;
		return this;
	}
	public Client setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}
	public Client setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
		return this;
	}
	public Client setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
		return this;
	}
	public Client setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
		return this;
	}
}
