/* VChannel.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Abstract class for RDP5 channels
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
package automenta.rdp.rdp5;

import automenta.rdp.*;
import automenta.rdp.crypto.CryptoException;
import automenta.rdp.rdp.RdpPacket;
import org.apache.log4j.Logger;

import java.io.IOException;

public abstract class VChannel {

	protected static Logger logger = Logger.getLogger(Input.class);

	private int mcs_id = 0;

	/**
	 * Provide the name of this channel
	 * 
	 * @return Channel name as string
	 */
	public abstract String name();

	/**
	 * Provide the set of flags specifying working options for this channel
	 * 
	 * @return Option flags
	 */
	public abstract int flags();
	
	public boolean mustEncrypt() {
	    return false;
	}

	/**
	 * Process a packet sent on this channel
	 * 
	 * @param data
	 *            Packet sent to this channel
	 * @throws RdesktopException
	 * @throws IOException
	 * @throws CryptoException
	 */
	public abstract void process(AbstractRdpPacket data) throws RdesktopException,
			IOException, CryptoException;

	public int mcs_id() {
		return mcs_id;
	}

	/**
	 * Set the MCS ID for this channel
	 * 
	 * @param mcs_id
	 *            New MCS ID
	 */
	public void set_mcs_id(int mcs_id) {
		this.mcs_id = mcs_id;
	}

	/**
	 * Initialise a packet for transmission over this virtual channel
	 * 
	 * @param length
	 *            Desired length of packet
	 * @return Packet prepared for this channel
	 * @throws RdesktopException
	 */
	public static RdpPacket init(int length) throws RdesktopException {
		RdpPacket s;

		s = Common.secure.init(Options.encryption ? Secure.SEC_ENCRYPT : 0,
				length + 8);
		s.setHeader(AbstractRdpPacket.CHANNEL_HEADER);
		s.positionAdd(8);

		return s;
	}

	/**
	 * Send a packet over this virtual channel
	 * 
	 * @param data
	 *            Packet to be sent
	 * @throws RdesktopException
	 * @throws IOException
	 * @throws CryptoException
	 */
	public void send_packet(RdpPacket data) throws RdesktopException,
			IOException, CryptoException {
		if (Common.secure == null)
			return;
		int length = data.size();

		int data_offset = 0;
		int packets_sent = 0;
//		int num_packets = (length / VChannels.CHANNEL_CHUNK_LENGTH);
//		num_packets += length - (VChannels.CHANNEL_CHUNK_LENGTH) * num_packets;

		while (data_offset < length) {

			int thisLength = Math.min(VChannels.CHANNEL_CHUNK_LENGTH, length
					- data_offset);

			RdpPacket s = Common.secure.init(
			        (Constants.encryption) ? Secure.SEC_ENCRYPT : 0,
					8 + thisLength);
			s.setLittleEndian32(length);

			int flags = ((data_offset == 0) ? VChannels.CHANNEL_FLAG_FIRST : 0);
			if (data_offset + thisLength >= length)
				flags |= VChannels.CHANNEL_FLAG_LAST;

			if ((this.flags() & VChannels.CHANNEL_OPTION_SHOW_PROTOCOL) != 0)
				flags |= VChannels.CHANNEL_FLAG_SHOW_PROTOCOL;

			s.setLittleEndian32(flags);
			s.copyFromPacket(data, data_offset, s.position(), thisLength);
			s.positionAdd(thisLength);
			s.markEnd();

			data_offset += thisLength;

			if (Common.secure != null)
				Common.secure.send_to_channel(s,
				        (Constants.encryption) ? Secure.SEC_ENCRYPT : 0, this
								.mcs_id());
			packets_sent++;
		}
	}

}
