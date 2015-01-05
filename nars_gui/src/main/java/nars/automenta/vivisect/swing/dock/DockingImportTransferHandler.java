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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import static java.util.logging.Logger.getLogger;
import javax.swing.TransferHandler;

/**
 *
 * @author kitfox
 */
public class DockingImportTransferHandler extends TransferHandler {

    DraggingOverlayPanel overlayPanel;

    public DockingImportTransferHandler(DraggingOverlayPanel overlayPanel) {
        this.overlayPanel = overlayPanel;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        Point point = info.getDropLocation().getDropPoint();
        overlayPanel.sampleImportPoint(point);

        return info.isDataFlavorSupported(DockingTransferType.FLAVOR);
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
//        Point point = info.getDropLocation().getDropPoint();
//        DockingPickRecord rec = overlayPanel.sampleImportPoint(point);

        //Clear overlay
//        overlayPanel.sampleImportPoint(null);
//        if (rec == null)
//        {
//            return false;
//        }
        Transferable xfer = info.getTransferable();
        DockingTransferType data = null;
        try {
            data = (DockingTransferType) xfer
                    .getTransferData(DockingTransferType.FLAVOR);
        } catch (UnsupportedFlavorException | IOException ex) {
            getLogger(DockingImportTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (data == null) {
            return false;
        }

        overlayPanel.importContent(data);

        //Clear overlay
        overlayPanel.sampleImportPoint(null);
        return true;
    }

}
