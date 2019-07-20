package dataserver.message;

public class NothingToDo extends Packet {

    public NothingToDo(byte[] buffer) {
        super(buffer);
    }

    public NothingToDo() {
        buffer = new byte[3];
        set8(0, 9);
        addCRC16();
    }
}
