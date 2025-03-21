package pojo;

public enum Scopes {

	profile,
	email;
	
	@Override
	public String toString()
	{
		return name();
	}
}