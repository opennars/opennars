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
import java.util.EventObject;
import javax.swing.JDialog;
import static javax.swing.SwingUtilities.getWindowAncestor;

/**
 *
 * @author kitfox
 */
public class DockingRegionWindow extends JDialog
        implements DockingRegionContainerListener {

    private final DockingRegionContainer root;

    public DockingRegionWindow(DockingRegionRoot dockRoot, DockingRegionContainer root) {
        super(getWindowAncestor(dockRoot),
                null, ModalityType.MODELESS);

        this.root = root;
//        root = new DockingRegionContainer(dockRoot);
        getContentPane().add(root, BorderLayout.CENTER);
        root.addDockingRegionContainerListener(this);
    }

    /**
     * @return the root
     */
    public DockingRegionContainer getRoot() {
        return root;
    }

    @Override
    public void dockingContainerEmpty(EventObject evt) {
        setVisible(false);
        dispose();
    }

}
