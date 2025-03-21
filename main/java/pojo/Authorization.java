package pojo;

public class Authorization {
	
	private int authId;
	private int clientRowId;
	private int userId;
	private String authCode;
	private long createdTime;
	private String status;
	
	public int getAuthId() {
		return authId;
	}
	public int getClientRowId() {
		return clientRowId;
	}
	public int getUserId() {
		return userId;
	}
	public String getAuthCode() {
		return authCode;
	}
	public long getCreatedTime() {
		return createdTime;
	}
	public String getStatus() {
		return status;
	}
	public Authorization setAuthId(int authId) {
		this.authId = authId;
		return this;
	}
	public Authorization setClientRowId(int clientRowId) {
		this.clientRowId = clientRowId;
		return this;
	}
	public Authorization setUserId(int userId) {
		this.userId = userId;
		return this;
	}
	public Authorization setAuthCode(String authCode) {
		this.authCode = authCode;
		return this;
	}
	public Authorization setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
		return this;
	}
	public Authorization setStatus(String status) {
		this.status = status;
		return this;
	}
}
