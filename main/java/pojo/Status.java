package pojo;

public enum Status 
{
	ACTIVE,
	INACTIVE;
	
	@Override
	public String toString()
	{
		return name();
	}
}
