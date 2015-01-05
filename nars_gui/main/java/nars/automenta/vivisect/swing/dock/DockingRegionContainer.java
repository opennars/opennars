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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.EventObject;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import static javax.swing.SwingUtilities.getWindowAncestor;

/**
 *
 * @author kitfox
 */
public class DockingRegionContainer extends JPanel implements DockingContainer {

    private DockingChild root;

    JPanel panel_minimizeBar;
    final JPanel panel_workArea;

    final ArrayList<MinMaxRecord> minimizedRecords = new ArrayList<>();

    MinMaxRecord maximizedRecord;
    private final DockingRegionRoot dockRoot;

    JComponent oldGlass;
    final DraggingOverlayPanel overlayPanel;

    public DockingRegionContainer(final DockingRegionRoot dockRoot) {
        this.dockRoot = dockRoot;
        setLayout(new BorderLayout());

        panel_workArea = new JPanel();
        panel_workArea.setLayout(new BorderLayout());
        add(panel_workArea, BorderLayout.CENTER);

        overlayPanel = new DraggingOverlayPanel(this);
        overlayPanel.setVisible(true);

        root = new DockingRegionTabbed();
        root.setDockParent(this);
        panel_workArea.add(root.getComponent());
    }

    public void addDockingRegionContainerListener(DockingRegionContainerListener l) {
        //listeners.add(l);
        listenerList.add(DockingRegionContainerListener.class, l);
    }

    public void removeDockingRegionContainerListener(DockingRegionContainerListener l) {
//        listeners.remove(l);
        listenerList.remove(DockingRegionContainerListener.class, l);
    }

    @Override
    public DockingRegionSplit split(DockingChild child,
            DockingContent content, boolean right, boolean vertical) {
        if (child != root) {
            //We should always be updating the old child
            throw new IllegalArgumentException();
        }

        DockingRegionTabbed newRegion = new DockingRegionTabbed();
        newRegion.addTab(content);

        DockingChild leftChild, rightChild;
        leftChild = right ? root : newRegion;
        rightChild = right ? newRegion : root;

        DockingRegionSplit split = new DockingRegionSplit(leftChild, rightChild);
        split.setDockParent(this);
        split.setOrientation(vertical ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT);
        root = split;

        panel_workArea.removeAll();
        panel_workArea.add(split, BorderLayout.CENTER);
        revalidate();
        return split;
    }

    @Override
    public void join(DockingChild oldChild, DockingChild newChild) {
        if (oldChild != root) {
            //We should always be updating the old child
            throw new IllegalArgumentException();
        }

        if (newChild == null) {
            fireDockingContainerEmpty();
            return;
        }

        root = newChild;
        newChild.setDockParent(this);

        panel_workArea.removeAll();
        panel_workArea.add(root.getComponent(), BorderLayout.CENTER);
        revalidate();
    }

    protected void fireDockingContainerEmpty() {
        EventObject evt = new EventObject(this);
        for (DockingRegionContainerListener l
                : listenerList.getListeners(DockingRegionContainerListener.class)) {
            l.dockingContainerEmpty(evt);
        }
    }

    /**
     * @return the root
     */
    public DockingChild getRoot() {
        return root;
    }

    public void setRoot(DockingChild root) {
        this.root = root;
        this.root.setDockParent(this);
//        panel_workArea.removeAll();
//        panel_workArea.add(root.getComponent(), BorderLayout.CENTER);

        minimizedRecords.clear();
        maximizedRecord = null;

        rebuildMinimizeBar();
        rebuildWorkArea();
        revalidate();
    }

//    @Override
//    public int getNumDockChildren()
//    {
//        return 1;
//    }
//
//    @Override
//    public DockingChild getDockChild(int index)
//    {
//        if (index != 1)
//        {
//            throw new IllegalArgumentException();
//        }
//
//        return root;
//    }
    public void addDockContent(DockingContent content) {
        root.addDockContent(content);
    }

    @Override
    public DockingPathRecord buildPath(DockingChild dockChild, DockingPathRecord childPath) {
        return childPath;
    }

    @Override
    public DockingRegionContainer getContainerRoot() {
        return this;
    }

    public void floatWindow(DockingContent content, DockingPathRecord partialPath) {
//        DockingRegionTabbed tab = (DockingRegionTabbed)partialPath.getLast().getDockingChild();
        DockingRegionTabbed tab = (DockingRegionTabbed) root.getDockingChild(partialPath);
        tab.removeTab(content);

        DockingRegionWindow win = dockRoot.createFloatingWindow();
        win.getRoot().addDockContent(content);
        win.pack();

        centerWindow(win, getWindowAncestor(dockRoot).getBounds());
    }

    public void maximize(DockingContent content, DockingPathRecord partialPath) {
        if (maximizedRecord != null) {
            throw new IllegalArgumentException();
        }

//        DockingRegionTabbed tab = (DockingRegionTabbed)partialPath.getLast().getDockingChild();
        DockingRegionTabbed tab = (DockingRegionTabbed) root.getDockingChild(partialPath);
        tab.removeTab(content);

        maximizedRecord = new MinMaxRecord(content, partialPath);

        rebuildWorkArea();
        rebuildMinimizeBar();
    }

