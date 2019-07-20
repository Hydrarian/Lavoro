package dataserver.message;

public class SingleTreatmentUpload extends Packet {
    
    public SingleTreatmentUpload(byte[] buffer) {
        super(buffer);
    }
    
/* Modifica del Singletreatment Data Base:
 * non era stato inserito il campo dati
 */
    public SingleTreatmentUpload(int startAddress, int dataLength, byte[] SingleTreatment) {
//        buffer = new byte[7];      
        buffer = new byte[8+dataLength];
        set8(0, 4);
        set24(1, startAddress);
        set16(4, dataLength);
        
        // inserimento nel pacchetto dei dati del single treatment
        
        System.arraycopy(SingleTreatment,0, buffer, 6, dataLength);
        addCRC16();
    }
    
}
