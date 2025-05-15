package pojo;

public enum Scopes {

	PROFILE("profile"),
	EMAIL("email"),
	OPENID("openid"),
	RESOURCE_ALL("RESOURCE.all"),
	RESOURCE_READ("RESOURCE.read"),
	REGISTER_CREATE("REGISTER.create");
	
	private String name;
	
	Scopes(String name)
	{
		this.name= name;
	}
	
	public String getName()
	{
		return name;
	}
}