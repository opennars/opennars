/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import nars.io.ExperienceReader;
import nars.io.ExperienceWriter;
import nars.main_nogui.NAR;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class NARServer extends WebSocketServer {

    NAR reasoner;
    private boolean logging;
    private PrintStream out = System.out;

    public NARServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public NARServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        this.sendToAll("new connection: " + handshake.getResourceDescriptor());
        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        this.sendToAll(conn + " has left the room!");
        System.out.println(conn + " has left the room!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        this.sendToAll(message);
        System.out.println(conn + ": " + message);
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        WebSocketImpl.DEBUG = true;
        int port = 8887; // 843 flash policy port
        try {
            port = Integer.parseInt(args[ 0]);
        } catch (Exception ex) {
        }
        NARServer s = new NARServer(port);
        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String in = sysin.readLine();
            s.sendToAll(in);
            if (in.equals("exit")) {
                s.stop();
                break;
            } else if (in.equals("restart")) {
                s.stop();
                s.start();
                break;
            }
        }
    
        /*
        NARRun nars = new NARRun();
        CommandLineParameters.decode(args, nars.getReasoner());
        nars.runInference(args);
        init();
        */    
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
     * @throws InterruptedException When socket related I/O errors occur.
     */
    public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }


    public void runInference(String args[]) {
        init(args);
        run();
    }

    public void init(String[] args) {
        if (args.length > 0) {
            ExperienceReader experienceReader = new ExperienceReader(reasoner);
            experienceReader.openLoadFile(args[0]);
        }
        reasoner.addOutputChannel(new ExperienceWriter(reasoner,
                new PrintWriter(out, true)));
    }

    public void runInference(BufferedReader r, BufferedWriter w) {
        init(r, w);
        run();
    }

    private void init(BufferedReader r, BufferedWriter w) {
        ExperienceReader experienceReader = new ExperienceReader(reasoner);
        experienceReader.setBufferedReader(r);
        reasoner.addOutputChannel(new ExperienceWriter(reasoner,
                new PrintWriter(w, true)));
    }

    public final void init() {
        reasoner = new NAR();
    }

    public void _run() {
        while (true) {
            log("NARSBatch.run():"
                    + " step " + reasoner.getTime()
                    + " " + reasoner.isFinishedInputs());
            reasoner.tick();
            log("NARSBatch.run(): after tick"
                    + " step " + reasoner.getTime()
                    + " " + reasoner.isFinishedInputs());
            if (reasoner.isFinishedInputs()
                    || reasoner.getTime() == 1000) {
                break;
            }
        }
    }

    public void setPrintStream(PrintStream out) {
        this.out = out;
    }

    private void log(String mess) {
        if (logging) {
            System.out.println("/ " + mess);
        }
    }

    public NAR getReasoner() {
        return reasoner;
    }

}
