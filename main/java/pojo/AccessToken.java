package pojo;

public class AccessToken {
	
	private int accessTokenId;
	private int refreshTokenId;
	private String accessToken;
	private long createdTime;
	private String status;
	private int authId;
	
	
	public int getAuthId() {
		return authId;
	}
	public int getAccessTokenId() {
		return accessTokenId;
	}
	public int getRefreshTokenId() {
		return refreshTokenId;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public long getCreatedTime() {
		return createdTime;
	}
	public String getStatus() {
		return status;
	}
	public AccessToken setAccessTokenId(int accessTokenId) {
		this.accessTokenId = accessTokenId;
		return this;
	}
	public AccessToken setRefreshTokenId(int refreshTokenId) {
		this.refreshTokenId = refreshTokenId;
		return this;
	}
	public AccessToken setAccessToken(String accessToken) {
		this.accessToken = accessToken;
		return this;
	}
	public AccessToken setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
		return this;
	}
	public AccessToken setStatus(String status) {
		this.status = status;
		return this;
	}
	public AccessToken setAuthId(int authId) {
		this.authId = authId;
		return this;
	}
}
