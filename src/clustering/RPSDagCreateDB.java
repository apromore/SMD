package clustering;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class RPSDagCreateDB {
	
	public static void initDB(String user, String passw, String db) throws Exception {	
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://localhost:3306/"+db;
		Connection con = DriverManager.getConnection(url, user, passw);
		
		dropTable(con, "roots");
		dropTable(con, "labelhash");
		dropTable(con, "parentChildMap");
		
		Statement stmt = con.createStatement();
		stmt.executeUpdate("CREATE TABLE roots (modelname varchar(100) primary key, nodeid int)");
		stmt.executeUpdate("CREATE TABLE labelhash (hash text, nodeid int auto_increment primary key, size int)");
		stmt.executeUpdate("CREATE TABLE parentChildMap (parent int, child int)");
		
		stmt.close();
		con.close();
	}

	private static void dropTable(Connection con, String tname) {
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("drop table " + tname);
		} catch (SQLException e) {
		}
	}
}
