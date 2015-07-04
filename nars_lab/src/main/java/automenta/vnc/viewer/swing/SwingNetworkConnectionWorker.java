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

package automenta.vnc.viewer.swing;

import automenta.vnc.viewer.CancelConnectionException;
import automenta.vnc.viewer.ConnectionErrorException;
import automenta.vnc.viewer.ConnectionPresenter;
import automenta.vnc.viewer.NetworkConnectionWorker;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.AccessControlException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SwingNetworkConnectionWorker extends SwingWorker<Socket, String> implements NetworkConnectionWorker {
    public static final int MAX_HOSTNAME_LENGTH_FOR_MESSAGES = 40;
    private final JFrame parentWindow;
    private final Logger logger;
    private boolean hasSshSupport;
    private ConnectionParams connectionParams;
    private ConnectionPresenter presenter;


    public SwingNetworkConnectionWorker(JFrame parentWindow) {
        this.parentWindow = parentWindow;
        logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public Socket doInBackground() throws Exception {
        String s = "<b>" +connectionParams.hostName + "</b>:" + connectionParams.getPortNumber();
        if (connectionParams.useSsh()) {
            s += " <i>(via ssh://" + connectionParams.sshUserName + '@' + connectionParams.sshHostName + ':' + connectionParams.getSshPortNumber() + ")</i>";
        }

        String message = "<html>Trying to connect to " + s + "</html>";
        logger.info(message.replaceAll("<[^<>]+?>", ""));
        publish(message);
        int port;
        String host;
        /*if (hasSshSupport && connectionParams.useSsh()) {

            SshConnectionManager sshConnectionManager = new SshConnectionManager(parentWindow);
            message = "Creating SSH tunnel to " + connectionParams.sshHostName + ":" + connectionParams.getSshPortNumber();
            logger.info(message);
            publish(message);
            port = sshConnectionManager.connect(connectionParams);
            if (sshConnectionManager.isConnected() ) {
                host = "127.0.0.1";
                message = "SSH tunnel established: " + host + ":" + port;
                logger.info(message);
                publish(message);
            } else {
                throw new ConnectionErrorException("Could not create SSH tunnel: " + sshConnectionManager.getErrorMessage());
            }

        } else */ {
            host = connectionParams.hostName;
            port = connectionParams.getPortNumber();
        }

        message = "Connecting to host " + host + ':' + port + (connectionParams.useSsh() ? " (tunneled)" : "");
        logger.info(message);
        publish(message);

        return new Socket(host, port);
    }

    private static String formatHostString(String hostName) {
        if (hostName.length() <= MAX_HOSTNAME_LENGTH_FOR_MESSAGES) {
            return  hostName;
        } else {
            return hostName.substring(0, MAX_HOSTNAME_LENGTH_FOR_MESSAGES) + "...";
        }
    }

    @Override
    protected void process(List<String> strings) { // EDT
        String message = strings.get(strings.size() - 1); // get last
        presenter.showMessage(message);
    }

    @Override
    protected void done() { // EDT
        try {
            final Socket socket = get();
            presenter.successfulNetworkConnection(socket);
        } catch (CancellationException e) {
            logger.info("Cancelled");
            presenter.showMessage("Cancelled");
            presenter.connectionFailed();
        } catch (InterruptedException e) {
            logger.info("Interrupted");
            presenter.showMessage("Interrupted");
            presenter.connectionFailed();
        } catch (ExecutionException e) {
            String errorMessage = null;
            try {
                throw e.getCause();
            } catch (UnknownHostException uhe) {
                logger.severe("Unknown host: " + connectionParams.hostName);
                errorMessage = "Unknown host: '" + formatHostString(connectionParams.hostName) + '\'';
            } catch (IOException ioe) {
                logger.severe("Couldn't connect to '" + connectionParams.hostName +
                        ':' + connectionParams.getPortNumber() + "':\n" + "IOException\n" + ioe.getMessage());
                logger.log(Level.FINEST, "Couldn't connect to '" + connectionParams.hostName +
                        ':' + connectionParams.getPortNumber() + "':\n" + "IOException\n" + ioe.getMessage(), ioe);
                errorMessage = "Couldn't connect to '" + formatHostString(connectionParams.hostName) +
                        ':' + connectionParams.getPortNumber() + "':\n" + "IOException\n" + ioe.getMessage();
            } catch (CancelConnectionException cce) {
                logger.severe("Cancelled: " + cce.getMessage());
            } catch (AccessControlException ace) {
                logger.severe("Couldn't connect to: " +
                        connectionParams.hostName + ':' + connectionParams.getPortNumber() +
                        ": " + "AccessControlException\n" + ace.getMessage());
                logger.log(Level.FINEST, "Couldn't connect to: " +
                        connectionParams.hostName + ':' + connectionParams.getPortNumber() +
                        ": " + "AccessControlException\n" + ace.getMessage(), ace);
                errorMessage = "Access control error";
            } catch (ConnectionErrorException cee) {
                logger.severe(cee.getMessage() + " host: " +
                        connectionParams.hostName + ':' + connectionParams.getPortNumber());
                errorMessage = cee.getMessage() + "\nHost: " +
                    formatHostString(connectionParams.hostName) + ':' + connectionParams.getPortNumber();
            } catch (Throwable throwable) {
                logger.log(Level.FINEST, "Couldn't connect to '" + formatHostString(connectionParams.hostName) +
                        ':' + connectionParams.getPortNumber() + "':\n" + "Caught a throwable\n" + throwable.getMessage(), throwable);
                errorMessage = "Couldn't connect to '" + formatHostString(connectionParams.hostName) +
                        ':' + connectionParams.getPortNumber() + "':\n" + "Caught a throwable\n" + throwable + '\n' + throwable.getMessage();
                throwable.printStackTrace();
                //throw new Exception("Some other Error", throwable);
            }
            presenter.showConnectionErrorDialog(errorMessage);
            presenter.clearMessage();
            presenter.connectionFailed();
        }
    }

    @Override
    public void setConnectionParams(ConnectionParams connectionParams) {
        this.connectionParams = connectionParams;
    }

    @Override
    public void setPresenter(ConnectionPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setHasSshSupport(boolean hasSshSupport) {
        this.hasSshSupport = hasSshSupport;
    }

    @Override
    public boolean cancel() {
        return super.cancel(true);
    }
}
