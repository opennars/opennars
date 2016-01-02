/* Rdp.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Rdp layer of communication
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * 
 * (See gpl.txt for details of the GNU General Public License.)
 * 
 */
package automenta.rdp;

import automenta.rdp.crypto.CryptoException;
import automenta.rdp.rdp.RdpPacket;
import automenta.rdp.rdp5.VChannels;
import org.apache.log4j.Logger;

import java.awt.image.IndexColorModel;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

public class Rdp {

    public static int RDP5_DISABLE_NOTHING = 0x00;

    public static int RDP5_NO_WALLPAPER = 0x01;

    public static int RDP5_NO_FULLWINDOWDRAG = 0x02;

    public static int RDP5_NO_MENUANIMATIONS = 0x04;

    public static int RDP5_NO_THEMING = 0x08;

    public static int RDP5_NO_CURSOR_SHADOW = 0x20;

    public static int RDP5_NO_CURSORSETTINGS = 0x40; /*
                                                         * disables cursor
														 * blinking
														 */

    protected static final Logger logger = Logger.getLogger(Rdp.class);

    /* constants for RDP Layer */
    public static final int RDP_LOGON_NORMAL = 0x33;

    public static final int RDP_LOGON_AUTO = 0x8;

    public static final int RDP_LOGON_BLOB = 0x100;

    // PDU Types
    private static final int RDP_PDU_DEMAND_ACTIVE = 1;

    private static final int RDP_PDU_CONFIRM_ACTIVE = 3;

    private static final int RDP_PDU_DEACTIVATE = 6;

    private static final int RDP_PDU_DATA = 7;

    // Data PDU Types
    private static final int RDP_DATA_PDU_UPDATE = 2;

    private static final int RDP_DATA_PDU_CONTROL = 20;

    private static final int RDP_DATA_PDU_POINTER = 27;

    private static final int RDP_DATA_PDU_INPUT = 28;

    private static final int RDP_DATA_PDU_SYNCHRONISE = 31;

    private static final int RDP_DATA_PDU_BELL = 34;

    private static final int RDP_DATA_PDU_LOGON = 38;

    private static final int RDP_DATA_PDU_FONT2 = 39;

    private static final int RDP_DATA_PDU_DISCONNECT = 47;

    // Control PDU types
    private static final int RDP_CTL_REQUEST_CONTROL = 1;

    private static final int RDP_CTL_GRANT_CONTROL = 2;

    private static final int RDP_CTL_DETACH = 3;

    private static final int RDP_CTL_COOPERATE = 4;

    // Update PDU Types
    private static final int RDP_UPDATE_ORDERS = 0;

    private static final int RDP_UPDATE_BITMAP = 1;

    private static final int RDP_UPDATE_PALETTE = 2;

    private static final int RDP_UPDATE_SYNCHRONIZE = 3;

    // Pointer PDU Types
    private static final int RDP_POINTER_SYSTEM = 1;

    private static final int RDP_POINTER_MOVE = 3;

    private static final int RDP_POINTER_COLOR = 6;

    private static final int RDP_POINTER_CACHED = 7;

    private static final int RDP_POINTER_NEW = 8;

    // System Pointer Types
    private static final int RDP_NULL_POINTER = 0;

    private static final int RDP_DEFAULT_POINTER = 0x7F00;

    // Input Devices
    private static final int RDP_INPUT_SYNCHRONIZE = 0;

    private static final int RDP_INPUT_CODEPOINT = 1;

    private static final int RDP_INPUT_VIRTKEY = 2;

    private static final int RDP_INPUT_SCANCODE = 4;

    private static final int RDP_INPUT_MOUSE = 0x8001;

    /* RDP capabilities */
    private static final int RDP_CAPSET_GENERAL = 1;

    private static final int RDP_CAPLEN_GENERAL = 0x18;

    private static final int OS_MAJOR_TYPE_UNIX = 4;

    private static final int OS_MINOR_TYPE_XSERVER = 7;

    private static final int RDP_CAPSET_BITMAP = 2;

    private static final int RDP_CAPLEN_BITMAP = 0x1C;

    private static final int RDP_CAPSET_ORDER = 3;

    private static final int RDP_CAPLEN_ORDER = 0x58;

    private static final int ORDER_CAP_NEGOTIATE = 2;

    private static final int ORDER_CAP_NOSUPPORT = 4;

    private static final int RDP_CAPSET_BMPCACHE = 4;

    private static final int RDP_CAPLEN_BMPCACHE = 0x28;

    private static final int RDP_CAPSET_CONTROL = 5;

    private static final int RDP_CAPLEN_CONTROL = 0x0C;

    private static final int RDP_CAPSET_ACTIVATE = 7;

    private static final int RDP_CAPLEN_ACTIVATE = 0x0C;

    private static final int RDP_CAPSET_POINTER = 8;

    private static final int RDP_CAPLEN_POINTER = 0x08;

    private static final int RDP_CAPLEN_NEWPOINTER = 0x0a;

    private static final int RDP_CAPSET_SHARE = 9;

    private static final int RDP_CAPLEN_SHARE = 0x08;

    private static final int RDP_CAPSET_COLCACHE = 10;

    private static final int RDP_CAPLEN_COLCACHE = 0x08;

    private static final int RDP_CAPSET_UNKNOWN = 13;

    private static final int RDP_CAPLEN_UNKNOWN = 0x9C;

    private static final int RDP_CAPSET_BMPCACHE2 = 19;

    private static final int RDP_CAPLEN_BMPCACHE2 = 0x28;

    private static final int BMPCACHE2_FLAG_PERSIST = (1 << 31);

    /* RDP bitmap cache (version 2) constants */
    public static final int BMPCACHE2_C0_CELLS = 0x78;

    public static final int BMPCACHE2_C1_CELLS = 0x78;

    public static final int BMPCACHE2_C2_CELLS = 0x150;

    public static final int BMPCACHE2_NUM_PSTCELLS = 0x9f6;

    private static final int RDP5_FLAG = 0x0030;

    private static final int INFOTYPE_LOGON = 0;
    private static final int INFOTYPE_LOGON_LONG = 1;
    private static final int INFOTYPE_LOGON_PLAINNOTIFY = 2;
    private static final int INFOTYPE_LOGON_EXTENDED_INF = 3;

    private static final int LOGON_EX_AUTORECONNECTCOOKIE = 1;
    private static final int LOGON_EX_LOGONERRORS = 2;

    private static final byte[] RDP_SOURCE = {(byte) 0x4D, (byte) 0x53,
            (byte) 0x54, (byte) 0x53, (byte) 0x43, (byte) 0x00}; // string

    // MSTSC
    // encoded
    // as 7 byte
    // US-Ascii

    protected final Secure SecureLayer;

    private RdesktopFrame frame = null;

    private RdesktopCanvas surface = null;

    protected final Orders orders;

    private final Cache cache;

    private int next_packet = 0;

    private int rdp_shareid = 0;

    private boolean connected = false;

    private RdpPacket stream = null;

