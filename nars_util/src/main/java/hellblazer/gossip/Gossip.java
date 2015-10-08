/** (C) Copyright 2010 Hal Hildebrand, All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package hellblazer.gossip;


import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.gs.collections.impl.map.mutable.ConcurrentHashMapUnsafe;
import com.hellblazer.utils.fd.FailureDetectorFactory;
import nars.util.data.list.FasterList;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * The embodiment of the gossip protocol. This protocol replicates state and
 * forms both a member discovery and failure detection service. Periodically,
 * the protocol chooses a random member from the system view and initiates a
 * round of gossip with it. A round of gossip is push/pull and involves 3
 * messages.
 * 
 * For example, if node A wants to initiate a round of gossip with node B it
 * starts off by sending node B a gossip message containing a digest of the view
 * number state of the local view of the replicated state. Node B on receipt of
 * this message sends node A a reply containing a list of digests representing
 * the updated state required, based on the received digests. In addition, the
 * node also sends along a list of updated state that is more recent, based on
 * the initial list of digests. On receipt of this message node A sends node B
 * the requested state that completes a round of gossip.
 * 
 * When messages are received, the protocol updates the endpoint's failure
 * detector with the liveness information. If the endpoint's failure detector
 * predicts that the endpoint has failed, the endpoint is marked dead and its
 * replicated state is abandoned.
 * 
 * To ensure liveness, a special heartbeat state is maintained. This special
 * state is not part of the notification regime and is updated periodically by
 * this host.
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class Gossip {
    public static final UUID                                 ALL_STATES              = new UUID(
                                                                                                0L,
                                                                                                0L);
    public static final UUID                                 HEARTBEAT               = new UUID(
                                                                                                0L,
                                                                                                1L);
    public static final int                                  DEFAULT_CLEANUP_CYCLES  = 4;
    public static final int                                  DEFAULT_HEARTBEAT_CYCLE = 1;
    public static final int                                  DEFAULT_REDUNDANCY      = 3;
    public final static Logger log                     = Logger.getLogger(Gossip.class.toString());
    private static final byte[]                              EMPTY_STATE             = new byte[0];

    private final int                                        cleanupCycles;
    private final GossipCommunications communications;
    private final Executor                                   dispatcher;
    private final ConcurrentMap<InetSocketAddress, Endpoint> endpoints               = new ConcurrentHashMap<>();
    private final Random                                     entropy;
    private final FailureDetectorFactory                     fdFactory;
    private ScheduledFuture<?>                               gossipTask;
    private final int                                        heartbeatCycle;
    private int                                              heartbeatCounter        = 0;
    private int                                        interval;
    private TimeUnit                                   intervalUnit;
    private final AtomicReference<GossipListener>            listener                = new AtomicReference<>();

    private final Map<UUID, ReplicatedState>                 localState
            = new ConcurrentHashMapUnsafe<>(4);

    private final int                                        redundancy;
    private final Ring                                       ring;
    private final AtomicBoolean                              running                 = new AtomicBoolean();
    private final ScheduledExecutorService                   scheduler;
    public final SystemView view;

    /**
     * 
     * @param systemView
     *            - the system management view of the member state
     * @param failureDetectorFactory
     *            - the factory producing instances of the failure detector
     * @param random
     *            - a source of entropy
     * @param gossipInterval
     *            - the period of the random gossiping
     * @param unit
     *            - time unit for the gossip interval
     */
    public Gossip(GossipCommunications communicationsService,
                  SystemView systemView,
                  FailureDetectorFactory failureDetectorFactory, Random random,
                  int gossipInterval, TimeUnit unit) {
        this(communicationsService,
             systemView, failureDetectorFactory, random, gossipInterval, unit,
             DEFAULT_CLEANUP_CYCLES, DEFAULT_HEARTBEAT_CYCLE,
             DEFAULT_REDUNDANCY);
    }

    /**
     * 
     * @param systemView
     *            - the system management view of the member state
     * @param failureDetectorFactory
     *            - the factory producing instances of the failure detector
     * @param random
     *            - a source of entropy
     * @param gossipInterval
     *            - the period of the random gossiping
     * @param unit
     *            - time unit for the gossip interval
     * @param cleanupCycles
     *            - the number of gossip cycles required to convict a failing
     *            endpoint
     */
    public Gossip(GossipCommunications communicationsService,
                  SystemView systemView,
                  FailureDetectorFactory failureDetectorFactory, Random random,
                  int gossipInterval, TimeUnit unit, int cleanupCycles) {
        this(communicationsService,
             systemView, failureDetectorFactory, random, gossipInterval, unit,
             DEFAULT_CLEANUP_CYCLES, DEFAULT_HEARTBEAT_CYCLE,
             DEFAULT_REDUNDANCY);
    }

    /**
     * 
     * @param idGenerator
     *            - the UUID generator for state ids on this node
     * @param systemView
     *            - the system management view of the member state
     * @param failureDetectorFactory
     *            - the factory producing instances of the failure detector
     * @param random
     *            - a source of entropy
     * @param gossipInterval
     *            - the period of the random gossiping
     * @param unit
     *            - time unit for the gossip interval
     */


    /**
     * 
//     * @param idGenerator
//     *            - the UUID generator for state ids on this node
     * @param systemView
     *            - the system management view of the member state
     * @param failureDetectorFactory
     *            - the factory producing instances of the failure detector
     * @param random
     *            - a source of entropy
     * @param gossipInterval
     *            - the period of the random gossiping
     * @param unit
     *            - time unit for the gossip interval
     * @param cleanupCycles
     *            - the number of gossip cycles required to convict a failing
     *            endpoint
     * @param heartbeatCycle
     *            = the number of gossip cycles per heartbeat
     * @param redundancy
     *            - the number of members to contact each gossip cycle
     */
    public Gossip(
                  GossipCommunications communicationsService,
                  SystemView systemView,
                  FailureDetectorFactory failureDetectorFactory, Random random,
                  int gossipInterval, TimeUnit unit, int cleanupCycles,
                  int heartbeatCycle, int redundancy) {
        communications = communicationsService;
        communications.setGossip(this);
        entropy = random;
        view = systemView;
        interval = gossipInterval;
        intervalUnit = unit;
        fdFactory = failureDetectorFactory;
        ring = new Ring(me(), communications);
        this.cleanupCycles = cleanupCycles;
        this.heartbeatCycle = heartbeatCycle;
        this.redundancy = redundancy;
        scheduler = Executors.newSingleThreadScheduledExecutor(new SchedulerThreadFactory());
        dispatcher = Executors.newSingleThreadExecutor(new DispatcherThreadFactory());
    }

    /**
     * Deregister the replicated state of this node identified by the id
     * 
     * @param id
     */
    public void remove(UUID id) {
        if (id == null) {
            throw new NullPointerException(
                                           "replicated state id must not be null");
        }
        ReplicatedState state = new ReplicatedState(id,
                                                    System.currentTimeMillis(),
                                                    EMPTY_STATE);
        //synchronized (localState) {
            localState.put(id, state);
        //}
        /*if (log.isDebugEnabled()) {
            log.debug(String.format("Member: %s abandoning replicated state",
                                    me()));
        }*/
        ring.send(new Update(me(), state));
    }

    /** current local address */
    public InetSocketAddress me() {
        return communications.getLocalAddress();
    }

    /**
     * @return
     */
    public int getMaxStateSize() {
        return communications.getMaxStateSize();
    }

    /**
     * Add an identified piece of replicated state to this node
     * 
     * @param replicatedState
     * @return the unique identifier for this state
     */
    public UUID put(byte[] replicatedState) {
        if (replicatedState == null) {
            throw new NullPointerException("replicated state must not be null");
        }
        if (replicatedState.length > communications.getMaxStateSize()) {
            throw new IllegalArgumentException(
                                               String.format("State size %s must not be > %s",
                                                             replicatedState.length,
                                                             communications.getMaxStateSize()));
        }
        UUID id = UUID.randomUUID(); //idGenerator.generate();
        ReplicatedState state = new ReplicatedState(id,
                                                    System.currentTimeMillis(),
                                                    replicatedState);
        //synchronized (localState) {
            localState.put(id, state);
        //}
        /*if (log.isDebugEnabled()) {
            log.debug(String.format("Member: %s registering replicated state",
                                    me()));
        }*/
        ring.send(new Update(me(), state));
        return id;
    }

    public void setListener(GossipListener gossipListener) {
        listener.set(gossipListener);
    }

    /**
     * Start the gossip replication process
     */
    public Gossip start() {
        if (running.compareAndSet(false, true)) {
            communications.start();
            reschedule();
            listener.get().onStart();
        }
        else {
            throw new RuntimeException("already stopped");
        }
        return this;
    }

    void reschedule() {
        gossipTask = scheduler.scheduleWithFixedDelay(gossipTask(),
                                                      interval, interval,
                                                      intervalUnit);
    }

    public synchronized void setInterval(int num, TimeUnit unit) {
        boolean wasRunning = running.get();
        if (wasRunning)
            unschedule();
        this.interval = num;
        this.intervalUnit = unit;
        if (wasRunning)
            reschedule();
    }

    public void unschedule() {
        gossipTask.cancel(false);
        gossipTask = null;
    }

    public void waitFor(long timeoutMS) throws InterruptedException {
        scheduler.awaitTermination(timeoutMS, TimeUnit.MILLISECONDS);
    }



    /**
     * Terminate the gossip replication process
     */
    public Gossip stop() {
        if (running.compareAndSet(true, false)) {
            listener.get().onStop();
            communications.terminate();
            unschedule();
            if (gossipTask!=null) {
                gossipTask.cancel(true);
                gossipTask = null;
            }
        }
        else {
            throw new RuntimeException("already stopped");
        }
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("Gossip [%s]", me());
    }

    /**
     * Update the local state
     * 
     * @param id
     * @param replicatedState
     */
    public void put(UUID id, byte[] replicatedState) {
        if (id == null) {
            throw new NullPointerException(
                                           "replicated state id must not be null");
        }
        if (replicatedState == null) {
            throw new NullPointerException("replicated state must not be null");
        }
        if (replicatedState.length > communications.getMaxStateSize()) {
            throw new IllegalArgumentException(
                                               String.format("State size %s must not be > %s",
                                                             replicatedState.length,
                                                             communications.getMaxStateSize()));
        }
        ReplicatedState state = new ReplicatedState(id,
                                                    System.currentTimeMillis(),
                                                    replicatedState);
        //synchronized (localState) {
            localState.put(id, state);
        //}
//        if (log.isDebugEnabled()) {
//            log.debug(String.format("Member: %s updating replicated state",
//                                    me()));
//        }
        updateRing();
        ring.send(new Update(me(), state));
    }

    /**
     * Add the local state indicated by the digetst to the list of deltaState
     * 
     * @param deltaState
     * @param digest
     */
    private void addUpdatedLocalState(List<Update> deltaState, Digest digest) {
        UUID id = digest.getId();
        if (ALL_STATES.equals(id)) {
            //synchronized (localState) {
                deltaState.addAll(localState.values().stream().map(s -> new Update(digest.getAddress(), s)).collect(Collectors.toList()));
            //}
        } else {
            ReplicatedState myState;
            //synchronized (localState) {
                myState = localState.get(id);
            //}
            if (myState != null && myState.getTime() > digest.getTime()) {
                deltaState.add(new Update(digest.getAddress(), myState));
            } else if (myState == null) {
//                log.trace(format("Looking for deleteded local state %s on %s",
//                                        digest, me()));
            }
        }
    }

    /**
     * Update the heartbeat state for this node, sending it across the ring
     */
    private void updateHeartbeat() {
        if (heartbeatCounter++ % heartbeatCycle != 0) {
            return;
        }
        /*if (log.isTraceEnabled()) {
            log.trace(String.format("%s updating heartbeat state",
                                    me()));
        }*/
        ReplicatedState heartbeat = new ReplicatedState(
                HEARTBEAT,
                System.currentTimeMillis(),
                EMPTY_STATE);
        //synchronized (localState) {
            localState.put(HEARTBEAT, heartbeat);
        //}
        ring.send(new Update(me(), heartbeat));
    }

    /**
     * Add the replicated state we maintain for an endpoint indicated by the
     * digest to the list of deltaState
     * 
     * @param endpoint
     * @param deltaState
     * @param digest
     */
    protected static void addUpdatedState(Endpoint endpoint, List<Update> deltaState,
                                          Digest digest) {
        ReplicatedState localCopy = endpoint.getState(digest.getId());
        if (localCopy != null && localCopy.getTime() > digest.getTime()) {
            /*if (log.isTraceEnabled()) {
                log.trace(format("local time stamp %s greater than %s for %s ",
                                 localCopy.getTime(), digest.getTime(), digest));
            }*/
            deltaState.add(new Update(digest.getAddress(), localCopy));
        }
    }

    /**
     * Add the replicated state indicated by the digest to the list of
     * deltaState
     * 
     * @param deltaState
     * @param digest
     */
    protected void addUpdatedState(List<Update> deltaState, Digest digest) {
        Endpoint endpoint = endpoints.get(digest.getAddress());
        if (endpoint != null) {
            addUpdatedState(endpoint, deltaState, digest);
        } else if (me().equals(digest.getAddress())) {
            addUpdatedLocalState(deltaState, digest);
        } else {
            /*log.trace(format("Looking for outdated state %s on %s",
                                    digest, me()));*/
        }
    }

    /**
     * @param gossiper
     */
    protected void checkConnectionStatus(final InetSocketAddress gossiper) {
        final Endpoint gossipingEndpoint = endpoints.get(gossiper);
        if (gossipingEndpoint == null) {
            discover(gossiper);
        } else {
            gossipingEndpoint.markAlive(() -> {
                /*if (log.isDebugEnabled()) {
                    log.debug(String.format("%s is now UP on %s (check connect)",
                                            gossiper, me()));
                }*/
                view.markAlive(gossiper);
                // Endpoint has been connected
                for (ReplicatedState state : gossipingEndpoint.getStates()) {
                    if (state.isNotifiable()) {
                        notifyRegister(state);
                    }
                }
                // We want it all, baby
                gossipingEndpoint.getHandler().gossip(Iterators.singletonIterator(new Digest(
                        gossiper,
                        ALL_STATES,
                        -1)));
            }, fdFactory);
        }
    }

    /**
     * Check the status of the endpoints we know about, updating the ring with
     * the new state
     */
    protected void checkStatus() {
        long now = System.currentTimeMillis();
        /*if (log.isTraceEnabled()) {
            log.trace("Checking the status of the living...");
        }*/
        for (Iterator<Entry<InetSocketAddress, Endpoint>> iterator = endpoints.entrySet().iterator(); iterator.hasNext();) {
            Entry<InetSocketAddress, Endpoint> entry = iterator.next();
            InetSocketAddress address = entry.getKey();
            if (address.equals(me())) {
                continue;
            }

            Endpoint endpoint = entry.getValue();
            if (endpoint.isAlive()
                && endpoint.shouldConvict(now, cleanupCycles)) {
                iterator.remove();
                endpoint.markDead();
                view.markDead(address, now);
                /*if (log.isDebugEnabled()) {
                    log.debug(format("Endpoint %s is now DEAD on node: %s",
                                     endpoint.getAddress(), me()));
                }*/
                endpoint.getStates().forEach(this::notifyRemove);
            }
        }

        /*if (log.isTraceEnabled()) {
            log.trace("Culling the quarantined...");
        }*/
        view.cullQuarantined(now);

        /*if (log.isTraceEnabled()) {
            log.trace("Culling the unreachable...");
        }*/
        view.cullUnreachable(now);
        updateRing();
    }

    private void updateRing() {
        SortedSet<Endpoint> members = new TreeSet<>(endpoints.values());
        members.remove(ring.endpoint);
        if (members.size() < 3) {
            if (Ring.log.isLoggable(Level.FINE)) {
                Ring.log.fine("Ring has not been formed");
            }
            return;
        }
        SortedSet<Endpoint> head = members.headSet(ring.endpoint);

        ring.neighbor.set( (!head.isEmpty() ? head.last() : members.last()).getAddress() );
    }

