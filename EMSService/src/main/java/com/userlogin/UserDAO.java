package com.userlogin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.ems.db.DBConnectionManager;

public class UserDAO {

	static Connection currentCon = null;
	static ResultSet rs = null;

	public static UserBean login(UserBean bean) {
		// preparing some objects for connection
		Statement stmt = null;
		String username = bean.getUsername();
		String password = bean.getPassword();
		String searchQuery = "select * from setup.userlogin where username='" + username + "' AND PASSWORD='" + password
				+ "'";
		// "System.out.println" prints in the console;

		System.out.println("Your user name is " + username);
		System.out.println("Your password is " + password);
		System.out.println("Query: " + searchQuery);
		try {
			// connect to DB
			currentCon = DBConnectionManager.getConnection();
			stmt = currentCon.createStatement();
			rs = stmt.executeQuery(searchQuery);
			boolean more = rs.next();
			// if user does not exist set the isValid variable to false
			if (!more) {
				System.out.println("Sorry, you are not a registered user! Please sign up first");
				bean.setValid(false);
			}
			// if user exists set the isValid variable to true
			else if (more) {
				String firstName = rs.getString("username");
				String lastName = rs.getString("PASSWORD");
				System.out.println("Welcome " + firstName);

				bean.setValid(true);
			}
		} catch (Exception ex) {
			System.out.println("Log In failed: An Exception has occurred! " + ex);
		}
		// some exception handling finally
		{
			if (rs != null) {
				try {

					rs.close();
				} catch (Exception e) {
				}
				rs = null;
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
				}
				stmt = null;
			}
			if (currentCon != null) {
				try {
					currentCon.close();
				} catch (Exception e) {
				}
				currentCon = null;
			}
		}
		return bean;
	}

}
