package dataserver.message;

import dataserver.Session;
import dataserver.SystemConstant;

public class Ack extends Packet {

    public Ack(byte[] buffer) {
        super(buffer);
    }

    public Ack(int p2, int p3) {
        buffer = new byte[3];
        set8(0, 200);
        set8(1, p2);
        set8(2, p3);
    }

    private int getCommandType() {
        return buffer[1];
    }

    private void handleSequence(Session ses) throws Exception {
        if (ses.isSentFirstMessagesSequence()) {
            log.debug("Current message index = [" + ses.getCurrentMessageIndex() + "]");
            ses.incMessagesIndex();
            if (ses.isFinishSequence()) {
                log.info("FINITO!!!");

                //non esegue update su activity in caso di download
                if(ses.getCommandRequest() == SystemConstant.COMMAND_REQUEST_LOG_DOWNLOAD || ses.getCommandRequest() == SystemConstant.COMMAND_REQUEST_PARAMETER_DOWNLOAD) {
                    log.debug("Command request = [" + ses.getCommandRequest() + "] non eseguo update su Activity e Devices!");
                    return;
                }
                ses.getTransaction().logToActivity(ses.getActivityId(), ses.getSendedBytes());
                ses.setSentFirstMessagesSequence(false);
            } else {
                Packet msg = ses.getMessagesSequence().get(ses.getCurrentMessageIndex());
                ses.sendPacket(msg);
                ses.startTimer(new Integer((String)SystemConstant.serverParameters.get(SystemConstant.T_ACK)), new KeepAlive.Riprovotto(ses));
            }
        } else {
            log.info("Nothing to do!");
        }
    }

    private void handleWmFirmwareUpload(Session ses) throws Exception {
        log.debug("Handling Ack message WM_FIRMWARE_UPLOAD...");
        handleSequence(ses);
    }

    private void handleTreatmentDatabaseUpload(Session ses) throws Exception {
        log.debug("Handling Ack message TREATMENT_DATABASE_UPLOAD...");
        handleSequence(ses);
    }

    private void handleSingleTreatmentUpload(Session ses) throws Exception {
        log.debug("Handling Ack message SINGLE TREATMENT UPLOAD...");
        handleSequence(ses);
    }

    private void handleTreatmentEnabling(Session ses) throws Exception {
        log.debug("Handling Ack message TREATMENT ENABLING...");
        handleSequence(ses);
    }

    private void handleParameterUpload(Session ses) throws Exception {
        log.debug("Handling Ack message PARAMETER UPLOAD...");
        handleSequence(ses);
    }
    
    private void handleLogDownload(Session ses) throws Exception {
        log.debug("Handling Ack message LOG DOWNLOAD...");
        handleSequence(ses);
    }
    
    private void handleParameterDownload(Session ses) throws Exception {
        log.debug("Handling Ack message PARAMETER DOWNLOAD...");
        handleSequence(ses);
    }

    private void handleNothingToDo(Session ses) throws Exception {
        log.debug("Handling Ack message NOTHING TO DO...");
        handleSequence(ses);
    }

    private void handleSatFirmwareUpload(Session ses) throws Exception {
        log.debug("Handling Ack message SAT_FIRMWARE_UPLOAD...");
        throw new Exception("DA IMPLEMENTARE");
    }
    
    private void handleTimeUpload(Session ses) throws Exception {
        log.debug("Handling Ack message TIME_UPDATE...");
        handleSequence(ses);
    }

    @Override
    public void execute(Session ses) throws Exception {

        int COMMAND_TYPE = getCommandType();
        switch (COMMAND_TYPE) {
            case SystemConstant.COMMAND_TYPE_WM_FIRMWARE_UPLOAD:
                handleWmFirmwareUpload(ses);
                break;
            case SystemConstant.COMMAND_TYPE_SAT_FIRMWARE_UPLOAD:
                handleSatFirmwareUpload(ses);
                break;
            case SystemConstant.COMMAND_TYPE_TREATEMENT_DATABASE_UPLOAD:
                handleTreatmentDatabaseUpload(ses);
                break;
            case SystemConstant.COMMAND_TYPE_SINGLE_TREATEMENT_UPLOAD:
                handleSingleTreatmentUpload(ses);
                break;
            case SystemConstant.COMMAND_TYPE_TREATEMENT_ENABLING:
                handleTreatmentEnabling(ses);
                break;
            case SystemConstant.COMMAND_TYPE_PARAMETER_UPLOAD:
                handleParameterUpload(ses);
                break;
            case SystemConstant.COMMAND_TYPE_LOG_DOWNLOAD:
                handleLogDownload(ses);
                break;
            case SystemConstant.COMMAND_TYPE_PARAMETER_DOWNLOAD:
                handleParameterDownload(ses);
                break;
            case SystemConstant.COMMAND_TYPE_NOTHING_TODO:
                handleNothingToDo(ses);
                break;
            case SystemConstant.COMMAND_TYPE_TIME_UPDATE:
                handleTimeUpload(ses);
                break;
            default:
                throw new Exception("Command type non gestito = [" + COMMAND_TYPE + "]");
        }
    }
}