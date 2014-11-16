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

import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 * @author kitfox
 */
public class DockingExportTransferHandler extends TransferHandler {

    DockingRegionTabbed tabbed;
    DockingContent content;

    public DockingExportTransferHandler(DockingRegionTabbed tabbed, DockingContent content) {
        this.tabbed = tabbed;
        this.content = content;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public Transferable createTransferable(JComponent c) {
        DockingPathRecord path = tabbed.getPath(content);
        DockingRegionContainer container = tabbed.getContainerRoot();
        int idx = container.getDockRoot().indexOf(container);

        container.getDockRoot().startDragging();

        DockingTransferType xfer = new DockingTransferType(path, idx);
        return xfer;
    }

    @Override
    public void exportDone(JComponent c, Transferable t, int action) {
        DockingRegionContainer container = tabbed.getContainerRoot();
        container.getDockRoot().stopDragging();
    }
}
