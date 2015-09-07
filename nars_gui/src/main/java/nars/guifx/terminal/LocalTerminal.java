package nars.guifx.terminal;


import javax.annotation.PreDestroy;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sergei.malafeev
 */
public class LocalTerminal extends TerminalPanel {
    private Thread thread;

    private Process pty;

    public LocalTerminal() throws IOException {
        super();

        connect();
    }

    public void connect() throws IOException {

        if (pty != null && pty.isAlive()) {
            return;
        }

        String cmd = "/bin/bash -i";
        Map<String, String> envs = new HashMap<>(System.getenv());
        envs.remove("TERM_PROGRAM"); // for OS X
        envs.put("TERM", "vt102");
        //envs.put("TERM", "console");


        //pty = PtyProcess.exec(cmd, envs, System.getProperty("user.home"));
        pty = Runtime.getRuntime().exec(cmd); //, new File(System.getProperty("user.home")));

        OutputStream os = pty.getOutputStream();
        InputStream is = pty.getInputStream();

        PrintStream printStream = new PrintStream(os, true);
        setPrintStream(printStream);

        Runnable run = new TerminalWatcher(is, getTextArea());
        thread = new Thread(run);
        thread.start();
    }

    @PreDestroy
    public void disconnect() {
        if (pty != null) {
            pty.destroy();
        }
        if (thread != null) {
            thread.interrupt();
        }
    }


}
