import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by MAXNIGELNEGRO 
 */
public class ExportData2CSV {
    public ResultSet rset;
    public String filename;
    public Boolean colomnName;
    public String charSep;

    public void ExportData2CSV(ResultSet rset, String filename, Boolean colomnName, String charSep) {
        this.rset = rset;
        this.filename = filename;
        this.colomnName = colomnName;
        this.charSep = charSep;
    }

    public void createFileCsv() throws SQLException, IOException {
        FileWriter cname = null;
        try {

                // WRITE COLOMN NAME
            ResultSetMetaData rsmd = rset.getMetaData();
            cname = new FileWriter(filename);
            if (colomnName) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    cname.append(rsmd.getColumnName(i));
                    cname.append(charSep);
                    cname.flush();
                }
                cname.append(System.getProperty("line.separator"));
            }

            // WRITE DATA
            while (rset.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    if (rset.getObject(i) != null) {
                        String data = rset.getObject(i).toString().replaceAll(charSep, "");
                        cname.append(data);
                        cname.append(charSep);
                    } else {
                        String data = "null";
                        cname.append(data);
                        cname.append(charSep);
                    }

                }
                //new line entered after each row
                cname.append(System.getProperty("line.separator"));

            }


        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (cname != null) {
                cname.flush();
                cname.close();
            }
            if (rset != null) {
                rset.close();
            }
        }
    }
}