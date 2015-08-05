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

package automenta.vnc;

import automenta.vivisect.swing.NWindow;
import automenta.vnc.drawing.Renderer;
import automenta.vnc.rfb.client.ClientToServerMessage;
import automenta.vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;
import automenta.vnc.rfb.protocol.ProtocolSettings;
import automenta.vnc.viewer.ConnectionPresenter;
import automenta.vnc.viewer.UiSettings;
import automenta.vnc.viewer.cli.VNCProperties;
import automenta.vnc.viewer.swing.*;
import automenta.vnc.viewer.swing.gui.ConnectionView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public abstract class VNCClient extends JPanel implements WindowListener, KeyListener {

	private final Logger logger;
    private int paramsMask;
    private boolean allowAppletInteractiveConnections;

    private final ConnectionParams connectionParams;
    private String passwordFromParams;
    boolean isSeparateFrame = true;
    boolean isApplet = true;
    private final ProtocolSettings settings;
    private final UiSettings uiSettings;
    private volatile boolean isAppletStopped = false;
    private ConnectionPresenter connectionPresenter;

    public static void main(String... args) {

        //TODO use parameters
        VNCProperties param = new VNCProperties("localhost",5091);

		ParametersHandler.completeParserOptions(param);

		param.parse(args);
		if (param.isSet(ParametersHandler.ARG_HELP)) {
			printUsage(param.optionsUsage());
			System.exit(0);
		}


        NWindow w = new NWindow("VNC",  new VNCClient(param) {
            @Override public String getParameter(String p) {
                return null;
            }
        }).show(800,600,true);



    }

    public static void printUsage(String additional) {
		System.out.println("Usage: java -jar (progfilename) [hostname [port_number]] [Options]\n" +
				"    or\n"+
				" java -jar (progfilename) [Options]\n" +
				"    or\n java -jar (progfilename) -help\n    to view this help\n\n" +
				"Where Options are:\n" + additional +
				"\nOptions format: -optionName=optionValue. Ex. -host=localhost -port=5900 -viewonly=yes\n" +
				"Both option name and option value are case insensitive.");

    }

    public VNCClient(String host, int port) {
        super();
        logger = Logger.getLogger(getClass().getName());
        connectionParams = new ConnectionParams();
        connectionParams.setHostName(host);
        connectionParams.setPortNumber(port);
        settings = ProtocolSettings.getDefaultSettings();
        uiSettings = new UiSettings();
        initVNC();
    }

	public VNCClient() {
        super();
        logger = Logger.getLogger(getClass().getName());
		connectionParams = new ConnectionParams();
		settings = ProtocolSettings.getDefaultSettings();
		uiSettings = new UiSettings();
        initVNC();

	}



    public VNCClient(VNCProperties parser) {
		this();

        setLoggingLevel(parser.isSet(ParametersHandler.ARG_VERBOSE) ? Level.FINE :
                parser.isSet(ParametersHandler.ARG_VERBOSE_MORE) ? Level.FINER :
                        Level.INFO);

        paramsMask = ParametersHandler.completeSettingsFromCLI(parser, connectionParams, settings, uiSettings);
		passwordFromParams = parser.getValueFor(ParametersHandler.ARG_PASSWORD);
		logger.info("TightVNC Viewer version " + ver());
		isApplet = false;
	}

    private static void setLoggingLevel(Level levelToSet) {
        final Logger appLogger = Logger.getLogger("com.glavsoft");
        appLogger.setLevel(levelToSet);
        ConsoleHandler ch = null;
        for (Handler h : appLogger.getHandlers()) {
            if (h instanceof ConsoleHandler) {
                ch = (ConsoleHandler) h;
                break;
            }
        }
        if (null == ch) {
            ch = new ConsoleHandler();
            appLogger.addHandler(ch);
        }
//        ch.setFormatter(new SimpleFormatter());
        ch.setLevel(levelToSet);
    }


    @Override
	public void windowClosing(WindowEvent e) {
		if (e != null && e.getComponent() != null) {
            final Window w = e.getWindow();
            if (w != null) {
                w.setVisible(false);
                w.dispose();
            }
		}
		closeApp();
	}

	/**
	 * Closes App(lication) or stops App(let).
	 */
    public void closeApp() {
        if (connectionPresenter != null) {
            connectionPresenter.cancelConnection();
            logger.info("Connections cancelled.");
        }
        if (isApplet) {
//            if ( ! isAppletStopped) {
//                logger.severe("Applet is stopped.");
//                isAppletStopped  = true;
//                repaint();
//                stop();
//            }
		} else {
			System.exit(0);
		}
	}

	@Override
	public void paint(Graphics g) {
			/*removeAll();
			g.clearRect(0, 0, getWidth(), getHeight());
			g.drawString("Disconnected", 10, 20);
*/
        super.paint(g);

	}


    abstract public String getParameter(String p);


	public void initVNC() {

		//paramsMask = ParametersHandler.completeSettingsFromApplet(this, connectionParams, settings, uiSettings);
		isSeparateFrame = ParametersHandler.isSeparateFrame;
		passwordFromParams = getParameter(ParametersHandler.ARG_PASSWORD);
		isApplet = true;
        allowAppletInteractiveConnections = ParametersHandler.allowAppletInteractiveConnections;
		repaint();



        final boolean hasJsch = checkJsch();
        final boolean allowInteractive = allowAppletInteractiveConnections || ! isApplet;
        connectionPresenter = new ConnectionPresenter(hasJsch, allowInteractive) {
            @Override public void frameBufferUpdate(automenta.vnc.drawing.Renderer renderer, FramebufferUpdateRectangle rect) {
                VNCClient.this.videoUpdate(renderer, rect);
            }

            @Override
            public void onMessageSend(ClientToServerMessage message) {
                VNCClient.this.onMessageSend(message);
            }

            @Override
            public void successfulRfbConnection() {
                super.successfulRfbConnection();
                onConnected();
            }
        };

        connectionPresenter.addModel("ConnectionParamsModel", connectionParams);
        final ConnectionView connectionView = new ConnectionView(
                VNCClient.this, // appWindowListener
                connectionPresenter, hasJsch);
        connectionPresenter.addView(ConnectionPresenter.CONNECTION_VIEW, connectionView);
//        if (isApplet) {
//            connectionPresenter.addView("AppletStatusStringView", new View() {
//                @Override
//                public void showView() { /*nop*/ }
//                @Override
//                public void closeView() { /*nop*/ }
//                @SuppressWarnings("UnusedDeclaration")
//                public void setMessage(String message) {
//                    VNCClient.this.getAppletContext().showStatus(message);
//                }
//            });
//        }


        connectionPresenter.setConnectionWorkerFactory(
                new SwingConnectionWorkerFactory(connectionView.getFrame(), passwordFromParams, connectionPresenter,
                        new SwingViewerWindowFactory(isSeparateFrame, isApplet, this)
                ));

        connectionPresenter.startConnection(settings, uiSettings, paramsMask);


    }

    protected void onMessageSend(ClientToServerMessage message) {

    }

    private void onConnected() {

        //Ex3:
        getSurface().key.setProxy(this);

    }

    public Surface getSurface() {
        if (connectionPresenter==null) return null;
        return connectionPresenter.getSurface();
    }

    //public void inputKey(char chr, boolean press) {
        //TODO
    //}


    /** inject keypress to event stream, to remote VNC host */
    @Deprecated public void inputKey(char chr) {
        getSurface().key.keyTyped(chr, this);
    }

    public void inputKey(KeyEvent k) {
        //System.err.println("TYPE: " + k);
        try {
            getSurface().dispatchEvent(k);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void inputKey(int code, int modifiers, int mode) {
        try {
            KeyEvent ke = new KeyEvent(getSurface(),
                    mode, System.currentTimeMillis(),
                    modifiers,
                    code);
            inputKey(ke);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** when received a new video frame buffer update */
    protected void videoUpdate(Renderer image, FramebufferUpdateRectangle rect) {
    }


    private static boolean checkJsch() {
        try {
            Class.forName("com.jcraft.jsch.JSch");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


	@Override
	public void windowOpened(WindowEvent e) { /* nop */ }
	@Override
	public void windowClosed(WindowEvent e) { /* nop */ }
	@Override
	public void windowIconified(WindowEvent e) { /* nop */ }
	@Override
	public void windowDeiconified(WindowEvent e) { /* nop */ }
	@Override
	public void windowActivated(WindowEvent e) { /* nop */ }
	@Override
	public void windowDeactivated(WindowEvent e) { /* nop */ }

	public static String ver() {
		final InputStream mfStream = VNCClient.class.getClassLoader().getResourceAsStream(
				"META-INF/MANIFEST.MF");
		if (null == mfStream) {
			System.out.println("No Manifest file found.");
			return "-1";
		}
		try {
			Manifest mf = new Manifest();
			mf.read(mfStream);
			Attributes atts = mf.getMainAttributes();
			return atts.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
		} catch (IOException e) {
			return "-2";
		}
	}

    @Override
    public void keyTyped(KeyEvent e) {
        //System.out.println("typed: " + e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //System.out.println("press: " + e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //System.out.println("release: " + e);
    }

}
