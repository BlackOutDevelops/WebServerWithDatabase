package com.example.project3;// Very basic JDBC example showing loading of JDBC driver, establishing connection
// creating a statement, executing a simple SQL query, and displaying the results.
// This version uses the implicit driver loading using a MySQLDataSource object
import java.sql.*;
import com.mysql.cj.jdbc.MysqlDataSource;

public class SimpleJDBC2 {
  public static void main(String[] args)
    throws SQLException, ClassNotFoundException {
	  System.out.println("Output from SimpleJDBC2:   Using an implicit driver load via a MySQLDataSource object.");
	  java.util.Date date = new java.util.Date();
	  System.out.println(date);  System.out.println();		 
	
	//not using a properties file so must include these statements defining the MySQLDataSource object parameters
	MysqlDataSource dataSource = new MysqlDataSource();
	dataSource.setUser("root");
	dataSource.setPassword("Lifeiscrazy19!");
	dataSource.setURL("jdbc:mysql://localhost:3306/project3?useTimezone=true&serverTimezone=UTC");
	  
	//establish a connection to the dataSource - i.e. the database
	Connection connection = dataSource.getConnection();
    System.out.println("Database connected");
	 
	 DatabaseMetaData dbMetaData = connection.getMetaData();
	 System.out.println("JDBC Driver name " + dbMetaData.getDriverName() );
	 System.out.println("JDBC Driver version " + dbMetaData.getDriverVersion());
	 System.out.println("Driver Major version " +dbMetaData.getDriverMajorVersion());
	 System.out.println("Driver Minor version " +dbMetaData.getDriverMinorVersion() );
	 System.out.println();

    // Create a statement object
	Statement statement = connection.createStatement();
	
    // Execute a query using the statement object and get a result returned from DB server
    ResultSet resultSet = statement.executeQuery ("select bikename,cost,mileage from bikes");

    // Iterate through the result set and print the returned results
    System.out.println("Results of the Query: . . . . . . . . . . . . . . . . . . . . . . . . . . . . .\n");
    while (resultSet.next())
      System.out.println(resultSet.getString("bikename") + "         \t" +
        resultSet.getString("cost") + "         \t" + resultSet.getString("mileage"));
		//the following print statement works exactly the same  
      //System.out.println(resultSet.getString(1) + "         \t" +
      //  resultSet.getString(2) + "         \t" + resultSet.getString(3));
     System.out.println();  System.out.println();
    // Close the connection
    connection.close();
  }
}

