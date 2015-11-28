package automenta.rdp.rdp5.disk;

import automenta.rdp.AbstractRdpPacket;
import automenta.rdp.CommunicationMonitor;
import automenta.rdp.RdesktopException;
import automenta.rdp.crypto.CryptoException;
import automenta.rdp.rdp.RdpPacket;
import automenta.rdp.rdp5.VChannel;
import automenta.rdp.rdp5.VChannels;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 专做远和程盘映射的cnannel
 * @author blee
 *
 */
public class DiskChannel extends VChannel implements DiskConst {
    
    private int versionMajor;
    private int versionMinor;
    private int clientID;
    
    private List<Device> devices;

    public DiskChannel() {
        super();
        devices = new ArrayList<Device>();
    }
    
    public void addDiskDevice(String diskName, String basePath) {
        DiskDevice dv = new DiskDevice(diskName, basePath);
        dv.setChannel(this);
        devices.add(dv);
    }

    @Override
    public String name() {
        return "rdpdr";
    }
    
    @Override
    public boolean mustEncrypt() {
        return versionMinor != 0x0C;
    }

    @Override
    public int flags() {
        return VChannels.CHANNEL_OPTION_INITIALIZED /*| VChannels.CHANNEL_OPTION_ENCRYPT_RDP*/
                | VChannels.CHANNEL_OPTION_COMPRESS_RDP;
    }
    
    private int receive_packet_index = 0;
    private int send_packet_index = 0;

    @Override
    public void process(AbstractRdpPacket data) throws RdesktopException, IOException,
            CryptoException {
//        int size = data.size();
//        boolean mark = false;
//        if(size > 0x60) {
//            size = 0x60;
//            mark = true;
//        }
//        int position = data.getPosition();
//        byte[] dump = new byte[size-position];
//        data.copyToByteArray(dump, 0, position, size-position);
//        System.out.print("\n"+(receive_packet_index++)+"------------------->>>>>>>>>>>>>>> data recieved.");
//        System.out.println(HexDump.dumpHexString(dump));
//        if(mark) {
//            System.out.println(".....");
//        }
        
        int component = data.getLittleEndian16();
        int packetId = data.getLittleEndian16();
        if(component == RDPDR_CTYP_CORE) {
            switch(packetId) {
            case PAKID_CORE_SERVER_ANNOUNCE:
                rdpdr_process_server_announce_request(data);
                rdpdr_send_client_announce_reply(data);
                rdpdr_send_client_name_request(data);
                break;
            case PAKID_CORE_SERVER_CAPABILITY:
                rdpdr_process_capability_request(data);
                rdpdr_send_capability_response(data);
                break;
            case PAKID_CORE_CLIENTID_CONFIRM:
                rdpdr_process_server_clientid_confirm(data);
                if(versionMinor == 0x0005) {
                    rdpdr_send_device_list_announce_request(data);
                }
                break;
            case PAKID_CORE_USER_LOGGEDON:
                rdpdr_send_device_list_announce_request(data);
                break;
            case PAKID_CORE_DEVICE_REPLY:
                System.out.println(data.getLittleEndian32() + " status = " + data.getLittleEndian32());
//                /*int deviceID = */data.getLittleEndian32();
//                /*int status = */data.getLittleEndian32();
                break;
            case PAKID_CORE_DEVICE_IOREQUEST:
                rdpdr_process_irp(data);
                break;
            }
        }
    }
    
    private void rdpdr_process_server_announce_request(AbstractRdpPacket data) {
        versionMajor = data.getLittleEndian16();
        versionMinor = data.getLittleEndian16();
        clientID = data.getLittleEndian32();
    }
    