//    /**
//     * Connect and gossip with a member that isn't currently connected. As we
//     * have no idea what state this member is in, we need to add a digest to the
//     * list that is manifestly out of date so that the member, if it responds,
//     * will update us with its state.
//     *
//     * @param address
//     *            - the address to connect to
//     * @param digests
//     *            - the digests in question
//     */
//    protected void connectAndGossipWith(final InetSocketAddress address,
//                                        final Iterator<Digest> digests) {
//        final Endpoint newEndpoint = new Endpoint(
//                                                  address,
//                                                  communications.handlerFor(address));
//        Endpoint previous = endpoints.putIfAbsent(address, newEndpoint);
//        if (previous == null) {
//            if (log.isDebugEnabled()) {
//                log.debug(format("%s connecting and gossiping with %s",
//                                 me(), address));
//            }
//
//            Iterator<Digest> id =
//                    Iterators.concat(
//                            Iterators.filter(digests, digest -> !digest.getAddress().equals(address),
//                                    Iterators.singletonIterator(new Digest(address, ALL_STATES, -1))));
//
//                    newEndpoint.getHandler().gossip(id);
//        }
//    }

    protected void connectAndGossipWith(final InetSocketAddress address,
                                                    final Iterator<Digest> digests) {

        gossipWith(digests, address);

//        final Endpoint newEndpoint = new Endpoint(
//                address,
//                communications.handlerFor(address));
//        Endpoint previous = endpoints.putIfAbsent(address, newEndpoint);
//        if (previous == null) {
//            if (log.isDebugEnabled()) {
//                log.debug(format("%s connecting and gossiping with %s",
//                        me(), address));
//            }
//            List<Digest> filtered = new ArrayList<Digest>(digests.size());
//            for (Digest digest : digests) {
//                if (!digest.getAddress().equals(address)) {
//                    filtered.add(digest);
//                }
//            }
//            filtered.add(new Digest(address, ALL_STATES, -1)); // We want it
//            // all, baby
//            newEndpoint.getHandler().gossip(filtered);
//        }
    }

    protected void discover(final InetSocketAddress address) {
//        if (log.isTraceEnabled()) {
//            log.trace(String.format("%s discovering %s", me(),
//                                    address));
//        }
        final Endpoint endpoint = new Endpoint(
                                               address,
                                               communications.handlerFor(address));
        Endpoint previous = endpoints.putIfAbsent(address, endpoint);
        if (previous != null) {
            /*if (log.isDebugEnabled()) {
                log.debug(format("%s already discovered on %s",
                                 endpoint.getAddress(), me()));
            }*/
        } else {
            endpoint.markAlive(() -> {
                view.markAlive(address);
                /*if (log.isDebugEnabled()) {
                    log.debug(String.format("%s is now UP on %s (discover)",
                                            address, me()));
                }*/
                // Endpoint has been connected
                for (ReplicatedState state : endpoint.getStates()) {
                    if (state.isNotifiable()) {
                        notifyRegister(state);
                    }
                }
                // We want it all, baby
                endpoint.getHandler().gossip(Iterators.singletonIterator(new Digest(
                        address,
                        ALL_STATES,
                        -1)));
            }, fdFactory);
        }
    }

    /**
     * Discover a connection with a previously unconnected member
     * 
     * @param update
     *            - the state from a previously unconnected member of the system
     *            view
     */
    protected void discover(final Update update) {
        final InetSocketAddress address = update.node;
        if (me().equals(address)) {
            return; // it's our state, dummy
        }
        Endpoint endpoint = new Endpoint(address, update.state,
                                         communications.handlerFor(address));
        Endpoint previous = endpoints.putIfAbsent(address, endpoint);
        if (previous != null) {
//            if (log.isDebugEnabled()) {
//                log.debug(format("%s already discovered on %s (update)",
//                                 endpoint.getAddress(), me()));
//            }
        } else {
//            if (log.isDebugEnabled()) {
//                log.debug(String.format("%s discovered and connecting with %s",
//                                        me(), address));
//            }
            // We want it all, baby
            endpoint.getHandler().gossip(Iterators.singletonIterator(new Digest(address,
                    ALL_STATES,
                    -1)));
        }
    }

    /**
     * Examine all the digests send by a gossiper. Determine whether we have out
     * of date state and need it from our informant, or whether our informant is
     * out of date and we need to send the updated state to the informant
     * 
     * @param digests
     * @param gossipHandler
     */
    protected void examine(TreeSet<Digest> digests, GossipMessages gossipHandler) {
//        if (log.isTraceEnabled()) {
//            log.trace(String.format("Member: %s receiving gossip digests: %s",
//                                    me(), Arrays.toString(digests)));
//        }
        List<Digest> deltaDigests = new FasterList(digests.size());
        List<Update> deltaState = new FasterList(localState.size());
        for (Digest digest : digests) {
            UUID dID = digest.getId();
            if (ALL_STATES.equals(dID)) {

                // They want it all, baby
                //synchronized (localState) {
                    deltaState.addAll(localState.values().stream().map(state -> new Update(me(), state)).collect(Collectors.toList()));
                //}
            } else {
                long remoteTime = digest.getTime();

                //TODO if digest addess the same as the previous, we can re-use it and not call endpoints.get

                Endpoint endpoint = endpoints.get(digest.getAddress());
                if (endpoint != null) {
                    ReplicatedState localState = endpoint.getState(dID);
                    if (localState != null) {
                        long localTime = localState.getTime();
                        if (remoteTime == localTime) {
                            continue;
                        }
                        if (remoteTime > localTime) {
                            deltaDigests.add(new Digest(digest.getAddress(),
                                                        localState));
                        } else if (remoteTime < localTime) {
                            addUpdatedState(endpoint, deltaState, digest);
                        }
                    } else {
                        deltaDigests.add(new Digest(digest.getAddress(),
                                dID, -1L));
                    }
                } else {
                    if (me().equals(digest.getAddress())) {
                        addUpdatedLocalState(deltaState, digest);
                    } else {
                        deltaDigests.add(new Digest(digest.getAddress(),
                                dID, -1L));
                    }
                }
            }
        }
        if (!deltaDigests.isEmpty() || !deltaState.isEmpty()) {
//            if (log.isTraceEnabled()) {
//                log.trace(String.format("Member: %s replying with digests: %s state: %s",
//                                        me(), deltaDigests,
//                                        deltaState));
//            }
            gossipHandler.reply(deltaDigests, deltaState);
        } else {
//            if (log.isTraceEnabled()) {
//                log.trace(String.format("Member: %s no state to send",
//                                        me()));
//            }
        }
    }

    /**
     * Perform the periodic gossip.
     * 
     * @param
     *            - the mechanism to send the gossip message to a peer
     */
    protected void gossip() {
        updateHeartbeat();

        List<Digest> digests = randomDigests();

        List<InetSocketAddress> members = new FasterList(redundancy);
        for (int i = 0; i < redundancy; i++) {
            members.add(gossipWithTheLiving(digests));
        }

        gossipWithTheDead(digests.iterator());
        gossipWithSeeds(digests.iterator(), members);
        checkStatus();
    }

    /**
     * The first message of the gossip protocol. The gossiping node sends a set
     * of digests of it's view of the replicated state. The receiver replies
     * with a list of digests indicating the state that needs to be updated on
     * the receiver. The receiver of the gossip also sends along any states
     * which are more recent than what the gossiper sent, based on the digests
     * provided by the gossiper.
     * 
     * @param digests
     *            - the list of replicated state digests
     * @param gossipHandler
     *            - the handler to send the reply of digests and states
     */
    protected void gossip(TreeSet<Digest> digests, GossipMessages gossipHandler) {
        checkConnectionStatus(gossipHandler.getGossipper());

        examine(digests, gossipHandler);
    }

    protected Runnable gossipTask() {
        return () -> {
            try {
                gossip();
            } catch (Throwable e) {
                log.severe(e.toString());
            }
        };
    }

