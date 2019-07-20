package dataserver.database;

import dataserver.domain.Activity;
import dataserver.SystemConstant;
import dataserver.message.Packet;
import dataserver.message.ParameterUpload;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class Transaction {

    private PreparedStatement ps;
    static private Logger log = Logger.getLogger(Transaction.class);

    public Transaction() {
    }
   
    public static String SSN2String(Long ssn) throws Exception {
        /* convert long ssn into 8 bytes array */
        byte[] ssn_bytes = ByteBuffer.allocate(8).putLong(ssn).array();
        String ssn_string = String.format("%02X",ssn_bytes[0])+":"+String.format("%02X",ssn_bytes[1])+":"+String.format("%02X",ssn_bytes[2])+":"+
                String.format("%02X",ssn_bytes[3])+":"+String.format("%02X",ssn_bytes[4])+":"+String.format("%02X",ssn_bytes[5])+":"+
                String.format("%02X",ssn_bytes[6])+":"+String.format("%02X",ssn_bytes[7]);
        
        if (XML_Scanner.IS_inList(ssn_string)){
            /* update actual Gateway Connected */
            return ssn_string;
        }
        
        return null;
    }
    
    public static String GetFromSSN_ParDow_FilePath (Long ssn) throws Exception {
        String ssn_string = SSN2String(ssn);
        /* get paramters from list */
        Gateway_Config conf = XML_Scanner.ScanGatewayConf(ssn_string);
        return conf.FileExport;

    }
    
    public Activity getActivity(Long ssn, int DatabaseV) throws Exception {
        Activity ret = null;
        /* convert long ssn into 8 bytes array */
        byte[] ssn_bytes = ByteBuffer.allocate(8).putLong(ssn).array();
        String ssn_string = String.format("%02X",ssn_bytes[0])+":"+String.format("%02X",ssn_bytes[1])+":"+String.format("%02X",ssn_bytes[2])+":"+
                String.format("%02X",ssn_bytes[3])+":"+String.format("%02X",ssn_bytes[4])+":"+String.format("%02X",ssn_bytes[5])+":"+
                String.format("%02X",ssn_bytes[6])+":"+String.format("%02X",ssn_bytes[7]);
        log.debug("\nSearching activity for SSN = [" + ssn_string + "] ");
        /* check if the SSN is registered */
        if (XML_Scanner.IS_inList(ssn_string)){
            /* update actual Gateway Connected */
            /* ActSSN = ssn_string; */
            Gateway_Config conf = XML_Scanner.ScanGatewayConf(ssn_string);
            /* check if there is new config to upload */
            if (conf.DatabaseV != DatabaseV){
                /* PARAMETER UPLOAD */
                ret = new Activity();
                ret.setId(SystemConstant.COMMAND_TYPE_PARAMETER_UPLOAD);
                ret.setType(SystemConstant.ACTIVITY_TYPE_PARAMETER_UPLOAD);
                log.debug("Returning activity = [" + ret + "]...");
            }
        }
        return ret;
    }
    
    public void logToActivity(Integer id,Integer value) throws Exception {
        /* INSERIRE LOG SU TESTO DELLE ATTIVITA' DEL SERVER */
    }

    public void updateSchema(Long ssn,String schema) throws Exception {
/*        Connection c = null;
        try {
            c = getConnection();
            log.debug("Updating schema for SSN hex = [" + Long.toHexString(ssn) + "]");
            ps = c.prepareStatement("UPDATE firmware SET firmware.SCHEMA_ID= ? WHERE id = (SELECT firmware_id FROM devices WHERE ssn = UPPER(?))");
            ps.setString(1, schema);
            ps.setString(2, Long.toHexString(ssn));
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error(e, e);
            throw e;
        } finally {
            c.close();
        }*/
    }

    public String loadSchema(Long ssn) throws Exception {
        Connection c = null;
        String ret = null;
/*        try {
            c = getConnection();
            ps = c.prepareStatement("SELECT firmware.SCHEMA_ID FROM firmware WHERE id = (SELECT firmware_id FROM devices WHERE ssn = UPPER(?))");
            ps.setString(1, Long.toHexString(ssn));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = rs.getString(1);
                log.debug("Returning schema = [" + ret + "]...");
            }
        } catch (SQLException e) {
            log.error(e, e);
            throw e;
        } finally {
            c.close();
        }*/
        return ret;
    }



    public List<Packet> createParameterUploadMessagesSequence(Long ssn) throws Exception {
        List ret = new ArrayList();

        byte[] ssn_bytes = ByteBuffer.allocate(8).putLong(ssn).array();
        String ssn_string = String.format("%02X", ssn_bytes[0]) + ":" + String.format("%02X", ssn_bytes[1]) + ":" + String.format("%02X", ssn_bytes[2]) + ":" +
                String.format("%02X", ssn_bytes[3]) + ":" + String.format("%02X", ssn_bytes[4]) + ":" + String.format("%02X", ssn_bytes[5]) + ":" +
                String.format("%02X", ssn_bytes[6]) + ":" + String.format("%02X", ssn_bytes[7]);
        log.debug("\n Send Parameters to SSN = [" + ssn_string + "] ");
        /* receive parameters from Parameter List */
        Gateway_Config conf = XML_Scanner.ScanGatewayConf(ssn_string);

        /* Init a Data Array */
        byte[] data = new byte[29];
        /* SSN String to Byte */

        /* Generate Packet Struct */
        data[0] = ssn_bytes[0];
        data[1] = ssn_bytes[1];
        data[2] = ssn_bytes[2];
        data[3] = ssn_bytes[3];
        data[4] = ssn_bytes[4];
        data[5] = ssn_bytes[5];
        data[6] = ssn_bytes[6];
        data[7] = ssn_bytes[7];

        data[8] = (byte) (conf.DatabaseV >> 24);
        data[9] = (byte) (conf.DatabaseV >> 16);
        data[10] = (byte) (conf.DatabaseV >> 8);
        data[11] = (byte) (conf.DatabaseV);
        data[12] = (byte) conf.Timer1;
        data[13] = (byte) conf.Timer2;
        data[14] = (byte) conf.Timer3;
        data[15] = (byte) conf.Timer4;
        data[16] = (byte) conf.Timer5;
        data[17] = (byte) conf.Timer6;
        data[18] = (byte) conf.Timer7;
        data[19] = (byte) conf.Timer8;
        data[20] = (byte) conf.Timer9;
        data[21] = (byte) conf.Timer10;
        data[22] = (byte) conf.StatNod;
        data[23] = (byte) conf.StatGat;
        data[24] = (byte) conf.MedSens;
        data[25] = (byte) conf.TimerS;
        data[26] = (byte) conf.LimiteTX;
        data[27] = (byte) conf.FinestraStat;
        data[28] = (byte) conf.AttRete;

        /* Generate the Parameter Upload Packet */
        ParameterUpload m = new ParameterUpload(data);
        ret.add(m);
        
        return ret;
    }
}