package dataserver;

//import dataserver.Session;
import dataserver.database.ConnectionPooler;
//import dataserver.database.Gateway_Config;
import dataserver.message.Packet;
import dataserver.message.MessageUtility;
import dataserver.network.PacketHandler;
import dataserver.database.XML_Scanner;

//import java.io.FileNotFoundException;
import java.util.concurrent.*;
import java.util.Timer;
import java.util.TimerTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import org.apache.log4j.Logger;

import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
import java.net.SocketTimeoutException;



public class Server extends Thread {

    static protected Logger log = Logger.getLogger(Server.class);
    private DatagramSocket socket = null;    //deve essere mappa sincronizzata
    private Map clients = new HashMap();

    private class cleanUp_Runnable extends TimerTask {
        
        private void cleanUp() {
            //itera sui client e vede se e' elapsed e fa la remove per evitare roba vecchia
            ArrayList sessionsToRemove = new ArrayList();
            Set keys = clients.keySet();
            Iterator i = keys.iterator();
            while (i.hasNext()) {
                String ip = (String) i.next();            
                //log.debug("Checking session [" + ip + "] for timeout...");
                Session s = (Session) clients.get(ip);          
                // ArrayList sessionsToRemove = new ArrayList();
                if (s.isOldSession()) {
                    sessionsToRemove.add(s);
                }
            }
        
            //ciclo di rimozione
            Iterator it = sessionsToRemove.iterator();
            while (it.hasNext()) {
                Session ss = (Session) it.next();
                String chiave = ss.getClientIp() + ":" + ss.getClientPort();
                log.debug("Removing session [" + chiave + "] for timeout!");
                clients.remove(chiave);
            }
        }

        public void run (){
            // run periodically the cleanup
            cleanUp();
        }
    
    }

    private synchronized Session getSession(DatagramPacket receivedPacket) {
        // execute cleanup before every session 
        cleanUp_Runnable clRU = new cleanUp_Runnable();
        clRU.cleanUp();
        
        String clientIp = receivedPacket.getAddress().getHostAddress();
        int clientPort = receivedPacket.getPort();
        String chiave = clientIp + ":" + clientPort;
        Session ses = (Session) clients.get(chiave);
        if (ses == null) {
            log.debug("Creating new session clientIp = [" + clientIp + "] clientPort = [" + clientPort + "] chiave = [" + chiave + "]");
            ses = new Session(clientPort, clientIp, socket);
            clients.put(chiave, ses);
        }
       
        //eventuale pulizia della mappa
        // prendo nuovamente la porta e riazzero il tempo di sessione
        ses.setStartNow();
        ses.setClientPort(receivedPacket.getPort());
        return ses;
    }

    
    @Override
    public void run() {
        log.info("MediCon Server v 1.01");
        log.info("Developed by MediCon Ingegneria s.r.l.");
        try {
            log.info("Loading config parameters...");
            ResourceBundle dbb = null;
            try {
                dbb = ResourceBundle.getBundle("server");
            } catch(MissingResourceException e) {
                log.error(e,e);
                return;
            }
        
            Enumeration keys = dbb.getKeys();
            while (keys.hasMoreElements()) {
                String k = (String) keys.nextElement();
                String v = dbb.getString(k);
                SystemConstant.serverParameters.put(k, v);
            }
            log.info("Parameters loaded!");
            
            /* starting cleanup */
            Timer cleanUptmr = new Timer();
            cleanUptmr.schedule(new cleanUp_Runnable(),  new Integer((String)SystemConstant.serverParameters.get(SystemConstant.T_SES)), new Integer((String)SystemConstant.serverParameters.get(SystemConstant.T_SES)));
            
            log.debug("Starting server...");
            socket = new DatagramSocket(new Integer((String)SystemConstant.serverParameters.get(SystemConstant.SERVER_PORT)));
            socket.setReuseAddress(true);
            
            while (true) {
                byte[] buf = new byte[SystemConstant.MAXIMUM_PACKET_SIZE];
                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(receivedPacket);
                    log.debug("Received packet from IP = [" + receivedPacket.getAddress().getHostAddress() + "] clientPort = [" + receivedPacket.getPort() + "]");
                    Packet request = MessageUtility.messageFactory(receivedPacket);
                    //log.debug("Received packet  = [" + request + "]");
                    Session ses = getSession(receivedPacket);
                    new PacketHandler(ses, request);
                }catch (SocketTimeoutException e){
                    log.debug("Socked timed out...");
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            log.error(e, e);
        }

    }

    public static void main(String[] args) {
        
        Utils.initLogger();
        //ConnectionPooler.initConnectionPooler();

        ExecutorService service = Executors.newFixedThreadPool(2);
        service.submit(new XML_Scanner());
        service.submit(new Server());

    }
}
