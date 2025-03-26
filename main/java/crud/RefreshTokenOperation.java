package crud;

import exception.ConstraintViolationException;
import exception.InvalidException;
import helper.Helper;
import mapping.Mapper;
import pojo.RefreshToken;
import pojo.Status;

public class RefreshTokenOperation {

//	private static final String tableName= "RefreshToken";
//	private static final String pk= "refreshTokenId";
//	private static final Class<RefreshToken> pojo= RefreshToken.class;
	private static Mapper newMap= new Mapper();
	
	public static RefreshToken createRTEntry(RefreshToken token) throws InvalidException
	{
		try
		{
			String refreshToken= Helper.generateCode();
			long millis= System.currentTimeMillis();
			
			token.setRefreshToken(refreshToken)
			.setCreatedTime(millis)
			.setStatus(Status.ACTIVE.name());
			
			int authId= (int) newMap.create(token, true);
			
			token.setRefreshTokenId(authId);
			return token;
		}
		catch (ConstraintViolationException error) 
		{
			return null;
		}
	}
}
