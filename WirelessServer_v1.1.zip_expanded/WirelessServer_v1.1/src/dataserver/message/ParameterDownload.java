package dataserver.message;

public class ParameterDownload extends Packet {

    public ParameterDownload() {
        buffer = new byte[3];
        set8(0, 7);
        addCRC16();
    }
}