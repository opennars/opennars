package hellblazer.gossip;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Performs broadcast and multicast peer detection. How well this works depends * on your network configuration * * @author ryanm
 * from: http://www.java-gaming.org/index.php?topic=21447.0
 */
public class PeerDiscovery {

/*
// constructs multicast-based peer
PeerDiscovery mpd = new PeerDiscovery( multicastGroupIP, port, packetTTL );

// constructs broadcast-based peer
PeerDiscovery bpd = new PeerDiscovery( groupIP, port);

// queries the group, and waits for responses
InetAddress[] peers = peer.getPeers( timeout );

// when you're done...
peer.disconnect();
*/

    private static final byte QUERY_PACKET = 80;
    private static final byte RESPONSE_PACKET = 81;
    /**
     * The group address. Determines the set of peers that are able to discover * each other
     */
    public final InetAddress group;
    /**
     * The port number that we operate on
     */
    public final int port;
    private final MulticastSocket mcastSocket;
    private final DatagramSocket bcastSocket;
    private final InetSocketAddress broadcastAddress;
    private boolean shouldStop = false;
    private List<InetAddress> responseList = null;
    /**
     * Used to detect and ignore this peers response to it's own query. When we * send a response packet, we set this to the destination. When we receive a * response, if this matches the source, we know that we're talking to * ourselves and we can ignore the response.
     */
    private InetAddress lastResponseDestination = null;
    /**
     * Redefine this to be notified of exceptions on the listen thread. Default * behaviour is to print to stdout. Can be left as null for no-op
     */
    public ExceptionHandler rxExceptionHandler = new ExceptionHandler();

    private Thread mcastListen = new Thread(PeerDiscovery.class.getSimpleName() + " multicast listen thread") {
        @Override
        public void run() {
            try {
                byte[] buffy = new byte[1];
                DatagramPacket rx = new DatagramPacket(buffy, buffy.length);
                DatagramPacket tx = new DatagramPacket(new byte[]{RESPONSE_PACKET}, 1, group, port);
                while (!shouldStop) {
                    try {
                        mcastSocket.receive(rx);
                        if (buffy[0] == QUERY_PACKET) {
                            lastResponseDestination = rx.getAddress();
                            mcastSocket.send(tx);
                        } else if (buffy[0] == RESPONSE_PACKET) {
                            if (responseList != null && !rx.getAddress().equals(lastResponseDestination)) {
                                responseList.add(rx.getAddress());
                            }
                        }
                    } catch (SocketException soe) { // someone may have called disconnect()
                    }
                }
                mcastSocket.disconnect();
                mcastSocket.close();
            } catch (IOException e) {
                if (rxExceptionHandler != null) {
                    rxExceptionHandler.handle(e);
                }
            }
        }
    };
    private Thread bcastListen = new Thread(PeerDiscovery.class.getSimpleName() + " broadcast listen thread") {
        @Override
        public void run() {
            try {
                byte[] buffy = new byte[1 + group.getAddress().length];
                DatagramPacket rx = new DatagramPacket(buffy, buffy.length);
                byte[] groupAddr = group.getAddress();
                while (!shouldStop) {
                    try {
                        Arrays.fill(buffy, (byte) 0);
                        bcastSocket.receive(rx);
                        boolean groupMatch = rx.getLength() == groupAddr.length + 1;
                        for (int i = 0; i < groupAddr.length; i++) {
                            groupMatch &= groupAddr[i] == buffy[i + 1];
                        }
                        if (groupMatch) {
                            if (buffy[0] == QUERY_PACKET) {
                                byte[] data = new byte[1 + group.getAddress().length];
                                data[0] = RESPONSE_PACKET;
                                System.arraycopy(group.getAddress(), 0, data, 1, data.length - 1);
                                DatagramPacket tx = new DatagramPacket(data, data.length, rx.getAddress(), port);
                                lastResponseDestination = rx.getAddress();
                                bcastSocket.send(tx);
                            } else if (buffy[0] == RESPONSE_PACKET) {
                                if (responseList != null && !rx.getAddress().equals(lastResponseDestination)) {
                                    responseList.add(rx.getAddress());
                                }
                            }
                        }
                    } catch (SocketException stoe) { // someone may have called disconnect()

                    }
                }
                bcastSocket.disconnect();
                bcastSocket.close();
            } catch (Exception e) {
                if (rxExceptionHandler != null) {
                    rxExceptionHandler.handle(e);
                }
            }
        }

        ;
    };

