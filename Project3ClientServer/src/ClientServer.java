/*
Name: Rebecca Pina Partidas
Course: CNT 4714 Fall 2023
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: October 29, 2023
Class: ClientServer
*/

// Necessary libraries
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import com.mysql.cj.jdbc.MysqlDataSource;

public class ClientServer extends JFrame{
	
	// GUI variables
	private JLabel connectDetailLabel, DBURLPropLabel, userPropLabel, usernameLabel, passwordLabel, enterComLabel, connectedToLabel, resultLabel;
	private JTextArea commandTextArea;
	private JTextField usernameTextField;
	private JButton connectButton, clearCommandButton, executeButton, clearResultButton;
	private JComboBox DBURLProp, userProp;
	private JPasswordField usrPassField;
	private JTable resultTable;
	
	private Connection connection;
	
	// Reference variables for event handlers
	private connectButtonHandler connectBHandler;
	private clearCommandButtonHandler clearComBHandler;
	private executeButtonHandler executeBHandler;
	private clearResultButtonHandler clearResBHandler;
	
	private ResultSetTableModel tableModel;
	private TableModel empty;
	
	public ClientServer() {
		
		String[] DBPropertiesItems = {"project3db.properties", "bikedb.properties"};
    	String[] usrPropertiesItems = {"root.properties", "client1.properties", "client2.properties"};
		
		// Centers frame on user's screen with defined height and width
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screen = toolkit.getScreenSize();
		
		int x = (screen.width - 700)/2;
		int y = (screen.height - 500)/2;
		
		setBounds(x, y, 700, 500);
		
		// Resets layout
		setLayout(null);
		
		// Sets title of frame
		setTitle("SQL Client Application - (RP - CNT 4714 - Fall 2023 - Project 3)");
		
		// Instantiates and set JLabel objects
		connectDetailLabel = new JLabel("\t\tConnection Details");
		DBURLPropLabel = new JLabel("\t\tDB URL Properties");
		userPropLabel = new JLabel("\t\tUser Properties");
		usernameLabel = new JLabel("\t\tUsername");
		passwordLabel = new JLabel("\t\tPassword");
		enterComLabel = new JLabel("\t\tEnter a SQL Command");
		connectedToLabel = new JLabel("\t\tNO CONNECTION NOW");
		resultLabel = new JLabel("\t\tSQL Execution Result Window");
		
		// Instantiates and set JTextField/JTextArea/JComboBox/JPasswordField objects
		usernameTextField = new JTextField();
		
		commandTextArea = new JTextArea();
		
		DBURLProp = new JComboBox(DBPropertiesItems);
		
		userProp = new JComboBox(usrPropertiesItems);
		
		usrPassField = new JPasswordField();
		
		// Instantiates JTable and an empty DefaultTableModel
		// Places JTable in box
		resultTable = new JTable();
		empty = new DefaultTableModel();
		final Box square = Box.createHorizontalBox();
		square.add(new JScrollPane(resultTable));
		resultTable.setEnabled(false);
		resultTable.setGridColor(Color.black);
		
		// Instantiates and set JButton objects
		// Registers handlers
		connectButton = new JButton("Connect to Database");
		connectBHandler = new connectButtonHandler();
		connectButton.addActionListener(connectBHandler);
		
		clearCommandButton = new JButton("Clear SQL Command");
		clearComBHandler = new clearCommandButtonHandler();
		clearCommandButton.addActionListener(clearComBHandler);
		
		executeButton = new JButton("Execute SQL Command");
		executeBHandler = new executeButtonHandler();
		executeButton.addActionListener(executeBHandler);
		
		clearResultButton = new JButton("Clear Result Window");
		clearResBHandler = new clearResultButtonHandler();
		clearResultButton.addActionListener(clearResBHandler);
		
		connectDetailLabel.setForeground(Color.blue);
		connectDetailLabel.setBounds(0, 0, 150, 20);
		add(connectDetailLabel);
		
		enterComLabel.setForeground(Color.blue);
		enterComLabel.setBounds(300, 0, 150, 20);
		add(enterComLabel);
		
		DBURLPropLabel.setBounds(0, 25, 150, 20);
		add(DBURLPropLabel);
		
		DBURLProp.setBounds(125, 25, 150, 20);
		add(DBURLProp);
		
		// Sets bounds for all elements and adds them to frame
		// Places commandTextArea into a box
		Box sqlCommand = Box.createHorizontalBox();
		sqlCommand.add(new JScrollPane(commandTextArea));
		sqlCommand.setBounds(305, 25, 340, 100);
		add(sqlCommand);
		
		userPropLabel.setBounds(0, 50, 150, 20);
		add(userPropLabel);
		
		userProp.setBounds(125, 50, 150, 20);
		add(userProp);
		
		usernameLabel.setBounds(0, 75, 150, 20);
		add(usernameLabel);
		
		usernameTextField.setBounds(125, 75, 150, 20);
		add(usernameTextField);
		
		passwordLabel.setBounds(0, 100, 150, 20);
		add(passwordLabel);
		
		usrPassField.setBounds(125, 100, 150, 20);
		add(usrPassField);
		
		connectButton.setBounds(65, 150, 150, 20);
		add(connectButton);
		
		clearCommandButton.setBounds(315, 150, 150, 20);
		add(clearCommandButton);
		
		executeButton.setBounds(485, 150, 150, 20);
		add(executeButton);
		
		connectedToLabel.setOpaque(true);
		connectedToLabel.setBackground(Color.black);
		connectedToLabel.setForeground(Color.red);
		connectedToLabel.setBounds(10, 190, 650, 20);
		add(connectedToLabel);
		
		resultLabel.setForeground(Color.blue);
		resultLabel.setBounds(0, 230, 200, 20);
		add(resultLabel);
		
		square.setBounds(10, 250, 650, 170);
		add(square);
		
		clearResultButton.setBounds(10, 425, 150, 20);
		add(clearResultButton);
	}