//    /**
//     * Gossip with one of the kernel members of the system view with some
//     * probability. If the live member that we gossiped with is a seed member,
//     * then don't worry about it.
//     *
//     * @param digests
//     *            - the digests to gossip.
//     * @param members
//     *            - the live member we've gossiped with.
//     */
//    protected void gossipWithSeedsOLD(final List<Digest> digests,
//                                   List<InetSocketAddress> members) {
//        InetSocketAddress address = view.getRandomSeedMember(members);
//        if (address == null) {
//            return;
//        }
//        Endpoint endpoint = endpoints.get(address);
//        if (endpoint != null) {
//            List<Digest> filtered = new ArrayList<Digest>(digests.size());
//            for (Digest digest : digests) {
//                if (!digest.getAddress().equals(address)) {
//                    filtered.add(digest);
//                }
//            }
//            filtered.add(new Digest(address, ALL_STATES, -1)); // We want it
//                                                               // all, baby
//            endpoint.getHandler().gossip(filtered);
//        } else {
//            connectAndGossipWith(address, digests);
//        }
//    }
//
    protected void gossipWithSeeds(final Iterator<Digest> digests,
                                   List<InetSocketAddress> members) {
        InetSocketAddress address = view.getRandomSeedMember(members);
        if (address == null) {
            return;
        }
        gossipWith(digests, address);
    }

    public void gossipWith(Iterator<Digest> digests, final InetSocketAddress address) {
        Endpoint endpoint = endpoints.get(address);

        if (endpoint == null) {
            final Endpoint newEndpoint = new Endpoint(
                    address,
                    communications.handlerFor(address));
            Endpoint previous = endpoints.putIfAbsent(address, newEndpoint);
            if (previous != null) {
                return;
            }
        }

        if (endpoint != null) {
            Iterator<Digest> filtered = Iterators.concat(
                    Iterators.filter(digests, new DigestPredicate(address)),
                    Iterators.singletonIterator(new Digest(address, ALL_STATES, -1)));
            endpoint.getHandler().gossip(filtered);
        }
    }

    /**
     * Gossip with a member who is currently considered dead, with some
     * probability.
     * 
     * @param digests
     *            - the digests of interest
     */
    protected void gossipWithTheDead(Iterator<Digest> digests) {
        InetSocketAddress address = view.getRandomUnreachableMember(endpoints.size());
        if (address == null) {
            return;
        }
        connectAndGossipWith(address, digests);
    }

    /**
     * Gossip with a live member of the view.
     * 
     * @param digests
     *            - the digests of interest
     * @return the address of the member contacted
     */
    protected InetSocketAddress gossipWithTheLiving(List<Digest> digests) {
        InetSocketAddress address = view.getRandomMember(endpoints.keySet());
        if (address == null) {
            return null;
        }
        Endpoint endpoint = endpoints.get(address);
        if (endpoint != null) {
//            if (log.isTraceEnabled()) {
//                log.trace(format("%s gossiping with: %s, #digests: %s",
//                                 me(), endpoint.getAddress(),
//                                 digests.size()));
//            }

            @Deprecated List<Digest> filtered = new ArrayList<>(digests.size());
            filtered.addAll(digests.stream().filter(digest -> !digest.getAddress().equals(address)).collect(Collectors.toList()));

            endpoint.getHandler().gossip(filtered.iterator());
            return address;
        }
        log.severe(format("Inconsistent state!  View thinks %s is alive, but service has no endpoint!",
                            address));
        view.markDead(address, System.currentTimeMillis());
        return null;
    }

    /**
     * Notify the gossip listener of the deregistration of the replicated state.
     * This is done on a seperate thread
     * 
     * @param state
     */
    protected void notifyRemove(final ReplicatedState state) {
        assert state != null;
        if (state.isHeartbeat()) {
            return;
        }
//        if (log.isDebugEnabled()) {
//            log.debug(String.format("Member: %s notifying deregistration of: %s",
//                                    me(), state));
//        }
        dispatcher.execute(() -> {
            try {
                GossipListener gossipListener = listener.get();
                if (gossipListener != null) {
                    gossipListener.onRemove(state.getId());
                }
            } catch (Throwable e) {
                log.severe(String.format("exception notifying listener of deregistration of state %s",
                                       state.getId()));
            }
        });
    }

    /**
     * Notify the gossip listener of the registration of the replicated state.
     * This is done on a seperate thread
     * 
     * @param state
     */
    protected void notifyRegister(final ReplicatedState state) {
        assert state != null : "State cannot be null";
        if (state.isHeartbeat()) {
            return;
        }
        assert state.getState().length > 0 : "State cannot be zero length";
//        if (log.isDebugEnabled()) {
//            log.debug(String.format("Member: %s notifying registration of: %s",
//                                    me(), state));
//        }
        dispatcher.execute(() -> {
            try {
                GossipListener gossipListener = listener.get();
                if (gossipListener != null) {
                    gossipListener.onPut(state.getId(), state.getState());
                }
            } catch (Throwable e) {
                log.severe(String.format("exception notifying listener of registration of state %s",
                                       state.getId()));
            }
        });
    }

    /**
     * Notify the gossip listener of the update of the replicated state. This is
     * done on a seperate thread
     * 
     * @param state
     */
    protected void notifyUpdate(final ReplicatedState state) {
        assert state != null : "State cannot be null";
        if (state.isHeartbeat()) {
            return;
        }
        assert state.getState().length > 0 : "State cannot be zero length";
//        if (log.isDebugEnabled()) {
//            log.debug(String.format("Member: %s notifying update of: %s",
//                                    me(), state));
//        }
        dispatcher.execute(() -> {
            try {
                GossipListener gossipListener = listener.get();

                if (gossipListener != null) {
                    System.out.println(state.getId() + " " + state.getState());
                    gossipListener.onSet(state.getId(), state.getState());
                }
            } catch (Throwable e) {
                log.severe(String.format("exception notifying listener of update of state %s",
                                       state.getId()));
            }
        });
    }

    /**
     * Answer a randomized digest list of all the replicated state this node
     * knows of
     * 
     * @return
     */
    protected List<Digest> randomDigests() {
        List<Digest> digests = new FasterList<>(endpoints.size() + 1);
        for (Endpoint endpoint : endpoints.values()) {
            endpoint.addDigestsTo(digests);
        }
        //synchronized (localState) {
            localState.values().stream().map(state -> new Digest(me(), state))
                    .collect(Collectors.toCollection(() -> digests));
        //}
        Collections.shuffle(digests, entropy);
//        if (log.isTraceEnabled()) {
//            log.trace(format("Random gossip digests from %s are : %s",
//                             me(), digests));
//        }
        return digests;
    }

    /**
     * The second message in the gossip protocol. This message is sent in reply
     * to the initial gossip message sent by this node. The response is a list
     * of digests that represent the replicated state that is out of date on the
     * sender. In addition, the sender also supplies replicated state that is
     * more recent than the digests supplied in the initial gossip message (sent
     * as update messages)
     * 
     * @param digests
     *            - the list of digests the gossiper would like to hear about
     * @param gossipHandler
     *            - the handler to send a list of replicated states that the
     *            gossiper would like updates for
     */
    protected void reply(TreeSet<Digest> digests, GossipMessages gossipHandler) {
//        if (log.isTraceEnabled()) {
//            log.trace(String.format("Member: %s receiving reply digests: %s",
//                                    me(), Arrays.toString(digests)));
//        }
        checkConnectionStatus(gossipHandler.getGossipper());

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<Update> deltaState = new ArrayList();
        //TODO use a consumer fucntion which calls the below gossipHandler.update itself
        for (Digest digest : digests) {
            addUpdatedState(deltaState, digest);
        }
        if (!deltaState.isEmpty()) {
//            if (log.isTraceEnabled()) {
//                log.trace(String.format("Member: %s sending update states: %s",
//                                        me(), deltaState));
//            }
            gossipHandler.update(deltaState);
        }
    }

    /**
     * The replicated state is being sent around the ring. If the state is
     * applied, continue sending the state around the ring
     * 
     * @param state
     * @param gossiper
     */
    protected void ringUpdate(Update state, InetSocketAddress gossiper) {
        assert !me().equals(state.node) : "Should never have received ring state for ourselves";
        ring.send(state);
        update(state, gossiper);
    }

    /**
     * The third message of the gossip protocol. This is the final message in
     * the gossip protocol. The supplied state is the updated state requested by
     * the receiver in response to the digests in the original gossip message.
     * 
     * @param update
     *            - the updated state requested from our partner
     * @param gossiper
     * @return true if the state was applied, false otherwise
     */
    protected boolean update(final Update update, InetSocketAddress gossiper) {
        checkConnectionStatus(gossiper);
//        if (log.isTraceEnabled()) {
//            log.trace(String.format("Member: %s receiving update state: %s",
//                                    me(), update));
//        }
        assert update.node != null : "endpoint address is null: "
                                                   + update;
        assert !update.node.equals(me()) : "Should not have received a state update we own";
        if (view.isQuarantined(update.node)) {
//            if (log.isDebugEnabled()) {
//                log.debug(format("Ignoring gossip for %s because it is a quarantined endpoint",
//                                 update));
//            }
            return false;
        }
        final Endpoint endpoint = endpoints.get(update.node);
        if (endpoint != null) {
            endpoint.updateState(update.state, Gossip.this);
        } else {
            discover(update);
        }
        return false;
    }

    public void connectAndGossipWith(InetSocketAddress seed) {
        connectAndGossipWith(seed, randomDigests().iterator());
    }

    private static class SchedulerThreadFactory implements ThreadFactory {
        int count = 0;

        @Override
        public Thread newThread(Runnable r) {
            Thread daemon = new Thread(r, "Gossip servicing thread "
                                          + count++);
            daemon.setDaemon(true);
            daemon.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    log.severe("Uncaught exception on the gossip servicing thread"
                             );
                }
            });
            return daemon;
        }
    }

    private static class DispatcherThreadFactory implements ThreadFactory {
        volatile int count = 0;

        @Override
        public Thread newThread(Runnable r) {
            Thread daemon = new Thread(r, "Gossip dispatching thread "
                                          + count++);
            daemon.setDaemon(true);
            daemon.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    log.severe("Uncaught exception on the gossip dispatching thread"
                             );
                }
            });
            return daemon;
        }
    }

    private static final class DigestPredicate implements Predicate<Digest> {
        private final InetSocketAddress address;

        public DigestPredicate(InetSocketAddress address) {
            this.address = address;
        }

        @Override
        public boolean apply(Digest digest) {
            return (!digest.getAddress().equals(address));
        }
    }
}
