package automenta.vnc.viewer.swing.gui;

import automenta.vnc.rfb.protocol.ProtocolSettings;
import automenta.vnc.viewer.UiSettingsData;
import automenta.vnc.viewer.mvp.Model;
import automenta.vnc.viewer.swing.ConnectionParams;
import automenta.vnc.utils.Strings;
import automenta.vnc.viewer.UiSettings;

import java.io.*;
import java.security.AccessControlException;
import java.util.*;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author dime at tightvnc.com
 */
public class ConnectionsHistory implements Model {
	private static final int MAX_ITEMS = 32;
    public static final String CONNECTIONS_HISTORY_ROOT_NODE = "com/glavsoft/viewer/connectionsHistory";
    public static final String NODE_HOST_NAME = "hostName";
    public static final String NODE_PORT_NUMBER = "portNumber";
    public static final String NODE_SSH_USER_NAME = "sshUserName";
    public static final String NODE_SSH_HOST_NAME = "sshHostName";
    public static final String NODE_SSH_PORT_NUMBER = "sshPortNumber";
    public static final String NODE_USE_SSH = "useSsh";
    public static final String NODE_SSH_PRIVATE_KEY = "sshPrivateKey";
    public static final String NODE_SSH_PUBLIC_KEY = "sshPublicKey";
    public static final String NODE_SSH_HOST_KEY = "sshHostKey";
    public static final String NODE_SSH_REPLACE_SYMBOL = "sshKeyReplaceSymbol";
    public static final String NODE_PROTOCOL_SETTINGS = "protocolSettings";
    public static final String NODE_UI_SETTINGS = "uiSettings";
    private final Logger logger;

    private Map<ConnectionParams, ProtocolSettings> protocolSettingsMap;
    private Map<ConnectionParams, UiSettingsData> uiSettingsDataMap;
    LinkedList<ConnectionParams> connections;

    public ConnectionsHistory() {
        logger = Logger.getLogger(getClass().getName());
        init();
        retrieve();
    }

    private void init() {
        protocolSettingsMap = new HashMap<>();
        uiSettingsDataMap = new HashMap<>();
        connections = new LinkedList<>();
    }

    private void retrieve() {
        Preferences connectionsHistoryNode;
        try {
            connectionsHistoryNode = getConnectionHistoryNode();
        } catch (AccessControlException ace) {
            return;
        }
        try {
            final String[] orderNums;
			orderNums = connectionsHistoryNode.childrenNames();
			SortedMap<Integer, ConnectionParams> conns = new TreeMap<>();
            HashSet<ConnectionParams> uniques = new HashSet<>();
			for (String orderNum : orderNums) {
                int num = 0;
                try {
                    num = Integer.parseInt(orderNum);
                } catch (NumberFormatException skip) {
                    //nop
                }
				Preferences node = connectionsHistoryNode.node(orderNum);
                String hostName = node.get(NODE_HOST_NAME, null);
                if (null == hostName) continue; // skip entries without hostName field
                ConnectionParams cp = new ConnectionParams(hostName, node.getInt(NODE_PORT_NUMBER, 0),
                        node.getBoolean(NODE_USE_SSH, false), node.get(NODE_SSH_HOST_NAME, ""),
                        node.getInt(NODE_SSH_PORT_NUMBER, 0), node.get(NODE_SSH_USER_NAME, ""),
                        node.get(NODE_SSH_PRIVATE_KEY, ""), node.get(NODE_SSH_PUBLIC_KEY, ""),
                        node.get(NODE_SSH_HOST_KEY, ""), node.get(NODE_SSH_REPLACE_SYMBOL, "<>")
                );
                if (uniques.contains(cp)) continue; // skip duplicates
                uniques.add(cp);
                conns.put(num, cp);
                logger.finest("deserialialize: " + cp.toPrint());
                retrieveProtocolSettings(node, cp);
                retrieveUiSettings(node, cp);
			}
			int itemsCount = 0;
			for (ConnectionParams cp : conns.values()) {
				if (itemsCount < MAX_ITEMS) {
                    connections.add(cp);
				} else {
					connectionsHistoryNode.node(cp.hostName).removeNode();
				}
				++itemsCount;
			}
		} catch (BackingStoreException e) {
            logger.severe("Cannot retrieve connections history info: " + e.getMessage());
		}
	}

