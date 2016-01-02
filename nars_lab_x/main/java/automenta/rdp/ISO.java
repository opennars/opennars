/* ISO.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: ISO layer of communication
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
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.Socket;

public abstract class ISO {
	static Logger logger = Logger.getLogger(ISO.class);

	private HexDump dump = null;

	protected Socket rdpsock = null;

	private DataInputStream in = null;

	private DataOutputStream out = null;

	/* this for the ISO Layer */
	private static final int CONNECTION_REQUEST = 0xE0;

	private static final int CONNECTION_CONFIRM = 0xD0;

	private static final int DISCONNECT_REQUEST = 0x80;

	private static final int DATA_TRANSFER = 0xF0;

	private static final int ERROR = 0x70;

	private static final int PROTOCOL_VERSION = 0x03;

	private static final int EOT = 0x80;

	/**
	 * Construct ISO object, initialises hex dump
	 */
	public ISO() {
		dump = new HexDump();
	}

	/**
	 * Initialise an ISO PDU
	 * 
	 * @param length
	 *            Desired length of PDU
	 * @return Packet configured as ISO PDU, ready to write at higher level
	 */
	public static RdpPacket init(int length) {
		RdpPacket data = new RdpPacket(length + 7);// getMemory(length+7);
		data.positionAdd(7);
		data.setStart(data.position());
		return data;
	}

	/*
	 * protected Socket negotiateSSL(Socket sock) throws Exception{ return sock; }
	 */

	/**
	 * Create a socket for this ISO object
	 * 
	 * @param host
	 *            Address of server
	 * @param port
	 *            Port on which to connect socket
	 * @throws IOException
	 */
	protected void doSocketConnect(InetAddress host, int port)
			throws IOException {
		this.rdpsock = new Socket(host, port);
	}

	/**
	 * Connect to a server
	 * 
	 * @param host
	 *            Address of server
	 * @param port
	 *            Port to connect to on server
	 * @throws IOException
	 * @throws RdesktopException
	 * @throws OrderException
	 * @throws CryptoException
	 */
	public void connect(InetAddress host, int port) throws IOException,
			RdesktopException, OrderException, CryptoException {
		int[] code = new int[1];
		doSocketConnect(host, port);
		rdpsock.setTcpNoDelay(Options.low_latency);

		// this.in = new InputStreamReader(rdpsock.getInputStream());
		this.in = new DataInputStream(new BufferedInputStream(rdpsock
				.getInputStream()));
		this.out = new DataOutputStream(new BufferedOutputStream(rdpsock
				.getOutputStream()));

		send_connection_request();

		receiveMessage(code);
		if (code[0] != CONNECTION_CONFIRM) {
			throw new RdesktopException("Expected CC got:"
					+ Integer.toHexString(code[0]).toUpperCase());
		}

		/*
		 * if(Options.use_ssl){ try { rdpsock = this.negotiateSSL(rdpsock);
		 * this.in = new DataInputStream(rdpsock.getInputStream()); this.out=
		 * new DataOutputStream(rdpsock.getOutputStream()); } catch (Exception
		 * e) { e.printStackTrace(); throw new RdesktopException("SSL
		 * negotiation failed: " + e.getMessage()); } }
		 */

	}

	/**
	 * Send a self contained iso-pdu
	 * 
	 * @param type
	 *            one of the following CONNECT_RESPONSE, DISCONNECT_REQUEST
	 * @exception IOException
	 *                when an I/O Error occurs
	 */
	private void sendMessage(int type) throws IOException {
		RdpPacket buffer = new RdpPacket(11);// getMemory(11);
		byte[] packet = new byte[11];

		buffer.set8(PROTOCOL_VERSION); // send Version Info
		buffer.set8(0); // reserved byte
		buffer.setBigEndian16(11); // Length
		buffer.set8(6); // Length of Header

		buffer.set8(type); // where code = CR or DR
		buffer.setBigEndian16(0); // Destination reference ( 0 at CC and DR)

		buffer.setBigEndian16(0); // source reference should be a reasonable
									// address we use 0
		buffer.set8(0); // service class
		buffer.copyToByteArray(packet, 0, 0, packet.length);
		out.write(packet);
		out.flush();
	}

	/**
	 * Send a packet to the server, wrapped in ISO PDU
	 * 
	 * @param buffer
	 *            Packet containing data to send to server
	 * @throws RdesktopException
	 * @throws IOException
	 */
	public void send(RdpPacket buffer) throws RdesktopException,
			IOException {
		if (rdpsock == null || out == null)
			return;
		if (buffer.getEnd() < 0) {
			throw new RdesktopException("No End Mark!");
		} else {
			int length = buffer.getEnd();
			byte[] packet = new byte[length];
			// RdpPacket data = this.getMemory(length+7);
			buffer.position(0);
			buffer.set8(PROTOCOL_VERSION); // Version
			buffer.set8(0); // reserved
			buffer.setBigEndian16(length); // length of packet

			buffer.set8(2); // length of header
			buffer.set8(DATA_TRANSFER);
			buffer.set8(EOT);
			buffer.copyToByteArray(packet, 0, 0, buffer.getEnd());
			
			if (Options.debug_hexdump) {
	            System.out.println("ISO Sending packet:");
	            System.out.println(automenta.rdp.tools.HexDump.dumpHexString(packet));
	        }
			
			out.write(packet);
			out.flush();
		}
	}

	/**
	 * Receive a data transfer message from the server
	 * 
	 * @return Packet containing message (as ISO PDU)
	 * @throws IOException
	 * @throws RdesktopException
	 * @throws OrderException
	 * @throws CryptoException
	 */
	public RdpPacket receive() throws IOException, RdesktopException,
			OrderException, CryptoException {
		int[] type = new int[1];
		RdpPacket buffer = receiveMessage(type);
		if (buffer == null)
			return null;
		if (type[0] != DATA_TRANSFER) {
			throw new RdesktopException("Expected DT got:" + type[0]);
		}

		return buffer;
	}

	private static int g_packetno = 0;
	/**
	 * Receive a specified number of bytes from the server, and store in a
	 * packet
	 * 
	 * @param p
	 *            Packet to append data to, null results in a new packet being
	 *            created
	 * @param length
	 *            Length of data to read
	 * @return Packet containing read data, appended to original data if
	 *         provided
	 * @throws IOException
	 */
	private RdpPacket tcp_recv(RdpPacket p, int length)
			throws IOException {
		//logger.debug("ISO.tcp_recv");
		RdpPacket buffer;
		byte[] packet = new byte[length];

		in.readFully(packet, 0, length);

		// try{ }
		// catch(IOException e){ logger.warn("IOException: " + e.getMessage());
		// return null; }
		if (Options.debug_hexdump) {
//		    dump.encode(packet, "RECEIVE" /* System.out */);
		    System.out.println(String.format("\nISO receive RDP packet # %d", ++g_packetno));
		    System.out.println(automenta.rdp.tools.HexDump.dumpHexString(packet));
		}

		if (p == null) {
			buffer = new RdpPacket(length);
			buffer.copyFromByteArray(packet, 0, 0, packet.length);
			buffer.markEnd(length);
			buffer.setStart(buffer.position());
		} else {
			buffer = new RdpPacket((p.getEnd() - p.getStart())
					+ length);
			buffer.copyFromPacket(p, p.getStart(), 0, p.getEnd());
			buffer.copyFromByteArray(packet, 0, p.getEnd(), packet.length);
			buffer.markEnd(p.size() + packet.length);
			buffer.position(p.position());
			buffer.setStart(0);
		}

		return buffer;
	}

	/**
	 * Receive a message from the server
	 * 
	 * @param type
	 *            Array containing message type, stored in type[0]
	 * @return Packet object containing data of message
	 * @throws IOException
	 * @throws RdesktopException
	 * @throws OrderException
	 * @throws CryptoException
	 */
	private RdpPacket receiveMessageex(int[] type, int[] rdpver) throws IOException,
			RdesktopException, OrderException, CryptoException {
		//logger.debug("ISO.receiveMessage");
		RdpPacket s = null;
		int length, version;

		next_packet: while (true) {
			//logger.debug("next_packet");
			s = tcp_recv(null, 4);
			//logger.debug("off next_packet");
			if (s == null)
				return null;

			version = s.get8();
			rdpver[0] = version;

			if (version == 3) {
				s.positionAdd(1); // pad
				length = s.getBigEndian16();
			} else {
				length = s.get8();
				if ((length & 0x80) != 0) {
					length &= ~0x80;
					length = (length << 8) + s.get8();
				}
			}

			s = tcp_recv(s, length - 4);
			if (s == null)
				return null;
			if ((version & 3) == 0) {
				//logger.debug("Processing rdp5 packet");
				Common.rdp.rdp5_process(s, (version & 0x80) != 0);
				continue next_packet;
			} else
				break;
		}

		s.get8();
		type[0] = s.get8();

		if (type[0] == DATA_TRANSFER) {
			//logger.debug("Data Transfer Packet");
			s.positionAdd(1); // eot
			return s;
		}

		s.positionAdd(5); // dst_ref, src_ref, class
		return s;
	}
	private RdpPacket receiveMessage(int[] type) throws IOException,
			RdesktopException, OrderException, CryptoException {
		int[] rdpver = new int[1];
		return receiveMessageex(type, rdpver);
	}

	/**
	 * Disconnect from an RDP session, closing all sockets
	 */
	public void disconnect() {
		if (rdpsock == null)
			return;
		try {
			sendMessage(DISCONNECT_REQUEST);
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (rdpsock != null)
				rdpsock.close();
		} catch (IOException e) {
			in = null;
			out = null;
			rdpsock = null;
			return;
		}
		in = null;
		out = null;
		rdpsock = null;
	}

	/**
	 * Send the server a connection request, detailing client protocol version
	 * 
	 * @throws IOException
	 */
	void send_connection_request() throws IOException {

		String uname = Options.username;
//		if (uname.length() > 9)
//			uname = uname.substring(0, 9);
		int length = 11 + (Options.username.length() > 0 ? ("Cookie: mstshash="
				.length()
				+ uname.length() + 2) : 0)/* + 8*/;
		RdpPacket buffer = new RdpPacket(length);
		byte[] packet = new byte[length];

		buffer.set8(PROTOCOL_VERSION); // send Version Info
		buffer.set8(0); // reserved byte
		buffer.setBigEndian16(length); // Length
		buffer.set8(length - 5); // Length of Header
		buffer.set8(CONNECTION_REQUEST);
		buffer.setBigEndian16(0); // Destination reference ( 0 at CC and DR)
		buffer.setBigEndian16(0); // source reference should be a reasonable
									// address we use 0
		buffer.set8(0); // service class
		if (Options.username.length() > 0) {
			logger.debug("Including username");
			buffer
					.out_uint8p("Cookie: mstshash=", "Cookie: mstshash="
							.length());
			buffer.out_uint8p(uname, uname.length());

			buffer.set8(0x0d); // unknown
			buffer.set8(0x0a); // unknown
		}

		/*
		 * // Authentication request? buffer.setLittleEndian16(0x01);
		 * buffer.setLittleEndian16(0x08); // Do we try to use SSL?
		 * buffer.set8(Options.use_ssl? 0x01 : 0x00);
		 * buffer.incrementPosition(3);
		 */
//		buffer.set8(0x01);
//		buffer.set8(0x00);
//		buffer.setLittleEndian16(0x08);
//		buffer.setLittleEndian32(0x01);
		
		buffer.copyToByteArray(packet, 0, 0, packet.length);
		out.write(packet);
		out.flush();
		
		if (Options.debug_hexdump) {
//          dump.encode(packet, "SEND"/* System.out */);
            System.out.println("ISO Sending packet:");
            System.out.println(automenta.rdp.tools.HexDump.dumpHexString(packet));
        }
		
	}
}
