/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dataserver.message;

/**
 *
 * @author Filippo
 */
public class KeepAliveError extends Packet{
    
    public KeepAliveError(byte[] buffer) {
        super(buffer);
    }

    public KeepAliveError() {
        buffer = new byte[3];
        set8(0, 10);
        addCRC16();
    }

}
