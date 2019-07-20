package dataserver;

import dataserver.message.Ack;
import dataserver.message.DataFirmwareUpload;
import dataserver.message.DataLogDownload;
import dataserver.message.DataParameterDownload;
import dataserver.message.KeepAlive;
import dataserver.message.MessageUtility;
import dataserver.message.Packet;
import dataserver.message.WmFirmwareUpload;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Client extends Thread {

    static protected Logger log = Logger.getLogger(Client.class);
    DatagramSocket socket = null;
    private int CLIENT_PORT = 50000;

    public Client() {
        try {
            socket = new DatagramSocket(CLIENT_PORT);
            //socket = new DatagramSocket();
            socket.setReuseAddress(false);
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    public void send(Packet p, long delay) throws Exception {
        //InetAddress ip = InetAddress.getByName("217.133.18.172");
        //InetAddress ip = InetAddress.getByName("137.204.91.199");
        InetAddress ip = InetAddress.getByName("93.46.80.7");
        log.debug("Sending packet to port = [" + SystemConstant.SERVER_PORT + "] ip = [" + ip + "]");
        DatagramPacket sendPacket = new DatagramPacket(p.getBytes(), p.getBytes().length, ip, 50000);
        socket.send(sendPacket);
        Thread.sleep(delay);
    }

    public int getDelay() {
        return 1000;
    //return 300 + (int) (Math.random() * 5000);
    }

    @Override
    public void run() {

        BasicConfigurator.configure();
        Properties props = new Properties();
        ResourceBundle log4jb = ResourceBundle.getBundle("log4j");
        Enumeration keys = log4jb.getKeys();
        while (keys.hasMoreElements()) {
            String k = (String) keys.nextElement();
            String v = log4jb.getString(k);
            props.put(k, v);
        }
        org.apache.log4j.LogManager.resetConfiguration();
        PropertyConfigurator.configure(props);

        //thread che si mette in ascolto per i messaggi dal server
        new Thread(new Runnable() {

            public void run() {

                try {
                    String fileName = "firmwareRicevuto.txt";
                    FileOutputStream fos = new FileOutputStream(fileName);
                    while (true) {
                        byte[] buf = new byte[SystemConstant.MAXIMUM_PACKET_SIZE];

                        DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                        socket.receive(receivedPacket);
                        Packet p = MessageUtility.messageFactory(receivedPacket);
                        log.debug("Received packet [" + p + "]");
                        log.debug("Packet type = [" + p.getClass().getCanonicalName() + "]");

                        if (p instanceof WmFirmwareUpload) {
                            int countDataPacket = ((WmFirmwareUpload) (p)).getNumDataPacket();
                            log.debug("Num packets = [" + countDataPacket + "]");
                        }

                        int countFirmwarePacket = 0;
                        if (p instanceof DataFirmwareUpload) {
                            DataFirmwareUpload dfa = (DataFirmwareUpload) p;
                            byte[] payload = dfa.getPayload();
                            fos.write(payload);
                            countFirmwarePacket++;
                            log.debug("count = [" + countFirmwarePacket + "]");
                            if (countFirmwarePacket == 6) {
                                log.debug("Flushing file...");
                                fos.flush();
                                fos.close();
                                //log.debug("Exiting from [" + file + "]...");
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            log.debug("Starting client 2...");
            //startFirmwareUploadSequence();
            //startParameterDownloadSequence();
            //startFirmwareUploadSequence();
            //startTreatmentDatabaseUploadSequence();
            //startSingleTreatmentUploadSequence();
            //startTreatmentEnablingSequence();
            //startParameterUploadSequence();
            //startNothingToDoSequence();
            startLogDownloadSequence();
            //startParameterDownloadSequence();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private byte[] getSerial() {
        byte[] b = new byte[6];
        for (int i = 0; i < 6; i++) {
            b[i] = (byte) (i + 1);
        }
        b = new BigInteger("0708090a0b0c", 16).toByteArray();
        //log.debug("ytes = [" + new String(b) + "]");
        return b;
    }

    private void startParameterDownloadSequence() throws Exception {
        int delay = 1000;
        KeepAlive ka = new KeepAlive();
        ka.setSSN(getSerial());
        ka.set8(13, SystemConstant.COMMAND_REQUEST_PARAMETER_DOWNLOAD);
        ka.set8(14, 3);
        send(ka, delay);

        //mando ack per parameter download
        Packet a = new Ack(SystemConstant.COMMAND_TYPE_PARAMETER_DOWNLOAD, 0);
        send(a, delay);
        byte[] buffer = new byte[1386];
        buffer[0] = 101;

        for(int k=100;k<1380;k++) {
            buffer[k] = 6;
        }

        Packet p = new DataParameterDownload(buffer);

        p.set8(0,100);
        p.set16(2, 1200);
        p.set8(1,1);
        send(p,delay);

        p.set8(0,100);
        p.set16(2, 1200);
        p.set8(1,2);
        send(p,delay);

        p.set8(0,100);
        p.set16(2, 1200);
        p.set8(1,3);
        send(p,delay);
    }
    
    private void startLogDownloadSequence() throws Exception {
        int delay = 1000;
        KeepAlive ka = new KeepAlive();
        ka.setSSN(getSerial());
        ka.set8(13, SystemConstant.COMMAND_REQUEST_LOG_DOWNLOAD);
        ka.set8(14, 3);
        send(ka, delay);
        
        //mando ack per log download
        Packet a = new Ack(SystemConstant.COMMAND_TYPE_LOG_DOWNLOAD, 0);
        send(a, delay);
        byte[] buffer = new byte[1386];
        buffer[0] = 101;
        
        for(int k=100;k<1380;k++) {
            buffer[k] = 6;
        }
        
        Packet p = new DataLogDownload(buffer);
        
        p.set16(2, 1200);
        p.set8(1,1);
        p.set8(0,101);
        send(p,delay);
        
        p.set16(2, 1200);
        p.set8(1,2);
        p.set8(0,101);
        send(p,delay);
        
        p.set16(2, 1200);
        p.set8(1,3);
        p.set8(0,101);
        send(p,delay);
    }

    private void startFirmwareUploadSequence() throws Exception {
        int delay = 1000;
        KeepAlive ka = new KeepAlive();
        ka.setSSN(getSerial());
        log.debug("SSN = [" + ka.getSSN() + "]");
        send(ka, delay);
        //send(new Ack(1, 0), delay);
        //send(new Ack(1, 0), delay);
        //send(new Ack(1, 0), delay);
        //send(new Ack(1, 0), delay);
        //send(new Ack(1, 0), delay);
        //send(new Ack(1, 0), delay);
        //send(new Ack(1, 0), delay);

    /*
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
    send(new Ack(1, 0), delay);
     */

    }

    private void startTreatmentDatabaseUploadSequence() throws Exception {
        int delay = 100;
        KeepAlive ka = new KeepAlive();
        ka.setSSN(getSerial());
        log.debug("SSN = [" + ka.getSSN() + "]");
        send(ka, delay);
        send(new Ack(SystemConstant.COMMAND_TYPE_TREATEMENT_DATABASE_UPLOAD, 0), delay);
        send(new Ack(SystemConstant.COMMAND_TYPE_TREATEMENT_DATABASE_UPLOAD, 0), delay);
        //send(new Ack(SystemConstant.COMMAND_TYPE_TREATEMENT_DATABASE_UPLOAD, 0), delay);
        //send(new Ack(SystemConstant.COMMAND_TYPE_TREATEMENT_DATABASE_UPLOAD, 0), delay);
    }
    
    private void startSingleTreatmentUploadSequence() throws Exception {
        int delay = 100;
        KeepAlive ka = new KeepAlive();
        ka.setSSN(getSerial());
        log.debug("SSN = [" + ka.getSSN() + "]");
        send(ka, delay);
        send(new Ack(SystemConstant.COMMAND_TYPE_SINGLE_TREATEMENT_UPLOAD, 0), delay);
    }
    
    private void startTreatmentEnablingSequence() throws Exception {
        int delay = 100;
        KeepAlive ka = new KeepAlive();
        ka.setSSN(getSerial());
        log.debug("SSN = [" + ka.getSSN() + "]");
        send(ka, delay);
        send(new Ack(SystemConstant.COMMAND_TYPE_TREATEMENT_ENABLING, 0), delay);
    }
    
    private void startParameterUploadSequence() throws Exception {
        int delay = 100;
        KeepAlive ka = new KeepAlive();
        ka.setSSN(getSerial());
        log.debug("SSN = [" + ka.getSSN() + "]");
        send(ka, delay);
        send(new Ack(SystemConstant.COMMAND_TYPE_PARAMETER_UPLOAD, 0), delay);
    }
    
    private void startNothingToDoSequence() throws Exception {
        int delay = 100;
        KeepAlive ka = new KeepAlive();
        ka.setSSN(getSerial());
        log.debug("SSN = [" + ka.getSSN() + "]");
        send(ka, delay);
        send(new Ack(SystemConstant.COMMAND_TYPE_NOTHING_TODO, 0), delay);
    }

    public static void main(String args[]) {
        Client c = new Client();
        c.run();
    }
}





//            int count = 11;
//            int d = 5;
            //while (true) {


            /*
            send(new KeepAlive(), getDelay());
            send(new Ack(0, 0), getDelay());
            send(new Ack(0, 0), getDelay());
            send(new Ack(0, 0), getDelay());
            send(new Ack(0, 0), getDelay());
            
            Thread.sleep(5000);
             */



            /*
            Nack n = new Nack(201,1,1);
            sendPacket = new DatagramPacket(n.getBytes(), n.getBytes().length, ip, SystemConstant.PORT);
            log.debug("Sending nack message [" + count + "]");
            clientSocket.send(sendPacket);
            Thread.sleep(1000);
            
            n = new Nack(201,1,2);
            sendPacket = new DatagramPacket(n.getBytes(), n.getBytes().length, ip, SystemConstant.PORT);
            log.debug("Sending nack message [" + count + "]");
            clientSocket.send(sendPacket);
            Thread.sleep(1000);
             */

            /*
            count++;
            if (count > 10) {
            break;
            }
             */
            //}
