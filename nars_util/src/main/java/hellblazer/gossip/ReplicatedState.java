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

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author hhildebrand
 * 
 */
public class ReplicatedState {
    private final UUID   id;
    private final byte[] state;
    private final long   time;

    /**
     * @param buffer
     * @throws UnknownHostException
     */
    public ReplicatedState(ByteBuffer buffer) {
        time = buffer.getLong();
        id = new UUID(buffer.getLong(), buffer.getLong());
        state = new byte[buffer.remaining()];
        buffer.get(state);
    }

    /**
     * @param id
     * @param state
     */
    public ReplicatedState(UUID id, long time, byte[] state) {
        this.id = id;
        this.time = time;
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
        ReplicatedState other = (ReplicatedState) obj;
        /*if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else */
        return id.equals(other.id);
    }

    /**
     * @return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return the state
     */
    public byte[] getState() {
        return state;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + (id == null ? 0 : id.hashCode());
//        return result;
        return id.hashCode();
    }

    /**
     * @return
     */
    public boolean isDeleted() {
        return state.length == 0 && !isHeartbeat();
    }

    public boolean isEmpty() {
        return state.length == 0;
    }

    public boolean isHeartbeat() {
        return Gossip.HEARTBEAT.equals(id);
    }

    public boolean isNotifiable() {
        return state.length > 0 && !isHeartbeat();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("ReplicatedState [id=%s,size=%s,time=%s]", id,
                             state.length, time);
    }

    /**
     * @param buffer
     */
    public void writeTo(ByteBuffer buffer) {
        buffer.putLong(time);
        buffer.putLong(id.getMostSignificantBits());
        buffer.putLong(id.getLeastSignificantBits());
        buffer.put(state);
    }
}
