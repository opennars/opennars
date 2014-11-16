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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author kitfox
 */
public class DockingRegionTabbed extends JTabbedPane implements DockingChild {

    private DockingContainer dockParent;
    HashMap<DockingContent, TabLayout> tabs = new HashMap<>();

    public DockingRegionTabbed() {
        setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public DockingContent getDockingContent(int index) {
        Component comp = getComponentAt(index);
        for (DockingContent key : tabs.keySet()) {
            if (tabs.get(key).component == comp) {
                return key;
            }
        }
        return null;
    }

    public int indexOf(DockingContent content) {
        Component comp = content.getComponent();
        return indexOfComponent(comp);
    }

    public void addTab(DockingContent content) {
        addTab(content, getTabCount());
    }

    public TabbedPaneTitle addTab(DockingContent content, int index) {
//        Component comp = content.getComponent();
//        comp.setPreferredSize(new Dimension(4, 4));
        TabLayout layout = new TabLayout(content);
        tabs.put(content, layout);

        insertTab(content.getTitle(), null, layout.component, null, index);
        int idx = indexOfComponent(layout.component);
        
        TabbedPaneTitle existing = content.getTab();
        
        TabbedPaneTitle tab = new TabbedPaneTitle(this, content);
        if (existing!=null) {
            tab.setLabel(existing.getLabel().getText());
            JComponent menuButton = existing.removeMenuButton();
            if (menuButton!=null)
                tab.setMenuButton(menuButton);
        }
        
        setTabComponentAt(idx, tab);
        content.setParent(this);
        content.setRestoreRecord(null);
        content.setTab(tab);
        
        return tab;
    }

    public void selectTab(DockingContent content) {
        TabLayout layout = tabs.get(content);
        if (layout!=null)
            setSelectedComponent(layout.component);
    }

    public void removeTab(DockingContent content) {
        TabLayout layout = tabs.remove(content);

        if (layout == null) {
            //throw new IllegalStateException("Content not part of this panel");
            return;
        }

        RestoreRecord rec = new RestoreRecord(getPath(content),
                getContainerRoot().getDockRoot().indexOf(getContainerRoot()));

        content.setRestoreRecord(rec);
        content.setParent(null);
        int idx = indexOfComponent(layout.component);
        removeTabAt(idx);

        if (getTabCount() == 0) {
            dockParent.join(this, null);
        }
    }

    @Override
    public void addDockContent(DockingContent content) {
        addTab(content);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    public void split(DockingContent content, boolean right, boolean vertical) {
        dockParent.split(this, content, right, vertical);
    }

    /**
     * @return the dockParent
     */
    @Override
    public DockingContainer getDockParent() {
        return dockParent;
    }

    /**
     * @param dockParent the dockParent to set
     */
    @Override
    public void setDockParent(DockingContainer dockParent) {
        this.dockParent = dockParent;
    }

    public DockingPathRecord getPath(DockingContent content) {
        int idx = indexOf(content);
        return dockParent.buildPath(this, new PathRecordTabbed(idx));
    }

    public DockingRegionContainer getContainerRoot() {
        return dockParent.getContainerRoot();
    }

    public void minimizeTab(DockingContent content) {
        DockingRegionContainer cont = getContainerRoot();
        cont.minimize(content, getPath(content));

//        int idx = indexOf(content);
//        PathRecordTabbed path = new PathRecordTabbed(idx);
//        dockParent.minimize(content, path);
    }

    public void maximizeTab(DockingContent content) {
        DockingRegionContainer cont = getContainerRoot();
        cont.maximize(content, getPath(content));
//        int idx = indexOf(content);
//        PathRecordTabbed path = new PathRecordTabbed(idx);
//        dockParent.maximize(content, path);
    }

    void floatTab(DockingContent content) {
        DockingRegionContainer cont = getContainerRoot();
        cont.floatWindow(content, getPath(content));

//        int idx = indexOf(content);
//        PathRecordTabbed path = new PathRecordTabbed(idx);
//        dockParent.floatWindow(content, path);
    }

    @Override
    public DockingChild getDockingChild(DockingPathRecord subpath) {
        return this;
    }

    @Override
    public void restore(DockingContent content, DockingPathRecord subpath) {
        if (subpath instanceof PathRecordTabbed) {
            //Path still consitent.  Pass restore down to indicated child
            PathRecordTabbed rec = (PathRecordTabbed) subpath;
            int index = min(rec.getIndex(), getTabCount());

            addTab(content, index);
        } else {
            //We've lost the trail.  Just add in the content here
            addDockContent(content);
        }
    }

    @Override
    public DockingPickRecord pickContainer(Point containerPoint) {
        Rectangle bounds = getBounds();
        bounds.x = bounds.y = 0;

        if (!bounds.contains(containerPoint)) {
            return null;
        }

        if (containerPoint.y < bounds.y + bounds.height / 3) {
            return new DockingPickRecord(this, SwingUtilities.NORTH);
        }
        if (containerPoint.y > bounds.y + bounds.height * 2 / 3) {
            return new DockingPickRecord(this, SwingUtilities.SOUTH);
        }
        if (containerPoint.x < bounds.x + bounds.width / 3) {
            return new DockingPickRecord(this, SwingUtilities.WEST);
        }
        if (containerPoint.x > bounds.x + bounds.width * 2 / 3) {
            return new DockingPickRecord(this, SwingUtilities.EAST);
        }

        return new DockingPickRecord(this, SwingUtilities.CENTER);
    }

    @Override
    public void closeAll() {
        for (DockingContent cont : new ArrayList<>(tabs.keySet())) {
            removeTab(cont);
        }
    }

    //---------------------------------
    public static class PathRecordTabbed extends DockingPathRecord {

        private final int index;

        public PathRecordTabbed(int index) {
            this.index = index;
        }

        /**
         * @return the index
         */
        public int getIndex() {
            return index;
        }
    }

    class TabLayout {

        DockingContent content;
        Component component;
//        JScrollPane scollPane;

        public TabLayout(DockingContent content) {
            this.content = content;
            this.component = content.getComponent();
//            this.scollPane = new JScrollPane(component);
        }
    }
}
