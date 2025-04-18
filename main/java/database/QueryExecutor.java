package database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.InternalException;
import helper.Helper;
import helper.Validator;
import pojo.Condition;
import pojo.Fields;

public class QueryExecutor 
{
	public static long executeInsert(StringBuilder queryBuilder, Map<String, Object> fieldsAndValues, boolean returnGeneratedKey) throws SQLException, InternalException
	{
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(queryBuilder.toString(),
						Statement.RETURN_GENERATED_KEYS))
		{
			int index = 1;
			for (Map.Entry<String, Object> entry : fieldsAndValues.entrySet()) 
			{
				statement.setObject(index, entry.getValue());
				index++;
			}

			System.out.println("Query before execution : " + statement.toString());
			int rowsAffected = statement.executeUpdate();

			if (rowsAffected != 0 && returnGeneratedKey) {
				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getLong(1);
					}
				}
			}
			return rowsAffected;
		}
	}
	
	public static <T> long executeInsertBatch(StringBuilder queryBuilder, Fields<T> fields) throws SQLException, InternalException
	{
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(queryBuilder.toString()))
		{
			List<T> records= (List<T>) fields.getRecords();
			Class<?> clazz= records.get(0).getClass();
			
			for(T iter: records)
			{
				int index = 1;
				for(Map.Entry<String, Object> entry: fields.getValues().entrySet())
				{
					String column= (String) entry.getValue();
					String getterMethodName = "get" + Helper.capitalize(column);
					Method getterMethod = clazz.getDeclaredMethod(getterMethodName);
					Object value = getterMethod.invoke(iter);

					statement.setObject(index, value);
					index++;
				}
				statement.addBatch();
			}

			System.out.println("Query before execution : " + statement.toString());
			int rowsAffected[] = statement.executeBatch();
			
			for(int i: rowsAffected)
			{
				if(i==0)
				{
					return 0;
				}
			}
			return 1;
		}
		catch(InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException error)
		{
			System.out.println(error.getMessage());
			throw new InternalException("Error while executing Batch insertion", error);
		}
	}
	
	public static List<Map<String,Object>> executeSelect(String queryType, StringBuilder queryBuilder, Map<Integer,Condition> conditions) throws InternalException, SQLException
	{
		try(Connection connection= DatabaseConnection.getConnection();
				PreparedStatement statement= prepareStatement(queryType, connection, queryBuilder.toString(), null, conditions))
		{
			System.out.println("Query before execution : "+statement.toString());
			try(ResultSet result= statement.executeQuery())
			{
				return resultSetConversion(result);					
			}
		}
	}

	public static int executeCount(String queryType, StringBuilder queryBuilder, Map<Integer,Condition> conditions) throws SQLException, InternalException
	{
		try(Connection connection= DatabaseConnection.getConnection();
				PreparedStatement statement= prepareStatement(queryType, connection, queryBuilder.toString(), null, conditions))
		{
			int count=0;
			System.out.println("Query before execution : "+statement.toString());
			try(ResultSet result= statement.executeQuery())
			{
				if(result.next())
				{
					count= result.getInt(1);
				}	
				return count;
			}
		}
	}
	
	public static <T> int executeUpdateAndDelete(String queryType, StringBuilder queryBuilder, Map<Integer,Condition> conditions, List<Fields<T>> newValues) throws SQLException, InternalException
	{
		try(Connection connection= DatabaseConnection.getConnection();
				PreparedStatement statement= prepareStatement(queryType, connection, queryBuilder.toString(), newValues, conditions))
		{		
			System.out.println("Query before execution : "+statement.toString());
			int result= statement.executeUpdate();
			return result;
		}
	}
	
	private static <T> PreparedStatement prepareStatement(String queryType, Connection connection, String query, List<Fields<T>> newValues, Map<Integer,Condition> conditions) throws SQLException, InternalException 
	{
		Validator.checkForNull(queryType);
		Validator.checkForNull(connection);
		Validator.checkForNull(query);
		
		PreparedStatement statement = connection.prepareStatement(query);
		int index =1;
		
		if(queryType.equals("UPDATE"))
		{
			Validator.checkForNull(newValues);
			
			int len= newValues.size();
			for(int i=0;i<len;i++)
			{
				Fields<T> iter= newValues.get(i);
				Map<String,Object> values= iter.getValues();
				
				for(Map.Entry<String, Object> entry: values.entrySet())
				{
					statement.setObject(index, entry.getValue());
					index++;
				}
			}
		}

		if (!conditions.isEmpty()) 
		{
			for(Map.Entry<Integer, Condition> entry: conditions.entrySet())
			{
				Condition iter= entry.getValue();
				statement.setObject(index, iter.getValue());
				index++;
			}
		}
		return statement;
	}
	
	private static List<Map<String,Object>> resultSetConversion(ResultSet result) throws SQLException, InternalException
	{
		Validator.checkForNull(result);
		
		List<Map<String, Object>> dataSet = new ArrayList<Map<String,Object>>();
		ResultSetMetaData metaData= result.getMetaData();
		int columnCount= metaData.getColumnCount();
		
		while (result.next()) 
		{
            Map<String, Object> row = new HashMap<>();
        
            for (int i = 1; i <= columnCount; i++) 
            {
                String columnName = metaData.getColumnName(i);
                Object columnValue = result.getObject(i);
                row.put(columnName, columnValue);
            }
            dataSet.add(row);
        }
        return dataSet;
	}
}
