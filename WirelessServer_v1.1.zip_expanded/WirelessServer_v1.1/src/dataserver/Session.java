package dataserver;

import dataserver.database.Transaction;
import dataserver.message.Packet;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.PortUnreachableException;
import java.net.InetAddress;
import java.util.List;
import org.apache.log4j.Logger;
import java.nio.ByteBuffer;

import java.io.IOException;


public class Session {

    static protected Logger log = Logger.getLogger(Session.class);
    private DatagramSocket socket;
    private Transaction transaction;
    private long start = 0;
    private String clientIp;
    private int clientPort;
    private Long ssn;
    private Integer activityId;
    private int commandRequest;
    
    //invio messaggi
    private int currentMessageIndex = 0;
    private boolean sentFirstMessagesSequence = false;
    private List<Packet> messagesSequence = null;
    private int riprovotti = 0;
    private int sendedBytes = 0;
    
    
    //ricezione messaggi di download dal client
    private ByteArrayOutputStream downloadBuffer = new ByteArrayOutputStream();
    private int currentReceivedMessageIndex = 0;
    private int numPacketsToReceive = 0;

    public void startTimer(final long delay, final Runnable r) {
        //log.debug("Starting timer...");
        new Thread(new Runnable() {

            public void run() {
                try {
                    //log.debug("Aspetto per [" + delay + "]");
                    Thread.currentThread().sleep(delay);
                    //log.debug("Lanciotto...");
                    new Thread(r).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Session(int clientPort, String clientIp, DatagramSocket socket) {
        this.socket = socket;
        this.clientIp = clientIp;
        this.clientPort = clientPort;
        this.start = System.currentTimeMillis();
        transaction = new Transaction();
    }

    public int getCommandRequest() {
        return commandRequest;
    }

    public void setCommandRequest(int commandRequest) {
        this.commandRequest = commandRequest;
    }

    public int getSendedBytes() {
        return sendedBytes;
    }

    public void resetDownload() {
        downloadBuffer = new ByteArrayOutputStream();
        currentReceivedMessageIndex = 0;
        numPacketsToReceive = 0;
    }
    
    public byte[] getDownloadedData() throws Exception {
        downloadBuffer.flush();
        //log.debug("Downloaded data buffer = [" + downloadBuffer.size() + "]");
        return downloadBuffer.toByteArray();
    }

    public int getNumPacketsToReceive() {
        return numPacketsToReceive;
    }

    public void setNumPacketsToReceive(int numPacketsToReceive) {
        this.numPacketsToReceive = numPacketsToReceive;
    }

    public void writeToDownloadBuffer(byte[] b) throws Exception {
        log.debug("Writing [" + b.length + "] to buffer...");
        downloadBuffer.write(b);
    }
    public void setStartNow(){
        this.start=System.currentTimeMillis();
    }

    public boolean isOldSession() {
        long now = System.currentTimeMillis();
        //log.debug("Elapsed [" + (now - start) + "] Tses = [" + SystemConstant.T_SES + "]");
        if ((now - start) > new Integer((String)SystemConstant.serverParameters.get(SystemConstant.T_SES))) {
            return true;
        }
        return false;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Long getSsn() {
        return ssn;
    }
    
    public String getSsn_String() {
        byte[] ssn_bytes = ByteBuffer.allocate(8).putLong(ssn).array();
        String ssn_string = String.format("%02X", ssn_bytes[0]) + ":" + String.format("%02X", ssn_bytes[1]) + ":" + String.format("%02X", ssn_bytes[2]) + ":" +
        String.format("%02X", ssn_bytes[3]) + ":" + String.format("%02X", ssn_bytes[4]) + ":" + String.format("%02X", ssn_bytes[5]) + ":" +
        String.format("%02X", ssn_bytes[6]) + ":" + String.format("%02X", ssn_bytes[7]);
        return ssn_string;
    }

    public void setSsn(Long ssn) {
        this.ssn = ssn;
    }
    public void setClientPort(int port){
        this.clientPort=port;
    }

    public int getClientPort() {
        return clientPort;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
    ////////////////////////////////////////////////////////////////////////////
    // INVIO LISTA MESSAGGI
    ////////////////////////////////////////////////////////////////////////////
    public void setMessagesSequence(List<Packet> msgs) {
        messagesSequence = msgs;
    }

    public List<Packet> getMessagesSequence() {
        return messagesSequence;
    }

    public int getCurrentMessageIndex() {
        return currentMessageIndex;
    }

    public void restartMessagesSequence() {
        this.currentMessageIndex = 0;
    }
    public void restartDataMessagesSequence() {
        this.currentMessageIndex = 1;
    }

    public boolean isSentFirstMessagesSequence() {
        return sentFirstMessagesSequence;
    }

    public void setSentFirstMessagesSequence(boolean b) {
        this.sentFirstMessagesSequence = b;
    }

    public void incMessagesIndex() {
        this.currentMessageIndex++;
        this.riprovotti = 0;
    }

    public boolean isFinishSequence() {
        if (currentMessageIndex >= (getMessagesSequence().size())) {
            return true;
        }
        return false;
    }

    public void incRiprovotto() {
        this.riprovotti++;
    }

    public boolean reachedMaxRiprovotto() {
        if (riprovotti < new Integer((String)SystemConstant.serverParameters.get(SystemConstant.MAX_RECOVERY))) {
            return false;
        }
        return true;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // RICEZIONE MESSAGGI
    ////////////////////////////////////////////////////////////////////////////
    public void incReceivedMessagesIndex() {
        this.currentReceivedMessageIndex++;
    }
    
    public int getCurrentReceivedMessageIndex() {
        return currentReceivedMessageIndex;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // INVIO MESSAGGIO
    ////////////////////////////////////////////////////////////////////////////
    public void sendPacket(Packet sendPacket) throws Exception {
        //DatagramSocket s = new DatagramSocket();
        DatagramPacket sp = new DatagramPacket(sendPacket.getBytes(), sendPacket.getBytes().length, InetAddress.getByName(clientIp), clientPort);
        log.debug("Sending message of type [" + sendPacket.getClass().getName() + "] to ip = [" + clientIp + "] port = [" + clientPort + "] packet lenght = [" + sp.getLength() + "]");
        try{
            socket.send(sp);
        }catch (IOException e){
            e.printStackTrace();
            log.debug("++++++ERROR++++++");
            log.debug("Error socket TX");
            log.debug("++++++ERROR++++++");
        }
        
        //aggiorno il contatore dei bytes inviati in questa sessione
        sendedBytes = sendedBytes + sp.getLength();
    }
}
