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

package vnc;

import vnc.rfb.protocol.ProtocolSettings;
import vnc.viewer.ConnectionPresenter;
import vnc.viewer.UiSettings;
import vnc.viewer.cli.Parser;
import vnc.viewer.mvp.View;
import vnc.viewer.swing.ConnectionParams;
import vnc.viewer.swing.ParametersHandler;
import vnc.viewer.swing.SwingConnectionWorkerFactory;
import vnc.viewer.swing.SwingViewerWindowFactory;
import vnc.viewer.swing.gui.ConnectionView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.*;

@SuppressWarnings("serial")
public class VNCClient extends JApplet implements Runnable, WindowListener {

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
		Parser parser = new Parser();
		ParametersHandler.completeParserOptions(parser);

		parser.parse(args);
		if (parser.isSet(ParametersHandler.ARG_HELP)) {
			printUsage(parser.optionsUsage());
			System.exit(0);
		}
		VNCClient viewer = new VNCClient(parser);
		SwingUtilities.invokeLater(viewer);
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

	public VNCClient() {
        logger = Logger.getLogger(getClass().getName());
		connectionParams = new ConnectionParams();
		settings = ProtocolSettings.getDefaultSettings();
		uiSettings = new UiSettings();
	}

	private VNCClient(Parser parser) {
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
            if ( ! isAppletStopped) {
                logger.severe("Applet is stopped.");
                isAppletStopped  = true;
                repaint();
                stop();
            }
		} else {
			System.exit(0);
		}
	}

	@Override
	public void paint(Graphics g) {
		if ( ! isAppletStopped) {
			super.paint(g);
		} else {
			getContentPane().removeAll();
			g.clearRect(0, 0, getWidth(), getHeight());
			g.drawString("Disconnected", 10, 20);
		}
	}

	@Override
	public void destroy() {
		closeApp();
		super.destroy();
	}

	@Override
	public void init() {
		paramsMask = ParametersHandler.completeSettingsFromApplet(this, connectionParams, settings, uiSettings);
		isSeparateFrame = ParametersHandler.isSeparateFrame;
		passwordFromParams = getParameter(ParametersHandler.ARG_PASSWORD);
		isApplet = true;
        allowAppletInteractiveConnections = ParametersHandler.allowAppletInteractiveConnections;
		repaint();

        try {
            SwingUtilities.invokeAndWait(this);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

	@Override
	public void start() {
		super.start();
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
	public void run() {

        final boolean hasJsch = checkJsch();
        final boolean allowInteractive = allowAppletInteractiveConnections || ! isApplet;
        connectionPresenter = new ConnectionPresenter(hasJsch, allowInteractive);
        connectionPresenter.addModel("ConnectionParamsModel", connectionParams);
        final ConnectionView connectionView = new ConnectionView(
                VNCClient.this, // appWindowListener
                connectionPresenter, hasJsch);
        connectionPresenter.addView(ConnectionPresenter.CONNECTION_VIEW, connectionView);
        if (isApplet) {
            connectionPresenter.addView("AppletStatusStringView", new View() {
                @Override
                public void showView() { /*nop*/ }
                @Override
                public void closeView() { /*nop*/ }
                @SuppressWarnings("UnusedDeclaration")
                public void setMessage(String message) {
                    VNCClient.this.getAppletContext().showStatus(message);
                }
            });
        }

        SwingViewerWindowFactory viewerWindowFactory = new SwingViewerWindowFactory(isSeparateFrame, isApplet, this);

        connectionPresenter.setConnectionWorkerFactory(
                new SwingConnectionWorkerFactory(connectionView.getFrame(), passwordFromParams, connectionPresenter, viewerWindowFactory));

        connectionPresenter.startConnection(settings, uiSettings, paramsMask);
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

}
