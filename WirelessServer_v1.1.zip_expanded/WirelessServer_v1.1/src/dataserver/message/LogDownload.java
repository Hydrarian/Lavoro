package dataserver.message;

public class LogDownload extends Packet {

    public LogDownload() {
        buffer = new byte[3];
        set8(0, 8);
        addCRC16();
    }
}
