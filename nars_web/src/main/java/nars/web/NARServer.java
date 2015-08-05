package nars.web;

import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;

import java.io.File;
import java.io.IOException;

import static io.undertow.Handlers.resource;


public class NARServer extends PathHandler {

    private static final int DEFAULT_WEBSOCKET_PORT = 10000;
    static final boolean WEBSOCKET_DEBUG = false;
    
    private static int cycleIntervalMS = 50;
    private final Undertow server;
    
//    class NARSWebSocketServer extends WebSocketServer  {
//
//        public NARSWebSocketServer(InetSocketAddress addr) throws UnknownHostException {
//            super(addr);
//        }
//     
//        @Override
//        public void onOpen(final WebSocket conn, ClientHandshake handshake) {
//            //this.sendToAll("new connection: " + handshake.getResourceDescriptor());
//
//            WebSocketImpl.DEBUG = WebSocket.DEBUG = WEBSOCKET_DEBUG;
//
//            if (WEBSOCKET_DEBUG) System.out.println("Connect: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
//
//            final NARConnection n = new NARConnection(new Default().build(), cycleIntervalMS) {
//                @Override public void println(String output) {
//                    conn.send(output);
//                }
//            };
//            socketSession.put(conn, n);        
//
//        }
//
//        @Override
//        public void onClose(WebSocket conn, int code, String rule, boolean remote) {
//            if (WEBSOCKET_DEBUG) System.out.println(conn + " disconnected");
//
//            NARConnection n = socketSession.get(conn);
//            if (n!=null) {
//                n.stop();
//                socketSession.remove(conn);
//            }
//        }
//
//        @Override
//        public void onMessage(WebSocket conn, String message) {
//
//            NARConnection n = socketSession.get(conn);
//            if (n!=null) {
//                n.read(message);
//            }
//        }
//
//
//        @Override
//        public void onError(WebSocket conn, Exception ex) {
//            ex.printStackTrace();
//            if (conn != null) {
//                // some errors like port binding failed may not be assignable to a specific websocket
//            }
//        }
//        
//    }
    
    //final NARSWebSocketServer websockets;
    //private final Map<WebSocket, NARConnection> socketSession = new HashMap();

    public NARServer(int httpPort) throws IOException {
        super();
        
        //websockets = new NARSWebSocketServer(new InetSocketAddress(webSocketsPort));
        //websockets.start();
        String clientPath = null;//TODO
        
        server = Undertow.builder()
                .addHttpListener(httpPort, "localhost")
                .setIoThreads(8)
                .setHandler(this)
                .build();

        //https://github.com/undertow-io/undertow/blob/master/examples/src/main/java/io/undertow/examples/sessionhandling/SessionServer.java
        addPrefixPath("/", resource(
                new FileResourceManager(new File(clientPath), 100)).
                setDirectoryListingEnabled(false));

        
    }



    public static void main(String[] args) throws Exception {
                
        int httpPort;
        
        String nlpHost = null;
        int nlpPort = 0;
        
        if (args.length < 1) {
            System.out.println("Usage: NARServer <httpPort> [nlpHost nlpPort] [cycleIntervalMS]");
            
            return;
        }
        else {
            httpPort = Integer.parseInt(args[0]);
            
            if (args.length >= 3) {
                nlpHost = args[1];
                if (!"null".equals(args[2])) {
                    nlpPort = Integer.parseInt(args[2]);
                    //nlp = new NLPInputParser(nlpHost, nlpPort);
                }
            }
            if (args.length >= 4) {
                cycleIntervalMS = Integer.parseInt(args[3]);
            }
        }
                
        NARServer s = new NARServer(httpPort);
        
        System.out.println("NARS Web Server ready. port: " + httpPort);
        System.out.println("  Cycle interval (ms): " + cycleIntervalMS);
        /*if (nlp!=null) {
            System.out.println("  NLP enabled, using: " + nlpHost + ":" + nlpPort);            
        }*/

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
