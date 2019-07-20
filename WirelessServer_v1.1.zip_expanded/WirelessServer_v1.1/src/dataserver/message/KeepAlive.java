package dataserver.message;

import dataserver.Utils;
import dataserver.domain.Activity;
import dataserver.Session;
import dataserver.SystemConstant;
import dataserver.database.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

public class KeepAlive extends Packet {

    public KeepAlive(byte[] buffer) {
        super(buffer);
//        super.checkReceivedCRC();
    }

    @Override
    public int getCRCOffset() {
        return 17;
    }

    public KeepAlive() {
        buffer = new byte[19];
        //buffer[13] = 1;
        buffer[0] = 0;
    }

    public int getNumPacketToReceive() {
        int np = 0;
        np = np + (buffer[16] & 0xFF);
        return np;
    }

    public String getSSN() {
        byte[] ssnb = new byte[8];
        System.arraycopy(buffer, 1, ssnb, 0, 8);
        StringBuffer s = new StringBuffer();
        for (int k = 0; k < 8; k++) {
            //s.append(Integer.toHexString(ssnb[k]));
            s.append(Utils.byteToHex(ssnb[k]));
        }
        return s.toString().toUpperCase();
    }

    public void setSSN(byte[] ssn) {
        System.arraycopy(ssn, 0, buffer, 1, 8);
    }
    
    public int getDatabaseVersion() {
        /* big endian */
        //return (buffer[10] & 0xFF) << 16 | (buffer[11] & 0xFF) << 8 | (buffer[12] & 0xFF);
        String DV = String.valueOf(new char[]{(char)buffer[12],(char)buffer[13],(char)buffer[14]});
        return Integer.parseInt(DV);
    }

    public Integer getCommandRequest() {
        int cr = buffer[15];
        return cr;
    }

    static public class Riprovotto implements Runnable {

        private Session ses;
        private int waitingSeq;

        Riprovotto(final Session ses) {
            this.ses = ses;
            this.waitingSeq = ses.getCurrentMessageIndex();
        }

        public void run() {
            synchronized (ses) {
                try {
                    if (ses.getCurrentMessageIndex() != waitingSeq) {
                        //log.debug("Arrivato ok NO Rilanciotto di [" + waitingSeq + "] perche arrivato ack");
                        return;
                    }

                    //manda l'ultimo pacchetto inviato
                    log.debug("Timeout rinvio pacco [" + ses.getCurrentMessageIndex() + "] di " +
                            "[" + ses.getSsn_String() + "] ");
                    Packet msg = ses.getMessagesSequence().get(ses.getCurrentMessageIndex());
                    ses.sendPacket(msg);
                    ses.setSentFirstMessagesSequence(true);
                    ses.incRiprovotto();
                    if (!ses.reachedMaxRiprovotto()) {
                        ses.startTimer(new Integer((String)SystemConstant.serverParameters.get(SystemConstant.T_ACK)), new Riprovotto(ses));
                    } else {
                        log.error("Raggiunti MAX_RECOVERY for = [" + ses.getSsn_String() + "]");                    
                        //metto l'errore nel campo note della tabella activity
                        ses.getTransaction().logToActivity(ses.getActivityId(), SystemConstant.ERROR_TYPE_MAX_RECOVERY_REACHED);
                    }
                } catch (Exception e) {
                    log.error(e,e);
                }
            }
        }
    }

    private void startSequence(Session ses, List<Packet> msgs) throws Exception {
        ses.setMessagesSequence(msgs);
        ses.sendPacket((Packet) msgs.get(0));
        ses.setSentFirstMessagesSequence(true);
        ses.startTimer(new Integer((String)SystemConstant.serverParameters.get(SystemConstant.T_ACK)), new Riprovotto(ses));
    }

