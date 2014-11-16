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
abstract public class DockingPathRecord {

    final DockingPathRecord next;

    public DockingPathRecord() {
        this.next = null;
    }

    public DockingPathRecord(final DockingPathRecord next) {
        this.next = next;
    }

//    abstract public DockingChild getDockingChild();
    public DockingPathRecord getLast() {
        return (next == null) ? this : next.getLast();
    }
}
