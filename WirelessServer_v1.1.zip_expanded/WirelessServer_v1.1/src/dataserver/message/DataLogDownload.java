package dataserver.message;

import dataserver.database.Transaction;
import dataserver.Session;
import dataserver.SystemConstant;

import java.io.IOException;
import java.io.FileWriter;

import java.util.Date;

import java.text.SimpleDateFormat;

public class DataLogDownload extends Packet {
    
/* STRUTTURA CASSETTA SEMPLICE STATISTICHE */
    private static String Orario;
    private static String MAC;
    private static int nRxOK;
    private static int nRxNOK;
    private static int nRxIgnored;
    private static int nRxStopped;
    private static int nRxBufFull;
    private static byte RSSI_Media;
    private static byte RSSI_Worst;
    private static byte RSSI_Last;
    private static int nTXConflicts;
    private static int lastTXPwr;
    
    private static boolean Data_to_GatewayStatistiche(byte[] data,Session ses) throws Exception{
        /* Gateway Statistiche Lenght */
        if (data.length == 16){
            
            long timestamp = 0;
            /* Timestamp -> Byte array to Long */
            for (int i = 12; i < 16 ; i++){
                timestamp = (timestamp << 8) + (data[i] & 0xFF);
            }
            /* Timestamp from 1900 to 1970 */
            timestamp = TimeEpochConversion(timestamp);
            /* Assuming that the time is since 1/1/1970 in seconds */
            String oras = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").format(new Date(timestamp * 1000L));
            Orario = oras;
            
            
            MAC = Transaction.SSN2String(ses.getSsn());
            MAC = MAC.replaceAll(":"," ");
            
            RSSI_Media = data[0];
            RSSI_Worst = data[1];
            RSSI_Last = data[2];
            
            nRxBufFull = (data[3] & 0xFF);
            nRxIgnored = (data[4] & 0xFF);
            
            nRxNOK = 0;
            /* Byte array to Int */
            for (int i = 5; i < 7 ; i++){
                nRxNOK = (nRxNOK << 8) + (data[i] & 0xFF);
            }
            
            nRxStopped = (data[7] & 0xFF);
            
            nRxOK = 0;
            /* Byte array to Int */
            for (int i = 8; i < 10 ; i++){
                nRxOK = (nRxOK << 8) + (data[i] & 0xFF);
            }
            
            nTXConflicts = 0;
            /* Byte array to Int */
            for (int i = 10; i < 12 ; i++){
                nTXConflicts = (nTXConflicts << 8) + (data[i] & 0xFF);
            }
            
            return true;
            
        }
        
        return false;
    }
    
    private static void Save_Log_GatewayStatistiche(Session ses) throws Exception{
        /* get predefined filepath */
        String filepath = Transaction.GetFromSSN_ParDow_FilePath(ses.getSsn());
        /* remove extension */
        filepath = filepath.substring(0,filepath.length() - 4);
        /* add new extension */
        filepath = filepath + "_statistiche.txt";
        
        try{
            FileWriter fw = new FileWriter(filepath,true);
            fw.write("\r\n NEW LOG! \r\n Time : " + Orario);
            fw.write("\r\n");
            fw.write("*********************************\r\n");
            fw.write("***    STATISTICHE RADIO RX   *** \r\n");
            fw.write("\r\n");
            fw.write("\r\n " + MAC + " \r\n");
            fw.write("AVERAGE RSSI: " + String.valueOf(RSSI_Media) + " \r\n");
            fw.write("WORST RSSI: " + String.valueOf(RSSI_Worst) + " \r\n");
            fw.write("LAST RSSI: " + String.valueOf(RSSI_Last) + " \r\n");
            fw.write("ERRORI BUFFER FULL: " + String.valueOf(nRxBufFull) + " \r\n");
            fw.write("PACCHETTI SCONOSCIUTI: " + String.valueOf(nRxIgnored) + " \r\n");
            fw.write("ERRORI CRC: " + String.valueOf(nRxNOK) + " \r\n");
            fw.write("ERRORI STRUTTURA: " + String.valueOf(nRxStopped) + " \r\n");
            fw.write("PACCHETTI RICEVUTI: " + String.valueOf(nRxOK) + " \r\n");
            fw.write("CONFLITTI TX: " + String.valueOf(nTXConflicts) + " \r\n");
            fw.write("\r\n");
            fw.write("\r\n**************CCS*************\r\n");
            fw.write("\r\n");
            
            fw.close();
            
        }catch(IOException ioe){
            log.debug("IOException: " + ioe.getMessage());
        }               
        
    }
    
    private static long TimeEpochConversion(long timestamp) {
        /* converts a 1900 Epoch timestamp to 1970 timestamp */
        timestamp = timestamp - 2208988800L;
        return timestamp;
    }

    public DataLogDownload(byte[] buffer) {
        super(buffer);
    }
    
    public Integer getNumDataBytes() {
        return get16(2);
    }
    
    @Override
    public int getCRCOffset() {
        int l = getPayload().length;
        return l + 4;
    }
    
    public Integer getCounter() {
        int c = buffer[1];
        return c;
    }

    public byte[] getPayload() {
        byte[] payload;
        payload = new byte[getNumDataBytes()];
        System.arraycopy(buffer, 4, payload,0,getNumDataBytes());
        //log.debug("Payload length = [" + payload.length + "]");
        return payload;
    }
    
    @Override
    public void execute(Session ses) throws Exception {
        log.debug("Handling DataLogDownload message...");
        
        //controllo crc
        if(checkReceivedCRC()) {
            ses.writeToDownloadBuffer(getPayload());
            ses.incReceivedMessagesIndex();
            Ack a = new Ack(SystemConstant.COMMAND_TYPE_LOG_DOWNLOAD,ses.getCurrentReceivedMessageIndex());
            ses.sendPacket(a);
            log.debug("Received packet counter = [" + getCounter() + "]");
            if(ses.getCurrentReceivedMessageIndex() == ses.getNumPacketsToReceive()) {
                byte[] data = ses.getDownloadedData();
                log.debug("LOG DOWNLOAD FINITO! Writing to DB [" + data.length + "] bytes");
                log.debug("Writing to Log table...");
                
                if (Data_to_GatewayStatistiche(data,ses)){
                    Save_Log_GatewayStatistiche(ses);
                }

                ses.resetDownload();
                log.debug("Log download done!");
            }
        } else {
            Nack n = new Nack(201,SystemConstant.DATA_TYPE_LOG_DOWNLOAD,SystemConstant.ERROR_TYPE_CRC,ses.getCurrentMessageIndex());
            ses.sendPacket(n);
        }
    }
}
