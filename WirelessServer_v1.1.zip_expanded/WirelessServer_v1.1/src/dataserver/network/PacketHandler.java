package dataserver.network;

import dataserver.Session;
import dataserver.message.Packet;
import org.apache.log4j.Logger;

public class PacketHandler extends Thread {

    static protected Logger log = Logger.getLogger(PacketHandler.class);

    public PacketHandler(final Session ses, final Packet request) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    synchronized (ses) {
                        request.execute(ses);
                    }
                } catch (Exception e) {

                    //loggare per ogni ip il suo flusso
                    log.error(e, e);
                }
            }
        }).start();
    }
}
