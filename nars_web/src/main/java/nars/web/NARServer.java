package nars.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.extensions.PerMessageDeflateHandshake;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import nars.NAR;
import nars.io.out.TextOutput;
import nars.nar.experimental.DefaultAlann;
import nars.util.language.JSON;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static io.undertow.Handlers.resource;
import static io.undertow.Handlers.websocket;


public class NARServer extends PathHandler {



    final NAR nar;

    private final Undertow server;

    long updatePeriodMS = 100;
    private Thread narThread;

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

    public class WebSocketCore extends AbstractReceiveListener implements WebSocketCallback<Void>, WebSocketConnectionCallback {

        //public static final Logger log = LoggerFactory.getLogger(WebSocketCore.class);
        private TextOutput textOutput;


        public WebSocketCore() {
            super();
        }


        public HttpHandler get() {
            return websocket(this).addExtension(new PerMessageDeflateHandshake());
        }


        @Override
        public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel socket) {

            /*if (log.isInfoEnabled())
                log.info(socket.getPeerAddress() + " connected websocket");*/

            socket.getReceiveSetter().set(this);
            socket.resumeReceives();


            textOutput = new TextOutput(nar) {

                @Override
                protected boolean output(final Channel channel, final Class event, final Object... args) {

                    final String prefix = channel.getLinePrefix(event, args);
                    final CharSequence s = channel.get(event, args);

                    if (s != null) {
                        return output(prefix, s);
                    }


                    return false;
                }

                @Override
                protected boolean output(String prefix, CharSequence s) {
                    send(socket, prefix + ": " + s);
                    return true;
                }
            };

        }

        @Override
        protected void onClose(WebSocketChannel socket, StreamSourceFrameChannel channel) throws IOException {

            if (textOutput!=null) {
                textOutput.stop();
                textOutput=null;
            }

            /*if (log.isInfoEnabled())
                log.info(socket.getPeerAddress() + " disconnected websocket");*/
        }


        @Override
        protected void onFullTextMessage(WebSocketChannel socket, BufferedTextMessage message) throws IOException {

            nar.input(message.getData());

//            if (attemptJSONParseOfText) {
//                try {
//                    System.out.println(socket + " recv txt: " + message.getData());
//                    //JsonNode j = Core.json.readValue(message.getData(), JsonNode.class);
//                    //onJSONMessage(socket, j);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }


        @Override
        protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {

            //System.out.println(channel + " recv bin: " + message.getData());
        }



        public void send(WebSocketChannel socket, Object object) {
            try {

                ByteBuffer data = ByteBuffer.wrap(JSON.omDeep.writeValueAsBytes(object));

                WebSockets.sendText(data, socket, this);


            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
            }
        }

//    @Override
//    public void complete(WebSocketChannel wsc, Void t) {
//        //System.out.println("Sent: " + wsc);
//    }

        @Override
        public void onError(WebSocketChannel wsc, Void t, Throwable thrwbl) {
            //System.out.println("Error: " + thrwbl);
        }

        @Override
        public void complete(WebSocketChannel channel, Void context) {
            //log.info("Complete: " + channel);
        }
    }

    public NARServer(NAR nar, int httpPort) throws IOException {
        super();

        this.nar = nar;

        //websockets = new NARSWebSocketServer(new InetSocketAddress(webSocketsPort));
        //websockets.start();
        String clientPath = "./nars_web/src/main/web";
        File c = new File(clientPath);
        System.out.println(c.getAbsolutePath());

        //https://github.com/undertow-io/undertow/blob/master/examples/src/main/java/io/undertow/examples/sessionhandling/SessionServer.java
        addPrefixPath("/", resource(
                new FileResourceManager(c, 100)).
                setDirectoryListingEnabled(false));
        addPrefixPath("/ws", new WebSocketCore().get());


        server = Undertow.builder()
                .addHttpListener(httpPort, "localhost")
                .setIoThreads(8)
                .setHandler(this)
                .build();

        narThread = new Thread(() -> {
            nar.loop(updatePeriodMS);
        });
    }


    public synchronized void start() {

        if (!nar.isRunning()) {
            System.out.println("starting");
            narThread.start();
            //TextOutput.out(nar).setShowInput(false);
            server.start();
        }

    }

    public void stop() {
        server.stop();
        nar.stop();
        try {
            narThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {


        DefaultAlann nar = new DefaultAlann(32);
        //d.memory.setClock(new RealtimeMSClock(false));





        int httpPort;
        
        if (args.length < 1) {
            //System.out.println("Usage: NARServer <httpPort>");
            httpPort = 8080;
        }
        else {
            httpPort = Integer.parseInt(args[0]);


        }
                
        NARServer s = new NARServer(nar, httpPort);
        
        System.out.println("NARS Web Server ready. port: " + httpPort);
        /*if (nlp!=null) {
            System.out.println("  NLP enabled, using: " + nlpHost + ":" + nlpPort);            
        }*/

        s.start();
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