    /**
     * Constructs a peer and connects it to a UDP multicast group * * @param group * a valid multicast address, i.e.: in the range 224.0.0.1 to * 239.255.255.255 inclusive * @param port * a valid port, i.e.: in the range 1025 to 65535 inclusive * @param ttl * The time-to-live for multicast packets. 0 = restricted to the * same host, 1 = Restricted to the same subnet, <32 = Restricted * to the same site, organisation or department, <64 = Restricted * to the same region, <128 = Restricted to the same continent, * <255 = unrestricted * @throws IOException
     */
    public PeerDiscovery(InetAddress group, int port, int ttl) throws IOException { /* * on systems with both IPv4 and IPv6 stacks, * MulticastSocket.setTimeToLive() does not work. This fixes things */
        Properties props = System.getProperties();
        props.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperties(props);
        this.group = group;
        this.port = port;
        mcastSocket = new MulticastSocket(port);
        mcastSocket.joinGroup(group);
        // confusingly, this *disables* loopback. it's only a hint though,
        // so we still need to look out for loopback manually
        mcastSocket.setLoopbackMode(true);
        mcastSocket.setTimeToLive(ttl);
        mcastListen.setDaemon(true);
        mcastListen.start();
        bcastSocket = null;
        bcastListen = null;
        broadcastAddress = null;
    }

    /**
     * Constructs a UDP broadcast-based peer * * @param group * The identifier shared by the peers that will be discovered. * @param port * a valid port, i.e.: in the range 1025 to 65535 inclusive * @throws IOException
     */
    public PeerDiscovery(InetAddress group, int port) throws IOException {
        this.group = group;
        this.port = port;
        mcastSocket = null;
        mcastListen = null;
        bcastSocket = new DatagramSocket(port);
        broadcastAddress = new InetSocketAddress("255.255.255.255", port);
        bcastListen.setDaemon(true);
        bcastListen.start();
    }

    /**
     * Signals this {@link PeerDiscovery} to shut down. This call will block * until everything's timed out and closed etc.
     */
    public void disconnect() {
        shouldStop = true;
        DatagramSocket sock = mcastSocket != null ? mcastSocket : bcastSocket;
        sock.close();
        sock.disconnect();
        Thread listen = mcastListen != null ? mcastListen : bcastListen;
        try {
            listen.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Queries the network and finds the addresses of other peers in the same * group * * @param timeout * How long to wait for responses, in milliseconds. Call will block * for this long, although you can {@link Thread#interrupt()} to * cut the wait short * @return The addresses of other peers in the group * @throws IOException * If something goes wrong when sending the query packet
     */
    public InetAddress[] getPeers(int timeout) throws IOException {
        responseList = new ArrayList();
        if (mcastSocket != null) {
            // send query byte
            DatagramPacket tx = new DatagramPacket(new byte[]{QUERY_PACKET}, 1, group, port);
            mcastSocket.send(tx);
        } else {
            // send query byte, appended with the group address
            byte[] data = new byte[1 + group.getAddress().length];
            data[0] = QUERY_PACKET;
            System.arraycopy(group.getAddress(), 0, data, 1, data.length - 1);
            DatagramPacket tx = new DatagramPacket(data, data.length, broadcastAddress);
            bcastSocket.send(tx);
        }

        // wait for the listen thread to do its thing
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
        }
        InetAddress[] peers = responseList.toArray(new InetAddress[responseList.size()]);
        responseList = null;
        return peers;
    }

    /**
     * Handles an exception. * * @author ryanm
     */
    public class ExceptionHandler {
        /**
         * Called whenever an exception is thrown from the listen thread. The * listen thread should now be dead * * @param e
         */
        public void handle(Exception e) {
            e.printStackTrace();
        }
    }
}