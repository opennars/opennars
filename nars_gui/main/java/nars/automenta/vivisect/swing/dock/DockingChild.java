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

import java.awt.Point;
import javax.swing.JComponent;

/**
 *
 * @author kitfox
 */
public interface DockingChild {

    public JComponent getComponent();

    public DockingContainer getDockParent();

    public void setDockParent(DockingContainer dockParent);

    public void addDockContent(DockingContent content);

    public DockingChild getDockingChild(DockingPathRecord subpath);

    public void restore(DockingContent content, DockingPathRecord subpath);

    public DockingPickRecord pickContainer(Point containerPoint);

    public void closeAll();

}