    @Override
    public void execute(final Session ses) throws Exception {
        List<Packet> msgs = new ArrayList();
        log.debug("Handling KeepAlive message for ip = [" + ses.getClientIp() + "] command request [" + getCommandRequest() + "]");

        // Controllo CRC se fallisce invio msg KeepAliveError
        if (this.checkReceivedCRC()) {

            /* save ssn to session */
            /* Due to long overflow is needed use the BigInteger in the half of conversion */
            BigInteger numbig = new BigInteger(this.getSSN(),16);
            ses.setSsn(numbig.longValue());
            Transaction t = ses.getTransaction();
            log.debug("Found SSN in keepalive message = [" + ses.getSsn_String() + "]");

            //gestione ricezione dati

            // modifica 
            ses.restartMessagesSequence();
            if (getCommandRequest() == SystemConstant.COMMAND_REQUEST_PARAMETER_DOWNLOAD) {
                log.debug("Handling command request parameter download...");
                ses.resetDownload();
                Packet m = new ParameterDownload();  
                ses.setActivityId(-1);
                msgs.add(m);
                ses.setNumPacketsToReceive(getNumPacketToReceive());
                log.debug("Num packets to receive = [" + getNumPacketToReceive() + "]");
                startSequence(ses, msgs);
                ses.setCommandRequest(SystemConstant.COMMAND_REQUEST_PARAMETER_DOWNLOAD);
                return;
            } else if (getCommandRequest() == SystemConstant.COMMAND_REQUEST_LOG_DOWNLOAD) {
                log.debug("Handling command request log download...");
                ses.resetDownload();
                Packet m = new LogDownload();
                ses.setActivityId(-1);
                msgs.add(m);
                ses.setNumPacketsToReceive(getNumPacketToReceive());
                startSequence(ses, msgs);
                ses.setCommandRequest(SystemConstant.COMMAND_REQUEST_LOG_DOWNLOAD);
                return;
            } else if (getCommandRequest() == SystemConstant.COMMAND_REQUEST_TIME_UPDATE) {
                log.debug("Handling command request time update...");
                ses.resetDownload();
                TimeUpload tu = new TimeUpload();
                msgs = tu.GenTimeUpload(ses.getSsn());
                ses.setActivityId(-1);
                //msgs.add(m);
                ses.setNumPacketsToReceive(0);
                startSequence(ses, msgs);
                ses.setCommandRequest(SystemConstant.COMMAND_REQUEST_TIME_UPDATE);
                return;
            }

            Activity act = t.getActivity(ses.getSsn(),getDatabaseVersion());
            //ses.restartMessagesSequence();
            if (act != null) {
                
                ses.setActivityId(act.getId());
                String actType = act.getType();
                if (SystemConstant.ACTIVITY_TYPE_FIRMWARE_UPLOAD_WM.equals(actType)) {
                    log.debug("WM firmware upload not supported!");
                    //msgs = t.createFirmwareUploadMessagesSequence(act.getId());
                } else if (SystemConstant.ACTIVITY_TYPE_FIRMWARE_UPLOAD_SAT.equals(actType)) {
                    log.debug("SAT firmware upload not supported!");
                    //msgs = t.createFirmwareUploadMessagesSequence(act.getId());
                } else if (SystemConstant.ACTIVITY_TYPE_TREATMENT_DATABASE_UPLOAD.equals(actType)) {
                    log.debug("Treatment database upload not supported!");
                    //msgs = t.createTreatmentDatabaseUploadMessagesSequence(act.getId());
                } else if (SystemConstant.ACTIVITY_TYPE_SINGLE_TREATMENT_UPLOAD.equals(actType)) {
                    log.debug("Single treatment upload not supported!");
                    //msgs = t.createSingleTreatmentUploadMessagesSequence(act.getId());
                } else if (SystemConstant.ACTIVITY_TYPE_TREATMENT_ENABLING.equals(actType)) {
                    log.debug("Treatment enabling not supported!");
                    //msgs = t.createTreatmentEnablingMessagesSequence(act.getId());
                } else if (SystemConstant.ACTIVITY_TYPE_PARAMETER_UPLOAD.equals(actType)) {
                    log.debug("Starting parameter upload...");
                    //msgs = t.createParameterUploadMessagesSequence(act.getId());
                    msgs = t.createParameterUploadMessagesSequence(ses.getSsn());
                } else {
                    log.debug("Unrecognized activity found [" + act.getType() + "]!");
                    return;
                }
            } else {
                log.debug("No ACTIVITY found for ssn = [" + ses.getSsn_String() + "] transmitting NOTHINGTODO message...");
                Packet m = new NothingToDo();
                ses.setActivityId(-1);
                msgs.add(m);                
                //aggiorno la tabella devices mettendo 8 in type
//                t.setNothingToDo(ses.getSsn());
            }
            
            //inizio sequenza
            startSequence(ses, msgs);
        } else {

            // Invio msg KeepAliveError
            log.debug("CRC Fail on the Keep Alive message received from ip = [" + ses.getClientIp() + "] transmitting KEEPALIVEERROR message...");
            Packet m = new KeepAliveError();
            ses.sendPacket(m);
        }

    }
}

