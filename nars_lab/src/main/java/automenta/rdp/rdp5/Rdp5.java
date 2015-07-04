/* Rdp5.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Handle RDP5 orders
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

import automenta.rdp.OrderException;
import automenta.rdp.RdesktopException;
import automenta.rdp.Rdp;
import automenta.rdp.crypto.CryptoException;
import automenta.rdp.rdp.RdpPacket;

public class Rdp5 extends Rdp {

    private VChannels channels;

    /**
     * Initialise the RDP5 communications layer, with specified virtual channels
     *
     * @param channels Virtual channels for RDP layer
     */
    public Rdp5(VChannels channels) {
        super(channels);
        this.channels = channels;
    }

//	/**
//	 * Process an RDP5 packet
//	 * 
//	 * @param s
//	 *            Packet to be processed
//	 * @param e
//	 *            True if packet is encrypted
//	 * @throws RdesktopException
//	 * @throws OrderException
//	 * @throws CryptoException
//	 */
//	public void rdp5_process(RdpPacket_Localised s, boolean e)
//			throws RdesktopException, OrderException, CryptoException {
//		rdp5_process(s, e, false);
//	}

    /**
     * Process an RDP5 packet
     *
     * @param s          Packet to be processed
     * @param encryption True if packet is encrypted
     * @param shortform  True if packet is of the "short" form
     * @throws RdesktopException
     * @throws OrderException
     * @throws CryptoException
     */
    public void rdp5_process(RdpPacket s, boolean encryption) throws RdesktopException, OrderException,
            CryptoException {
        //logger.debug("Processing RDP 5 order");

        int length, count;
        int type;
        int next;

        byte[] packet = null;
        if (encryption) {
            s.positionAdd(8); /* signature */

            byte[] data = new byte[s.size() - s.position()];
            s.copyToByteArray(data, 0, s.position(), data.length);
            packet = SecureLayer.decrypt(data);
        }
        else {
            packet = new byte[s.size() - s.position()];
            s.copyToByteArray(packet, 0, s.position(), packet.length);
        }

        // printf("RDP5 data:\n");
        // hexdump(s->p, s->end - s->p);
        RdpPacket bf = new RdpPacket(packet.length);
        bf.copyFromByteArray(packet, 0, 0, packet.length);
        bf.positionAdd(packet.length);
        bf.markEnd();
        bf.position(0);

        while (bf.position() < bf.getEnd()) {
            type = bf.get8();
            length = bf.getLittleEndian16();
            /* next_packet = */
            next = bf.position() + length;

            //logger.info("RDP5: type = " + type);


            switch (type) {
                case 0: /* orders */
                    count = bf.getLittleEndian16();
                    orders.processOrders(bf, next, count);
                    break;
                case 1: /* bitmap update (???) */
                    bf.positionAdd(2); /* part length */
                    processBitmapUpdates(bf);
                    break;
                case 2: /* palette */
                    bf.positionAdd(2);
                    processPalette(bf);
                    break;
                case 3: /* probably an palette with offset 3. Weird */
                    break;
                case 5:
                    process_null_system_pointer_pdu(bf);
                    break;
                case 6: // default pointer
                    break;
                case 9:
                    process_colour_pointer_pdu(bf);
                    break;
                case 10:
                    process_cached_pointer_pdu(bf);
                    break;
                case 11:
                    process_new_pointer_pdu(bf);
                    break;
                default:
                    logger.warn("Unimplemented RDP5 opcode " + type);
            }

            bf.position(next);
        }
    }

    /**
     * Process an RDP5 packet from a virtual channel
     *
     * @param s         Packet to be processed
     * @param channelno Channel on which packet was received
     */
    void rdp5_process_channel(RdpPacket s, int channelno) {
        VChannel channel = channels.find_channel_by_channelno(channelno);
        if (channel != null) {
            try {
                channel.process(s);
            } catch (Exception e) {
            }
        }
    }

}
