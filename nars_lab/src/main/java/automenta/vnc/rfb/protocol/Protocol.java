// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//

package automenta.vnc.rfb.protocol;

import automenta.vnc.core.SettingsChangedEvent;
import automenta.vnc.drawing.Renderer;
import automenta.vnc.exceptions.*;
import automenta.vnc.rfb.*;
import automenta.vnc.rfb.client.ClientToServerMessage;
import automenta.vnc.rfb.client.FramebufferUpdateRequestMessage;
import automenta.vnc.rfb.client.SetEncodingsMessage;
import automenta.vnc.rfb.client.SetPixelFormatMessage;
import automenta.vnc.rfb.encoding.PixelFormat;
import automenta.vnc.rfb.encoding.decoder.DecodersContainer;
import automenta.vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;
import automenta.vnc.rfb.protocol.state.HandshakeState;
import automenta.vnc.rfb.protocol.state.ProtocolState;
import automenta.vnc.transport.Reader;
import automenta.vnc.transport.Writer;

import java.util.logging.Logger;

abstract public class Protocol implements ProtocolContext, IChangeSettingsListener {
	private ProtocolState state;
	private final Logger logger;
	private final IPasswordRetriever passwordRetriever;
	private final ProtocolSettings settings;
	private int fbWidth;
	private int fbHeight;
	private PixelFormat pixelFormat;
	private final Reader reader;
	private final Writer writer;
	private String remoteDesktopName;
	private MessageQueue messageQueue;
	private final DecodersContainer decoders;
	private SenderTask senderTask;
	private ReceiverTask receiverTask;
	private IRfbSessionListener rfbSessionListener;
	private IRepaintController repaintController;
	private PixelFormat serverPixelFormat;
	private Thread senderThread;
	private Thread receiverThread;
    private boolean isTight;
    private String protocolVersion;

    public Protocol(Reader reader, Writer writer,
			IPasswordRetriever passwordRetriever, ProtocolSettings settings) {
		this.reader = reader;
		this.writer = writer;
		this.passwordRetriever = passwordRetriever;
		this.settings = settings;
		decoders = new DecodersContainer();
		decoders.instantiateDecodersWhenNeeded(settings.encodings);
		state = new HandshakeState(this);
        logger = Logger.getLogger(getClass().getName());
    }

	@Override
	public void changeStateTo(ProtocolState state) {
		this.state = state;
	}

	public void handshake() throws UnsupportedProtocolVersionException, UnsupportedSecurityTypeException,
			AuthenticationFailedException, TransportException, FatalException {
		while (state.next()) {
			// continue;
		}
		this.messageQueue = new MessageQueue();
	}

	@Override
	public PixelFormat getPixelFormat() {
		return pixelFormat;
	}

	@Override
	public void setPixelFormat(PixelFormat pixelFormat) {
		this.pixelFormat = pixelFormat;
		if (repaintController != null) {
			repaintController.setPixelFormat(pixelFormat);
		}
	}

	@Override
	public String getRemoteDesktopName() {
		return remoteDesktopName;
	}

	@Override
	public void setRemoteDesktopName(String name) {
		remoteDesktopName = name;
	}

	@Override
	public int getFbWidth() {
		return fbWidth;
	}

	@Override
	public void setFbWidth(int fbWidth) {
		this.fbWidth = fbWidth;
	}

	@Override
	public int getFbHeight() {
		return fbHeight;
	}

	@Override
	public void setFbHeight(int fbHeight) {
		this.fbHeight = fbHeight;
	}

	@Override
	public IPasswordRetriever getPasswordRetriever() {
		return passwordRetriever;
	}

	@Override
	public ProtocolSettings getSettings() {
		return settings;
	}

    @Override
	public Writer getWriter() {
		return writer;
	}

	@Override
	public Reader getReader() {
		return reader;
	}

	/**
	 * Following the server initialisation message it's up to the client to send
	 * whichever protocol messages it wants.  Typically it will send a
	 * SetPixelFormat message and a SetEncodings message, followed by a
	 * FramebufferUpdateRequest.  From then on the server will send
	 * FramebufferUpdate messages in response to the client's
	 * FramebufferUpdateRequest messages.  The client should send
	 * FramebufferUpdateRequest messages with incremental set to true when it has
	 * finished processing one FramebufferUpdate and is ready to process another.
	 * With a fast client, the rate at which FramebufferUpdateRequests are sent
	 * should be regulated to avoid hogging the network.
	 */
	public void startNormalHandling(IRfbSessionListener rfbSessionListener,
			IRepaintController repaintController, ClipboardController clipboardController) {
		this.rfbSessionListener = rfbSessionListener;
		this.repaintController = repaintController;
//		if (settings.getColorDepth() == 0) {
//			settings.setColorDepth(pixelFormat.depth); // the same the server sent when not initialized yet
//		}
		serverPixelFormat = pixelFormat;
        correctServerPixelFormat();
		setPixelFormat(createPixelFormat(settings));
		sendMessage(new SetPixelFormatMessage(pixelFormat));
		logger.fine("sent: " + pixelFormat);

		sendSupportedEncodingsMessage(settings);
		settings.addListener(this); // to support pixel format (color depth), and encodings changes
		settings.addListener(repaintController);

		sendRefreshMessage();
		senderTask = new SenderTask(messageQueue, writer, this);
		senderThread = new Thread(senderTask, "RfbSenderTask");
		senderThread.start();
		decoders.resetDecoders();
		receiverTask = new ReceiverTask(
				reader, repaintController,
				clipboardController,
				decoders, this) {
            @Override
            protected void frameBufferUpdate(Renderer renderer, FramebufferUpdateRectangle rect) {
                Protocol.this.frameBufferUpdate(renderer, rect);
            }
        };
		receiverThread = new Thread(receiverTask, "RfbReceiverTask");
		receiverThread.start();
	}

