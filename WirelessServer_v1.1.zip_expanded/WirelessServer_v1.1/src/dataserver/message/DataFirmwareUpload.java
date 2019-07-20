package dataserver.message;

public class DataFirmwareUpload extends Packet {

    byte[] payload;

    public DataFirmwareUpload(byte[] buffer) {
        super(buffer);
        byte[] lb = new byte[2];
        log.debug("Buffer lenght = [" + buffer.length + "]");
        System.arraycopy(buffer, 2, lb,0,2);
        int length = Packet.ByteToInt2(lb);
        payload = new byte[length];
        log.debug("Data length = [" + length + "]");
        System.arraycopy(buffer, 4, payload,0,length);
    }
    
    public DataFirmwareUpload(int seq, byte[] payload) {
        buffer = new byte[6 + payload.length];
        set8(0,100);
        set8(1,seq);
        set16(2,payload.length);
        setData(4,payload);
        addCRC16();
    }

    public byte[] getPayload() {
        return payload;
    }

}
