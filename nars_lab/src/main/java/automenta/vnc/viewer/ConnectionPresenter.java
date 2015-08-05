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

package automenta.vnc.viewer;

import automenta.vnc.drawing.Renderer;
import automenta.vnc.rfb.client.ClientToServerMessage;
import automenta.vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;
import automenta.vnc.rfb.protocol.ProtocolSettings;
import automenta.vnc.utils.Strings;
import automenta.vnc.viewer.mvp.Presenter;
import automenta.vnc.viewer.swing.ConnectionParams;
import automenta.vnc.viewer.swing.Surface;
import automenta.vnc.viewer.swing.WrongParameterException;
import automenta.vnc.viewer.swing.gui.ConnectionView;
import automenta.vnc.viewer.swing.gui.ConnectionsHistory;

import java.net.Socket;
import java.util.logging.Logger;

/**
 * @author dime at tightvnc.com
 */
public abstract class ConnectionPresenter extends Presenter {
    public static final String PROPERTY_HOST_NAME = "HostName";
    public static final String PROPERTY_RFB_PORT_NUMBER = "PortNumber";
    public static final String PROPERTY_USE_SSH = "UseSsh";
    private static final String PROPERTY_SSH_USER_NAME = "SshUserName";
    private static final String PROPERTY_SSH_HOST_NAME = "SshHostName";
    private static final String PROPERTY_SSH_PORT_NUMBER = "SshPortNumber";
    private static final String PROPERTY_SSH_PRIVATE_KEY = "SshPrivateKey";
    private static final String PROPERTY_SSH_PUBLIC_KEY = "SshPublicKey";
    private static final String PROPERTY_SSH_HOST_KEY = "SshHostKey";
    private static final String PROPERTY_STATUS_BAR_MESSAGE = "Message";
    private static final String PROPERTY_CONNECTION_IN_PROGRESS = "ConnectionInProgress";
    public static final String CONNECTION_PARAMS_MODEL = "ConnectionParamsModel";
    public static final String CONNECTIONS_HISTORY_MODEL = "ConnectionsHistoryModel";
    public static final String CONNECTION_VIEW = "ConnectionView";

    private final boolean hasSshSupport;
    private final boolean allowInteractive;
    private ConnectionsHistory connectionsHistory;
    private ProtocolSettings rfbSettings;
    private UiSettings uiSettings;
    private final Logger logger;
    private RfbConnectionWorker rfbConnectionWorker;
    private AbstractConnectionWorkerFactory connectionWorkerFactory;
    private NetworkConnectionWorker networkConnectionWorker;
    private boolean needReconnection = true;

    public RfbConnectionWorker getRfbConnectionWorker() {
        return rfbConnectionWorker;
    }

    public NetworkConnectionWorker getNetworkConnectionWorker() {
        return networkConnectionWorker;
    }

    public ConnectionPresenter(boolean hasSshSupport, boolean allowInteractive) {
        this.hasSshSupport = hasSshSupport;
        this.allowInteractive = allowInteractive;
        logger = Logger.getLogger(getClass().getName());
    }

    public void startConnection(ProtocolSettings rfbSettings, UiSettings uiSettings, int paramSettingsMask)
            throws IllegalStateException {
        this.rfbSettings = rfbSettings;
        this.uiSettings = uiSettings;
        if ( ! isModelRegisteredByName(CONNECTION_PARAMS_MODEL)) {
            throw new IllegalStateException("No Connection Params model added.");
        }
        connectionsHistory = new ConnectionsHistory();
        addModel(CONNECTIONS_HISTORY_MODEL, connectionsHistory);
        if (syncModels(paramSettingsMask)) {
            if (allowInteractive) {
                show();
                populate();
            } else {
                connect();
            }
        }
    }



    public void setUseSsh(boolean useSsh) {
        setModelProperty(PROPERTY_USE_SSH, useSsh, boolean.class);
    }

