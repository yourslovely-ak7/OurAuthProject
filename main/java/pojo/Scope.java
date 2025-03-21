package pojo;

public class Scope {
	
	private int authId;
	private String scope;
	
	public int getAuthId() {
		return authId;
	}
	public String getScope() {
		return scope;
	}
	public Scope setAuthId(int authId) {
		this.authId = authId;
		return this;
	}
	public Scope setScope(String scope) {
		this.scope = scope;
		return this;
	}
}
