import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class exportQueryToCsv {

	public exportQueryToCsv(){}
    public static void  exportQueryToCsv (String string, String[] driverDB, String[] connDB
                                            , String[] userDB, String[] passDB, String[] queryDB, Boolean intestaFile
                                            , String[] charSep) throws SQLException, IOException {
        Statement stmt=null;
        ResultSet rset=null;
        Connection conn=null;
        try { DBConn connessione = new DBConn();
        conn=connessione.connect(driverDB[0],connDB[0],userDB[0],passDB[0]);
        conn.setAutoCommit(false);

        stmt = conn.createStatement();

        rset = stmt.executeQuery(queryDB[0]);

        ExportData2CSV csv = new ExportData2CSV();
        csv.ExportData2CSV(rset,string,intestaFile,charSep[0]);

            csv.createFileCsv();
        } catch (SQLException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
        finally {
            if (stmt != null) {stmt.close();}
            if (conn != null) {conn.close();}
            if (rset != null) {rset.close();}



        }


    }

}
