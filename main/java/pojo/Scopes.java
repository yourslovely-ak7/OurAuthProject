package pojo;

public enum Scopes {

	profile,
	email,
	openid;
	
	@Override
	public String toString()
	{
		return name();
	}
}