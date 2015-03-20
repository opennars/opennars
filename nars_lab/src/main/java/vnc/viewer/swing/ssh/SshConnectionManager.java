//// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
//// All rights reserved.
////
////-------------------------------------------------------------------------
//// This file is part of the TightVNC software.  Please visit our Web site:
////
////                       http://www.tightvnc.com/
////
//// This program is free software; you can redistribute it and/or modify
//// it under the terms of the GNU General Public License as published by
//// the Free Software Foundation; either version 2 of the License, or
//// (at your option) any later version.
////
//// This program is distributed in the hope that it will be useful,
//// but WITHOUT ANY WARRANTY; without even the implied warranty of
//// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//// GNU General Public License for more details.
////
//// You should have received a copy of the GNU General Public License along
//// with this program; if not, write to the Free Software Foundation, Inc.,
//// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
////-------------------------------------------------------------------------
////
//
//package vnc.viewer.swing.ssh;
//
//import vnc.utils.Strings;
//import vnc.viewer.CancelConnectionException;
//import vnc.viewer.swing.ConnectionParams;
//import vnc.viewer.swing.Utils;
//import com.jcraft.jsch.*;
//
//import javax.swing.*;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.reflect.InvocationTargetException;
//import java.util.logging.Logger;
//import java.util.prefs.Preferences;
//
//public class SshConnectionManager implements SshKnownHostsManager {
//
//    public static final String SSH_NODE = "com/glavsoft/viewer/ssh";
//    public static final String KNOWN_HOSTS = "known_hosts";
//    private Session session;
//    private String errorMessage = "";
//	private final JFrame parentWindow;
//    private JSch jsch;
//    private Logger logger;
//
//    public SshConnectionManager(JFrame parentWindow) {
//        this.parentWindow = parentWindow;
//        logger = Logger.getLogger(getClass().getName());
//	}
//
//	public int connect(ConnectionParams connectionParams) throws CancelConnectionException {
//        if (Strings.isTrimmedEmpty(connectionParams.sshUserName)) {
//            connectionParams.sshUserName = getInteractivelySshUserName();
//        }
//
//        if (session != null && session.isConnected()) {
//			session.disconnect();
//		}
//        jsch = new JSch();
//
//        if (connectionParams == null){
//            System.out.println("######################## ConnectionParms was NULL #####################");
//        }
//
//        String rawKey = connectionParams.sshPrivateKey;
//
//        if (rawKey == null){
//            System.out.println("######################## THE RAW KEY WAS NULL #####################");
//        }
//        if (connectionParams.sshKeyReplaceSymbol == null){
//            System.out.println("!!!!!!!!!! Replace symbol was null, using default");
//            connectionParams.sshKeyReplaceSymbol = "<>";
//        }
//        String key = rawKey.replace(connectionParams.sshKeyReplaceSymbol, "\n");
//        try {
//            if (connectionParams.sshHostKey != null && !connectionParams.sshHostKey.isEmpty()){
//                String fullHostKey = connectionParams.sshHostName + " " + connectionParams.sshHostKey;
//                byte[] publicKeyBytes = fullHostKey.getBytes();
//                ByteArrayInputStream knownHostBytes = new ByteArrayInputStream(publicKeyBytes);
//                jsch.setKnownHosts(knownHostBytes);
//                logger.info("Added known host passed in through parameters\n" + fullHostKey);
//            } else {
//                logger.info("No known host passed in, reverting to default");
//                jsch.setKnownHosts(getKnownHostsStream());
//            }
//
//            if (connectionParams.useSshKeyPair()){
//                logger.info("Added Public key to known hosts");
//                //jsch.addIdentity(connectionParams.sshPrivateKey);
//                jsch.addIdentity(
//                        "vnc_id",
//                        key.getBytes(),
//                        connectionParams.sshPublicKey.getBytes(),
//                        new byte[0]);
//            } else {
//                logger.info("No private or public key set!");
//            }
//        } catch (JSchException e) {
//            logger.severe("Cannot set JSCH known hosts: " + e.getMessage());
//        }
//        logger.info("Private Key:\n" + key);
//
//        logger.info("Public Key:\n" + connectionParams.sshPublicKey);
//        int port = 0;
//		try {
//			session = jsch.getSession(
//                    connectionParams.sshUserName, connectionParams.sshHostName, connectionParams.getSshPortNumber());
//			UserInfo ui = new SwingSshUserInfo(parentWindow);
//			session.setUserInfo(ui);
//			session.connect();
//            sync();
//			port = session.setPortForwardingL(0, connectionParams.hostName, connectionParams.getPortNumber());
//        } catch (JSchException e) {
//            session.disconnect();
//			errorMessage = e.getMessage();
//		}
//		return port;
//	}
//
//    private String getInteractivelySshUserName() throws CancelConnectionException {
//        class Pair {
//            int intRes;
//            String stringRes;
//        }
//        final Pair result = new Pair();
//        try {
//            SwingUtilities.invokeAndWait(new Runnable() {
//                @Override
//                public void run() {
//                    final JOptionPane pane = new JOptionPane("Please enter the user name for SSH connection:",
//                            JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
//                    pane.setWantsInput(true);
//                    final JDialog dialog = pane.createDialog(parentWindow, "SSH User Name");
//                    Utils.decorateDialog(dialog);
//                    dialog.setVisible(true);
//                    result.stringRes = pane.getInputValue() != null ? (String) pane.getInputValue() : "";
//                    result.intRes = pane.getValue() != null ? (Integer) pane.getValue() : JOptionPane.OK_CANCEL_OPTION;
//                    dialog.dispose();
//                }
//            });
//        } catch (InterruptedException e) {
//            Logger.getLogger(getClass().getName()).severe(e.getMessage());
//        } catch (InvocationTargetException e) {
//            Logger.getLogger(getClass().getName()).severe(e.getMessage());
//        }
//        if (result.intRes != JOptionPane.OK_OPTION) {
//            throw new CancelConnectionException("No Ssh User Name entered");
//        }
//        return result.stringRes;
//    }
//
//    public boolean isConnected() {
//		return session.isConnected();
//	}
//
//	public String getErrorMessage() {
//		return errorMessage;
//	}
//
//    private InputStream getKnownHostsStream() {
//        Preferences sshNode = Preferences.userRoot().node(SSH_NODE);
//        return new ByteArrayInputStream(sshNode.getByteArray(KNOWN_HOSTS, new byte[0]));
//    }
//
//    @Override
//    public void sync() {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        HostKeyRepository repository = jsch.getHostKeyRepository();
//        try {
//            HostKey[] hostKey = repository.getHostKey();
//            if (null == hostKey) return;
//            for (HostKey hk : hostKey) {
//                String host = hk.getHost();
//                    String type = hk.getType();
//                    if (type.equals("UNKNOWN")) {
//                        write(out, host);
//                        write(out, "\n");
//                        continue;
//                    }
//                    write(out, host);
//                    write(out, " ");
//                    write(out, type);
//                    write(out, " ");
//                    write(out, hk.getKey());
//                    write(out, "\n");
//            }
//        } catch (IOException e) {
//            Logger.getLogger(this.getClass().getName()).severe("Cannot sync JSCH known hosts: " + e.getMessage());
//        }
//        Preferences sshNode = Preferences.userRoot().node(SSH_NODE);
//        sshNode.putByteArray(KNOWN_HOSTS, out.toByteArray());
//    }
//
//    private void write(ByteArrayOutputStream out, String str) throws IOException {
//        try {
//            out.write(str.getBytes("UTF-8"));
//        } catch (java.io.UnsupportedEncodingException e) {
//            out.write(str.getBytes());
//        }
//    }
//
//}