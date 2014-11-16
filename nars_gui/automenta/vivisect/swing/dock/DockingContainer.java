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
public interface DockingContainer {

    /**
     * Divide this container in two
     *
     * @param child The child component which is being split. Must be a child of
     * this container.
     * @param content Content to add to the newly created side of the split
     * @param right If true, content will be placed on the right or bottom of
     * the new split
     * @param vertical If true, split will divide the panel into a top and
     * bottom. Otherwise will divide left and right.
     * @return The newly created region.
     */
    public DockingRegionSplit split(DockingChild child,
            DockingContent content,
            boolean right, boolean vertical);

    /**
     * Indicates that oldChild has become invalid and should be replaced with
     * newChild. If newChild is null, indicates oldChild should be removed.
     *
     * @param oldChild Child to be removed
     * @param newChild Child to replace oldChild with. If null, oldChild is
     * simply removed
     */
    public void join(DockingChild oldChild, DockingChild newChild);

    public DockingPathRecord buildPath(DockingChild dockChild, DockingPathRecord childPath);

    public DockingRegionContainer getContainerRoot();
}
