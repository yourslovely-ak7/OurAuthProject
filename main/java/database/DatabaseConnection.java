package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import exception.InvalidException;

public class DatabaseConnection 
{
	private static final String URL = "jdbc:mysql://localhost:3306/accounts";
    private static final String USER = "athi-pt7617";
    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException, InvalidException {
    	try
    	{
    		Class.forName("com.mysql.cj.jdbc.Driver");
    		return DriverManager.getConnection(URL, USER, PASSWORD);    		
    	}
    	catch (ClassNotFoundException error) {
			System.out.println(error.getMessage());
			throw new InvalidException("Error while getting Connection!", error);
		}
    }
}