    private void retrieveUiSettings(Preferences node, ConnectionParams cp) {
        byte[] bytes = node.getByteArray(NODE_UI_SETTINGS, new byte[0]);
        if (bytes.length != 0) {
            try {
                UiSettingsData settings = (UiSettingsData) (new ObjectInputStream(
                        new ByteArrayInputStream(bytes))).readObject();
                uiSettingsDataMap.put(cp, settings);
                logger.finest("deserialialize: " + settings);
            } catch (IOException e) {
                logger.info("Cannot deserialize UiSettings: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                logger.severe("Cannot deserialize UiSettings : " + e.getMessage());
            }
        }
    }

    private void retrieveProtocolSettings(Preferences node, ConnectionParams cp) {
        byte[] bytes = node.getByteArray(NODE_PROTOCOL_SETTINGS, new byte[0]);
        if (bytes.length != 0) {
            try {
                ProtocolSettings settings = (ProtocolSettings) (new ObjectInputStream(
                        new ByteArrayInputStream(bytes))).readObject();
                settings.refine();
                protocolSettingsMap.put(cp, settings);
                logger.finest("deserialialize: " + settings);
            } catch (IOException e) {
                logger.info("Cannot deserialize ProtocolSettings: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                logger.severe("Cannot deserialize ProtocolSettings : " + e.getMessage());
            }
        }
    }

    /**
     * Implicit Model interface
     */
    @SuppressWarnings("UnusedDeclaration")
    public LinkedList<ConnectionParams> getConnectionsList() {
		return connections;
	}

    public ProtocolSettings getProtocolSettings(ConnectionParams cp) {
        return protocolSettingsMap.get(cp);
    }

    public UiSettingsData getUiSettingsData(ConnectionParams cp) {
        return uiSettingsDataMap.get(cp);
    }

    public void save() {
        try {
            cleanStorage();
            Preferences connectionsHistoryNode = getConnectionHistoryNode();
            int num = 0;
            for (ConnectionParams cp : connections) {
                if (num >= MAX_ITEMS) break;
                if ( ! Strings.isTrimmedEmpty(cp.hostName)) {
                        addNode(cp, connectionsHistoryNode, num++);
                }
            }
        } catch (AccessControlException ace) { /*nop*/ }
	}


    public void clear() {
        cleanStorage();
        init();
    }

    private void cleanStorage() {
        Preferences connectionsHistoryNode = getConnectionHistoryNode();
        try {
            for (String host : connectionsHistoryNode.childrenNames()) {
				connectionsHistoryNode.node(host).removeNode();
			}
		} catch (BackingStoreException e) {
            logger.severe("Cannot remove node: " + e.getMessage());
		}
    }

    private Preferences getConnectionHistoryNode() {
        Preferences root = Preferences.userRoot();
        return root.node(CONNECTIONS_HISTORY_ROOT_NODE);
    }

    private void addNode(ConnectionParams connectionParams, Preferences connectionsHistoryNode, int orderNum) {
        ProtocolSettings protocolSettings = protocolSettingsMap.get(connectionParams);
        UiSettingsData uiSettingsData = uiSettingsDataMap.get(connectionParams);
        final Preferences node = connectionsHistoryNode.node(String.valueOf(orderNum));
        serializeConnectionParams(node, connectionParams);
        serializeProtocolSettings(node, protocolSettings);
        serializeUiSettingsData(node, uiSettingsData);
        try {
			node.flush();
		} catch (BackingStoreException e) {
            logger.severe("Cannot retrieve connections history info: " + e.getMessage());
		}
	}

    private void serializeUiSettingsData(Preferences node, UiSettingsData uiSettingsData) {
        if (uiSettingsData != null) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(uiSettingsData);
                node.putByteArray(NODE_UI_SETTINGS, byteArrayOutputStream.toByteArray());
                logger.finest("serialized (" + node.name() + ") " + uiSettingsData);
            } catch (IOException e) {
                logger.severe("Cannot serialize UiSettings: " + e.getMessage());
            }
        }
    }

