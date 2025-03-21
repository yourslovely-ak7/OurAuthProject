package crud;

import exception.ConstraintViolationException;
import exception.InvalidException;
import helper.Helper;
import mapping.Mapper;
import pojo.AccessToken;
import pojo.Authorization;
import pojo.Status;

public class AccessTokenOperation {

	private static final String tableName= "AccessToken";
	private static final String pk= "accessTokenId";
	private static final Class<AccessToken> pojo= AccessToken.class;
	private static Mapper newMap= new Mapper();
	
	public static AccessToken createATEntry(AccessToken token) throws InvalidException
	{
		try
		{
			String accessToken= Helper.generateCode();
			long millis= System.currentTimeMillis();
			
			token.setAccessToken(accessToken)
			.setCreatedTime(millis)
			.setStatus(Status.ACTIVE.name());
			
			int authId= (int) newMap.create(token, true);
			
			token.setAccessTokenId(authId);
			return token;
		}
		catch (ConstraintViolationException error) 
		{
			return null;
		}
	}
}
