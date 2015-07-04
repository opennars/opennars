package automenta.rdp.rdp5.disk;

import java.io.IOException;

import automenta.rdp.AbstractRdpPacket;
import automenta.rdp.rdp5.VChannel;

public interface Device {

    public int getType();
    public String getName();
    public void setChannel(VChannel channel);
    
    public int process(AbstractRdpPacket data, IRP irp) throws IOException;
    
}
