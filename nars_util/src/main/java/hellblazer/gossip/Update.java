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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static hellblazer.gossip.Endpoint.readInetAddress;
import static hellblazer.gossip.Endpoint.writeInetAddress;


/**
 * @author hhildebrand
 * 
 */
public class Update {
    public final InetSocketAddress node;
    public final ReplicatedState state;

    public Update(ByteBuffer buffer) throws UnknownHostException {
        node = readInetAddress(buffer);
        state = new ReplicatedState(buffer);
    }

    /**
     * @param node
     * @param state
     */
    public Update(InetSocketAddress node, ReplicatedState state) {
        this.node = node;
        this.state = state;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Update other = (Update) obj;
        if (node == null) {
            if (other.node != null) {
                return false;
            }
        } else if (!node.equals(other.node)) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (node == null ? 0 : node.hashCode());
        result = prime * result + (state == null ? 0 : state.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("Update [%s,%s,%d]", node, state.getId(), state.getTime());
    }

    public void writeTo(ByteBuffer buffer) {
        writeInetAddress(node, buffer);
        state.writeTo(buffer);
    }
}