	private class connectButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Gets properties file info from input
			String DBURLInfo = (String) DBURLProp.getSelectedItem();
			String UserInfo = (String) userProp.getSelectedItem();
			// Gets username and password provided by user
			String usrNameInput = usernameTextField.getText();
			String pswInput = String.valueOf(usrPassField.getPassword());		
			
			// Initialization for reading in properties files and placing information in data source
			Properties connectProperties = new Properties();
			Properties userProperties = new Properties();
			FileInputStream connectFilein = null;
			FileInputStream userFilein = null;
			MysqlDataSource dataSource = null;
			
	        //read properties file
		    try {
		    	connectFilein = new FileInputStream(DBURLInfo);
		    	connectProperties.load(connectFilein);
		    	dataSource = new MysqlDataSource();
		    	
		    	userFilein = new FileInputStream(UserInfo);
		    	userProperties.load(userFilein);
		    	
		    	// Gets correct username and password from properties files with proper url for the corresponding database that was chosen
		    	String reqUsr = userProperties.getProperty("MYSQL_DB_USERNAME");
		    	String reqPass = userProperties.getProperty("MYSQL_DB_PASSWORD");
		    	String url = connectProperties.getProperty("MYSQL_DB_URL");
		    	
		    	// User authentication in order to make connection
		    	if (reqUsr.equals(usrNameInput) && reqPass.equals(pswInput)) {
		    		dataSource.setURL(connectProperties.getProperty("MYSQL_DB_URL"));
		    		dataSource.setUser(userProperties.getProperty("MYSQL_DB_USERNAME"));
			    	dataSource.setPassword(userProperties.getProperty("MYSQL_DB_PASSWORD")); 
		    		connection = dataSource.getConnection();
		    		connectedToLabel.setForeground(Color.yellow);
		    		connectedToLabel.setText("\tCONNECTED TO: " + url);
		    	}
		    	else {
		    		connectedToLabel.setText("NOT CONNECTED - User Credentials Do Not Match Properties File!");
		    	}
		    	
			}
		    catch ( SQLException sqlException ) {
		         sqlException.printStackTrace();
		         JOptionPane.showMessageDialog(null, sqlException.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
		    } // end catch
		    catch (IOException e1) {
		   	     e1.printStackTrace();
		   	     JOptionPane.showMessageDialog(null, e1.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
		    }  
			
		}
	}
	
	private class clearCommandButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Empties text area for command
			commandTextArea.setText("");
		}
	}
	
	private class executeButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Variable initialization
			int result = 0;
			String updateTxt = "";
			
			// Gets user input from command area
			String commandInp = commandTextArea.getText();
			
			try {
				// Creates new table model object with connection to database
				resultTable.setEnabled(true);
				resultTable.setAutoscrolls(true);
		    	tableModel = new ResultSetTableModel(connection, commandInp);
		    	
		    	// Sets query if command starts with "SELECT"
		    	if (commandInp.toUpperCase().contains("SELECT")) {
		    		tableModel.setQuery(commandInp);
		    		// Sets result table
		    		resultTable.setModel(tableModel);
		    	}
		    	// Sets update for other commands
		    	else {
		    		// Gets how many rows were updated with command input
		    		result = tableModel.setUpdate(commandInp);
		    		
		    		// If rows were updated, it was a successful update and outputs metadata
		    		if (result > 0) {
			    		updateTxt = "Successful Update... " + result + " rows updated.";
			    		JOptionPane.showMessageDialog(null, updateTxt, "Successful Update", JOptionPane.INFORMATION_MESSAGE);
		    		}
		    		// If no rows were updated, it was an unsuccessful update
		    		else if (result == 0) {
		    			updateTxt = "Unsuccessful Update... " + result + " rows updated.";
			    		JOptionPane.showMessageDialog(null, updateTxt, "Unsuccessful Update", JOptionPane.ERROR_MESSAGE);
		    		}
		    	}
		    	
			}
		    catch (SQLException sqlException) {
		        sqlException.printStackTrace();
		        JOptionPane.showMessageDialog(null, sqlException.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
		    } // end catch
		    catch (ClassNotFoundException NotFound) {
		    	NotFound.printStackTrace();
		   	    JOptionPane.showMessageDialog(null, "MySQL driver not found", "Driver Not Found", JOptionPane.ERROR_MESSAGE);
		    }  
		}
	}
	
	private class clearResultButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Empties result table for blank space of result area
			resultTable.setModel(empty);
		}
	}
	
	public static void main(String[] args) {
		// Creates frame object and shows GUI
		JFrame clientServer = new ClientServer();
		clientServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		clientServer.setVisible(true);
	}

}
