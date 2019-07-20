package dataserver.message;

import dataserver.Session;
import dataserver.SystemConstant;
import dataserver.database.Transaction;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Date;
import java.text.SimpleDateFormat;
import org.xml.sax.SAXException;

public class DataParameterDownload extends Packet {
    
    /* Documento XML */
    private static File XML_file_p;
    
    /* STRUTTURA CASSETTA SEMPLICE */
    private static String Orario = "2000-01-01T00:00:00";
    private static String MAC = "000000000000000000";
    private static float Weight = 0;
    private static float Battery = 0;
    private static int Errore = 0;
    
    /* STRUTTURA CASSETTA SEMPLICE STATISTICHE */
//    private String Orario;
//    private String MAC;
    private static int nRxOK;
    private static int nRxNOK;
    private static int nRxIgnored;
    private static int nRxStopped;
    private static int nRxBufFull;
    private static byte lastRSSI;
    private static int nTXConflicts;
    private static int lastTXPwr;
    
    private static int Data_ptr;
    
    private static Document doc = null;
    
    private Integer New_Data_ptr(byte[] data) {
        /* ad the pointer to next packet */
        /* 32 bytes alligned */
        Data_ptr = Data_ptr + 32;
        /* to avoid stack pointer with assymetric packet */
        if ((Data_ptr + 32) <= data.length){
            return Data_ptr;
        }else{
            return data.length;
        }
    }
    
    private static void Reset_Data_ptr() {
        /* ad the pointer to next packet */
        /* 32 bytes alligned */
        Data_ptr = 0;
    }
    
    private static long TimeEpochConversion(long timestamp) {
        /* converts a 1900 Epoch timestamp to 1970 timestamp */
        timestamp = timestamp - 2208988800L;
        /* remove zone offset */
        TimeUpload tu = new TimeUpload();
        timestamp = tu.OffsetSystemTimestamp(timestamp);
        return timestamp;
    }
    
    private static boolean Data_to_CassettaSemplice(byte[] data){
        /* CASSETTA SEMPLICE PACKET ID */
        if (data[Data_ptr] == 0x01){
            
            long timestamp = 0;
            /* Timestamp -> Byte array to Long */
            for (int i = 1; i < 5 ; i++){
                timestamp = (timestamp << 8) + (data[Data_ptr + i] & 0xFF);
            }
            /* Timestamp from 1900 to 1970 */
            timestamp = TimeEpochConversion(timestamp);
            /* Assuming that the time is since 1/1/1970 in seconds */
            String oras = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp * 1000L));
            oras = oras.replaceAll(" ","T");
            Orario = oras;
            
            
            MAC = String.format("%02X",0) + 
                    String.format("%02X",data[Data_ptr + 5]) + String.format("%02X",data[Data_ptr + 6]) + 
                    String.format("%02X",data[Data_ptr + 7]) + String.format("%02X",data[Data_ptr + 8]) + 
                    String.format("%02X",data[Data_ptr + 9]) + String.format("%02X",data[Data_ptr + 10]) + 
                    String.format("%02X",data[Data_ptr + 11]) + String.format("%02X",data[Data_ptr + 12]);
            
            
            int Weight_;
            /* comp 2 rappresentation */
            if ((data[Data_ptr + 13] & 0x80) == 0){
                // positive number
                Weight_ = 0;
            }else{
                 Weight_ = 0xFF;
            }
            /* Weight -> Byte array to Int */
            for (int i = 13; i < 16 ; i++){
                Weight_ = (Weight_ << 8) + (data[Data_ptr + i] & 0xFF);
            }
           
            /* negtive numbers are not supported */
            if(Weight_ < 0){
                Weight_ = 0;
            }
            
            /* conversion g to Kg */
            Weight = Weight_;
            Weight = Weight / 1000;
            
            Battery = (data[Data_ptr + 16] & 0xFF);
            /* conversion 100mV to V */
            Battery = Battery / 10;
            
