/* ClipChannel.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: 
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
package automenta.rdp.rdp5.cliprdr;

import automenta.rdp.*;
import automenta.rdp.crypto.CryptoException;
import automenta.rdp.rdp.RdpPacket;
import automenta.rdp.rdp5.VChannel;
import automenta.rdp.rdp5.VChannels;
import org.apache.log4j.Logger;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.Arrays;

public class ClipChannel extends VChannel implements ClipInterface,
		ClipboardOwner, FocusListener {

	String[] types = { "unused", "CF_TEXT", "CF_BITMAP", "CF_METAFILEPICT",
			"CF_SYLK", "CF_DIF", "CF_TIFF", "CF_OEMTEXT", "CF_DIB",
			"CF_PALETTE", "CF_PENDATA", "CF_RIFF", "CF_WAVE", "CF_UNICODETEXT",
			"CF_ENHMETAFILE", "CF_HDROP", "CF_LOCALE", "CF_MAX" };

	protected static final Logger logger = Logger.getLogger(Input.class);

	// Message types
	public static final int CLIPRDR_CONNECT = 1;

	public static final int CLIPRDR_FORMAT_ANNOUNCE = 2;

	public static final int CLIPRDR_FORMAT_ACK = 3;

	public static final int CLIPRDR_DATA_REQUEST = 4;

	public static final int CLIPRDR_DATA_RESPONSE = 5;

	// Message status codes
	public static final int CLIPRDR_REQUEST = 0;

	public static final int CLIPRDR_RESPONSE = 1;

	public static final int CLIPRDR_ERROR = 2;

	Clipboard clipboard;

	// TypeHandler for data currently being awaited
	TypeHandler currentHandler = null;

	// All type handlers available
	TypeHandlerList allHandlers = null;

	byte[] localClipData = null;

	public ClipChannel() {
		this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		// initialise all clipboard format handlers
		allHandlers = new TypeHandlerList();
		allHandlers.add(new UnicodeHandler());
		allHandlers.add(new TextHandler());
		allHandlers.add(new DIBHandler());
		// allHandlers.add(new MetafilepictHandler());
	}

	/*
	 * VChannel inherited abstract methods
	 */
	public String name() {
		return "cliprdr";
	}

	public int flags() {
		return VChannels.CHANNEL_OPTION_INITIALIZED
				| VChannels.CHANNEL_OPTION_ENCRYPT_RDP
				| VChannels.CHANNEL_OPTION_COMPRESS_RDP
				| VChannels.CHANNEL_OPTION_SHOW_PROTOCOL;
	}

	/*
	 * Data processing methods
	 */
	public void process(AbstractRdpPacket data) throws RdesktopException, IOException,
			CryptoException {

		int type, status;
		int length;

		type = data.getLittleEndian16();
		status = data.getLittleEndian16();
		length = data.getLittleEndian32();

		if (status == CLIPRDR_ERROR) {
			if (type == CLIPRDR_FORMAT_ACK) {
				send_format_announce();
				return;
			}

			return;
		}

		switch (type) {
		case CLIPRDR_CONNECT:
			send_format_announce();
			break;
		case CLIPRDR_FORMAT_ANNOUNCE:
			handle_clip_format_announce(data, length);
			return;
		case CLIPRDR_FORMAT_ACK:
			break;
		case CLIPRDR_DATA_REQUEST:
			handle_data_request(data);
			break;
		case CLIPRDR_DATA_RESPONSE:
			handle_data_response(data, length);
			break;
		case 7:
			break;
		default:
			// System.out.println("Unimplemented packet type! " + type);
		}

	}

	public void send_null(int type, int status) {
		RdpPacket s;

		//TODO cache these by (type,status) tuples

		s = new RdpPacket(12);
		s.setLittleEndian16(type);
		s.setLittleEndian16(status);
		s.setLittleEndian32(0);
		s.setLittleEndian32(0); // pad
		s.markEnd();

		try {
			this.send_packet(s);
		} catch (RdesktopException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (CryptoException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	void send_format_announce() throws RdesktopException, IOException,
			CryptoException {
//		Transferable clipData = clipboard.getContents(clipboard);
//		DataFlavor[] dataTypes = clipboard.getAvailableDataFlavors();//clipData.getTransferDataFlavors();
//
//		TypeHandlerList availableFormats = allHandlers
//				.getHandlersForClipboard(dataTypes);
//
//		RdpPacket_Localised s;
//		int number_of_formats = availableFormats.count();
//
//		s = new RdpPacket_Localised(number_of_formats * 36 + 12);
//		s.setLittleEndian16(CLIPRDR_FORMAT_ANNOUNCE);
//		s.setLittleEndian16(CLIPRDR_REQUEST);
//		s.setLittleEndian32(number_of_formats * 36);
//
//		TypeHandler handler = null;
//		for (Iterator i = availableFormats.iterator(); i.hasNext();) {
//			handler = (TypeHandler) i.next();
//			s.setLittleEndian32(handler.preferredFormat());
//			s.incrementPosition(32);
//		}
//
//		s.setLittleEndian32(0); // pad
//		s.markEnd();
//		send_packet(s);
	}

	private void handle_clip_format_announce(AbstractRdpPacket data, int length)
			throws RdesktopException, IOException, CryptoException {
		TypeHandlerList serverTypeList = new TypeHandlerList();

		// System.out.print("Available types: ");
		for (int c = length; c >= 36; c -= 36) {
			int typeCode = data.getLittleEndian32();
			// if(typeCode < types.length) System.out.print(types[typeCode] + "
			// ");
			data.positionAdd(32);
			serverTypeList.add(allHandlers.getHandlerForFormat(typeCode));
		}
		// System.out.println();

		send_null(CLIPRDR_FORMAT_ACK, CLIPRDR_RESPONSE);
		currentHandler = serverTypeList.getFirst();

		if (currentHandler != null)
			request_clipboard_data(currentHandler.preferredFormat());
	}

	void handle_data_request(AbstractRdpPacket data) {
		int format = data.getLittleEndian32();
		Transferable clipData = clipboard.getContents(this);

		TypeHandler outputHandler = allHandlers.getHandlerForFormat(format);
		if (outputHandler != null) {
			outputHandler.send_data(clipData, this);
			// outData = outputHandler.fromTransferable(clipData);
			// if(outData != null){
			// send_data(outData,outData.length);
			// return;
			// }
			// else System.out.println("Clipboard data to send == null!");
		}

		// this.send_null(CLIPRDR_DATA_RESPONSE,CLIPRDR_ERROR);
	}

	void handle_data_response(AbstractRdpPacket data, int length) {
		// if(currentHandler !=
		// null)clipboard.setContents(currentHandler.handleData(data,
		// length),this);
		// currentHandler = null;
		if (currentHandler != null)
			currentHandler.handleData(data, length, this);
		currentHandler = null;
	}

	void request_clipboard_data(int formatcode) throws RdesktopException,
			IOException, CryptoException {

		RdpPacket s = Common.secure.init(
				Constants.encryption ? Secure.SEC_ENCRYPT : 0, 24);
		s.setLittleEndian32(16); // length

		int flags = VChannels.CHANNEL_FLAG_FIRST | VChannels.CHANNEL_FLAG_LAST;
		if ((this.flags() & VChannels.CHANNEL_OPTION_SHOW_PROTOCOL) != 0)
			flags |= VChannels.CHANNEL_FLAG_SHOW_PROTOCOL;

		s.setLittleEndian32(flags);
		s.setLittleEndian16(CLIPRDR_DATA_REQUEST);
		s.setLittleEndian16(CLIPRDR_REQUEST);
		s.setLittleEndian32(4); // Remaining length
		s.setLittleEndian32(formatcode);
		s.setLittleEndian32(0); // Unknown. Garbage pad?
		s.markEnd();

		Common.secure.send_to_channel(s,
				Constants.encryption ? Secure.SEC_ENCRYPT : 0, this.mcs_id());
	}

	public void send_data(byte[] data, int length) {
		synchronized (CommunicationMonitor.synch) {

			RdpPacket all = new RdpPacket(12 + length);

			all.setLittleEndian16(CLIPRDR_DATA_RESPONSE);
			all.setLittleEndian16(CLIPRDR_RESPONSE);
			all.setLittleEndian32(length + 4); // don't know why, but we need to
			// add between 1 and 4 to the
			// length,
			// otherwise the server cliprdr thread hangs
			all.copyFromByteArray(data, 0, all.position(), length);
			all.positionAdd(length);
			all.setLittleEndian32(0);

			try {
				this.send_packet(all);
			} catch (RdesktopException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				if (!Common.underApplet)
					System.exit(-1);
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				if (!Common.underApplet)
					System.exit(-1);
			} catch (CryptoException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				if (!Common.underApplet)
					System.exit(-1);
			}
		}

	}

	/*
	 * FocusListener methods
	 */
	public void focusGained(FocusEvent arg0) {
		// synchronise the clipboard types here, so the server knows what's
		// available
		if (Options.use_rdp5) {
			try {
				send_format_announce();
			} catch (RdesktopException e) {
			} catch (IOException e) {
			} catch (CryptoException e) {
			}
		}
	}

	public void focusLost(FocusEvent arg0) {
	}

	/*
	 * Support methods
	 */
	private static void reset_bool(boolean[] x) {
		Arrays.fill(x, false);
	}

	/*
	 * ClipboardOwner methods
	 */
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		logger.debug("Lost clipboard ownership");
	}

	public void copyToClipboard(Transferable t) {
		clipboard.setContents(t, this);
	}

}
