package hellblazer.gossip;

import hellblazer.gossip.configuration.GossipConfiguration;
import nars.util.data.list.FasterList;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * https://github.com/ChiralBehaviors/Chinese-Whispers/wiki/Usage
 */
public class GossipPeer implements GossipListener {

    public final Gossip gossip;
    //private Object peers;


    public GossipPeer(int port) throws SocketException {
        super();

        GossipConfiguration config = new GossipConfiguration();
        config.seeds = new FasterList();
        config.endpoint = new InetSocketAddress("localhost", port);
        config.gossipInterval = 1;


        gossip = config.construct();

        gossip.setListener(this);

        gossip.start();


    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+ ":" + gossip.communications.getLocalAddress();
    }

    public Logger log() {
        return gossip.log;
    }

    public UUID put(byte[] replicatedState) {
        return gossip.put(replicatedState);
    }

    public UUID put(Serializable j) {
        return put(null, j);
    }



    public UUID put(UUID u, Serializable j) {

//        byte[] ba;
//        try {

        byte[] ba;
        try {
            ba = null; //TODO marshaller.objectToByteBuffer(j);
        } catch (Exception e) {
            log(e);
            return null;
        }

        //serialize data
            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //baos.write(ba);



//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }


//        try {
//            JsonNode treenode = Core.bson.valueToTree(j);
//            Core.bson.writeValue(baos, treenode);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
        //byte[] ba = baos.toByteArray();

        if (u == null)
            return put(ba);
        else {
            put(u, ba);
            return u;
        }
    }

    public void remove(UUID id) {
        gossip.remove(id);
    }

    public void put(UUID id, byte[] replicatedState) {
        gossip.put(id, replicatedState);
    }



    /** data removed */
    @Override public void onRemove(UUID id) {

    }

    /** data has appeared */
    @Override public void onPut(UUID id, byte[] state) {
        onUpdate(id, state);
    }

    /** data has changed */
    @Override public void onSet(UUID id, byte[] state) {
        onUpdate(id, state);
    }

    public void onUpdate(UUID id, byte[] state) {
        //ByteArrayInputStream bais = new ByteArrayInputStream(state);
        try {
            //Map<String,Object> x = Core.bson.readValue(bais, Map.class);
            //JsonNode x = Core.bson.readValue(bais, JsonNode.class);
            //bais.
            //JsonNode x = Core.bson.readTree(bais);

            Object x = null; //TODO marshaller.objectFromByteBuffer(state);
            onUpdate(id, x);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public void onUpdate(UUID id, Object j) {
        //System.out.println("onUpdate: " + id + " = " + j);
    }

    public void connect(InetSocketAddress seed) {
        gossip.view.connect(seed);
        gossip.connectAndGossipWith(seed);
    }


    public void stop() {
        gossip.stop();
    }

    public void connect(String host, int port) {
        connect(new InetSocketAddress(host, port));
    }

    public void log(Exception e) {
        log().severe(e.toString());
    }

    public Collection<Endpoint> getPeers() {
        return gossip.endpoints.values();
    }

    /**
     * Full
     *
     * cleanupCycles: 5
     commThreads: 10
     endpoint: localhost:0
     gossipInterval: 3
     gossipUnit: SECONDS
     heartbeatCycle: 2
     hmac: HmacMD5
     hmacKey: I0WDrSNGg60jRYOtI0WDrQ==
     quarantineDelay: 30000
     receiveBufferMultiplier: 6
     sendBufferMultiplier: 5
     unreachableDelay: 36000

     seeds:
     - localhost:666
     - localhost:668

     fdFactory: !adaptiveFailureDetectorFactory
     convictionThreshold: 0.9
     windowSize: 100
     scale: 0.8
     expectedSampleInterval: 20000
     initialSamples: 10
     minimumInterval: 3000
     */

    /*
    Minimal
    # an example of a minimal gossip configuartion

    seeds:
    - localhost:6754
    - localhost:6543
     */
}
