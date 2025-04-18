package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import exception.InternalException;

public class DatabaseConnection 
{
	private static final String URL = "jdbc:mysql://localhost:3306/accounts";
    private static final String USER = "athi-pt7617";
    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException, InternalException {
    	try
    	{
    		Class.forName("com.mysql.cj.jdbc.Driver");
    		return DriverManager.getConnection(URL, USER, PASSWORD);    		
    	}
    	catch (ClassNotFoundException error) {
			System.out.println(error.getMessage());
			throw new InternalException("Error while getting Connection!", error);
		}
    }
}
