/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dataserver.message;

import java.nio.ByteBuffer;
//import java.util.ArrayList;
//import java.util.List;
import dataserver.SystemConstant;
//import java.util.Calendar;
import java.util.*;

/**
 *
 * @author MediCon
 */

public class TimeUpload extends Packet{
    
    private static int GetSystemTimestamp() {
        
        Calendar cal = Calendar.getInstance();
        long milliDiff = cal.get(Calendar.ZONE_OFFSET);
        /* check dft */
        if (TimeZone.getDefault().inDaylightTime( new Date() )){
            milliDiff = milliDiff + 3600000;
        }
        /* Timestamp with local timezone */
        log.debug("System timestamp timezone in s: " 
                + (System.currentTimeMillis() + milliDiff)/1000);
        
        return (int)((System.currentTimeMillis() + milliDiff)/1000);
    }
    
    public long OffsetSystemTimestamp(long timestamp) {
        if (TimeZone.getDefault().inDaylightTime( new Date() )){
            timestamp = timestamp - 3600;
        }
        
        Calendar cal = Calendar.getInstance();
        long milliDiff = cal.get(Calendar.ZONE_OFFSET);
        timestamp = timestamp - (milliDiff/1000);
        
        return timestamp;
    }
    
    private class PktTimeUpload extends Packet {

        public PktTimeUpload(byte[] data, byte[] ssn_bytes) {
            int dataLen = data.length;
        
            /* Modifica per l'aumento a 2 byte per il campo lunghezza del parameterUpload
            */ 
            buffer = new byte[13 + dataLen];
            /* p1 */
            set8(0, SystemConstant.COMMAND_TYPE_TIME_UPDATE);
            /* p2 */
            //set8(1, 0);
            /* p3 */
            set16(1,4);
            /* p4 */  
            System.arraycopy(ssn_bytes, 0, buffer, 3, 8);
            System.arraycopy(data, 0, buffer, 11, dataLen);
            /* crc */
            addCRC16();
        }
    }
    
    public List<Packet> GenTimeUpload(Long ssn) throws Exception {
        List ret = new ArrayList();

        byte[] ssn_bytes = ByteBuffer.allocate(8).putLong(ssn).array();
        String ssn_string = String.format("%02X", ssn_bytes[0]) + ":" + String.format("%02X", ssn_bytes[1]) + ":" + String.format("%02X", ssn_bytes[2]) + ":" +
                String.format("%02X", ssn_bytes[3]) + ":" + String.format("%02X", ssn_bytes[4]) + ":" + String.format("%02X", ssn_bytes[5]) + ":" +
                String.format("%02X", ssn_bytes[6]) + ":" + String.format("%02X", ssn_bytes[7]);
        log.debug("\n Send timestamp to SSN = [" + ssn_string + "] ");

        /* Init a Data Array */
        buffer = new byte[4];
        /* Generate Packet Struct */
        set32(0,GetSystemTimestamp());

        /* Generate the Parameter Upload Packet */
        PktTimeUpload m = new PktTimeUpload(buffer,ssn_bytes);
        ret.add(m);
        
        return ret;
    }
        
    public TimeUpload() {
        buffer = new byte[3];
        set8(0, 20);
        addCRC16();
    }
}


