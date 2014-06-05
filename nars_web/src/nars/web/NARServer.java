package nars.web;

import java.io.File;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import nars.main_nogui.NAR;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class NARServer  {

    static final boolean WEBSOCKET_DEBUG = false;
    
    class NARSWebSocketServer extends WebSocketServer  {

        public NARSWebSocketServer(InetSocketAddress addr) throws UnknownHostException {
            super(addr);
        }
     
        @Override
        public void onOpen(final WebSocket conn, ClientHandshake handshake) {
            //this.sendToAll("new connection: " + handshake.getResourceDescriptor());

            WebSocketImpl.DEBUG = WebSocket.DEBUG = WEBSOCKET_DEBUG;

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


        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
            if (conn != null) {
                // some errors like port binding failed may not be assignable to a specific websocket
            }
        }
        
    }
    
    final NARSWebSocketServer websockets;
    private final Map<WebSocket, NARConnection> socketSession = new HashMap();

    public NARServer(int httpPort, int webSocketsPort) throws UnknownHostException, IOException {
        websockets = new NARSWebSocketServer(new InetSocketAddress(webSocketsPort));
        websockets.start();
        
        new HTTPServer(httpPort, new File("nars_web/client"));
        
    }



    public static void main(String[] args) throws InterruptedException, IOException {
                
        int httpPort = 9999;
        int wsPort = 10000;
        /*try {
            port = Integer.parseInt(args[ 0]);
        } catch (Exception ex) {        }*/
        
                
     
        NARServer s = new NARServer(httpPort, wsPort);
        
        System.out.println("NARS Web Server ready on port: " + httpPort);
        //System.out.println("Open " + new File("nars_web/client/index.html").getAbsolutePath() + " in a web browser.");    
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
