package dataserver.message;

import dataserver.Session;
import dataserver.SystemConstant;

public class Nack extends Packet {

    public Nack(byte[] buffer) {
        super(buffer);
    }
    
    public Nack(int p1,int p2,int p3,int p4) {
        buffer = new byte[4];
        set8(0,p1);
        set8(1,p2);
        set8(2,p3);
        set8(3,p4);
    }
    
    public byte getP3() {
        return buffer[2];
    }

    @Override
    public void execute(Session ses) throws Exception {
        log.debug("Handling Nack message...");
        if (ses.isSentFirstMessagesSequence()) {     
            if (getP3() == 2) {
                //se nack per fuori seq mando tutto
                log.debug("Rinviando tutta la sequenza");
                ses.restartDataMessagesSequence();
            } else {
                log.debug("Rinviando l'ultimo pacchetto");
//            }
                Packet msg = ses.getMessagesSequence().get(ses.getCurrentMessageIndex());
                ses.sendPacket(msg);
                ses.startTimer(new Integer((String)SystemConstant.serverParameters.get(SystemConstant.T_ACK)),new KeepAlive.Riprovotto(ses));
            }            
        } else {
            log.debug("Nothing to do with NACK message!");
        }
    }
    
}
