package dataserver.message;

import java.net.DatagramPacket;
import org.apache.log4j.Logger;

public class MessageUtility {

    static protected Logger log = Logger.getLogger(MessageUtility.class);

    public static Packet messageFactory(DatagramPacket receivedDatagram) throws Exception {
        Packet packet = null;
        byte data[] = receivedDatagram.getData();
        int messageType = Packet.unsignedByteToInt(data[0]);
        //log.debug("Message type = [" + messageType + "]");
        try {
            switch (messageType) {
                case 0:
                    packet = new KeepAlive(data);
                    break;
                case 1:
                    packet = new WmFirmwareUpload(data);
                    break;
                case 3:
                    packet = new TreatmentDatabaseUpload(data);
                    break;
                case 4:
                    packet = new SingleTreatmentUpload(data);
                    break;
                case 5:
                    packet = new TreatmentEnabling(data);
                    break;
                case 6:
                    packet = new ParameterUpload(data);
                    break;
                case 9:
                    packet = new NothingToDo();
                    break;
                
                case 200:
                    packet = new Ack(data);
                    break;
                case 202:
                    packet = new Nack(data);
                    break;
                
                case 100:
                    packet = new DataParameterDownload(data);
                    break;
                case 101:
                    packet = new DataLogDownload(data);
                    break;
                            
                default:
                    packet = new Packet(data);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("errore nella creazione di messaggio");
        }
        packet.setReceivedDatagram(receivedDatagram);
        return packet;
    }
}
