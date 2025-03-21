package database;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

import exception.ConstraintViolationException;
import exception.InvalidException;
import helper.Validator;
import pojo.Condition;
import pojo.Fields;
import pojo.Join;
import pojo.Order;

public class SQLQueryBuilder 
{	
	public <T> long insert(String tableName, Fields<T> fields, boolean returnGeneratedKey, boolean isBatchOpt) throws InvalidException, ClassNotFoundException, ConstraintViolationException
	{
		try {
			Validator.checkForNull(tableName);
			Validator.checkForNull(fields);
			Map<String, Object> fieldsAndValues = fields.getValues();
			Validator.checkForNull(fieldsAndValues);

			String queryType = "INSERT";
			StringBuilder queryBuilder = new StringBuilder();

			queryBuilder.append(queryType + " INTO ").append(tableName);

			StringBuilder columns = new StringBuilder();
			StringBuilder placeholders = new StringBuilder();

			int size = fieldsAndValues.size(), index = 0;
			for (Map.Entry<String, Object> entry : fieldsAndValues.entrySet()) 
			{
				String fieldName = entry.getKey();

				columns.append(fieldName);
				placeholders.append("?");

				if (index++ < size - 1) {
					columns.append(", ");
					placeholders.append(", ");
				}
			}
			
			queryBuilder.append("(" + columns + ") VALUES (" + placeholders + ");");
			System.out.println("Query before replacing placeholders : " + queryBuilder);

			if(isBatchOpt)
			{
				return QueryExecutor.executeInsertBatch(queryBuilder, fields);
			}
			else
			{
				return QueryExecutor.executeInsert(queryBuilder, fieldsAndValues, returnGeneratedKey);				
			}
		}
		catch(SQLIntegrityConstraintViolationException error)
		{
			System.out.println("Query Layer : " + error.getMessage());
			throw new ConstraintViolationException(error.getMessage(), error);
		}
		catch (SQLException | InvalidException error) {
			System.out.println("Query Layer : " + error.getMessage());
			throw new InvalidException("Error occurred while inserting record!", error);
		}
	}
	
	public <T> List<Map<String,Object>> select(List<Fields<T>> fields, List<Join> join, Map<Integer,Condition> conditions, Order order) throws InvalidException, ClassNotFoundException
	{
		try
		{
			Validator.checkForNull(fields);
			Validator.checkForNull(join);
			Validator.checkForNull(conditions);
			Validator.checkForNull(order);
			
			StringBuilder queryBuilder= new StringBuilder();
			String queryType= "SELECT";
			String tableName= fields.get(0).getTableName();
			
			queryBuilder.append(queryType+" ")
			.append(buildColumns(fields))
			.append(" FROM ")
			.append(tableName);
			
			if(!join.isEmpty())
			{
				queryBuilder.append(buildJoin(join));
			}
			
			if(!conditions.isEmpty())
			{
				queryBuilder.append(buildCondition(conditions));
			}
			
			queryBuilder.append(buildOrder(order));
			queryBuilder.append(";");
			System.out.println("Query before replacing placeholders : "+queryBuilder);
			
			return QueryExecutor.executeSelect(queryType, queryBuilder, conditions);
		}
		catch(SQLException | InvalidException error)
		{
			System.out.println("Query Layer : "+error.getMessage());
			throw new InvalidException("Error occurred while fetching records!",error);
		}
	}
	
	public int selectCount(String tableName, List<Join> join, Map<Integer,Condition> conditions) throws ClassNotFoundException, InvalidException
	{
		try
		{
			Validator.checkForNull(tableName);
			Validator.checkForNull(join);
			Validator.checkForNull(conditions);
			
			StringBuilder queryBuilder= new StringBuilder();
			String queryType= "SELECT";

			queryBuilder.append(queryType)
			.append(" COUNT(*)")
			.append(" FROM ")
			.append(tableName);
			
			if(!join.isEmpty())
			{
				queryBuilder.append(buildJoin(join));
			}
			
			if(!conditions.isEmpty())
			{
				queryBuilder.append(buildCondition(conditions));
			}
			
			queryBuilder.append(";");
			System.out.println("Query before replacing placeholders : "+queryBuilder);
			
			return QueryExecutor.executeCount(queryType, queryBuilder, conditions);
		}
		catch(SQLException | InvalidException error)
		{
			System.out.println("Query Layer : "+error.getMessage());
			throw new InvalidException("Error occurred while fetching count!", error);
		}
	}

	public <T> int update(List<Fields<T>> newValues, List<Join> join, Map<Integer,Condition> conditions) throws ClassNotFoundException, InvalidException
	{
		try
		{
			Validator.checkForNull(newValues);
			Validator.checkForNull(join);
			Validator.checkForNull(conditions);
			
			String queryType= "UPDATE";
			String tableName= newValues.get(0).getTableName();

			StringBuilder queryBuilder= new StringBuilder();
			queryBuilder.append(queryType+" ")
			.append(tableName);

			if(!join.isEmpty())
			{
				queryBuilder.append(buildJoin(join));
			}
			
			queryBuilder.append(buildSet(newValues));
			
			if(!conditions.isEmpty())
			{
				queryBuilder.append(buildCondition(conditions));
			}
			
			queryBuilder.append(";");
			System.out.println("Query before replacing placeholders : "+queryBuilder);
			
			return QueryExecutor.executeUpdateAndDelete(queryType, queryBuilder, conditions, newValues);
		}
		catch(SQLException | InvalidException error)
		{
			System.out.println("Query Layer : "+error.getMessage());
			throw new InvalidException("Error occurred while updating records!",error);
		}
	}

