package pojo;

public class Uri {
	
	private int clientRowId;
	private String redirectUri;
	
	public int getClientRowId() {
		return clientRowId;
	}
	public String getRedirectUri() {
		return redirectUri;
	}
	public Uri setClientRowId(int clientRowId) {
		this.clientRowId = clientRowId;
		return this;
	}
	public Uri setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
		return this;
	}
}
