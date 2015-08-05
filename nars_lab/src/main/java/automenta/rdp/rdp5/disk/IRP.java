package automenta.rdp.rdp5.disk;

import automenta.rdp.AbstractRdpPacket;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class IRP {

    public int fileId;
    
    public int majorFunction;
    
    public int minorFunction; 

    public DataOutputStream out;
    public ByteArrayOutputStream bout;
    
    public AbstractRdpPacket data;
    
    public int deviceId;
    
    public int completionId;
    
    
    public IRP(int fileId, int majorFunction, int minorFunction) {
        super();
        this.fileId = fileId;
        this.majorFunction = majorFunction;
        this.minorFunction = minorFunction;
        
        bout = new ByteArrayOutputStream();
        out = new DataOutputStream(bout);
    }
    
}
