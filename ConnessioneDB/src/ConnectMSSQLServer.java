import java.sql.*;
import com.microsoft.sqlserver.jdbc.*;

public class ConnectMSSQLServer
{
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		// Create a variable for the connection string.
		String connectionUrl = "jdbc:sqlserver://172.16.91.14:1433;" + "databaseName=BERA7;user=sa;password=ber-sql-sa";

		// Declare the JDBC objects.
		Connection con = null;
		//Statement stmt = null;
		//ResultSet rs = null;
		// Establish the connection.
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    	con = DriverManager.getConnection(connectionUrl);
    	String codiceOld="UR0000325F001";		//impostare il codice da sostituire
    	executeTransaction(con, codiceOld);
    		
	}
	
	public static void executeTransaction(Connection con, String codiceOld) {
	    try {
	        //Switch to manual transaction mode by setting
	        //autocommit to false. Note that this starts the first manual transaction.
	    	con.setAutoCommit(false);

    		System.out.println("--- LIFO ---");
    		PreparedStatement stmt = con.prepareStatement("UPDATE BERA7.dbo.MG89_LIFOART     SET MG89_CODART_MG66 = ? WHERE MG89_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld); stmt.setString(2, codiceOld);
    		int rowsAffected = stmt.executeUpdate();		//righe modificate dal preparedStatement stmt
    		System.out.println("rowsAffected "+ rowsAffected);
    		
    		stmt = con.prepareStatement("UPDATE VIBO7.dbo.MG89_LIFOART     SET MG89_CODART_MG66 = ? WHERE MG89_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld); stmt.setString(2, codiceOld);
    		rowsAffected = stmt.executeUpdate();
    		System.out.println("rowsAffected "+ rowsAffected);
    		
    		stmt = con.prepareStatement("UPDATE VITM7.dbo.MG89_LIFOART     SET MG89_CODART_MG66 = ? WHERE MG89_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld); stmt.setString(2, codiceOld);
    		rowsAffected = stmt.executeUpdate();
    		System.out.println("rowsAffected "+ rowsAffected);
    		
    		
    		System.out.println("--- MG1Q_ARTNOMENCLATURE ---");
    		stmt = con.prepareStatement("DELETE from BERA7.dbo.MG1Q_ARTNOMENCLATURE     WHERE MG1Q_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		rowsAffected = stmt.executeUpdate();
    		System.out.println("rowsAffected "+ rowsAffected);
    		
    		stmt = con.prepareStatement("DELETE from VIBO7.dbo.MG1Q_ARTNOMENCLATURE     WHERE MG1Q_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		rowsAffected = stmt.executeUpdate();
    		System.out.println("rowsAffected "+ rowsAffected);
    		
    		stmt = con.prepareStatement("DELETE from VITM7.dbo.MG1Q_ARTNOMENCLATURE     WHERE MG1Q_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		rowsAffected = stmt.executeUpdate();
    		System.out.println("rowsAffected "+ rowsAffected);

    		
    		System.out.println("--- MG43_ARTPERS ---");
    		stmt = con.prepareStatement("DELETE from BERA7.dbo.MG43_ARTPERS      	   where MG43_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		rowsAffected = stmt.executeUpdate();
    		System.out.println("rowsAffected "+ rowsAffected);
    		
    		stmt = con.prepareStatement("DELETE from VIBO7.dbo.MG43_ARTPERS      	   where MG43_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		rowsAffected = stmt.executeUpdate();
    		System.out.println("rowsAffected "+ rowsAffected);
    		
    		stmt = con.prepareStatement("DELETE from VITM7.dbo.MG43_ARTPERS      	   where MG43_CODART_MG66 = ? \r\n");
    		stmt.setString(1, codiceOld);
    		rowsAffected = stmt.executeUpdate();
    		System.out.println("rowsAffected "+ rowsAffected);
    		
    		
    		System.out.println("--- PD27_ANAFORMULE ---");
    		stmt = con.prepareStatement("DELETE FROM BERA7.dbo.PD27_ANAFORMULE \r\n");
    		rowsAffected = stmt.executeUpdate();
    		System.out.println("rowsAffected "+ rowsAffected);
    		con.commit();
    		
    		SQLWarning warning = stmt.getWarnings();
    		System.out.println(warning);
    		while (warning != null)
    		{
    		   System.out.println(warning.getMessage());
    		   warning = warning.getNextWarning();
    		   System.out.println(warning);
    		}
    		
    		stmt.close();
	        System.out.println("Transazione avvenuta con successo.");
	    }
	    catch (SQLException ex) {
	        ex.printStackTrace();
	        try {
	            System.out.println("Transazione fallita.");
	            con.rollback();
	        }
	        catch (SQLException se) {
	            se.printStackTrace();
	        }
	    }
	}
	
}