    /*
     * private final byte[] canned_caps = { (byte)0x01, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x09, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x04,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x0C, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0C, (byte)0x00, (byte)0x08,
     * (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0E,
     * (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00,
     * (byte)0x00, (byte)0x10, (byte)0x00, (byte)0x34, (byte)0x00, (byte)0xFE,
     * (byte)0x00, (byte)0x04, (byte)0x00, (byte)0xFE, (byte)0x00, (byte)0x04,
     * (byte)0x00, (byte)0xFE, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0xFE,
     * (byte)0x00, (byte)0x08, (byte)0x00, (byte)0xFE, (byte)0x00, (byte)0x10,
     * (byte)0x00, (byte)0xFE, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0xFE,
     * (byte)0x00, (byte)0x40, (byte)0x00, (byte)0xFE, (byte)0x00, (byte)0x80,
     * (byte)0x00, (byte)0xFE, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x40,
     * (byte)0x00, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x01, (byte)0x00,
     * (byte)0x01, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00 };
     */
    private final byte[] canned_caps = {0x01, 0x00, 0x00, 0x00, 0x09, 0x04,
            0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0C,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x0C, 0x00, 0x08, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0E, 0x00,
            0x08, 0x00, 0x01, 0x00, 0x00, 0x00, 0x10, 0x00, 0x34, 0x00,
            (byte) 0xfe, 0x00, 0x04, 0x00, (byte) 0xfe, 0x00, 0x04, 0x00,
            (byte) 0xFE, 0x00, 0x08, 0x00, (byte) 0xFE, 0x00, 0x08, 0x00,
            (byte) 0xFE, 0x00, 0x10, 0x00, (byte) 0xFE, 0x00, 0x20, 0x00,
            (byte) 0xFE, 0x00, 0x40, 0x00, (byte) 0xFE, 0x00, (byte) 0x80,
            0x00, (byte) 0xFE, 0x00, 0x00, 0x01, 0x40, 0x00, 0x00, 0x08, 0x00,
            0x01, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00};