    public void submitConnection(String hostName) throws WrongParameterException {
        if (Strings.isTrimmedEmpty(hostName)) {
            throw new WrongParameterException("Host name is empty", PROPERTY_HOST_NAME);
        }
        setModelProperty(PROPERTY_HOST_NAME, hostName);

        final String rfbPort = (String) getViewPropertyOrNull(PROPERTY_RFB_PORT_NUMBER);
        setModelProperty(PROPERTY_RFB_PORT_NUMBER, rfbPort);
        try {
            throwPossiblyHappenedException();
        } catch (Throwable e) {
            throw new WrongParameterException("Wrong Port", PROPERTY_HOST_NAME);
        }
        setSshOptions();

        saveHistory();
        populateFrom(CONNECTIONS_HISTORY_MODEL);

        connect();
    }

    public void saveHistory() {
        final ConnectionParams cp = (ConnectionParams) getModel(CONNECTION_PARAMS_MODEL);
        connectionsHistory.reorder(cp, rfbSettings, uiSettings);
        connectionsHistory.save();
    }

    private void connect() {
        final ConnectionParams connectionParams = (ConnectionParams) getModel(CONNECTION_PARAMS_MODEL);
        // TODO check connectionWorkerFactory is init
        networkConnectionWorker = connectionWorkerFactory.createNetworkConnectionWorker();
        networkConnectionWorker.setConnectionParams(connectionParams);
        networkConnectionWorker.setPresenter(this);
        networkConnectionWorker.setHasSshSupport(hasSshSupport);
        networkConnectionWorker.execute();
    }

    public void connectionFailed() {
        cancelConnection();
        if (allowInteractive) {
            enableConnectionDialog();
        } else {
            connect();
        }
    }

    public void connectionCancelled() {
        cancelConnection();
        if (allowInteractive) {
            enableConnectionDialog();
        } else {
            final ConnectionView connectionView = (ConnectionView) getView(CONNECTION_VIEW);
            if (connectionView != null) {
                connectionView.closeApp();
            }
        }
    }

    private void enableConnectionDialog() {
        setViewProperty(PROPERTY_CONNECTION_IN_PROGRESS, false, boolean.class);
    }

    public void successfulNetworkConnection(Socket workingSocket) { // EDT
        logger.info("Connected");
        showMessage("Connected");
        rfbConnectionWorker = connectionWorkerFactory.createRfbConnectionWorker();
        rfbConnectionWorker.setWorkingSocket(workingSocket);
        rfbConnectionWorker.setRfbSettings(rfbSettings);
        rfbConnectionWorker.setUiSettings(uiSettings);
        rfbConnectionWorker.setConnectionString(
                getModelProperty(PROPERTY_HOST_NAME) + ":" + getModelProperty(PROPERTY_RFB_PORT_NUMBER));
        rfbConnectionWorker.execute();
    }

    public void successfulRfbConnection() {
        enableConnectionDialog();
        getView(CONNECTION_VIEW).closeView();
    }

    public void cancelConnection() {
        if (networkConnectionWorker != null) {
            networkConnectionWorker.cancel();
        }
        if (rfbConnectionWorker != null) {
            rfbConnectionWorker.cancel();
        }
    }

    public void showConnectionErrorDialog(String message) {
        final ConnectionView connectionView = (ConnectionView) getView(CONNECTION_VIEW);
        if (connectionView != null) {
            connectionView.showConnectionErrorDialog(message);
        }
    }

    public void showReconnectDialog(String errorTitle, String errorMessage) {
        final ConnectionView connectionView = (ConnectionView) getView(CONNECTION_VIEW);
        if (connectionView != null) {
            connectionView.showReconnectDialog(errorTitle, errorMessage);
        }
    }

    private void setSshOptions() {
		if (hasSshSupport) {
            try {
                final boolean useSsh = (Boolean)getViewProperty(PROPERTY_USE_SSH);
                setModelProperty(PROPERTY_USE_SSH, useSsh, boolean.class);
            } catch (PropertyNotFoundException e) {
                //nop
            }
            setModelProperty(PROPERTY_SSH_USER_NAME, getViewPropertyOrNull(PROPERTY_SSH_USER_NAME));
            setModelProperty(PROPERTY_SSH_HOST_NAME, getViewPropertyOrNull(PROPERTY_SSH_HOST_NAME));
            setModelProperty(PROPERTY_SSH_PORT_NUMBER, getViewPropertyOrNull(PROPERTY_SSH_PORT_NUMBER));
            setViewProperty(PROPERTY_SSH_PORT_NUMBER, getModelProperty(PROPERTY_SSH_PORT_NUMBER));
        }
	}

