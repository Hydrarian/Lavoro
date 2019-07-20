package dataserver.message;

public class TreatmentEnabling extends Packet {

    public TreatmentEnabling(byte[] data) {
        buffer = new byte[6];
        set8(0, 5);
        System.arraycopy(data, 0,buffer,1,3);
        addCRC16();
    }
}
