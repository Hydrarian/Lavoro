package dataserver.message;

public class TreatmentDatabaseUpload extends Packet {

    public TreatmentDatabaseUpload(byte[] buffer) {
        super(buffer);
    }

    public TreatmentDatabaseUpload(int startAddress, int numPacket) {
        buffer = new byte[7];
        set8(0, 3);
        set24(1, startAddress);
        log.debug("Num packet = [" + numPacket + "]");
        set8(4, numPacket);
        addCRC16();
    }
    
    public int getNumDataPacket() {
        int ret = 0;
        ret = this.get8(4);
        return ret;
    }
}