    private boolean syncModels(int paramSettingsMask) {

        final ConnectionParams cp = (ConnectionParams) getModel(CONNECTION_PARAMS_MODEL);
        if (cp==null) return false;

        final ConnectionParams mostSuitableConnection = connectionsHistory.getMostSuitableConnection(cp);
        if (mostSuitableConnection==null) return false;

        cp.completeEmptyFieldsFrom(mostSuitableConnection);
        rfbSettings.copyDataFrom(connectionsHistory.getProtocolSettings(mostSuitableConnection), paramSettingsMask & 0xffff);
        uiSettings.copyDataFrom(connectionsHistory.getUiSettingsData(mostSuitableConnection), (paramSettingsMask >> 16) & 0xffff);
        if ( ! cp.isHostNameEmpty()) {
            connectionsHistory.reorder(cp, rfbSettings, uiSettings);
        }

        return true;
//        protocolSettings.addListener(connectionsHistory);
//        uiSettings.addListener(connectionsHistory);
    }

    public void populateFromHistoryItem(ConnectionParams connectionParams) {
        setModelProperty(PROPERTY_HOST_NAME, connectionParams.hostName);
        setModelProperty(PROPERTY_RFB_PORT_NUMBER, connectionParams.getPortNumber(), int.class);
        setModelProperty(PROPERTY_USE_SSH, connectionParams.useSsh(), boolean.class);
        setModelProperty(PROPERTY_SSH_HOST_NAME, connectionParams.sshHostName);
        setModelProperty(PROPERTY_SSH_PORT_NUMBER, connectionParams.getSshPortNumber(), int.class);
        setModelProperty(PROPERTY_SSH_USER_NAME, connectionParams.sshUserName);
        populateFrom(CONNECTION_PARAMS_MODEL);
        rfbSettings.copyDataFrom(connectionsHistory.getProtocolSettings(connectionParams));
        uiSettings.copyDataFrom(connectionsHistory.getUiSettingsData(connectionParams));
    }

    public void clearHistory() {
        connectionsHistory.clear();
        connectionsHistory.reorder((ConnectionParams) getModel(CONNECTION_PARAMS_MODEL), rfbSettings, uiSettings);
        populateFrom(CONNECTIONS_HISTORY_MODEL);
        clearMessage();
    }

    public void showMessage(String message) {
        setViewProperty(PROPERTY_STATUS_BAR_MESSAGE, message);
    }

    public void clearMessage() {
        showMessage("");
    }

    public void setConnectionWorkerFactory(AbstractConnectionWorkerFactory connectionWorkerFactory) {
        this.connectionWorkerFactory = connectionWorkerFactory;
    }

    public void reconnect(String predefinedPassword) {
        connectionWorkerFactory.setPredefinedPassword(predefinedPassword);
        if (allowInteractive) {
            clearMessage();
            enableConnectionDialog();
            show();
            populate();
        } else if (needReconnection) {
            connect();
        }
    }

    public void clearPredefinedPassword() {
        connectionWorkerFactory.setPredefinedPassword(null);
    }

    public UiSettings getUiSettings() {
        return uiSettings;
    }

    public ProtocolSettings getRfbSettings() {
        return rfbSettings;
    }

    public boolean needReconnection() {
        return needReconnection;
    }

    public void setNeedReconnection(boolean need) {
        needReconnection = need;
    }

    public boolean allowInteractive() {
        return allowInteractive;
    }


    abstract public void frameBufferUpdate(Renderer renderer, FramebufferUpdateRectangle rect);

    public Surface getSurface() {
        if (getRfbConnectionWorker()==null) return null;
        return getRfbConnectionWorker().getSurface();
    }

    public abstract void onMessageSend(ClientToServerMessage message);
}