    void restoreFromMaximize(DockingContent content) {
        if (maximizedRecord.getContent() != content) {
            throw new IllegalArgumentException();
        }

        DockingRegionTabbed tab = (DockingRegionTabbed) root.getDockingChild(maximizedRecord.path);
        tab.restore(content, maximizedRecord.path.getLast());

//        maximizedRecord.path.getDockingChild().restore(
//                maximizedRecord.getContent(), maximizedRecord.getPath());
        maximizedRecord = null;
        rebuildWorkArea();
        rebuildMinimizeBar();
    }

    void closeFromMaximize(DockingContent content) {
        if (maximizedRecord.getContent() != content) {
            throw new IllegalArgumentException();
        }

        maximizedRecord = null;

        rebuildWorkArea();
        rebuildMinimizeBar();
    }

    void floatFromMaximize(DockingContent content) {
        if (maximizedRecord.getContent() != content) {
            throw new IllegalArgumentException();
        }

        maximizedRecord = null;

        rebuildWorkArea();
        rebuildMinimizeBar();

        DockingRegionWindow win = dockRoot.createFloatingWindow();
        win.getRoot().addDockContent(content);
        win.pack();
        centerWindow(win, getWindowAncestor(dockRoot).getBounds());
    }

    public void minimize(DockingContent content, DockingPathRecord partialPath) {
        DockingRegionTabbed tab = (DockingRegionTabbed) root.getDockingChild(partialPath);
//        DockingRegionTabbed tab = (DockingRegionTabbed)partialPath.getLast().getDockingChild();
        tab.removeTab(content);

        MinMaxRecord rec = new MinMaxRecord(content, partialPath);
        minimizedRecords.add(rec);
        rebuildMinimizeBar();
    }

    void closeMinimized(MinMaxRecord record) {
        minimizedRecords.remove(record);
        rebuildMinimizeBar();
    }

    void restoreMinimized(MinMaxRecord record) {
        minimizedRecords.remove(record);
        rebuildMinimizeBar();

//        record.path.getDockingChild().restore(
//                record.getContent(), record.getPath());
        DockingRegionTabbed tab = (DockingRegionTabbed) root.getDockingChild(record.getPath());
        tab.restore(record.getContent(), record.getPath().getLast());
    }

    private void rebuildMinimizeBar() {
        if (minimizedRecords.isEmpty() || maximizedRecord != null) {
            if (panel_minimizeBar != null) {
                remove(panel_minimizeBar);
                panel_minimizeBar = null;
            }
        } else {
            if (panel_minimizeBar == null) {
                panel_minimizeBar = new JPanel();
                add(panel_minimizeBar, BorderLayout.SOUTH);
                FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
                panel_minimizeBar.setLayout(layout);
            }

            panel_minimizeBar.removeAll();
            for (MinMaxRecord rec : minimizedRecords) {
                MinimizeTitlePanel button = new MinimizeTitlePanel(rec);
                panel_minimizeBar.add(button);
            }
        }
        revalidate();
        repaint();
    }

    private void rebuildWorkArea() {
        panel_workArea.removeAll();

        if (maximizedRecord == null) {
            panel_workArea.add(root.getComponent(), BorderLayout.CENTER);
        } else {
            DockingRegionMaximized panel = new DockingRegionMaximized(maximizedRecord);
            panel_workArea.add(panel, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    private void centerWindow(DockingRegionWindow win, Rectangle bounds) {
        int dw = (bounds.width - win.getWidth()) / 2;
        int dh = (bounds.height - win.getHeight()) / 2;

        win.setLocation(bounds.x + dw, bounds.y + dh);
    }

    /**
     * @return the dockRoot
     */
    public DockingRegionRoot getDockRoot() {
        return dockRoot;
    }

    void showDragControl() {
//System.err.println("showDragControl()");

        JRootPane rootPane = getRootPane();
        oldGlass = (JComponent) rootPane.getGlassPane();

//        overlayPanel = new DraggingOverlayPanel(this);
        rootPane.setGlassPane(overlayPanel);
        overlayPanel.setVisible(true);

        rootPane.revalidate();
        rootPane.repaint();
    }

    void hideDragControl() {
//System.err.println("hideDragControl()");
        JRootPane rootPane = getRootPane();

        rootPane.setGlassPane(oldGlass);
        oldGlass = null;
//        overlayPanel = null;

        rootPane.revalidate();
        rootPane.repaint();
    }

    void clearAllOverlays() {
        dockRoot.clearAllOverlays();
    }

    void clearOverlay() {
        overlayPanel.clearOverlay();
    }

    public DockingPickRecord pickContainer(Point containerPoint) {
        return root.pickContainer(containerPoint);
    }

    public DockingChild getDockingChild(DockingPathRecord path) {
        return root.getDockingChild(path);
    }

    public void closeAll() {
        minimizedRecords.clear();
        maximizedRecord = null;
        root.closeAll();
    }

    //---------------------------------
    public class MinMaxRecord {

        private final DockingContent content;
        private final DockingPathRecord path;

        public MinMaxRecord(DockingContent content, DockingPathRecord path) {
            this.content = content;
            this.path = path;
        }

        /**
         * @return the content
         */
        public DockingContent getContent() {
            return content;
        }

        /**
         * @return the path
         */
        public DockingPathRecord getPath() {
            return path;
        }

        public DockingRegionContainer getContainer() {
            return DockingRegionContainer.this;
        }
    }
}
