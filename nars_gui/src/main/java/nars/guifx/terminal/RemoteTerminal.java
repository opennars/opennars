//package nars.guifx.terminal;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.PrintStream;
//
//import javax.annotation.PreDestroy;
//
//import org.jftclient.ssh.Connection;
//import org.springframework.stereotype.Component;
//
//import com.jcraft.jsch.ChannelShell;
//import com.jcraft.jsch.JSch;
//import com.jcraft.jsch.JSchException;
//import com.jcraft.jsch.Session;
//
///**
// * @author sergei.malafeev
// */
//@Component
//public class RemoteTerminal {
//    private Session session;
//    private Thread thread;
//    private String key;
//
//    public void connect(Connection connection, TerminalPanel remoteTerminalPanel) throws JSchException, IOException {
//        String newKey = connection.getUser() + connection.getRemoteHost();
//        if (newKey.equals(key)) {
//            return;
//        }
//        key = newKey;
//
//        disconnect();
//
//        JSch jsch = new JSch();
//        session = jsch.getSession(connection.getUser(), connection.getRemoteHost(), 22);
//        session.setPassword(connection.getPassword());
//        session.setConfig("StrictHostKeyChecking", "no");
//        session.connect(5000);
//
//        ChannelShell channel = (ChannelShell) session.openChannel("shell");
//        channel.setPtyType("vt102");
//
//        OutputStream inputToChannel = channel.getOutputStream();
//        PrintStream printStream = new PrintStream(inputToChannel, true);
//        remoteTerminalPanel.setPrintStream(printStream);
//
//        InputStream outFromChannel = channel.getInputStream();
//
//        Runnable run = new TerminalWatcher(outFromChannel, remoteTerminalPanel.getTextArea());
//        thread = new Thread(run);
//        thread.start();
//
//        channel.connect();
//    }
//
//    @PreDestroy
//    public void disconnect() {
//        if (session != null) {
//            session.disconnect();
//        }
//        if (thread != null) {
//            thread.interrupt();
//        }
//    }
// }
