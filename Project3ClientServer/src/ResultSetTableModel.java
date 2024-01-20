/*
Name: Rebecca Pina Partidas
Course: CNT 4714 Fall 2023
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: October 29, 2023
Class: ResultSetTableModel
*/

// Necessary libraries
import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.Properties;
import javax.sql.DataSource;
import com.mysql.cj.jdbc.MysqlDataSource;

public class ResultSetTableModel extends AbstractTableModel {
	   // Variables necessary for connection and creation of ResultSetTableModel
	   private Connection connection;
	   private Connection operationsConnection;
	   private Statement statement;
	   private ResultSet resultSet;
	   private ResultSetMetaData metaData;
	   private int numberOfRows;

	   // keep track of database connection status
	   private boolean connectedToDatabase = false;
	   
	   // constructor initializes resultSet and obtains its meta data object;
	   // determines number of rows
	   public ResultSetTableModel(Connection incomingConnection, String query ) 
	      throws SQLException, ClassNotFoundException
	   {         
		   
		    	this.connection = incomingConnection;
		    	
	            // create Statement to query database
	            this.statement = incomingConnection.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );

	            // update database connection status
	            connectedToDatabase = true;
	   }

	   // get class that represents column type
	   public Class getColumnClass( int column ) throws IllegalStateException
	   {
	      // ensure database connection is available
	      if ( !connectedToDatabase ) 
	         throw new IllegalStateException( "Not Connected to Database" );

	      // determine Java class of column
	      try 
	      {
	         String className = metaData.getColumnClassName( column + 1 );
	         
	         // return Class object that represents className
	         return Class.forName( className );
	      } 
	      catch ( Exception exception ) 
	      {
	         exception.printStackTrace();
	      } 
	      
	      // if problems occur above, assume type Object
	      return Object.class; 
	   } 

	   // get number of columns in ResultSet
	   public int getColumnCount() throws IllegalStateException
	   {   
	      // ensure database connection is available
	      if ( !connectedToDatabase ) 
	         throw new IllegalStateException( "Not Connected to Database" );

	      // determine number of columns
	      try 
	      {
	         return metaData.getColumnCount(); 
	      } 
	      catch ( SQLException sqlException ) 
	      {
	         sqlException.printStackTrace();
	      } 
	      
	      // if problems occur above, return 0 for number of columns
	      return 0; 
	   } 

	   // get name of a particular column in ResultSet
	   public String getColumnName( int column ) throws IllegalStateException
	   {    
	      // ensure database connection is available
	      if ( !connectedToDatabase ) 
	         throw new IllegalStateException( "Not Connected to Database" );

	      // determine column name
	      try 
	      {
	         return metaData.getColumnName( column + 1 );  
	      } 
	      catch ( SQLException sqlException ) 
	      {
	         sqlException.printStackTrace();
	      } 
	      
	      // if problems, return empty string for column name
	      return ""; 
	   } 

	   // return number of rows in ResultSet
	   public int getRowCount() throws IllegalStateException
	   {      
	      // ensure database connection is available
	      if ( !connectedToDatabase ) 
	         throw new IllegalStateException( "Not Connected to Database" );
	 
	      return numberOfRows;
	   } 

	   // obtain value in particular row and column
	   public Object getValueAt( int row, int column ) 
	      throws IllegalStateException
	   {
	      // ensure database connection is available
	      if ( !connectedToDatabase ) 
	         throw new IllegalStateException( "Not Connected to Database" );

	      // obtain a value at specified ResultSet row and column
	      try 
	      {
			   resultSet.next();  /* fixes a bug in MySQL/Java with date format */
	         resultSet.absolute( row + 1 );
	         return resultSet.getObject( column + 1 );
	      } // end try
	      catch ( SQLException sqlException ) 
	      {
	         sqlException.printStackTrace();
	      } 
	      
	      // if problems, return empty string object
	      return ""; 
	   } 
	   
	   // set new database query string
	   public void setQuery( String query ) 
	      throws SQLException, IllegalStateException 
	   {
	      // ensure database connection is available
	      if ( !connectedToDatabase ) 
	         throw new IllegalStateException( "Not Connected to Database" );

	      // specify query and execute it
	      resultSet = statement.executeQuery( query );

	      // obtain meta data for ResultSet
	      metaData = resultSet.getMetaData();

	      // determine number of rows in ResultSet
	      resultSet.last();                   // move to last row
	      numberOfRows = resultSet.getRow();  // get row number      
	      
	      // notify JTable that model has changed
	      fireTableStructureChanged();
	      
	      // Initialization for reading in properties file for operationsLog and placing information in data source
	      Properties connectProperties = new Properties();
		  FileInputStream operationsFilein = null;
		  MysqlDataSource dataSource = null;
		  DatabaseMetaData curDBMeta;
		  String curUsr = "";
		  ResultSet checkUsr = null;
		  
		  //read properties file
		  try {
		    	operationsFilein = new FileInputStream("project3app.properties");
		    	connectProperties.load(operationsFilein);
		    	dataSource = new MysqlDataSource();
		    	
		    	// Gets correct username and password from properties file with proper url for the operationsLog database
		    	dataSource.setURL(connectProperties.getProperty("MYSQL_DB_URL"));
	    		dataSource.setUser(connectProperties.getProperty("MYSQL_DB_USERNAME"));
		    	dataSource.setPassword(connectProperties.getProperty("MYSQL_DB_PASSWORD")); 
		    	
		    	// Create connection
	    		operationsConnection = dataSource.getConnection();
	    		
	    		// Get metadata from connection
	    		curDBMeta = connection.getMetaData();
	    		
	    		// Get current username being used
	    		curUsr = curDBMeta.getUserName();
	    		
	    		// Create prepared statements for whichever user is currently being used
	    		// Checks if a user has a spot in the operationscount table
	    		String stringCheck = "SELECT * FROM operationscount WHERE login_username = ?;";
	    		// Inserts new user into operationscount table if it doesn't exist
	    		String stringMakeUsr = "INSERT INTO operationscount VALUES (?, 1, 0);";
	    		// Adds 1 to numqueries for an existing user
	    		String stringUpdateUsr = "UPDATE operationscount SET num_queries = (num_queries + 1) where login_username = ?;";
	    		
	    		PreparedStatement prpStmtCheck = operationsConnection.prepareStatement(stringCheck);
	    		PreparedStatement prpStmtMakeUsr = operationsConnection.prepareStatement(stringMakeUsr);
	    		PreparedStatement prpStmtUpdateUsr = operationsConnection.prepareStatement(stringUpdateUsr);
	    		
	    		// Sets user to ?
	    		prpStmtCheck.setString(1, curUsr);
	    		prpStmtMakeUsr.setString(1, curUsr);
	    		prpStmtUpdateUsr.setString(1, curUsr);
	    		
	    		checkUsr = prpStmtCheck.executeQuery();
	    		
	    		// If something gets returned for checkUsr, a user already exists and an update should be made
	    		if (checkUsr.next()) {
	    			prpStmtUpdateUsr.executeUpdate();
	    		}
	    		// If something doesn't get returned for checkUsr, a user does not exist and an insert should be made
	    		else {
	    			prpStmtMakeUsr.executeUpdate();
	    		}
	    		
	    		// Close connection
	    		operationsConnection.close();  
		    	
		  }
		  catch ( SQLException sqlException ) {
		         sqlException.printStackTrace();
		         JOptionPane.showMessageDialog(null, sqlException.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
		  }
		  catch (IOException e1) {
		   	     e1.printStackTrace();
		   	     JOptionPane.showMessageDialog(null, e1.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
		  }
	   } 


	// set new database update-query string
	   public int setUpdate( String query ) 
	      throws SQLException, IllegalStateException 
	   {
		  int res;
		  
	      // ensure database connection is available
	      if ( !connectedToDatabase ) 
	         throw new IllegalStateException( "Not Connected to Database" );

	      // specify query and execute it
	      res = statement.executeUpdate( query );   
	   
	      // notify JTable that model has changed
	      fireTableStructureChanged();
	      
	      if (res != 0) {
	    	  // Initialization for reading in properties file for operationsLog and placing information in data source
		      Properties connectProperties = new Properties();
			  FileInputStream operationsFilein = null;
			  MysqlDataSource dataSource = null;
			  DatabaseMetaData curDBMeta;
			  String curUsr = "";
			  ResultSet checkUsr = null;
		  
			  //read properties file
			  try {
			    	operationsFilein = new FileInputStream("project3app.properties");
			    	connectProperties.load(operationsFilein);
			    	dataSource = new MysqlDataSource();
			    	
			    	// Gets correct username and password from properties file with proper url for the operationsLog database
			    	dataSource.setURL(connectProperties.getProperty("MYSQL_DB_URL"));
		    		dataSource.setUser(connectProperties.getProperty("MYSQL_DB_USERNAME"));
			    	dataSource.setPassword(connectProperties.getProperty("MYSQL_DB_PASSWORD")); 
			    	
			    	// Create connection
		    		operationsConnection = dataSource.getConnection();
		    		
		    		// Get metadata from connection
		    		curDBMeta = connection.getMetaData();
		    		
		    		// Get current username being used
		    		curUsr = curDBMeta.getUserName();
		    		
		    		// Create prepared statements for whichever user is currently being used
		    		// Checks if a user has a spot in the operationscount table
		    		String stringCheck = "SELECT * FROM operationscount WHERE login_username = ?;";
		    		// Inserts new user into operationscount table if it doesn't exist
		    		String stringMakeUsr = "INSERT INTO operationscount VALUES (?, 0, 1);";
		    		// Adds 1 to numupdates for an existing user
		    		String stringUpdateUsr = "UPDATE operationscount SET num_updates = (num_updates + 1) where login_username = ?;";
		    		
		    		PreparedStatement prpStmtCheck = operationsConnection.prepareStatement(stringCheck);
		    		PreparedStatement prpStmtMakeUsr = operationsConnection.prepareStatement(stringMakeUsr);
		    		PreparedStatement prpStmtUpdateUsr = operationsConnection.prepareStatement(stringUpdateUsr);
		    		
		    		// Sets user to ?
		    		prpStmtCheck.setString(1, curUsr);
		    		prpStmtMakeUsr.setString(1, curUsr);
		    		prpStmtUpdateUsr.setString(1, curUsr);
		    		
		    		checkUsr = prpStmtCheck.executeQuery();
		    		
		    		// If something gets returned for checkUsr, a user already exists and an update should be made
		    		if (checkUsr.next()) {
		    			prpStmtUpdateUsr.executeUpdate();
		    		}
		    		// If something doesn't get returned for checkUsr, a user does not exist and an insert should be made
		    		else {
		    			prpStmtMakeUsr.executeUpdate();
		    		}
		    		
		    		// Close connection
		    		operationsConnection.close();  
			    	
			  }
			  catch ( SQLException sqlException ) {
			         sqlException.printStackTrace();
			         JOptionPane.showMessageDialog(null, sqlException.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
			  } 
			  catch (IOException e1) {
			   	     e1.printStackTrace();
			   	     JOptionPane.showMessageDialog(null, e1.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
			  }
		  }
	      
	      // Return how many rows were updated
	      return res;
	   } 

	   // close Statement and Connection               
	   public void disconnectFromDatabase()            
	   {              
	      if ( !connectedToDatabase )                  
	         return;
	      // close Statement and Connection            
	      else try                                          
	      {                                            
	         statement.close();                        
	         connection.close();                       
	      }                                 
	      catch ( SQLException sqlException )          
	      {                                            
	         sqlException.printStackTrace();           
	      }                              
	      finally  // update database connection status
	      {                                            
	         connectedToDatabase = false;              
	      }                            
	   }        
} 


