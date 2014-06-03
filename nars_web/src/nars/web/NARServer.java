package nars.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.WeakHashMap;
import nars.main_nogui.NAR;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class NARServer extends WebSocketServer {

    private boolean logging;
    private PrintStream out = System.out;
    private Map<WebSocket, NARConnection> socketSession = new WeakHashMap();

    public NARServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public NARServer(InetSocketAddress address) {
        super(address);
    }
    
    @Override
    public void onOpen(final WebSocket conn, ClientHandshake handshake) {
        //this.sendToAll("new connection: " + handshake.getResourceDescriptor());
        
        System.out.println("Connect: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());

        final NARConnection n = new NARConnection(new NAR()) {
            @Override public void println(String output) {
                conn.send(output);
            }
        };
        socketSession.put(conn, n);        
        
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println(conn + " disconnected");
               
        NARConnection n = socketSession.get(conn);
        if (n!=null) {
            n.stop();
            socketSession.remove(conn);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        
        NARConnection n = socketSession.get(conn);
        if (n!=null) {
            n.read(message);
        }
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
        System.out.println("NARS WebSockets ready on port: " + s.getPort());
        System.out.println("Open " + new File("nars_web/client/index.html").getAbsolutePath() + " in a web browser.");

    
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
    /*public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }*/



}
