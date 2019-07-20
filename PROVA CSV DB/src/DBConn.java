import java.sql.*;

	public class DBConn {
		
	    public DBConn() {
	    }
	    
	    public Connection connect(String driverDB, String db_connect_str, String db_userid, String db_password) {
	        Connection conn;

	        try {
	            Class.forName(driverDB).newInstance();
	            conn = DriverManager.getConnection(db_connect_str, db_userid, db_password);


	        } catch (Exception e) {
	            e.printStackTrace();
	            conn = null;

	        }
	        return conn;
	    }
	}