            Errore = (data[Data_ptr + 17] & 0xFF);
            
//            log.debug("MAC: " + MAC + " Ora: " + Orario + " Peso: " + String.format("%d",Weight) + " B: " + String.format("%d",Battery) + " E: " + String.format("%d",Errore));
            return true;
        }
        
        return false;
    }
    
        private static boolean Data_to_CassettaSempliceStatistiche(byte[] data){
        /* CASSETTA SEMPLICE STATISTICHE PACKET ID */
        if (data[Data_ptr] == 0x03){
            
            long timestamp = 0;
            /* Timestamp -> Byte array to Long */
            for (int i = 1; i < 5 ; i++){
                timestamp = (timestamp << 8) + (data[Data_ptr + i] & 0xFF);
            }
            /* Timestamp from 1900 to 1970 */
            timestamp = TimeEpochConversion(timestamp);
            /* Assuming that the time is since 1/1/1970 in seconds */
            String oras = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").format(new Date(timestamp * 1000L));
            Orario = oras;
            
            
            MAC = 
                    String.format("%02X ",data[Data_ptr + 5]) + String.format("%02X ",data[Data_ptr + 6]) + 
                    String.format("%02X ",data[Data_ptr + 7]) + String.format("%02X ",data[Data_ptr + 8]) + 
                    String.format("%02X ",data[Data_ptr + 9]) + String.format("%02X ",data[Data_ptr + 10]) + 
                    String.format("%02X ",data[Data_ptr + 11]) + String.format("%02X ",data[Data_ptr + 12]);
            
            nRxOK = 0;
            /* Byte array to Int */
            for (int i = 13; i < 15 ; i++){
                nRxOK = (nRxOK << 8) + (data[Data_ptr + i] & 0xFF);
            }
            
            nRxNOK = 0;
            /* Byte array to Int */
            for (int i = 15; i < 17 ; i++){
                nRxNOK = (nRxNOK << 8) + (data[Data_ptr + i] & 0xFF);
            }
            
            nRxIgnored = (data[Data_ptr + 17] & 0xFF);
            nRxStopped = (data[Data_ptr + 18] & 0xFF);
            nRxBufFull = (data[Data_ptr + 19] & 0xFF);
            
            lastRSSI = data[Data_ptr + 20];
            
            nTXConflicts = 0;
            /* Byte array to Int */
            for (int i = 21; i < 23 ; i++){
                nTXConflicts = (nTXConflicts << 8) + (data[Data_ptr + i] & 0xFF);
            }
            
            lastTXPwr = (data[Data_ptr + 23] & 0xFF);
            
            return true;
            
        }
        
        return false;
    }
    
    private static boolean CheckTime (String time1,String time2) throws ParseException {
        /* RETURN 1 IF TIME 1 IS OLDER THAN TIME 2 */
        /* delete T char to avoid error on parse*/
        time1 = time1.replaceAll("T"," ");
        time2 = time2.replaceAll("T"," ");
        /* parse date into Date */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = sdf.parse(time1);
        Date date2 = sdf.parse(time2);

        if (date1.compareTo(date2) > 0) {
            //System.out.println("Date1 is after Date2");
            return true;
        } else  if (date1.compareTo(date2) < 0) {
            //System.out.println("Date1 is before Date2");
            return false;
        } else if (date1.compareTo(date2) == 0) {
            //System.out.println("Date1 is equal to Date2");
            return false;
        } else {
            //System.out.println("How to get here?");
            return false;
        }

    }
    
    public static Element OpenGatewayXML (String filepath) throws SAXException, TransformerConfigurationException, TransformerException, IOException{
        
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            //Parse existing file to DOM
            //Document doc;
           
            try {
                /* try to open existing file */
                XML_file_p = new File(filepath);
                doc = docBuilder.parse(XML_file_p);
                
                /* root elements */
                NodeList node = doc.getElementsByTagName("site");
                Element rootElement = (Element)node.item(0);
                return rootElement;
            } catch (IOException ex) {

                System.out.print("Save new File in: " + filepath);
                doc = docBuilder.newDocument();
                /* Set DOC Attribute */
                doc.setXmlVersion("1.0");
                doc.setXmlStandalone(true);
                
                Element eventdata = doc.createElement("eventdata");
                eventdata.setAttribute("xmlns","http://www.e-nventory.net/xml-schema/eventdata");
                doc.appendChild(eventdata);
                
                Element doctipe = doc.createElement("doctype");
                doctipe.appendChild(doc.createTextNode("DIGI SENS e-nventory datafile v1.0"));
                eventdata.appendChild(doctipe);
                
                /* get name from path */
                String string_creator = new File(filepath).getName();
                /* remove file extension */
                if (string_creator.length() > 4){
                    string_creator = string_creator.substring(0,string_creator.length() - 4);
                    
                    Element creator = doc.createElement("creator");
                    creator.appendChild(doc.createTextNode(string_creator));
                    eventdata.appendChild(creator);
                }
                
                Element site = doc.createElement("site");
                eventdata.appendChild(site);
                
                if (string_creator.length() != 0){
                    Element sitelink = doc.createElement("sitelink");
                    sitelink.appendChild(doc.createTextNode(string_creator));
                    site.appendChild(sitelink);
                }
                
                Element createdate = doc.createElement("createdate");
                String ora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                ora = ora.replaceAll(" ","T");
                createdate.appendChild(doc.createTextNode(ora));
                eventdata.appendChild(createdate);
                
                // write the content into xml file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(filepath);

                transformer.transform(source, result);

                System.out.println("\nFile saved!\n");
                
                /* try to open existing file */
                doc = docBuilder.parse(new File(filepath));
                /* root elements */
                NodeList node = doc.getElementsByTagName("site");
                Element rootElement = (Element)node.item(0);
                return rootElement;

                
            }

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } 
        
        return null;
        
    }
    
    public static boolean CheckOldSensorNodeLink (Element site) throws XPathExpressionException, ParseException, TransformerConfigurationException, TransformerException{
        /* verify old saved log with same NodeLink number */
        
        boolean ret = true;
        
        NodeList measurements = site.getElementsByTagName("measurement");

        //System.out.println("\nTotale Log: " + measurements.getLength());   

        /* Update configuration and update the list */
        if (measurements.getLength() > 0){
            for (int i = 0; i < measurements.getLength(); i++) {
                Node nodo = measurements.item(i);
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    Element mea = (Element) nodo;
                    /* check the MAC */
                    if (MAC.equals(mea.getElementsByTagName("nodelink").item(0).getFirstChild().getNodeValue())){
                        String aa = mea.getElementsByTagName("time").item(0).getFirstChild().getNodeValue();
                        if (CheckTime (Orario,aa)){
                            /* update file */
                            mea.getParentNode().removeChild(mea);                             
                            ret = true;
                        }else{
                            /* old packet */
                            ret = false;
                        }
                    }
                }           
            }
        }else{
            /* no packet */
            ret = true;
        }
        
        /* save new value */
        return ret;
        
    }
    
    public static void AddCassettaSemplice2XML (Element site,Session ses) throws XPathExpressionException, ParseException, TransformerConfigurationException, TransformerException, Exception{
        /* Add measurement */
        try {
            Element measurement = doc.createElement("measurement");
            site.appendChild(measurement);
            
            // nodelink elements
            Element nodelink = doc.createElement("nodelink");
            nodelink.appendChild(doc.createTextNode(MAC));
            measurement.appendChild(nodelink);
        
            // nodelink elements
            Element time = doc.createElement("time");
            time.appendChild(doc.createTextNode(Orario));
            measurement.appendChild(time);
        
            // peso elements
            Element peso = doc.createElement("value");
            peso.setAttribute("type","n_v_unitvalue");
            peso.appendChild(doc.createTextNode(String.format("%.3f",Weight)));
            measurement.appendChild(peso);
        
            // Battery elements
            Element bat = doc.createElement("value");
            bat.setAttribute("type","Battery_V");
            bat.appendChild(doc.createTextNode(Float.toString(Battery)));
            measurement.appendChild(bat);
        
            // Battery elements
            Element er = doc.createElement("value");
            er.setAttribute("type","Error");
            er.appendChild(doc.createTextNode(String.valueOf(Errore)));
            measurement.appendChild(er);
        
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(Transaction.GetFromSSN_ParDow_FilePath(ses.getSsn()));

            transformer.transform(source, result);
            
            //close file and stream
            //result.getOutputStream().close();
            
        }catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            String note = "Errore creando Log per SSN = [" + ses.getSsn_String() + "]";
            log.error(note);
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
            String note = "Errore creando Log per SSN = [" + ses.getSsn_String() + "]";
            log.error(note);
        }
        
    }
    
    private static void Save_Log_CassettaSempliceStatistiche(Session ses) throws Exception{
        /* get predefined filepath */
        //String filepath = Transaction.GetActSSN_ParDow_FilePath();
        String filepath = Transaction.GetFromSSN_ParDow_FilePath(ses.getSsn());
        /* remove extension */
        filepath = filepath.substring(0,filepath.length() - 4);
        /* add new extension */
        filepath = filepath + "_statistiche.txt";
        
        try{
            FileWriter fw = new FileWriter(filepath,true);
            fw.write("\r\n NEW DATA! \r\n Time : " + Orario);
            fw.write("\r\n");
            fw.write("*********************************\r\n");
            fw.write("\r\n " + MAC + "\r\n");
            fw.write("LAST RSSI: " + String.valueOf(lastRSSI) + " \r\n");
            fw.write("ERRORI BUFFER FULL: " + String.valueOf(nRxBufFull) + " \r\n");
            fw.write("PACCHETTI SCONOSCIUTI: " + String.valueOf(nRxIgnored) + " \r\n");
            fw.write("ERRORI CRC: " + String.valueOf(nRxNOK) + " \r\n");
            fw.write("ERRORI STRUTTURA: " + String.valueOf(nRxStopped) + " \r\n");
            fw.write("PACCHETTI RICEVUTI: " + String.valueOf(nRxOK) + " \r\n");
            fw.write("CONFLITTI TX: " + String.valueOf(nTXConflicts) + " \r\n");
            fw.write("POWER TX: " + String.valueOf(lastTXPwr) + " \r\n");
            fw.write("\r\n**************CCS*************\r\n");
            fw.write("\r\n");
            
            fw.close();
            
        }catch(IOException ioe){
            log.debug("IOException: " + ioe.getMessage());
        }               
        
    }

    public DataParameterDownload(byte[] buffer) {
        super(buffer);
    }
    
    @Override
     public int getCRCOffset() {
        int l = getPayload().length;
        return l + 4;
    }
    
    public Integer getNumDataBytes() {
        return get16(2);
    }
    
    public Integer getCounter() {
        int c = buffer[1];
        return c;
    }

    public byte[] getPayload() {
        byte[] payload;
        payload = new byte[getNumDataBytes()];
        System.arraycopy(buffer, 4, payload,0,getNumDataBytes());
        return payload;
    }
    
    @Override
    public void execute(Session ses) throws Exception {
        //log.debug("Handling DataParameterDownload message...");
        
        //controllo crc
        if(checkReceivedCRC() && (ses.getNumPacketsToReceive() != 0)) {
            ses.writeToDownloadBuffer(getPayload());
            ses.incReceivedMessagesIndex();
            Ack a = new Ack(SystemConstant.COMMAND_TYPE_PARAMETER_DOWNLOAD,ses.getCurrentReceivedMessageIndex());
            ses.sendPacket(a);
            log.debug("Parameter Download Packet Number: " + String.valueOf(ses.getCurrentReceivedMessageIndex()) + " / " + String.valueOf(ses.getNumPacketsToReceive()));
            if(ses.getCurrentReceivedMessageIndex() >= ses.getNumPacketsToReceive()) {
                byte[] data = ses.getDownloadedData();
                
                //update schema sulla tabella firmware
                log.debug("PARAMETER DOWNLOAD FINITO!");
                
                /* wait 100ms the file and check if it already opened by other ses */
                /*File filetest = new File(Transaction.GetFromSSN_ParDow_FilePath(ses.getSsn()));
                int timer_canwrite = 0;
                while((!filetest.canWrite()) && 
                        (timer_canwrite < SystemConstant.TIMER_FILE_WRITE_MS)){
                    timer_canwrite = timer_canwrite + 1;
                    Thread.sleep(1);
                }*/
                
                try {
                    /* Interpret data and call XML management */
                    Reset_Data_ptr();
                    while (Data_ptr < data.length){
                        /* Parse Data */
                        if (Data_to_CassettaSemplice(data)){   
                            //Write to XML file
                            Element site = DataParameterDownload.OpenGatewayXML(Transaction.GetFromSSN_ParDow_FilePath(ses.getSsn()));
                            /* check if old log is present */
                            if (DataParameterDownload.CheckOldSensorNodeLink (site)){
                                /* write new packet to log */
                                DataParameterDownload.AddCassettaSemplice2XML(site,ses);
                            }
                        }
                        else if (Data_to_CassettaSempliceStatistiche(data)){
                            /* save statistics to .txt */
                            Save_Log_CassettaSempliceStatistiche(ses);
                        }
                        New_Data_ptr(data);
                    }

                } catch(Exception e) {
                    String note = "Errore creando Log per SSN = [" + ses.getSsn_String() + "]";
                    log.error(note);
                    log.error(e,e);
                } finally {
                    ses.resetDownload();
                }
            }
        } else {
            Nack n = new Nack(201,SystemConstant.DATA_TYPE_PARAMETER_DOWNLOAD,SystemConstant.ERROR_TYPE_CRC,ses.getCurrentMessageIndex());
            ses.sendPacket(n);
        }
    }
}
