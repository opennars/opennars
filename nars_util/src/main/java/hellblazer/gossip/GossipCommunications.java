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

/**
 * The service interface for connecting to new members
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 */

public interface GossipCommunications {

    /**
     * Answer the local address of the communcations endpoint
     * 
     * @return the socket address
     */
    InetSocketAddress getLocalAddress();

    /**
     * @return the maximum byte size for replicated state
     */
    int getMaxStateSize();

    /**
     * Answer a gossip handler for the address
     * 
     * @param address
     *            - the address to connect
     * @return
     */
    GossipMessages handlerFor(InetSocketAddress address);

    /**
     * Set the gossip service
     * 
     * @param gossip
     */
    void setGossip(Gossip gossip);

    /**
     * Start the communications service
     */
    void start();

    /**
     * Tereminate the communications service
     */
    void terminate();

    /**
     * Send the replicated state around the ring via the left members
     * 
     * @param state
     * @param inetSocketAddress
     */
    void update(Update state, InetSocketAddress inetSocketAddress);
}