	public int delete(String tableName, Map<Integer,Condition> conditions) throws InvalidException, ClassNotFoundException
	{
		try
		{
			Validator.checkForNull(tableName);
			Validator.checkForNull(conditions);
			
			String queryType= "DELETE";
			StringBuilder queryBuilder= new StringBuilder();
			
			queryBuilder.append(queryType+" FROM ")
			.append(tableName);
			
			if(!conditions.isEmpty())
			{
				queryBuilder.append(buildCondition(conditions));
				
			}
			queryBuilder.append(";");
			System.out.println("Query before replacing placeholders : "+queryBuilder);
			
			return QueryExecutor.executeUpdateAndDelete(queryType, queryBuilder, conditions, null);
		}
		catch(SQLException | InvalidException error)
		{
			System.out.println("Query Layer : "+error.getMessage());
			throw new InvalidException("Error occurred while removing records!", error);
		}
	}

	private StringBuilder buildJoin(List<Join> join)
	{
		StringBuilder joinBuilder= new StringBuilder();
		
		for(Join iter: join)
		{			
			joinBuilder.append(" JOIN ");
			joinBuilder.append(iter.getReferenceTable()+" ON "+ iter.getTableName()+"."+iter.getFieldName()
			+" = "+iter.getReferenceTable()+"."+iter.getReferenceField());
		}
		return joinBuilder;
	}

	private StringBuilder buildCondition(Map<Integer,Condition> conditions)
	{
		StringBuilder conditionBuilder= new StringBuilder();
		conditionBuilder.append(" WHERE ");
		
		for(Map.Entry<Integer, Condition> entry: conditions.entrySet())
		{
			Condition iter= entry.getValue();
			
			if(iter!= null)
			{
				String conjuctiveOpt = iter.getConjuctiveOpt();
				if (conjuctiveOpt != null && !conjuctiveOpt.isEmpty())
				{
					conditionBuilder.append(conjuctiveOpt+" ");
				}
				
				String operator= iter.getOperator();
				if(operator.equals(" LIKE "))
				{
					conditionBuilder.append("CONVERT ("+iter.getTableName()+"."+iter.getFieldName()+", CHAR)"+operator+" ? ");
				}
				else
				{
					conditionBuilder.append(iter.getTableName()+"."+iter.getFieldName()+operator+"? ");
				}
			}
		}
		
		return conditionBuilder;
	}
	
	private <T> StringBuilder buildSet(List<Fields<T>> newValues)
	{
		StringBuilder setBuilder= new StringBuilder();
		setBuilder.append(" SET ");
		int len= newValues.size();
		boolean appendComma= false;
		for(int i=0;i<len;i++)
		{
			Fields<T> iter = newValues.get(i);
			Map<String,Object> valueSet= iter.getValues();
			int j=0, size= valueSet.size();
			
			if(appendComma && size!=0)
			{
				setBuilder.append(", ");
				appendComma= false;
			}
			
			for(Map.Entry<String, Object> entry: valueSet.entrySet())
			{
				setBuilder.append(entry.getKey()+" = ? ");
				if(++j < size)
				{
					setBuilder.append(", ");
				}
			}
			
			if(i< len-1 && size!=0)
			{
				appendComma= true;
			}
		}
		return setBuilder;
	}
	
	private <T> StringBuilder buildColumns(List<Fields<T>> fields)
	{
		StringBuilder columnBuilder= new StringBuilder();
		int len= fields.size();
		boolean appendComma= false;
		
		for(int i=0;i<len;i++)
		{
			Fields<T> iter= fields.get(i);
			String tableName= iter.getTableName();
			List<String> columnNames= iter.getFieldNames();
			int j=0, size= columnNames.size();
			
			if(appendComma && size!=0)
			{
				columnBuilder.append(", ");
				appendComma= false;
			}
			
			for(String column: columnNames)
			{
				columnBuilder.append(tableName+"."+column);
				
				if(++j < size)
				{
					columnBuilder.append(", ");
				}
			}
			
			if(i< len-1 && size!=0)
			{
				appendComma= true;
			}
		}
		return columnBuilder;
	}
	
	private StringBuilder buildOrder(Order order)
	{
		StringBuilder orderBuilder= new StringBuilder();
		
		List<String> orderBy= order.getOrderBy();
		int limit= order.getLimit();
		int offset= order.getOffset();
		boolean desc= order.isDesc();
		int i=0,size= orderBy.size();
		
		if(!orderBy.isEmpty())
		{
			orderBuilder.append(" ORDER BY ");
			for(String column: orderBy)
			{
				orderBuilder.append(column);
				
				if(++i < size)
				{
					orderBuilder.append(", ");
				}
			}
		}
		
		if(desc)
		{
			orderBuilder.append(" DESC ");
		}
		
		if(limit!=0)
		{
			orderBuilder.append(" LIMIT "+limit);
		}
		
		if(offset!=0)
		{
			orderBuilder.append(" OFFSET "+offset);
		}
		
		return orderBuilder;
	}
}