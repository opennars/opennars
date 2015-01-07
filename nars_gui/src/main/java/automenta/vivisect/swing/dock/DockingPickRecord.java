/*
 * Copyright 2011 Mark McKay
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
package automenta.vivisect.swing.dock;

/**
 *
 * @author kitfox
 */
public class DockingPickRecord {

    private final DockingChild child;
    private final int direction;

    public DockingPickRecord(final DockingChild child, final int direction) {
        this.child = child;
        this.direction = direction;
    }

    /**
     * @return the child
     */
    public DockingChild getChild() {
        return child;
    }

    /**
     * @return the direction
     */
    public int getDirection() {
        return direction;
    }

}