    private void rdpdr_send_client_announce_reply(AbstractRdpPacket data) {
        RdpPacket s = new RdpPacket(12);
        s.setLittleEndian16(RDPDR_CTYP_CORE);
        s.setLittleEndian16(PAKID_CORE_CLIENTID_CONFIRM);
        s.setLittleEndian16(1);// versionMajor, must be set to 1
        s.setLittleEndian16(versionMinor);// versionMinor
        if (clientID > 0) {
            s.setLittleEndian32(clientID); // clientID, given by the server in a Server Announce Request
        } else {
            s.setLittleEndian32(0x815ed39d);// /* IP address (use 127.0.0.1) // 0x815ed39d */
        }
        s.markEnd();

        try {
            this.send_packet(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void rdpdr_send_client_name_request(AbstractRdpPacket data) {
        int clientNameLen = CLIENT_NAME.length() * 2;
        RdpPacket s =new RdpPacket(16 + clientNameLen + 2);

        s.setLittleEndian16(RDPDR_CTYP_CORE);
        s.setLittleEndian16(PAKID_CORE_CLIENT_NAME);
        s.setLittleEndian32(0x00000001);/* unicodeFlag, 0 for ASCII and 1 for Unicode */
        s.setLittleEndian32(0);/* codePage, must be set to zero */
        s.setLittleEndian32(clientNameLen + 2); //ComputerNameLen,including null terminator.
        if (clientNameLen > 0) {
            try {
                s.copyFromByteArray(CLIENT_NAME.getBytes("UTF-16LE"), 0,
                        s.position(), clientNameLen);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            s.positionAdd(clientNameLen);
        }
        s.setLittleEndian16(0);//the null terminator of client name
        s.markEnd();

        try {
            this.send_packet(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void rdpdr_process_capability_request(AbstractRdpPacket data) {
        int numCapabilities = data.getLittleEndian16();
        data.positionAdd(2);//2 bytes padding
        
        for(int i = 0; i < numCapabilities; i++) {
            int capabilityType = data.getLittleEndian16();
            int capabilityLength;
            switch(capabilityType) {
            case CAP_GENERAL_TYPE:
            case CAP_PRINTER_TYPE:
            case CAP_PORT_TYPE:
            case CAP_DRIVE_TYPE:
            case CAP_SMARTCARD_TYPE:
                capabilityLength = data.getLittleEndian16();
                data.positionAdd(capabilityLength - 4);
                break;
            }
        }
    }
    
    private void rdpdr_send_capability_response(AbstractRdpPacket data) {
        RdpPacket s = new RdpPacket(0x54);
        s.setLittleEndian16(RDPDR_CTYP_CORE);
        s.setLittleEndian16(PAKID_CORE_CLIENT_CAPABILITY);
        
        s.setLittleEndian16(5);
        s.setLittleEndian16(0);// padding
        
        //general
        s.setLittleEndian16(CAP_GENERAL_TYPE);
        s.setLittleEndian16(44);
        s.setLittleEndian32(GENERAL_CAPABILITY_VERSION_02);//header
        s.setLittleEndian32(0);// osType, ignored on receipt
        s.setLittleEndian32(0);// osVersion, unused and must be set to zero
        s.setLittleEndian16(1); // protocolMajorVersion, must be set to 1
        s.setLittleEndian16(versionMinor);// protocolMinorVersion
        s.setLittleEndian32(0x0000FFFF); // ioCode1
        s.setLittleEndian32(0);// ioCode2, must be set to zero, reserved for future use
        s.setLittleEndian32(RDPDR_DEVICE_REMOVE_PDUS | RDPDR_CLIENT_DISPLAY_NAME_PDU | RDPDR_USER_LOGGEDON_PDU);
        s.setLittleEndian32(ENABLE_ASYNCIO);
        s.setLittleEndian32(0);
        s.setLittleEndian32(0);
        
        //printer
        s.setLittleEndian16(CAP_PRINTER_TYPE);
        s.setLittleEndian16(8);
        s.setLittleEndian32(PRINT_CAPABILITY_VERSION_01);
        
        //port
        s.setLittleEndian16(CAP_PORT_TYPE); /* third */
        s.setLittleEndian16(8); /* length */
        s.setLittleEndian32(PORT_CAPABILITY_VERSION_01);
        
        //driver
        s.setLittleEndian16(CAP_DRIVE_TYPE); /* fourth */
        s.setLittleEndian16(8); /* length */
        s.setLittleEndian32(DRIVE_CAPABILITY_VERSION_01);
        
        //smartcard
        s.setLittleEndian16(CAP_SMARTCARD_TYPE); /* fifth */
        s.setLittleEndian16(8); /* length */
        s.setLittleEndian32(SMARTCARD_CAPABILITY_VERSION_01);
        
        s.markEnd();

        try {
            this.send_packet(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private void rdpdr_process_server_clientid_confirm(AbstractRdpPacket data) {
        int _versionMajor = data.getLittleEndian16();
        int _versionMinor = data.getLittleEndian16();
        int _clientId = data.getLittleEndian32();
        versionMajor = _versionMajor;
        versionMinor = _versionMinor;
        clientID = _clientId;
    }
    
    private int announcedata_size() {
        int size;
        size = 8; /* static announce size */
        size += devices.size() * 0x14;

//        for (RdpdrDevice dev : devices) {
//            if (dev.type == RDPDR_DTYP_PRINT) {
//                size += dev.deviceData.size();
//            }
//        }

        return size;
    }
    
    private void rdpdr_send_device_list_announce_request(AbstractRdpPacket data) {
        RdpPacket s = new RdpPacket(announcedata_size());
        s.setLittleEndian16(RDPDR_CTYP_CORE);
        s.setLittleEndian16(PAKID_CORE_DEVICELIST_ANNOUNCE);
        
        s.setLittleEndian32(devices.size());
        
        for(int i = 0; i < devices.size(); i++) {
            Device dev = devices.get(i);
            s.setLittleEndian32(dev.getType());
            s.setLittleEndian32(i);//device id
            String name = dev.getName().replace(" ", "_").substring(0,
                    dev.getName().length() > 8 ? 8 : dev.getName().length());
            s.copyFromByteArray(name.getBytes(), 0, s.position(),
                    name.length());
            s.positionAdd(8);
            
            s.setLittleEndian32(0);//datalength
            //TODO write data,but disk has no data, so ignore now.
            
        }
        s.markEnd();
        
        try {
            this.send_packet(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static byte[] empty_buffer = new byte[0];
    
    private void rdpdr_process_irp(AbstractRdpPacket data) {
        int deviceId = data.getLittleEndian32();
        int fileId = data.getLittleEndian32();
        int completionId = data.getLittleEndian32();
        int majorFunction = data.getLittleEndian32();
        int minorFunction = data.getLittleEndian32();
        
        Device device = devices.get(deviceId);

        if(device != null) {
            IRP irp = new IRP(fileId, majorFunction, minorFunction);
            irp.deviceId = deviceId;
            irp.completionId = completionId;
            irp.data = data;
            
            byte[] buffer = null;
            
            int ioStatus = 0;
            
            try {
                ioStatus = device.process(data, irp);
                if(ioStatus != RD_STATUS_PENDING) {
                    irp.out.flush();
                    irp.bout.flush();
                    buffer = irp.bout.toByteArray();
                } else {
                    buffer = empty_buffer;
                }
            } catch (IOException e) {
                e.printStackTrace();
                buffer = empty_buffer;
            }
            
            if(ioStatus != RD_STATUS_PENDING) {
                //device i/o response header
                RdpPacket s = new RdpPacket(16 + (ioStatus == RD_STATUS_CANCELLED ? 4 : buffer.length));
                s.setLittleEndian16(RDPDR_CTYP_CORE);// PAKID_CORE_DEVICE_REPLY?
                s.setLittleEndian16(PAKID_CORE_DEVICE_IOCOMPLETION);
                s.setLittleEndian32(deviceId);
                s.setLittleEndian32(completionId);
                s.setLittleEndian32(ioStatus);
                if(ioStatus == RD_STATUS_CANCELLED) {
                    s.setLittleEndian32(0);
                } else {
                    if(buffer.length > 0) {
                        s.copyFromByteArray(buffer, 0, s.position(), buffer.length);
                    }
                }
                
                s.markEnd();
                try {
                    this.send_packet(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        }
    }
    
    public void send_packet(RdpPacket s) throws RdesktopException, IOException, CryptoException {
        synchronized (CommunicationMonitor.synch) {
            super.send_packet(s);
        }

//        int size = s.size();
//        boolean mark = false;
//        if(size > 0x60) {
//            size = 0x60;
//            mark = true;
//        }
//        byte[] dump = new byte[size];
//        s.copyToByteArray(dump, 0, 0, size);
//        System.out.print("\n"+(send_packet_index++)+"=======================>>>>>>>> data sent");
//        System.out.println(HexDump.dumpHexString(dump));
//        if(mark) {
//            System.out.println(".....");
//        }
    }

}