    private void serializeProtocolSettings(Preferences node, ProtocolSettings protocolSettings) {
        if (protocolSettings != null) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(protocolSettings);
                node.putByteArray(NODE_PROTOCOL_SETTINGS, byteArrayOutputStream.toByteArray());
                logger.finest("serialized (" + node.name() + ") " + protocolSettings);
            } catch (IOException e) {
                logger.severe("Cannot serialize ProtocolSettings: " + e.getMessage());
            }
        }
    }

    private void serializeConnectionParams(Preferences node, ConnectionParams connectionParams) {
        node.put(NODE_HOST_NAME, connectionParams.hostName);
        node.putInt(NODE_PORT_NUMBER, connectionParams.getPortNumber());
        if (connectionParams.useSsh()) {
            node.putBoolean(NODE_USE_SSH, connectionParams.useSsh());
            node.put(NODE_SSH_USER_NAME, connectionParams.sshUserName != null ? connectionParams.sshUserName: "");
		    node.put(NODE_SSH_HOST_NAME, connectionParams.sshHostName != null ? connectionParams.sshHostName: "");
		    node.putInt(NODE_SSH_PORT_NUMBER, connectionParams.getSshPortNumber());
        }
        logger.finest("serialized (" + node.name() + ") " + connectionParams.toPrint());
    }

    public void reorder(ConnectionParams connectionParams) {
        reorder(connectionParams, getProtocolSettings(connectionParams), getUiSettingsData(connectionParams));
    }

    public void reorder(ConnectionParams connectionParams, ProtocolSettings protocolSettings, UiSettings uiSettings) {
        reorder(connectionParams, protocolSettings, uiSettings != null? uiSettings.getData() : null);
    }

    private void reorder(ConnectionParams connectionParams, ProtocolSettings protocolSettings, UiSettingsData uiSettingsData) {
        while (connections.remove(connectionParams)) {/*empty - remove all occurrences (if any)*/}
		LinkedList<ConnectionParams> cpList = new LinkedList<>();
		cpList.addAll(connections);

		connections.clear();
		connections.add(new ConnectionParams(connectionParams));
		connections.addAll(cpList);
        storeSettings(connectionParams, protocolSettings, uiSettingsData);
    }

    private void storeSettings(ConnectionParams connectionParams, ProtocolSettings protocolSettings, UiSettingsData uiSettingsData) {
        if (protocolSettings != null) {
        ProtocolSettings savedSettings = protocolSettingsMap.get(connectionParams);
            if (savedSettings != null) {
                savedSettings.copyDataFrom(protocolSettings);
            } else {
                protocolSettingsMap.put(new ConnectionParams(connectionParams), new ProtocolSettings(protocolSettings));
            }
        }
        if (uiSettingsData != null) {
            uiSettingsDataMap.put(new ConnectionParams(connectionParams), new UiSettingsData(uiSettingsData));
        }
    }

    /**
     * Search most suitable connectionParams (cp) from history.
     * When history is empty, returns original parameter.
     * When original parameter is empty (null or hostName is empty) returns the very first cp from history.
     * Then subsequently compare cp from history with original for most fields will match in sequent of
     * hostName, portNumber, useSsh, sshHostName, sshPortName, sshPortNumber.
     * When any match found it returns.
     * When no matches found returns the very first cp from history.
     *
     * @param orig connectionParams to search
     * @return most suitable cp
     */
    public ConnectionParams getMostSuitableConnection(ConnectionParams orig) {
        ConnectionParams res = connections.isEmpty()? orig: connections.get(0);
        if (null == orig || Strings.isTrimmedEmpty(orig.hostName)) return res;
        for (ConnectionParams cp : connections) {
            if (orig.equals(cp)) return cp;
            if (compareTextFields(orig.hostName, res.hostName, cp.hostName)) {
                res = cp;
                continue;
            }
            if (orig.hostName.equals(cp.hostName) &&
                    comparePorts(orig.getPortNumber(), res.getPortNumber(), cp.getPortNumber())) {
                res = cp;
                continue;
            }
            if (orig.hostName.equals(cp.hostName) &&
                orig.getPortNumber() == cp.getPortNumber() &&
                    orig.useSsh() == cp.useSsh() && orig.useSsh() != res.useSsh()) {
                res = cp;
                continue;
            }
            if (orig.hostName.equals(cp.hostName) &&
                    orig.getPortNumber() == cp.getPortNumber() &&
                    orig.useSsh() && cp.useSsh() &&
                    compareTextFields(orig.sshHostName, res.sshHostName, cp.sshHostName)) {
                res = cp;
                continue;
            }
            if (orig.hostName.equals(cp.hostName) &&
                    orig.getPortNumber() == cp.getPortNumber() &&
                    orig.useSsh() && cp.useSsh() &&
                    orig.sshHostName != null && orig.sshHostName.equals(cp.hostName) &&
                    comparePorts(orig.getSshPortNumber(), res.getSshPortNumber(), cp.getSshPortNumber())) {
                res = cp;
                continue;
            }
            if (orig.hostName.equals(cp.hostName) &&
                    orig.getPortNumber() == cp.getPortNumber() &&
                    orig.useSsh() && cp.useSsh() &&
                    orig.sshHostName != null && orig.sshHostName.equals(cp.hostName) &&
                    orig.getSshPortNumber() == cp.getSshPortNumber() &&
                    compareTextFields(orig.sshUserName, res.sshUserName, cp.sshUserName)) {
                res = cp;
            }
        }
        return res;
    }

    private boolean comparePorts(int orig, int res, int test) {
        return orig == test && orig != res;
    }

    private boolean compareTextFields(String orig, String res, String test) {
        return (orig != null && test != null && res != null) &&
                orig.equals(test) && ! orig.equals(res);
    }

    public boolean isEmpty() {
        return connections.isEmpty();
    }
}
