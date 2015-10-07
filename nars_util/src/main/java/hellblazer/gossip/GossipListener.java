/** 
 * (C) Copyright 2010 Hal Hildebrand, All Rights Reserved
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

import java.util.UUID;

/**
 * @author hhildebrand
 * 
 */
public interface GossipListener {

    default void onStart() { }

    default void onStop() { }

    /**
     * Previously known state has been abandoned
     * 
     * @param id
     *            - the id of the state that has been aba
     */
    void onRemove(UUID id);

    /**
     * The state is newly discovered
     * 
     * @param id
     *            - the id assigned to this state
     * @param state
     *            - the content of the state
     */
    void onPut(UUID id, byte[] state);

    /**
     * Previously known state has been updated
     * 
     * @param id
     *            - the id assigned to this state
     * @param state
     *            - the updated content of the state
     */
    void onSet(UUID id, byte[] state);
}
