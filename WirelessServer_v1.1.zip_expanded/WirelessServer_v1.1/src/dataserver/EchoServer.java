package dataserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import org.apache.log4j.Logger;

public class EchoServer {

    static protected Logger log = Logger.getLogger(Server.class);

    public static void main(String[] args) {
        DatagramSocket sock;
        DatagramPacket pack = new DatagramPacket(new byte[SystemConstant.MAXIMUM_PACKET_SIZE], SystemConstant.MAXIMUM_PACKET_SIZE);
        try {
            sock = new DatagramSocket((Integer)SystemConstant.serverParameters.get("6000"));
        } catch (SocketException e) {
            log.error(e, e);
            return;
        }

        log.debug("Starting echo server...");
        while (true) {
            try {
                sock.receive(pack);
                log.debug("Received packet length = [" + pack.getLength() + "] Sending to PORT [" + pack.getPort() + "]");
                sock.send(pack);
            } catch (IOException ioe) {
                log.error(ioe, ioe);
            }
        }
    }
}