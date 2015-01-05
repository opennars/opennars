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

/*
 * DraggingOverlayPanel.java
 *
 * Created on Jan 13, 2011, 6:02:58 AM
 */
package automenta.vivisect.swing.dock;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.SwingUtilities;
import static javax.swing.SwingUtilities.convertPoint;
import static javax.swing.SwingUtilities.convertRectangle;
import automenta.vivisect.swing.dock.DockingRegionTabbed.PathRecordTabbed;

/**
 *
 * @author kitfox
 */
public class DraggingOverlayPanel extends javax.swing.JPanel {

    final DockingRegionContainer container;
    final DockingImportTransferHandler xferHandler;

//    Point samplePoint;
    DockingPickRecord pickRecord;
    final AlphaComposite transpComposite;

    /**
     * Creates new form DraggingOverlayPanel
     */
    public DraggingOverlayPanel(DockingRegionContainer container) {
        this.container = container;

        initComponents();

        xferHandler
                = new DockingImportTransferHandler(this);
        setTransferHandler(xferHandler);

        transpComposite = AlphaComposite.SrcOver.derive(.5f);
    }

    @Override
    protected void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;

        g.setColor(Color.blue);
//        g.setColor(Color.red);
//        g.drawOval(0, 0, 300, 300);

        if (pickRecord != null) {
            DockingRegionTabbed tabbed
                    = (DockingRegionTabbed) pickRecord.getChild();
            Rectangle tabbedBounds = tabbed.getBounds();
            tabbedBounds.x = tabbedBounds.y = 0;
            Rectangle rect = convertRectangle(
                    tabbed, tabbedBounds, this);

            switch (pickRecord.getDirection()) {
                case SwingUtilities.NORTH:
                    rect.height /= 3;
                    break;
                case SwingUtilities.SOUTH:
                    rect.height /= 3;
                    rect.y += rect.height * 2;
                    break;
                case SwingUtilities.WEST:
                    rect.width /= 3;
                    break;
                case SwingUtilities.EAST:
                    rect.width /= 3;
                    rect.x += rect.width * 2;
                    break;
            }

            g.setComposite(transpComposite);
            g.fill(rect);
            g.setComposite(AlphaComposite.SrcOver);
            g.draw(rect);
//            g.fillOval(samplePoint.x - 8, samplePoint.y - 8,
//                    16, 16);
        }
    }

    public DockingPickRecord sampleImportPoint(Point point) {
        container.clearAllOverlays();

        if (point != null) {
            Point containerPoint = convertPoint(
                    this,
                    point,
                    container);
            pickRecord = container.pickContainer(containerPoint);
        }

        repaint();
        return pickRecord;
    }

    public void clearOverlay() {
        pickRecord = null;
        repaint();
    }

    void importContent(DockingTransferType data) {
        if (pickRecord == null) {
            return;
        }

        DockingRegionRoot dockRoot = container.getDockRoot();
        DockingRegionContainer srcContainer = dockRoot.getContainer(data.getContainerIndex());
        DockingRegionTabbed srcChild
                = (DockingRegionTabbed) srcContainer.getDockingChild(data.getPath());

        PathRecordTabbed pathLast = (PathRecordTabbed) data.getPath().getLast();
        DockingContent content = srcChild.getDockingContent(pathLast.getIndex());
        srcChild.removeTab(content);

        DockingRegionTabbed destChild = (DockingRegionTabbed) pickRecord.getChild();
        switch (pickRecord.getDirection()) {
            case SwingUtilities.CENTER:
                destChild.addDockContent(content);
                break;
            case SwingUtilities.NORTH:
                destChild.split(content, false, true);
                break;
            case SwingUtilities.SOUTH:
                destChild.split(content, true, true);
                break;
            case SwingUtilities.WEST:
                destChild.split(content, false, false);
                break;
            case SwingUtilities.EAST:
                destChild.split(content, true, false);
                break;
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setOpaque(false);
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }

            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseDragged(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseDragged
    {//GEN-HEADEREND:event_formMouseDragged
        System.err.println("Overlay drag");
    }//GEN-LAST:event_formMouseDragged

    private void formMouseMoved(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseMoved
    {//GEN-HEADEREND:event_formMouseMoved
        System.err.println("Overlay move");
    }//GEN-LAST:event_formMouseMoved

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
