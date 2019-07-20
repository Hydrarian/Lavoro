package dataserver.message;

public class ParameterUpload extends Packet {

    public ParameterUpload(byte[] data) {
        int dataLen = data.length;
        
        /* Modifica per l'aumento a 2 byte per il campo lunghezza del parameterUpload
         */ 
//        buffer = new byte[4 + dataLen];
        buffer = new byte[5 + dataLen];
        set8(0, 6);
//        set8(1,dataLen);
        set16(1,dataLen);
//        System.arraycopy(data, 0, buffer, 2,dataLen);        
        System.arraycopy(data, 0, buffer, 3,dataLen);
        addCRC16();
    }
}