    static byte caps_0x0d[] = {0x01, 0x00, 0x00, 0x00, 0x09, 0x04, 0x00, 0x00,
            0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    static byte caps_0x0c[] = {0x01, 0x00, 0x00, 0x00};

    static byte caps_0x0e[] = {0x01, 0x00, 0x00, 0x00};

    static byte caps_0x10[] = {(byte) 0xFE, 0x00, 0x04, 0x00, (byte) 0xFE,
            0x00, 0x04, 0x00, (byte) 0xFE, 0x00, 0x08, 0x00, (byte) 0xFE, 0x00,
            0x08, 0x00, (byte) 0xFE, 0x00, 0x10, 0x00, (byte) 0xFE, 0x00, 0x20,
            0x00, (byte) 0xFE, 0x00, 0x40, 0x00, (byte) 0xFE, 0x00,
            (byte) 0x80, 0x00, (byte) 0xFE, 0x00, 0x00, 0x01, 0x40, 0x00, 0x00,
            0x08, 0x00, 0x01, 0x00, 0x01, 0x02, 0x00, 0x00, 0x00};

    /**
     * Process a general capability set
     *
     * @param data Packet containing capability set data at current read position
     */
    static void processGeneralCaps(RdpPacket data) {
        int pad2octetsB; /* rdp5 flags? */

        data.positionAdd(10); // in_uint8s(s, 10);
        pad2octetsB = data.getLittleEndian16(); // in_uint16_le(s, pad2octetsB);

        if (pad2octetsB != 0)
            Options.use_rdp5 = false;
    }

    /**
     * Process a bitmap capability set
     *
     * @param data Packet containing capability set data at current read position
     */
    static void processBitmapCaps(RdpPacket data) {
        int width, height, bpp;

        bpp = data.getLittleEndian16(); // in_uint16_le(s, bpp);
        data.positionAdd(6); // in_uint8s(s, 6);

        width = data.getLittleEndian16(); // in_uint16_le(s, width);
        height = data.getLittleEndian16(); // in_uint16_le(s, height);

        logger.debug("setting desktop size and bpp to: " + width + 'x' + height
                + 'x' + bpp);

		/*
		 * The server may limit bpp and change the size of the desktop (for
		 * example when shadowing another session).
		 */
        if (Options.server_bpp != bpp) {
            logger.warn("colour depth changed from " + Options.server_bpp
                    + " to " + bpp);
            Options.server_bpp = bpp;
        }
        if (Options.width != width || Options.height != height) {
            logger.warn("screen size changed from " + Options.width + 'x'
                    + Options.height + " to " + width + 'x' + height);
            Options.width = width;
            Options.height = height;
            // ui_resize_window(); TODO: implement resize thingy
        }
    }

    /**
     * Process server capabilities
     *
     * @param data Packet containing capability set data at current read position
     */
    static void processServerCaps(RdpPacket data, int length) {
        int n;
        int next, start;
        int ncapsets, capset_type, capset_length;

        start = data.position();

        ncapsets = data.getLittleEndian16(); // in_uint16_le(s, ncapsets);
        data.positionAdd(2); // in_uint8s(s, 2); /* pad */

        for (n = 0; n < ncapsets; n++) {
            if (data.position() > start + length)
                return;

            capset_type = data.getLittleEndian16(); // in_uint16_le(s,
            // capset_type);
            capset_length = data.getLittleEndian16(); // in_uint16_le(s,
            // capset_length);

            next = data.position() + capset_length - 4;

            switch (capset_type) {
                case RDP_CAPSET_GENERAL:
                    processGeneralCaps(data);
                    break;

                case RDP_CAPSET_BITMAP:
                    processBitmapCaps(data);
                    break;
            }

            data.position(next);
        }
    }

    protected static void processPduLogon(RdpPacket data) {
        int infoType = data.getLittleEndian32();
        if (infoType == INFOTYPE_LOGON_EXTENDED_INF) {
            data.getLittleEndian16();
            int fieldsPresent = data.getLittleEndian32();
            if ((fieldsPresent & LOGON_EX_AUTORECONNECTCOOKIE) != 0) {
                data.getLittleEndian32();//cbFieldData
                int len = data.getLittleEndian32();
                int version = 0;
                if (len != 28) {
                    logger.warn("Invalid length in Auto-Reconnect packet\n");
                    return;
                }
                version = data.getLittleEndian32();
                if (version != 1) {
                    logger.warn("Unsupported version of Auto-Reconnect packet\n");
                    return;
                }
                Rdesktop.gReconnectLogonid = data.getLittleEndian32();
                data.positionAdd(16);
                //...
            }
        }
    }

    /**
     * Process a disconnect PDU
     *
     * @param data Packet containing disconnect PDU at current read position
     * @return Code specifying the reason for disconnection
     */
    protected static int processDisconnectPdu(RdpPacket data) {
        logger.debug("Received disconnect PDU");
        return data.getLittleEndian32();
    }

    /**
     * Initialise RDP comms layer, and register virtual channels
     *
     * @param channels Virtual channels to be used in connection
     */
    public Rdp(VChannels channels) {
        this.SecureLayer = new Secure(channels);
        Common.secure = SecureLayer;
        this.orders = new Orders();
        this.cache = new Cache();
        Orders.registerCache(cache);
    }

    /**
     * Initialise a packet for sending data on the RDP layer
     *
     * @param size Size of RDP data
     * @return Packet initialised for RDP
     * @throws RdesktopException
     */
    public RdpPacket newPacket(int size)  {
        RdpPacket buffer = SecureLayer.init(
                Constants.encryption ? Secure.SEC_ENCRYPT : 0, size + 18);
        buffer.pushLayer(AbstractRdpPacket.RDP_HEADER, 18);
        // buffer.setHeader(RdpPacket_Localised.RDP_HEADER);
        // buffer.incrementPosition(18);
        // buffer.setStart(buffer.getPosition());
        return buffer;
    }

    /**
     * Send a packet on the RDP layer
     *
     * @param data          Packet to send
     * @param data_pdu_type Type of data
     * @throws RdesktopException
     * @throws IOException
     * @throws CryptoException
     */
    private void send(RdpPacket data, int data_pdu_type)
            throws RdesktopException, IOException, CryptoException {

        synchronized (CommunicationMonitor.synch) {

            int length;

            data.position(data.getHeader(AbstractRdpPacket.RDP_HEADER));
            length = data.getEnd() - data.position();

            data.setLittleEndian16(length);
            data.setLittleEndian16(RDP_PDU_DATA | 0x10);
            data.setLittleEndian16(SecureLayer.getUserID() + 1001);

            data.setLittleEndian32(this.rdp_shareid);
            data.set8(0); // pad
            data.set8(1); // stream id
            data.setLittleEndian16(length - 14);
            data.set8(data_pdu_type);
            data.set8(0); // compression type
            data.setLittleEndian16(0); // compression length

            SecureLayer.send(data, Constants.encryption ? Secure.SEC_ENCRYPT : 0);

        }
    }

    /**
     * Receive a packet from the RDP layer
     *
     * @param type Type of PDU received, stored in type[0]
     * @return Packet received from RDP layer
     * @throws IOException
     * @throws RdesktopException
     * @throws CryptoException
     * @throws OrderException
     */
    private RdpPacket recv(int[] type) throws IOException,
            RdesktopException, CryptoException, OrderException {
        int length = 0;
        if ((this.stream == null) || (this.next_packet >= this.stream.getEnd())) {
            this.stream = SecureLayer.receive();
            if (stream == null)
                return null;
            this.next_packet = this.stream.position();
        } else {
            this.stream.position(this.next_packet);
        }
        length = this.stream.getLittleEndian16();

		/* 32k packets are really 8, keepalive fix - rdesktop 1.2.0 */
        if (length == 0x8000) {
            //logger.warn("32k packet keepalive fix");
            next_packet += 8;
            type[0] = 0;
            return stream;
        }
        type[0] = this.stream.getLittleEndian16() & 0xf;
        if (stream.position() != stream.getEnd()) {
            stream.positionAdd(2);//user id
        }

        if (Options.debug_hexdump) {
            byte[] packet = new byte[length];
            stream.copyToByteArray(packet, 0, next_packet, length);
            System.out.println(String.format("\nreceive RDP packet"));
            System.out.println(automenta.rdp.tools.HexDump.dumpHexString(packet));
        }

        this.next_packet += length;
        return stream;
    }

    /**
     * Connect to a server
     *
     * @param username  Username for log on
     * @param server    Server to connect to
     * @param flags     Flags defining logon type
     * @param domain    Domain for log on
     * @param password  Password for log on
     * @param command   Alternative shell for session
     * @param directory Initial working directory for connection
     * @throws ConnectionException
     */
    public void connect(String username, InetAddress server, int flags,
                        String domain, String password, String command, String directory)
            throws ConnectionException {
        try {
            SecureLayer.connect(server);
            this.connected = true;
            this.sendLogonInfo(flags, domain, username, password, command,
                    directory);
        }
        // Handle an unresolvable hostname
        catch (UnknownHostException e) {
            throw new ConnectionException("Could not resolve host name: "
                    + server);
        }
        // Handle a refused connection
        catch (ConnectException e) {
            throw new ConnectionException(
                    "Connection refused when trying to connect to " + server
                            + " on port " + Options.port);
        }
        // Handle a timeout on connecting
        catch (NoRouteToHostException e) {
            throw new ConnectionException(
                    "Connection timed out when attempting to connect to "
                            + server);
        } catch (IOException e) {
            logger.info(e.toString());
            e.printStackTrace();
            throw new ConnectionException("Connection Failed");
        } catch (RdesktopException e) {
            throw new ConnectionException(e.getMessage());
        } catch (OrderException e) {
            throw new ConnectionException(e.getMessage());
        } catch (CryptoException e) {
            throw new ConnectionException(e.getMessage());
        }

    }

    /**
     * Disconnect from an RDP session
     */
    public void disconnect() {
        this.connected = false;
        SecureLayer.disconnect();
    }

    /**
     * Retrieve status of connection
     *
     * @return True if connection to RDP session
     */
    public boolean isConnected() {
        return this.connected;
    }

    boolean deactivated;

    int ext_disc_reason;

    /**
     * RDP receive loop
     *
     * @param deactivated     On return, stores true in deactivated[0] if the session
     *                        disconnected cleanly
     * @param ext_disc_reason On return, stores the reason for disconnection in
     *                        ext_disc_reason[0]
     * @throws IOException
     * @throws RdesktopException
     * @throws OrderException
     * @throws CryptoException
     */
    public void mainLoop(boolean[] deactivated, int[] ext_disc_reason)
            throws IOException, RdesktopException, OrderException,
            CryptoException {
        int[] type = new int[1];
        boolean disc = false; /* True when a disconnect PDU was received */
        boolean cont = true;

        RdpPacket data = null;

        while (cont) {
            try {
                data = this.recv(type);
                if (data == null)
                    return;
            } catch (EOFException e) {
                return;
            }
            //System.out.println("dataType=" + type[0]);
            switch (type[0]) {

                case (Rdp.RDP_PDU_DEMAND_ACTIVE):
                    //logger.debug("Rdp.RDP_PDU_DEMAND_ACTIVE");
                    // get this after licence negotiation, just before the 1st
                    // order...
                    //NDC.push("processDemandActive");

                    this.processDemandActive(data);

                    // can use this to trigger things that have to be done before
                    // 1st order
                    //logger.debug("ready to send (got past licence negotiation)");
                    Rdesktop.readytosend = true;
                    frame.triggerReadyToSend();
                    //NDC.pop();

                    deactivated[0] = false;
                    break;

                case (Rdp.RDP_PDU_DEACTIVATE):
                    // get this on log off
                    deactivated[0] = true;
                    this.stream = null; // ty this fix
                    break;

                case (Rdp.RDP_PDU_DATA):
                    // all the others should be this
                    //NDC.push("processData");

                    disc = this.processData(data, ext_disc_reason);
                    //NDC.pop();
                    break;

                case 0:
                    break; // 32K keep alive fix, see receive() - rdesktop 1.2.0.

                default:
                    throw new RdesktopException("Unimplemented type in main loop :"
                            + type[0]);
            }

            if (disc)
                return;
        }
        return;
    }

    /**
     * Send user logon details to the server
     *
     * @param flags     Set of flags defining logon type
     * @param domain    Domain for logon
     * @param username  Username for logon
     * @param password  Password for logon
     * @param command   Alternative shell for session
     * @param directory Starting working directory for session
     * @throws RdesktopException
     * @throws IOException
     * @throws CryptoException
     */
    private void sendLogonInfo(int flags, String domain, String username,
                               String password, String command, String directory)
            throws RdesktopException, IOException, CryptoException {

        int len_ip = 2 * "127.0.0.1".length();
        int len_dll = 2 * "C:\\WINNT\\System32\\mstscax.dll".length();
        int packetlen = 0;

        int sec_flags = Constants.encryption ? (Secure.SEC_LOGON_INFO | Secure.SEC_ENCRYPT)
                : Secure.SEC_LOGON_INFO;
        int domainlen = 2 * domain.length();
        int userlen = 2 * username.length();
        int passlen = 2 * password.length();
        int commandlen = 2 * command.length();
        int dirlen = 2 * directory.length();

        RdpPacket data;

        if (!Options.use_rdp5 || 1 == Options.server_rdp_version) {
            logger.debug("Sending RDP4-style Logon packet");

            data = SecureLayer.init(sec_flags, 18 + domainlen + userlen
                    + passlen + commandlen + dirlen + 10);

            data.setLittleEndian32(0);
            data.setLittleEndian32(flags);
            data.setLittleEndian16(domainlen);
            data.setLittleEndian16(userlen);
            data.setLittleEndian16(passlen);
            data.setLittleEndian16(commandlen);
            data.setLittleEndian16(dirlen);
            data.outUnicodeString(domain, domainlen);
            data.outUnicodeString(username, userlen);
            data.outUnicodeString(password, passlen);
            data.outUnicodeString(command, commandlen);
            data.outUnicodeString(directory, dirlen);

        } else {
            flags |= RDP_LOGON_BLOB;
            logger.debug("Sending RDP5-style Logon packet");
            packetlen = /* size of TS_INFO_PACKET */
                    4 + /* CodePage */
                            4 + /* flags */
                            2 + /* cbDomain */
                            2 + /* cbUserName */
                            2 + /* cbPassword */
                            2 + /* cbAlternateShell */
                            2 + /* cbWorkingDir */
                            2 + domainlen +    /* Domain */
                            2 + userlen +  /* UserName */
                            2 + passlen +  /* Password */
                            2 + commandlen +   /* AlternateShell */
                            2 + dirlen + /* WorkingDir */
		            /* size of TS_EXTENDED_INFO_PACKET */
                            2 + /* clientAdressFamily */
                            2 + /* cbClientAdress */
                            len_ip +    /* clientAddress */
                            2 + /* cbClientDir */
                            len_dll +   /* clientDir */
		            /* size of TS_TIME_ZONE_INFORMATION */
                            4 + /* Bias, (UTC = local time + bias */
                            64 +    /* StandardName, 32 unicode char array, Descriptive standard time on client */
                            16 +    /* StandardDate */
                            4 + /* StandardBias */
                            64 +    /* DaylightName, 32 unicode char array */
                            16 +    /* DaylightDate */
                            4 + /* DaylightBias */
                            4 + /* clientSessionId */
                            4 + /* performanceFlags */
                            2 + /* cbAutoReconnectCookie, either 0 or 0x001c */
		            /* size of ARC_CS_PRIVATE_PACKET */
                            28; /* autoReconnectCookie */

            data = SecureLayer.init(sec_flags, packetlen); // s =
            // sec_init(sec_flags,
            // packetlen);
            // logger.debug("Called sec_init with packetlen " + packetlen);

            data.setLittleEndian32(0); // out_uint32(s, 0); // Unknown
            data.setLittleEndian32(flags); // out_uint32_le(s, flags);
            data.setLittleEndian16(domainlen); // out_uint16_le(s, len_domain);
            data.setLittleEndian16(userlen); // out_uint16_le(s, len_user);
            data.setLittleEndian16(passlen); // out_uint16_le(s,
            data.setLittleEndian16(commandlen); // out_uint16_le(s,
            data.setLittleEndian16(dirlen); // out_uint16_le(s,

            if (0 < domainlen) {
                data.outUnicodeString(domain, domainlen); // rdp_out_unistr(s,
            } else {
                data.setLittleEndian16(0); // out_uint16_le(s, 0);
            }

            if (0 < userlen) {
                data.outUnicodeString(username, userlen); // rdp_out_unistr(s,
            } else {
                data.setLittleEndian16(0);
            }

            if (0 < passlen) {
                data.outUnicodeString(password, passlen); // rdp_out_unistr(s,
            } else {
                data.setLittleEndian16(0);
            }

            if (0 < commandlen) {
                data.outUnicodeString(command, commandlen); // rdp_out_unistr(s,
            } else {
                data.setLittleEndian16(0);
            }

            if (0 < dirlen) {
                data.outUnicodeString(directory, dirlen); // rdp_out_unistr(s,
            } else {
                data.setLittleEndian16(0);
            }
			
			/* TS_EXTENDED_INFO_PACKET */
            data.setLittleEndian16(2); // out_uint16_le(s, 2);
            data.setLittleEndian16(len_ip + 2); // out_uint16_le(s, len_ip + 2);
            // // Length of client ip
            data.outUnicodeString("127.0.0.1", len_ip); // rdp_out_unistr(s,
            // "127.0.0.1",
            // len_ip);
            data.setLittleEndian16(len_dll + 2); // out_uint16_le(s, len_dll
            // + 2);
            data.outUnicodeString("C:\\WINNT\\System32\\mstscax.dll", len_dll); // rdp_out_unistr(s,
            // "C:\\WINNT\\System32\\mstscax.dll",
			
			/* TS_TIME_ZONE_INFORMATION */
            data.setLittleEndian16(0xffc4); // out_uint16_le(s, 0xffc4);
            data.setLittleEndian16(0xffff); // out_uint16_le(s, 0xffff);
            data.outUnicodeString("GTB, normaltid", 2 * "GTB, normaltid"
                    .length()); // rdp_out_unistr(s, "GTB, normaltid", 2 *
            // strlen("GTB, normaltid"));
            data.positionAdd(62 - 2 * "GTB, normaltid".length()); // out_uint8s(s,

            data.setLittleEndian32(0x0a0000); // out_uint32_le(s, 0x0a0000);
            data.setLittleEndian32(0x050000); // out_uint32_le(s, 0x050000);
            data.setLittleEndian32(3); // out_uint32_le(s, 3);
            data.setLittleEndian32(0); // out_uint32_le(s, 0);
            data.setLittleEndian32(0); // out_uint32_le(s, 0);

            data.outUnicodeString("GTB, sommartid", 2 * "GTB, sommartid"
                    .length()); // rdp_out_unistr(s, "GTB, sommartid", 2 *
            // strlen("GTB, sommartid"));
            data.positionAdd(62 - 2 * "GTB, sommartid".length()); // out_uint8s(s,

            data.setLittleEndian32(0x30000); // out_uint32_le(s, 0x30000);
            data.setLittleEndian32(0x050000); // out_uint32_le(s, 0x050000);
            data.setLittleEndian32(2); // out_uint32_le(s, 2);
            data.setLittleEndian32(0); // out_uint32(s, 0);
            data.setLittleEndian32(0xffffffc4); // out_uint32_le(s, 0xffffffc4);
            data.setLittleEndian32(0); // out_uint32_le(s, 0xfffffffe);
            data.setLittleEndian32(Options.rdp5_performanceflags); // out_uint32_le(s,
            // 0x0f);
            data.setLittleEndian32(0); // out_uint32(s, 0);
        }

        data.markEnd();
        byte[] buffer = new byte[data.getEnd()];
        data.copyToByteArray(buffer, 0, 0, data.getEnd());
        SecureLayer.send(data, sec_flags);
    }

    /**
     * Process an activation demand from the server (received between licence
     * negotiation and 1st order)
     *
     * @param data Packet containing demand at current read position
     * @throws RdesktopException
     * @throws IOException
     * @throws CryptoException
     * @throws OrderException
     */
    private void processDemandActive(RdpPacket data)
            throws RdesktopException, IOException, CryptoException,
            OrderException {
        int type[] = new int[1];

        this.rdp_shareid = data.getLittleEndian32();

        this.sendConfirmActive();

        this.sendSynchronize();
        this.sendControl(RDP_CTL_COOPERATE);
        this.sendControl(RDP_CTL_REQUEST_CONTROL);

        this.recv(type); // Receive RDP_PDU_SYNCHRONIZE
        this.recv(type); // Receive RDP_CTL_COOPERATE
        this.recv(type); // Receive RDP_CTL_GRANT_CONTROL

        this.sendInput(0, RDP_INPUT_SYNCHRONIZE, 0, 0, 0);
        this.sendFonts(1);
        this.sendFonts(2);

        this.recv(type); // Receive an unknown PDU Code = 0x28

        this.orders.resetOrderState();
    }

    /**
     * Process a data PDU received from the server
     *
     * @param data            Packet containing data PDU at current read position
     * @param ext_disc_reason If a disconnect PDU is received, stores disconnection reason
     *                        at ext_disc_reason[0]
     * @return True if disconnect PDU was received
     * @throws RdesktopException
     * @throws OrderException
     */
    private boolean processData(RdpPacket data, int[] ext_disc_reason)
            throws RdesktopException, OrderException {
        int data_type, ctype, clen, len, roff, rlen;
        data_type = 0;

        data.positionAdd(6); // skip shareid, pad, streamid
        len = data.getLittleEndian16();
        data_type = data.get8();
        ctype = data.get8(); // compression type
        clen = data.getLittleEndian16(); // compression length
        clen -= 18;

        switch (data_type) {

            case (Rdp.RDP_DATA_PDU_UPDATE):
                logger.debug("Rdp.RDP_DATA_PDU_UPDATE");
                this.processUpdate(data);
                break;

            case RDP_DATA_PDU_CONTROL:
                logger.debug(("Received Control PDU\n"));
                break;

            case RDP_DATA_PDU_SYNCHRONISE:
                logger.debug(("Received Sync PDU\n"));
                break;

            case (Rdp.RDP_DATA_PDU_POINTER):
                logger.debug("Received pointer PDU");
                this.processPointer(data);
                break;
            case (Rdp.RDP_DATA_PDU_BELL):
                logger.debug("Received bell PDU");
                Toolkit tx = Toolkit.getDefaultToolkit();
                tx.beep();
                break;
            case (Rdp.RDP_DATA_PDU_LOGON):
                logger.debug("User logged on");
                Rdesktop.loggedon = true;
                processPduLogon(data);
                break;
            case RDP_DATA_PDU_DISCONNECT:
			/* We used to return true and disconnect immediately here, but
			 * Windows Vista sends a disconnect PDU with reason 0 when
			 * reconnecting to a disconnected session, and MSTSC doesn't
			 * drop the connection.  I think we should just save the status.
			 */
                ext_disc_reason[0] = processDisconnectPdu(data);
                break;

            default:
                logger.warn("Unimplemented Data PDU type " + data_type);

        }
        return false;
    }

    private void processUpdate(RdpPacket data) throws OrderException,
            RdesktopException {
        final int update_type = data.getLittleEndian16();



        switch (update_type) {

            case (Rdp.RDP_UPDATE_ORDERS):
                data.positionAdd(2); // pad
                int n_orders = data.getLittleEndian16();
                data.positionAdd(2); // pad
                this.orders.processOrders(data, next_packet, n_orders);
                break;
            case (Rdp.RDP_UPDATE_BITMAP):
                this.processBitmapUpdates(data);
                break;
            case (Rdp.RDP_UPDATE_PALETTE):
                this.processPalette(data);
                break;
            case (Rdp.RDP_UPDATE_SYNCHRONIZE):
                break;
            default:
                logger.warn("Unimplemented Update type " + update_type);
        }
    }

    private void sendConfirmActive() throws RdesktopException, IOException,
            CryptoException {
        int sec_flags = Options.encryption ? (RDP5_FLAG | Secure.SEC_ENCRYPT)
                : RDP5_FLAG;

        int caplen = RDP_CAPLEN_GENERAL + RDP_CAPLEN_BITMAP + RDP_CAPLEN_ORDER
                + RDP_CAPLEN_COLCACHE
                + RDP_CAPLEN_ACTIVATE + RDP_CAPLEN_CONTROL
                + RDP_CAPLEN_SHARE
    			/*RDP_CAPLEN_BRUSHCACHE*/ + 0x58 + 0x08 + 0x08 + 0x34 /* unknown caps */
                + 4; // this is a fix
        // for W2k.
        // Purpose
        // unknown

        if (Options.use_rdp5) {
            caplen += RDP_CAPLEN_BMPCACHE2;
            caplen += RDP_CAPLEN_NEWPOINTER;
        } else {
            caplen += RDP_CAPLEN_BMPCACHE;
            caplen += RDP_CAPLEN_POINTER;
        }

        RdpPacket data = SecureLayer.init(sec_flags, 6 + 14 + caplen
                + RDP_SOURCE.length);

        // RdpPacket_Localised data = this.init(14 + caplen +
        // RDP_SOURCE.length);

        data.setLittleEndian16(2 + 14 + caplen + RDP_SOURCE.length);
        data.setLittleEndian16((RDP_PDU_CONFIRM_ACTIVE | 0x10));
        data.setLittleEndian16(Common.mcs.getUserID() /* McsUserID() */ + 1001);

        data.setLittleEndian32(this.rdp_shareid);
        data.setLittleEndian16(0x3ea); // user id
        data.setLittleEndian16(RDP_SOURCE.length);
        data.setLittleEndian16(caplen);

        data.copyFromByteArray(RDP_SOURCE, 0, data.position(),
                RDP_SOURCE.length);
        data.positionAdd(RDP_SOURCE.length);
        data.setLittleEndian16(0xd); // num_caps
        data.positionAdd(2); // pad

        Rdp.sendGeneralCaps(data);
        // ta.incrementPosition(this.RDP_CAPLEN_GENERAL);
        Rdp.sendBitmapCaps(data);
        Rdp.sendOrderCaps(data);

        if (Options.use_rdp5) {
            logger.info("Persistent caching enabled");
            Rdp.sendBitmapcache2Caps(data);
            Rdp.sendNewPointerCaps(data);
        } else {
            Rdp.sendBitmapcacheCaps(data);
            Rdp.sendPointerCaps(data);
        }

        Rdp.sendColorcacheCaps(data);
        Rdp.sendActivateCaps(data);
        Rdp.sendControlCaps(data);
        Rdp.sendShareCaps(data);
        // this.sendUnknownCaps(data);

        Rdp.sendUnknownCaps(data, 0x0d, 0x58, caps_0x0d); // rdp_out_unknown_caps(s,
        // 0x0d, 0x58,
        // caps_0x0d); /*
        // international? */
        Rdp.sendUnknownCaps(data, 0x0c, 0x08, caps_0x0c); // rdp_out_unknown_caps(s,
        // 0x0c, 0x08,
        // caps_0x0c);
        Rdp.sendUnknownCaps(data, 0x0e, 0x08, caps_0x0e); // rdp_out_unknown_caps(s,
        // 0x0e, 0x08,
        // caps_0x0e);
        Rdp.sendUnknownCaps(data, 0x10, 0x34, caps_0x10); // rdp_out_unknown_caps(s,
        // 0x10, 0x34,
        // caps_0x10); /*
        // glyph cache? */

        data.markEnd();
        logger.debug("confirm active");
        // this.send(data, RDP_PDU_CONFIRM_ACTIVE);
        Common.secure.send(data, sec_flags);
    }

    private static void sendGeneralCaps(RdpPacket data) {

        data.setLittleEndian16(RDP_CAPSET_GENERAL);
        data.setLittleEndian16(RDP_CAPLEN_GENERAL);

        data.setLittleEndian16(1); /* OS major type */
        data.setLittleEndian16(3); /* OS minor type */
        data.setLittleEndian16(0x200); /* Protocol version */
        data.setLittleEndian16(0);//pad
        data.setLittleEndian16(0); /* Compression types */
        data.setLittleEndian16(Options.use_rdp5 ? 0x40d : 0);
        // data.setLittleEndian16(Options.use_rdp5 ? 0x1d04 : 0); // this seems
		/*
		 * Pad, according to T.128. 0x40d seems to trigger the server to start
		 * sending RDP5 packets. However, the value is 0x1d04 with W2KTSK and
		 * NT4MS. Hmm.. Anyway, thankyou, Microsoft, for sending such
		 * information in a padding field..
		 */
        data.setLittleEndian16(0); /* Update capability */
        data.setLittleEndian16(0); /* Remote unshare capability */
        data.setLittleEndian16(0); /* Compression level */
        data.setLittleEndian16(0); /* Pad */
    }

    private static void sendBitmapCaps(RdpPacket data) {

        data.setLittleEndian16(RDP_CAPSET_BITMAP);
        data.setLittleEndian16(RDP_CAPLEN_BITMAP);

        data.setLittleEndian16(Options.server_bpp); /* Preferred BPP */
        data.setLittleEndian16(1); /* Receive 1 BPP */
        data.setLittleEndian16(1); /* Receive 4 BPP */
        data.setLittleEndian16(1); /* Receive 8 BPP */
        data.setLittleEndian16(800); /* Desktop width */
        data.setLittleEndian16(600); /* Desktop height */
        data.setLittleEndian16(0); /* Pad */
        data.setLittleEndian16(1); /* Allow resize */
        data.setLittleEndian16(Options.bitmap_compression ? 1 : 0); /*
																	 * Support
																	 * compression
																	 */
        data.setLittleEndian16(0); /* Unknown */
        data.setLittleEndian16(1); /* Unknown */
        data.setLittleEndian16(0); /* Pad */
    }

    private static void sendOrderCaps(RdpPacket data) {

        byte[] order_caps = new byte[32];
        order_caps[0] = 1;    /* dest blt */
        order_caps[1] = 1;  /* pat blt */
        order_caps[2] = 1;  /* screen blt */
        order_caps[3] = (byte) (Options.bitmap_caching ? 1 : 0);   /* memblt */
        order_caps[4] = 0;  /* triblt */
        order_caps[8] = 1;  /* line */
        order_caps[9] = 1;  /* line */
        order_caps[10] = 1; /* rect */
        order_caps[11] = (byte) (Constants.desktop_save ? 1 : 0);  /* desksave */
        order_caps[13] = 1; /* memblt */
        order_caps[14] = 1; /* triblt */
        order_caps[20] = (byte) (Options.polygon_ellipse_orders ? 1 : 0);    /* polygon */
        order_caps[21] = (byte) (Options.polygon_ellipse_orders ? 1 : 0);    /* polygon2 */
        order_caps[22] = 1; /* polyline */
        order_caps[25] = (byte) (Options.polygon_ellipse_orders ? 1 : 0);    /* ellipse */
        order_caps[26] = (byte) (Options.polygon_ellipse_orders ? 1 : 0);    /* ellipse2 */
        order_caps[27] = 1; /* text2 */

        data.setLittleEndian16(RDP_CAPSET_ORDER);
        data.setLittleEndian16(RDP_CAPLEN_ORDER);

        data.positionAdd(20); /* Terminal desc, pad */
        data.setLittleEndian16(1); /* Cache X granularity */
        data.setLittleEndian16(20); /* Cache Y granularity */
        data.setLittleEndian16(0); /* Pad */
        data.setLittleEndian16(1); /* Max order level */
        data.setLittleEndian16(0x147); /* Number of fonts */
        data.setLittleEndian16(0x2a); /* Capability flags */
        data.copyFromByteArray(order_caps, 0, data.position(), 32); /*
																		 * Orders
																		 * supported
																		 */
        data.positionAdd(32);
        data.setLittleEndian16(0x6a1); /* Text capability flags */
        data.positionAdd(6); /* Pad */
        data.setLittleEndian32(Constants.desktop_save ? 0x38400 : 0); /*
																		 * Desktop
																		 * cache
																		 * size
																		 */
        data.setLittleEndian32(0); /* Unknown */
        data.setLittleEndian32(0x4e4); /* Unknown */
    }

    private static void sendBitmapcacheCaps(RdpPacket data) {

        data.setLittleEndian16(RDP_CAPSET_BMPCACHE);
        data.setLittleEndian16(RDP_CAPLEN_BMPCACHE);

        data.positionAdd(24); /* unused */
        data.setLittleEndian16(0x258); /* entries */
        data.setLittleEndian16(0x100); /* max cell size */
        data.setLittleEndian16(0x12c); /* entries */
        data.setLittleEndian16(0x400); /* max cell size */
        data.setLittleEndian16(0x106); /* entries */
        data.setLittleEndian16(0x1000); /* max cell size */
    }

    /* Output bitmap cache v2 capability set */
    private static void sendBitmapcache2Caps(RdpPacket data) {
        data.setLittleEndian16(RDP_CAPSET_BMPCACHE2); // out_uint16_le(s,
        // RDP_CAPSET_BMPCACHE2);
        data.setLittleEndian16(RDP_CAPLEN_BMPCACHE2); // out_uint16_le(s,
        // RDP_CAPLEN_BMPCACHE2);

        data.setLittleEndian16(Options.persistent_bitmap_caching ? 2 : 0); /* version */

        data.setBigEndian16(3); /* number of caches in this set */

		/* max cell size for cache 0 is 16x16, 1 = 32x32, 2 = 64x64, etc */
        data.setLittleEndian32(BMPCACHE2_C0_CELLS); // out_uint32_le(s,
        // BMPCACHE2_C0_CELLS);
        data.setLittleEndian32(BMPCACHE2_C1_CELLS); // out_uint32_le(s,
        // BMPCACHE2_C1_CELLS);

        // data.setLittleEndian32(PstCache.pstcache_init(2) ?
        // (BMPCACHE2_NUM_PSTCELLS | BMPCACHE2_FLAG_PERSIST) :
        // BMPCACHE2_C2_CELLS);

        if (PstCache.pstcache_init(2)) {
            logger.info("Persistent cache initialized");
            data.setLittleEndian32(BMPCACHE2_NUM_PSTCELLS
                    | BMPCACHE2_FLAG_PERSIST);
        } else {
            logger.info("Persistent cache not initialized");
            data.setLittleEndian32(BMPCACHE2_C2_CELLS);
        }
        data.positionAdd(20); // out_uint8s(s, 20); /* other bitmap caches
        // not used */
    }

    private static void sendColorcacheCaps(RdpPacket data) {

        data.setLittleEndian16(RDP_CAPSET_COLCACHE);
        data.setLittleEndian16(RDP_CAPLEN_COLCACHE);

        data.setLittleEndian16(6); /* cache size */
        data.setLittleEndian16(0); /* pad */
    }

    private static void sendActivateCaps(RdpPacket data) {

        data.setLittleEndian16(RDP_CAPSET_ACTIVATE);
        data.setLittleEndian16(RDP_CAPLEN_ACTIVATE);

        data.setLittleEndian16(0); /* Help key */
        data.setLittleEndian16(0); /* Help index key */
        data.setLittleEndian16(0); /* Extended help key */
        data.setLittleEndian16(0); /* Window activate */
    }

    private static void sendControlCaps(RdpPacket data) {

        data.setLittleEndian16(RDP_CAPSET_CONTROL);
        data.setLittleEndian16(RDP_CAPLEN_CONTROL);

        data.setLittleEndian16(0); /* Control capabilities */
        data.setLittleEndian16(0); /* Remote detach */
        data.setLittleEndian16(2); /* Control interest */
        data.setLittleEndian16(2); /* Detach interest */
    }

    private static void sendPointerCaps(RdpPacket data) {

        data.setLittleEndian16(RDP_CAPSET_POINTER);
        data.setLittleEndian16(RDP_CAPLEN_POINTER);

        data.setLittleEndian16(0); /* Colour pointer */
        data.setLittleEndian16(20); /* Cache size */
    }

    private static void sendNewPointerCaps(RdpPacket data) {
        data.setLittleEndian16(RDP_CAPSET_POINTER);
        data.setLittleEndian16(RDP_CAPLEN_NEWPOINTER);

        data.setLittleEndian16(1); /* Colour pointer */
        data.setLittleEndian16(20); /* Cache size */
        data.setLittleEndian16(20); /* Cache size for new pointers */
    }

    private static void sendShareCaps(RdpPacket data) {

        data.setLittleEndian16(RDP_CAPSET_SHARE);
        data.setLittleEndian16(RDP_CAPLEN_SHARE);

        data.setLittleEndian16(0); /* userid */
        data.setLittleEndian16(0); /* pad */
    }

    private static void sendUnknownCaps(RdpPacket data, int id, int length,
                                        byte[] caps) {

        data.setLittleEndian16(id /* RDP_CAPSET_UNKNOWN */);
        data.setLittleEndian16(length /* 0x58 */);

        data.copyFromByteArray(caps, 0, data.position(), /* RDP_CAPLEN_UNKNOWN */
                length - 4);
        data.positionAdd(/* RDP_CAPLEN_UNKNOWN */length - 4);
    }

    protected RdpPacket newSynchPacket() {
        RdpPacket data = newPacket(4);

        data.setLittleEndian16(1); // type
        data.setLittleEndian16(1002);

        data.markEnd();
        return data;
    }

    private void sendSynchronize() throws RdesktopException, IOException, CryptoException {
        RdpPacket data = newSynchPacket();

        //logger.debug("sync");
        this.send(data, RDP_DATA_PDU_SYNCHRONISE);
    }

    private void sendControl(int action) throws RdesktopException, IOException,
            CryptoException {

        RdpPacket data = this.newPacket(8);

        data.setLittleEndian16(action);
        data.setLittleEndian16(0); // userid
        data.setLittleEndian32(0); // control id

        data.markEnd();
        //logger.debug("control");

        this.send(data, RDP_DATA_PDU_CONTROL);
    }

    public void sendInput(int time, int message_type, int device_flags,
                          int param1, int param2) {

        RdpPacket data = this.newPacket(16);

        data.setLittleEndian16(1); /* number of events */
        data.setLittleEndian16(0); /* pad */

        data.setLittleEndian32(time);
        data.setLittleEndian16(message_type);
        data.setLittleEndian16(device_flags);
        data.setLittleEndian16(param1);
        data.setLittleEndian16(param2);

        data.markEnd();
        // logger.info("input");
        // if(logger.isInfoEnabled()) logger.info(data);

        try {
            this.send(data, RDP_DATA_PDU_INPUT);
        } catch (RdesktopException r) {
            if (Common.rdp.isConnected())
                Rdesktop.error(r, Common.rdp, Common.frame, true);
            Common.exit();
        } catch (CryptoException c) {
            if (Common.rdp.isConnected())
                Rdesktop.error(c, Common.rdp, Common.frame, true);
            Common.exit();
        } catch (IOException i) {
            if (Common.rdp.isConnected())
                Rdesktop.error(i, Common.rdp, Common.frame, true);
            Common.exit();
        }
    }

    private void sendFonts(int seq) throws RdesktopException, IOException,
            CryptoException {

        RdpPacket data = this.newPacket(8);

        data.setLittleEndian16(0); /* number of fonts */
        data.setLittleEndian16(0); /* pad? */
        data.setLittleEndian16(seq); /* unknown */
        data.setLittleEndian16(0x32); /* entry size */

        data.markEnd();
        logger.debug("fonts");
        this.send(data, RDP_DATA_PDU_FONT2);
    }

    private void processPointer(RdpPacket data)
            throws RdesktopException {
        int message_type = 0;
        int x = 0, y = 0;

        message_type = data.getLittleEndian16();
        data.positionAdd(2);
        switch (message_type) {

            case (Rdp.RDP_POINTER_MOVE):
                logger.debug("Rdp.RDP_POINTER_MOVE");
                x = data.getLittleEndian16();
                y = data.getLittleEndian16();

                if (data.position() <= data.getEnd()) {
                    surface.movePointer(x, y);
                }
                break;

            case (Rdp.RDP_POINTER_COLOR):
                process_colour_pointer_pdu(data);
                break;

            case (Rdp.RDP_POINTER_CACHED):
                process_cached_pointer_pdu(data);
                break;

            case RDP_POINTER_SYSTEM:
                process_system_pointer_pdu(data);
                break;

            case RDP_POINTER_NEW:
                process_new_pointer_pdu(data);
                break;

            default:
                break;
        }
    }

    private void process_system_pointer_pdu(RdpPacket data) {
        int system_pointer_type = 0;

        data.getLittleEndian16(system_pointer_type); // in_uint16(s,
        // system_pointer_type);
        switch (system_pointer_type) {
            case RDP_NULL_POINTER:
                //logger.debug("RDP_NULL_POINTER");
                surface.setCursor(null);
                break;

            default:
                logger.warn("Unimplemented system pointer message 0x"
                        + Integer.toHexString(system_pointer_type));
                // unimpl("System pointer message 0x%x\n", system_pointer_type);
        }
    }

    protected void processBitmapUpdates(RdpPacket data)
            throws RdesktopException {
        // logger.info("processBitmapUpdates");
        int n_updates = 0;
        int left = 0, top = 0, right = 0, bottom = 0, width = 0, height = 0;
        int cx = 0, cy = 0, bitsperpixel = 0, compression = 0, buffersize = 0, size = 0;
        byte[] pixel = null;

        int minX, minY, maxX, maxY;

        maxX = maxY = 0;
        minX = surface.getWidth();
        minY = surface.getHeight();

        n_updates = data.getLittleEndian16();

        for (int i = 0; i < n_updates; i++) {

            left = data.getLittleEndian16();
            top = data.getLittleEndian16();
            right = data.getLittleEndian16();
            bottom = data.getLittleEndian16();
            width = data.getLittleEndian16();
            height = data.getLittleEndian16();
            bitsperpixel = data.getLittleEndian16();
            int Bpp = (bitsperpixel + 7) / 8;
            compression = data.getLittleEndian16();
            buffersize = data.getLittleEndian16();

            cx = right - left + 1;
            cy = bottom - top + 1;

            if (minX > left)
                minX = left;
            if (minY > top)
                minY = top;
            if (maxX < right)
                maxX = right;
            if (maxY < bottom)
                maxY = bottom;

			/* Server may limit bpp - this is how we find out */
            if (Options.server_bpp != bitsperpixel) {
                logger.warn("Server limited colour depth to " + bitsperpixel
                        + " bits");
                Options.set_bpp(bitsperpixel);
            }

            if (compression == 0) {
                // logger.info("compression == 0");
                pixel = new byte[width * height * Bpp];

                for (int y = 0; y < height; y++) {
                    data.copyToByteArray(pixel, (height - y - 1)
                            * (width * Bpp), data.position(), width * Bpp);
                    data.positionAdd(width * Bpp);
                }

                surface.displayImage(Bitmap.convertImage(pixel, Bpp), width,
                        height, left, top, cx, cy);
                continue;
            }
            else if ((compression & 0x400) != 0) {
                // logger.info("compression & 0x400 != 0");
                size = buffersize;
            } else {
                // logger.info("compression & 0x400 == 0");
                data.positionAdd(2); // pad
                size = data.getLittleEndian16();

                data.positionAdd(4); // line size, final size

            }
            if (Bpp == 1) {
                pixel = Bitmap.decompress(width, height, size, data, Bpp);
                if (pixel != null)
                    surface.displayImage(Bitmap.convertImage(pixel, Bpp),
                            width, height, left, top, cx, cy);
                else
                    logger.warn("Could not decompress bitmap");
            } else {

                if (Options.bitmap_decompression_store == Options.INTEGER_BITMAP_DECOMPRESSION) {
                    int[] pixeli = Bitmap.decompressInt(width, height, size,
                            data, Bpp);
                    if (pixeli != null)
                        surface.displayImage(pixeli, width, height, left, top,
                                cx, cy);
                    else
                        logger.warn("Could not decompress bitmap");
                } else if (Options.bitmap_decompression_store == Options.BUFFEREDIMAGE_BITMAP_DECOMPRESSION) {
                    Image pix = Bitmap.decompressImg(width, height, size, data,
                            Bpp, null);
                    if (pix != null)
                        surface.displayImage(pix, left, top, width, height);
                    else
                        logger.warn("Could not decompress bitmap");
                } else {
                    surface.displayCompressed(left, top, width, height, size,
                            data, Bpp, null);
                }
            }
        }
        //surface.repainted(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    protected void processPalette(RdpPacket data) {
        int n_colors = 0;
        IndexColorModel cm = null;
        byte[] palette = null;

        byte[] red = null;
        byte[] green = null;
        byte[] blue = null;
        int j = 0;

        data.positionAdd(2); // pad
        n_colors = data.getLittleEndian16(); // Number of Colors in Palette
        data.positionAdd(2); // pad
        palette = new byte[n_colors * 3];
        red = new byte[n_colors];
        green = new byte[n_colors];
        blue = new byte[n_colors];
        data.copyToByteArray(palette, 0, data.position(), palette.length);
        data.positionAdd(palette.length);
        for (int i = 0; i < n_colors; i++) {
            red[i] = palette[j];
            green[i] = palette[j + 1];
            blue[i] = palette[j + 2];
            j += 3;
        }
        cm = new IndexColorModel(8, n_colors, red, green, blue);
        surface.registerPalette(cm);
    }

    public void registerDrawingSurface(RdesktopFrame fr) {
        this.frame = fr;
        RdesktopCanvas ds = fr.getCanvas();
        this.surface = ds;
        orders.registerDrawingSurface(ds);
    }

    /* Process a null system pointer PDU */
    protected void process_null_system_pointer_pdu(RdpPacket s)
            throws RdesktopException {
        // FIXME: We should probably set another cursor here,
        // like the X window system base cursor or something.
        surface.setCursor(cache.getCursor(0));
    }

    protected void process_colour_pointer_common(RdpPacket data, int bpp) throws RdesktopException {
        logger.debug("Rdp.RDP_POINTER_COLOR");
        int x = 0, y = 0, width = 0, height = 0, cache_idx = 0, masklen = 0, datalen = 0;
        byte[] mask = null, pixel = null;
        Cursor cursor = null;

        cache_idx = data.getLittleEndian16();
        x = data.getLittleEndian16();
        y = data.getLittleEndian16();
        width = data.getLittleEndian16();
        height = data.getLittleEndian16();
        masklen = data.getLittleEndian16();
        datalen = data.getLittleEndian16();
        mask = new byte[masklen];
        pixel = new byte[datalen];
        data.copyToByteArray(pixel, 0, data.position(), datalen);
        data.positionAdd(datalen);
        data.copyToByteArray(mask, 0, data.position(), masklen);
        data.positionAdd(masklen);
        cursor = surface.createCursor(cache_idx, x, y, width, height, mask, pixel, bpp);
        // logger.info("Creating and setting cursor " + cache_idx);
        surface.setCursor(cursor);
        cache.putCursor(cache_idx, cursor);
    }

    protected void process_colour_pointer_pdu(RdpPacket data)
            throws RdesktopException {
        process_colour_pointer_common(data, 24);
    }

    protected void process_cached_pointer_pdu(RdpPacket data)
            throws RdesktopException {
        logger.debug("Rdp.RDP_POINTER_CACHED");
        int cache_idx = data.getLittleEndian16();
        // logger.info("Setting cursor "+cache_idx);
        surface.setCursor(cache.getCursor(cache_idx));
    }

    protected void process_new_pointer_pdu(RdpPacket data) throws RdesktopException {
        int xor_bpp = data.getLittleEndian16();
        process_colour_pointer_common(data, xor_bpp);
    }
}

