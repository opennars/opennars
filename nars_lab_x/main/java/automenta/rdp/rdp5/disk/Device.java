package automenta.rdp.rdp5.disk;

import automenta.rdp.AbstractRdpPacket;
import automenta.rdp.rdp5.VChannel;

import java.io.IOException;

public interface Device {

    public int getType();
    public String getName();
    public void setChannel(VChannel channel);
    
    public int process(AbstractRdpPacket data, IRP irp) throws IOException;
    
}
