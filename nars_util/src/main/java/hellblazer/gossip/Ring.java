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


import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Ring {
    private final GossipCommunications comms;
    public final AtomicReference<InetSocketAddress> neighbor = new AtomicReference<>();
    public final Endpoint                           endpoint;
    public static final Logger log      = Logger.getLogger(Ring.class.getCanonicalName());

    public Ring(InetSocketAddress address, GossipCommunications comms) {
        endpoint = new Endpoint(address);
        this.comms = comms;
    }

    /**
     * Send the heartbeat around the ring in both directions.
     * 
     * @param state
     */
    public final void send(Update state) {
        InetSocketAddress target = neighbor.get();
        if (target != null) {
            if (target.equals(state.node)) {
                //if (log.isLoggable(Level.FINE)) {
                    //log.severe(String.format("Not forwarding state %s to the node that owns it",
                                           // state));
                //}
            } else {
                comms.update(state, target);
            }
        } //else {
            //if (log.isLoggable(Level.FINE)) {
                //log.severe("Ring has not been formed, not forwarding state");
            //}
        //}
    }

}
