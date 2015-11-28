/* MCS.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: MCS Layer of communication
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
import automenta.rdp.rdp.ISO_Localised;
import automenta.rdp.rdp.RdpPacket;
import automenta.rdp.rdp5.VChannels;
import org.apache.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;

public class MCS {
	static Logger logger = Logger.getLogger(MCS.class);

	private ISO IsoLayer = null;

	private int McsUserID;

	/* this for the MCS Layer */
	private static final int CONNECT_INITIAL = 0x7f65;

	private static final int CONNECT_RESPONSE = 0x7f66;

	private static final int BER_TAG_BOOLEAN = 1;

	private static final int BER_TAG_INTEGER = 2;

	private static final int BER_TAG_OCTET_STRING = 4;

	private static final int BER_TAG_RESULT = 10;

	private static final int TAG_DOMAIN_PARAMS = 0x30;

	public static final int MCS_GLOBAL_CHANNEL = 1003;

	public static final int MCS_USERCHANNEL_BASE = 1001;

	private static final int EDRQ = 1; /* Erect Domain Request */

	private static final int DPUM = 8; /* Disconnect Provider Ultimatum */

	private static final int AURQ = 10; /* Attach User Request */

	private static final int AUCF = 11; /* Attach User Confirm */

	private static final int CJRQ = 14; /* Channel Join Request */

	private static final int CJCF = 15; /* Channel Join Confirm */

	private static final int SDRQ = 25; /* Send Data Request */

	private static final int SDIN = 26; /* Send Data Indication */

	private VChannels channels;

	/**
	 * Initialise the MCS layer (and lower layers) with provided channels
	 * 
	 * @param channels
	 *            Set of available MCS channels
	 */
	public MCS(VChannels channels) {
		this.channels = channels;
		IsoLayer = new ISO_Localised();
	}

	/**
	 * Connect to a server
	 * 
	 * @param host
	 *            Address of server
	 * @param port
	 *            Port to connect to on server
	 * @param data
	 *            Packet to use for sending connection data
	 * @throws IOException
	 * @throws RdesktopException
	 * @throws OrderException
	 * @throws CryptoException
	 */
	public void connect(InetAddress host, int port, RdpPacket data)
			throws IOException, RdesktopException, OrderException,
			CryptoException {
		logger.debug("MCS.connect");
		IsoLayer.connect(host, port);

		this.sendConnectInitial(data);
		this.receiveConnectResponse(data);

		logger.debug("connect response received");

		send_edrq();
		send_aurq();

		this.McsUserID = receive_aucf();
		send_cjrq(this.McsUserID + MCS_USERCHANNEL_BASE);
		receive_cjcf();
		send_cjrq(MCS_GLOBAL_CHANNEL);
		receive_cjcf();

		for (int i = 0; i < channels.num_channels(); i++) {
			send_cjrq(VChannels.mcs_id(i));
			receive_cjcf();
		}

	}

	/**
	 * Disconnect from server
	 * 
	 */
	public void disconnect() {
		IsoLayer.disconnect();
		// in=null;
		// out=null;
	}

	/**
	 * Initialise a packet as an MCS PDU
	 * 
	 * @param length
	 *            Desired length of PDU
	 * @return
	 * @throws RdesktopException
	 */
	public RdpPacket init(int length) throws RdesktopException {
		RdpPacket data = ISO.init(length + 8);
		// data.pushLayer(RdpPacket_Localised.MCS_HEADER, 8);
		data.setHeader(AbstractRdpPacket.MCS_HEADER);
		data.positionAdd(8);
		data.setStart(data.position());
		return data;
	}

	/**
	 * Send a packet to the global channel
	 * 
	 * @param buffer
	 *            Packet to send
	 * @throws RdesktopException
	 * @throws IOException
	 */
	public void send(RdpPacket buffer) throws RdesktopException,
			IOException {
		send_to_channel(buffer, MCS_GLOBAL_CHANNEL);
	}

	/**
	 * Send a packet to a specified channel
	 * 
	 * @param buffer
	 *            Packet to send to channel
	 * @param channel
	 *            Id of channel on which to send packet
	 * @throws RdesktopException
	 * @throws IOException
	 */
	public void send_to_channel(RdpPacket buffer, int channel)
			throws RdesktopException, IOException {
		int length = 0;
		buffer.position(buffer.getHeader(AbstractRdpPacket.MCS_HEADER));

		length = buffer.getEnd() - buffer.getHeader(AbstractRdpPacket.MCS_HEADER) - 8;
		length |= 0x8000;

		buffer.set8((SDRQ << 2));
		buffer.setBigEndian16(this.McsUserID);
		buffer.setBigEndian16(channel);
		buffer.set8(0x70); // Flags
		buffer.setBigEndian16(length);
		IsoLayer.send(buffer);
	}

	/**
	 * Receive an MCS PDU from the next channel with available data
	 * 
	 * @param channel
	 *            ID of channel will be stored in channel[0]
	 * @return Received packet
	 * @throws IOException
	 * @throws RdesktopException
	 * @throws OrderException
	 * @throws CryptoException
	 */
	public RdpPacket receive(int[] channel) throws IOException,
			RdesktopException, OrderException, CryptoException {
		logger.debug("receive");
		int opcode = 0, appid = 0, length = 0;
		RdpPacket buffer = IsoLayer.receive();
		if (buffer == null)
			return null;
		buffer.setHeader(AbstractRdpPacket.MCS_HEADER);
		opcode = buffer.get8();

		appid = opcode >> 2;

		if (appid != SDIN) {
			if (appid != DPUM) {
				throw new RdesktopException("Expected data got" + opcode);
			}
			throw new EOFException("End of transmission!");
		}

		buffer.positionAdd(2); // Skip UserID
		channel[0] = buffer.getBigEndian16(); // Get ChannelID
		logger.debug("Channel ID = " + channel[0]);
		buffer.positionAdd(1); // Skip Flags

		length = buffer.get8();

		if ((length & 0x80) != 0) {
			buffer.positionAdd(1);
		}
		buffer.setStart(buffer.position());
		return buffer;
	}

	/**
	 * send an Integer encoded according to the ISO ASN.1 Basic Encoding Rules
	 * 
	 * @param buffer
	 *            Packet in which to store encoded value
	 * @param value
	 *            Integer value to store
	 */
	public void sendBerInteger(RdpPacket buffer, int value) {

//		int len = 1;
//
//		if (value > 0xff)
//			len = 2;

		sendBerHeader(buffer, BER_TAG_INTEGER, 2);
		buffer.setBigEndian16(value);

//		if (value > 0xff) {
//			buffer.setBigEndian16(value);
//		} else {
//			buffer.set8(value);
//		}

	}

	/**
	 * Determine the size of a BER header encoded for the specified tag and data
	 * length
	 * 
	 * @param tagval
	 *            Value of tag identifying data type
	 * @param length
	 *            Length of data header will precede
	 * @return
	 */
	private static int berHeaderSize(int tagval, int length) {
		int total = 0;
		if (tagval > 0xff) {
			total += 2;
		} else {
			total += 1;
		}

		if (length >= 0x80) {
			total += 3;
		} else {
			total += 1;
		}
		return total;
	}

	/**
	 * Send a Header encoded according to the ISO ASN.1 Basic Encoding rules
	 * 
	 * @param buffer
	 *            Packet in which to send the header
	 * @param tagval
	 *            Data type for header
	 * @param length
	 *            Length of data header precedes
	 */
	public static void sendBerHeader(RdpPacket buffer, int tagval, int length) {
		if (tagval > 0xff) {
			buffer.setBigEndian16(tagval);
		} else {
			buffer.set8(tagval);
		}

		if (length >= 0x80) {
			buffer.set8(0x82);
			buffer.setBigEndian16(length);
		} else {
			buffer.set8(length);
		}
	}

	/**
	 * Determine the size of a BER encoded integer with specified value
	 * 
	 * @param value
	 *            Value of integer
	 * @return Number of bytes the encoded data would occupy
	 */
	private static int BERIntSize(int value) {
		if (value > 0xff)
			return 4;
		else
			return 3;
	}

	/**
	 * Determine the size of the domain parameters, encoded according to the ISO
	 * ASN.1 Basic Encoding Rules
	 * 
	 * @param max_channels
	 *            Maximum number of channels
	 * @param max_users
	 *            Maximum number of users
	 * @param max_tokens
	 *            Maximum number of tokens
	 * @param max_pdusize
	 *            Maximum size of an MCS PDU
	 * @return Number of bytes the domain parameters would occupy
	 */
	private int domainParamSize(int max_channels, int max_users,
			int max_tokens, int max_pdusize) {
		int endSize = BERIntSize(max_channels) + BERIntSize(max_users)
				+ BERIntSize(max_tokens) + BERIntSize(1) + BERIntSize(0)
				+ BERIntSize(1) + BERIntSize(max_pdusize) + BERIntSize(2);
		return berHeaderSize(TAG_DOMAIN_PARAMS, endSize) + endSize;
	}

	/**
	 * send a DOMAIN_PARAMS structure encoded according to the ISO ASN.1 Basic
	 * Encoding rules
	 * 
	 * @param buffer
	 *            Packet in which to send the structure
	 * @param max_channels
	 *            Maximum number of channels
	 * @param max_users
	 *            Maximum number of users
	 * @param max_tokens
	 *            Maximum number of tokens
	 * @param max_pdusize
	 *            Maximum size for an MCS PDU
	 */
	public void sendDomainParams(RdpPacket buffer, int max_channels,
			int max_users, int max_tokens, int max_pdusize) {

//		int size = BERIntSize(max_channels) + BERIntSize(max_users)
//				+ BERIntSize(max_tokens) + BERIntSize(1) + BERIntSize(0)
//				+ BERIntSize(1) + BERIntSize(max_pdusize) + BERIntSize(2);

		sendBerHeader(buffer, TAG_DOMAIN_PARAMS, 32);
		sendBerInteger(buffer, max_channels);
		sendBerInteger(buffer, max_users);
		sendBerInteger(buffer, max_tokens);

		sendBerInteger(buffer, 1); // num_priorities
		sendBerInteger(buffer, 0); // min_throughput
		sendBerInteger(buffer, 1); // max_height

		sendBerInteger(buffer, max_pdusize);
		sendBerInteger(buffer, 2); // ver_protocol
	}

	/**
	 * 
	 * Send an MCS_CONNECT_INITIAL message (encoded as ASN.1 Ber)
	 * 
	 * @param data
	 *            Packet in which to send the message
	 * @throws IOException
	 * @throws RdesktopException
	 */
	public void sendConnectInitial(RdpPacket data)
			throws IOException, RdesktopException {
		logger.debug("MCS.sendConnectInitial");

		int datalen = data.getEnd();
//		int length = 9 + domainParamSize(34, 2, 0, 0xffff)
//				+ domainParamSize(1, 1, 1, 0x420)
//				+ domainParamSize(0xffff, 0xfc17, 0xffff, 0xffff) + 4 + datalen; // RDP5
		int length = 9 + 3 * 34 + 4 + datalen;
		// Code

		RdpPacket buffer = ISO.init(length + 5);

		sendBerHeader(buffer, CONNECT_INITIAL, length);
		sendBerHeader(buffer, BER_TAG_OCTET_STRING, 1); // calling domain
		buffer.set8(1); // RDP5 Code
		sendBerHeader(buffer, BER_TAG_OCTET_STRING, 1); // called domain
		buffer.set8(1); // RDP5 Code

		sendBerHeader(buffer, BER_TAG_BOOLEAN, 1);
		buffer.set8(0xff); // upward flag

		sendDomainParams(buffer, 34, 2, 0, 0xffff); // target parameters // RDP5
		// Code
		sendDomainParams(buffer, 1, 1, 1, 0x420); // minimum parameters
		sendDomainParams(buffer, 0xffff, 0xfc17, 0xffff, 0xffff); // maximum
		// parameters

		sendBerHeader(buffer, BER_TAG_OCTET_STRING, datalen);

		data.copyToPacket(buffer, 0, buffer.position(), data.getEnd());
		buffer.positionAdd(data.getEnd());
		buffer.markEnd();
		IsoLayer.send(buffer);
	}

	/**
	 * Receive and handle a connect response from the server
	 * 
	 * @param data
	 *            Packet containing response data
	 * @throws IOException
	 * @throws RdesktopException
	 * @throws OrderException
	 * @throws CryptoException
	 */
	public void receiveConnectResponse(RdpPacket data)
			throws IOException, RdesktopException, OrderException,
			CryptoException {

		logger.debug("MCS.receiveConnectResponse");

		String[] connect_results = { "Successful", "Domain Merging",
				"Domain not Hierarchical", "No Such Channel", "No Such Domain",
				"No Such User", "Not Admitted", "Other User ID",
				"Parameters Unacceptable", "Token Not Available",
				"Token Not Possessed", "Too Many Channels", "Too Many Tokens",
				"Too Many Users", "Unspecified Failure", "User Rejected" };

		int result = 0;
		int length = 0;

		RdpPacket buffer = IsoLayer.receive();
		logger.debug("Received buffer");
		length = berParseHeader(buffer, CONNECT_RESPONSE);
		length = berParseHeader(buffer, BER_TAG_RESULT);

		result = buffer.get8();
		if (result != 0) {
			throw new RdesktopException("MCS Connect failed: "
					+ connect_results[result]);
		}
		length = berParseHeader(buffer, BER_TAG_INTEGER);
		length = buffer.get8(); // connect id
		parseDomainParams(buffer);
		length = berParseHeader(buffer, BER_TAG_OCTET_STRING);

		Common.secure.processMcsData(buffer);

		/*
		 * if (length > data.size()) { logger.warn("MCS Datalength exceeds
		 * size!"+length); length=data.size(); } data.copyFromPacket(buffer,
		 * buffer.getPosition(), 0, length); data.setPosition(0);
		 * data.markEnd(length); buffer.incrementPosition(length);
		 * 
		 * if (buffer.getPosition() != buffer.getEnd()) { throw new
		 * RdesktopException(); }
		 */
	}

	/**
	 * Transmit an EDrq message
	 * 
	 * @throws IOException
	 * @throws RdesktopException
	 */
	public void send_edrq() throws IOException, RdesktopException {
		logger.debug("send_edrq");
		RdpPacket buffer = ISO.init(5);
		buffer.set8(EDRQ << 2);
		buffer.setBigEndian16(1); // height
		buffer.setBigEndian16(1); // interval
		buffer.markEnd();
		IsoLayer.send(buffer);
	}

	/**
	 * Transmit a CJrq message
	 * 
	 * @param channelid
	 *            Id of channel to be identified in request
	 * @throws IOException
	 * @throws RdesktopException
	 */
	public void send_cjrq(int channelid) throws IOException, RdesktopException {
		RdpPacket buffer = ISO.init(5);
		buffer.set8(CJRQ << 2);
		buffer.setBigEndian16(this.McsUserID); // height
		buffer.setBigEndian16(channelid); // interval
		buffer.markEnd();
		IsoLayer.send(buffer);
	}

	/**
	 * Transmit an AUcf message
	 * 
	 * @throws IOException
	 * @throws RdesktopException
	 */
	public void send_aucf() throws IOException, RdesktopException {
		RdpPacket buffer = ISO.init(2);

		buffer.set8(AUCF << 2);
		buffer.set8(0);
		buffer.markEnd();
		IsoLayer.send(buffer);
	}

	/**
	 * Transmit an AUrq mesage
	 * 
	 * @throws IOException
	 * @throws RdesktopException
	 */
	public void send_aurq() throws IOException, RdesktopException {
		RdpPacket buffer = ISO.init(1);

		buffer.set8(AURQ << 2);
		buffer.markEnd();
		IsoLayer.send(buffer);
	}

	/**
	 * Receive and handle a CJcf message
	 * 
	 * @throws IOException
	 * @throws RdesktopException
	 */
	public void receive_cjcf() throws IOException, RdesktopException,
			OrderException, CryptoException {
		logger.debug("receive_cjcf");
		int opcode = 0, result = 0;
		RdpPacket buffer = IsoLayer.receive();

		opcode = buffer.get8();
		if ((opcode >> 2) != CJCF) {
			throw new RdesktopException("Expected CJCF got" + opcode);
		}

		result = buffer.get8();
		if (result != 0) {
			throw new RdesktopException("Expected CJRQ got " + result);
		}

		buffer.positionAdd(4); // skip userid, req_channelid

		if ((opcode & 2) != 0) {
			buffer.positionAdd(2); // skip join_channelid
		}

		if (buffer.position() != buffer.getEnd()) {
			throw new RdesktopException();
		}
	}

	/**
	 * Receive an AUcf message
	 * 
	 * @return UserID specified in message
	 * @throws IOException
	 * @throws RdesktopException
	 * @throws OrderException
	 * @throws CryptoException
	 */
	public int receive_aucf() throws IOException, RdesktopException,
			OrderException, CryptoException {
		logger.debug("receive_aucf");
		int opcode = 0, result = 0, UserID = 0;
		RdpPacket buffer = IsoLayer.receive();

		opcode = buffer.get8();
		if ((opcode >> 2) != AUCF) {
			throw new RdesktopException("Expected AUCF got " + opcode);
		}

		result = buffer.get8();
		if (result != 0) {
			throw new RdesktopException("Expected AURQ got " + result);
		}

		if ((opcode & 2) != 0) {
			UserID = buffer.getBigEndian16();
		}

		if (buffer.position() != buffer.getEnd()) {
			throw new RdesktopException();
		}
		return UserID;
	}

	/**
	 * Parse a BER header and determine data length
	 * 
	 * @param data
	 *            Packet containing header at current read position
	 * @param tagval
	 *            Tag ID for data type
	 * @return Length of following data
	 * @throws RdesktopException
	 */
	public static int berParseHeader(RdpPacket data, int tagval)
			throws RdesktopException {
		int tag = 0;
		int length = 0;
		int len;

		if (tagval > 0x000000ff) {
			tag = data.getBigEndian16();
		} else {
			tag = data.get8();
		}

		if (tag != tagval) {
			throw new RdesktopException("Unexpected tag got " + tag
					+ " expected " + tagval);
		}

		len = data.get8();

		if ((len & 0x00000080) != 0) {
			len &= ~0x00000080; // subtract 128
			length = 0;
			while (len-- != 0) {
				length = (length << 8) + data.get8();
			}
		} else {
			length = len;
		}

		return length;
	}

	/**
	 * Parse domain parameters sent by server
	 * 
	 * @param data
	 *            Packet containing domain parameters at current read position
	 * @throws RdesktopException
	 */
	public void parseDomainParams(RdpPacket data)
			throws RdesktopException {
		int length;

		length = MCS.berParseHeader(data, TAG_DOMAIN_PARAMS);
		data.positionAdd(length);

		if (data.position() > data.getEnd()) {
			throw new RdesktopException();
		}
	}

	/**
	 * Retrieve the user ID stored by this MCS object
	 * 
	 * @return User ID
	 */
	public int getUserID() {
		return this.McsUserID;
	}
}
