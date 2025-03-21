package pojo;

public class RefreshToken {
	
	private int refreshTokenId;
	private int clientRowId;
	private int userId;
	private int authId;
	private String refreshToken;
	private long createdTime;
	private String status;
	
	public int getRefreshTokenId() {
		return refreshTokenId;
	}
	public int getClientRowId() {
		return clientRowId;
	}
	public int getUserId() {
		return userId;
	}
	public int getAuthId() {
		return authId;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public long getCreatedTime() {
		return createdTime;
	}
	public String getStatus() {
		return status;
	}
	public RefreshToken setRefreshTokenId(int refreshTokenId) {
		this.refreshTokenId = refreshTokenId;
		return this;
	}
	public RefreshToken setClientRowId(int clientRowId) {
		this.clientRowId = clientRowId;
		return this;
	}
	public RefreshToken setUserId(int userId) {
		this.userId = userId;
		return this;
	}
	public RefreshToken setAuthId(int authId) {
		this.authId = authId;
		return this;
	}
	public RefreshToken setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
		return this;
	}
	public RefreshToken setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
		return this;
	}
	public RefreshToken setStatus(String status) {
		this.status = status;
		return this;
	}
}
