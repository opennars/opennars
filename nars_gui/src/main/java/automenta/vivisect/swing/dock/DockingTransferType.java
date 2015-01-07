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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 *
 * @author kitfox
 */
public class DockingTransferType implements Transferable {

    public static final DataFlavor FLAVOR
            = new DataFlavor(DockingTransferType.class, null);

    private final DockingPathRecord path;
    private final int windowIndex;

    public DockingTransferType(final DockingPathRecord path, final int windowIndex) {
        this.path = path;
        this.windowIndex = windowIndex;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return this;
    }

    /**
     * @return the path
     */
    public DockingPathRecord getPath() {
        return path;
    }

    /**
     * @return the windowIndex
     */
    public int getContainerIndex() {
        return windowIndex;
    }

}
