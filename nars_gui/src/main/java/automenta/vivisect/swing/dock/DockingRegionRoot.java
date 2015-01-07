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
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * The root window of the Raven docking system.
 *
 * @author kitfox
 */
public class DockingRegionRoot extends JPanel
        implements WindowListener {

    public final DockingRegionContainer dockingRoot;

    ArrayList<DockingRegionWindow> floating
            = new ArrayList<>();
    ArrayList<DockingRegionContainer> containers
            = new ArrayList<>();

    public DockingRegionRoot() {
        setLayout(new BorderLayout());

        dockingRoot = new DockingRegionContainer(this);
        containers.add(dockingRoot);
        add(dockingRoot, BorderLayout.CENTER);
    }

    public DockingRegionWindow createFloatingWindow(DockingContent content,
            String title, Rectangle bounds) {
        DockingRegionWindow win = createFloatingWindow();
        win.setTitle(title);
        win.setBounds(bounds);

        win.getRoot().addDockContent(content);

        return win;
    }

    public int getNumFloatingWindows() {
        return floating.size();
    }

    public DockingRegionWindow getFloatingWindow(int index) {
        return floating.get(index);
    }

    public DockingRegionWindow createFloatingWindow() {
        DockingRegionContainer root = new DockingRegionContainer(this);
        containers.add(root);

        DockingRegionWindow dockWin = new DockingRegionWindow(this, root);

        floating.add(dockWin);

        dockWin.setVisible(true);
        return dockWin;
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        Window closing = e.getWindow();
        DockingRegionWindow dockWin = (DockingRegionWindow) closing;
        floating.remove(dockWin);
        containers.remove(dockWin.getRoot());
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    /**
     * @return the root
     */
    public DockingRegionContainer getDockingRoot() {
        return dockingRoot;
    }

    public int indexOf(DockingRegionContainer container) {
        return containers.indexOf(container);
    }

    void startDragging() {
        for (DockingRegionContainer cont : containers) {
            cont.showDragControl();
        }
    }

    void stopDragging() {
        for (DockingRegionContainer cont : containers) {
            cont.hideDragControl();
        }
    }

    void clearAllOverlays() {
        for (DockingRegionContainer cont : containers) {
            cont.clearOverlay();
        }
    }

    public DockingRegionContainer getContainer(int containerIndex) {
        return containers.get(containerIndex);
    }

    public void closeAll() {
        for (DockingRegionWindow win : new ArrayList<>(floating)) {
            win.getRoot().closeAll();
        }

        dockingRoot.closeAll();
    }

    public void addRootContent(DockingContent cont) {
        getDockingRoot().addDockContent(cont);
    }

}