    protected abstract void frameBufferUpdate(Renderer renderer, FramebufferUpdateRectangle rect);

    private void correctServerPixelFormat() {
        // correct true color flag - we don't support color maps, so always set it up
        serverPixelFormat.trueColourFlag = 1;
        // correct .depth to use actual depth 24 instead of incorrect 32, used by ex. UltraVNC server, that cause
        // protocol incompatibility in ZRLE encoding
        final long significant = serverPixelFormat.redMax << serverPixelFormat.redShift |
                serverPixelFormat.greenMax << serverPixelFormat.greenShift |
                serverPixelFormat.blueMax << serverPixelFormat.blueShift;
        if (32 == serverPixelFormat.bitsPerPixel &&
                ((significant & 0x00ff000000L) == 0 || (significant & 0x000000ffL) == 0) &&
                32 == serverPixelFormat.depth) {
            serverPixelFormat.depth = 24;
        }
    }

    @Override
	public void sendMessage(ClientToServerMessage message) {
		messageQueue.put(message);
	}

	private void sendSupportedEncodingsMessage(ProtocolSettings settings) {
		decoders.instantiateDecodersWhenNeeded(settings.encodings);
		SetEncodingsMessage encodingsMessage = new SetEncodingsMessage(settings.encodings);
		sendMessage(encodingsMessage);
		//logger.fine("sent: " + encodingsMessage.toString());
	}

	/**
	 * create pixel format by bpp
	 */
	private PixelFormat createPixelFormat(ProtocolSettings settings) {
		int serverBigEndianFlag = serverPixelFormat.bigEndianFlag;
		switch (settings.getColorDepth()) {
		case ProtocolSettings.COLOR_DEPTH_24:
			return PixelFormat.create24bitColorDepthPixelFormat(serverBigEndianFlag);
		case ProtocolSettings.COLOR_DEPTH_16:
			return PixelFormat.create16bitColorDepthPixelFormat(serverBigEndianFlag);
		case ProtocolSettings.COLOR_DEPTH_8:
			return PixelFormat.create8bitColorDepthBGRPixelFormat(serverBigEndianFlag);
		case ProtocolSettings.COLOR_DEPTH_6:
			return PixelFormat.create6bitColorDepthPixelFormat(serverBigEndianFlag);
		case ProtocolSettings.COLOR_DEPTH_3:
			return PixelFormat.create3bppPixelFormat(serverBigEndianFlag);
		case ProtocolSettings.COLOR_DEPTH_SERVER_SETTINGS:
			return serverPixelFormat;
		default:
			// unsupported bpp, use default
			return PixelFormat.create24bitColorDepthPixelFormat(serverBigEndianFlag);
		}
	}

	@Override
	public void settingsChanged(SettingsChangedEvent e) {
		ProtocolSettings settings = (ProtocolSettings) e.getSource();
		if (settings.isChangedEncodings()) {
			sendSupportedEncodingsMessage(settings);
		}
		if (settings.isChangedColorDepth() && receiverTask != null) {
			receiverTask.queueUpdatePixelFormat(createPixelFormat(settings));
		}
	}

	@Override
	public void sendRefreshMessage() {
		sendMessage(new FramebufferUpdateRequestMessage(0, 0, fbWidth, fbHeight, false));
		logger.fine("sent: full FB Refresh");
	}

	@Override
	public void cleanUpSession(String message) {
		cleanUpSession();
		rfbSessionListener.rfbSessionStopped(message);
	}

	public synchronized void cleanUpSession() {
		if (senderTask != null) { senderTask.stopTask(); }
		if (receiverTask != null) { receiverTask.stopTask(); }
		if (senderTask != null) {
			try {
				senderThread.join(1000);
			} catch (InterruptedException e) {
				// nop
			}
			senderTask = null;
		}
		if (receiverTask != null) {
			try {
				receiverThread.join(1000);
			} catch (InterruptedException e) {
				// nop
			}
			receiverTask = null;
		}
	}

    @Override
    public void setTight(boolean isTight) {
        this.isTight = isTight;
    }

    @Override
    public boolean isTight() {
        return isTight;
    }

    @Override
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public String getProtocolVersion() {
        return protocolVersion;
    }

}
