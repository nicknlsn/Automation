package org.lds.cm.content.automation.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.lds.cm.content.automation.settings.Constants;


public class JDBCUtils {
	
	private static Connection conn = null;
	private static Statement stmt = null;
	private static ResultSet rs = null;
	
	/**
	 * Opens a connection only if it is not open yet
	 */
	private static Connection getConnection() {
		if (conn == null) {
			//System.out.println("Connecting to database...");
			try {
				Class.forName(Constants.dbDriver);
				conn = DriverManager.getConnection(Constants.dbUrl, Constants.dbUsername, Constants.dbPassword);
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return conn;
	}
	 
	/**
	 * Returns the result set of a query, returns null if not a 'select...' query
	 */
	public static ResultSet getResultSet(String query) {
		rs = null;
		if (query.substring(0, 6).equals("select")) {
			conn = getConnection();
			try {
				rs = conn.createStatement().executeQuery(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} 
		return rs;
	}
	
	/**
	 * Used for inserting, updating, or deleting records
	 * @param query
	 * @throws SQLException
	 */
	public static void executeQuery(String query) {
		conn = getConnection();
		try {
			System.out.println("Query: " + query);
			conn.createStatement().executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes resources
	 */
	public static void closeAll() {
		if (conn != null) {
			//System.out.println("Closing connection...");
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (stmt != null) {
			try {
				//System.out.println("Closing Statement...");
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (rs != null) {
			try {
				//System.out.println("Closing result set...");
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}

