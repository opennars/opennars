///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jnetention.gui.swing;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.Rectangle;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.geom.Point2D;
//import java.io.File;
//import javax.swing.JPanel;
//import javax.swing.event.MouseInputListener;
//import org.jxmapviewer.JXMapViewer;
//import org.jxmapviewer.OSMTileFactoryInfo;
//import org.jxmapviewer.input.CenterMapListener;
//import org.jxmapviewer.input.PanKeyListener;
//import org.jxmapviewer.input.PanMouseInputListener;
//import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
//import org.jxmapviewer.painter.Painter;
//import org.jxmapviewer.viewer.DefaultTileFactory;
//import org.jxmapviewer.viewer.GeoPosition;
//import org.jxmapviewer.viewer.LocalResponseCache;
//import org.jxmapviewer.viewer.TileFactoryInfo;
//
///**
// *
// * @author me
// */
//public class SwingMap extends JPanel {
//
//    /**
//     * @param args the program args (ignored)
//     */
//    public SwingMap(GeoPosition initialPosition) {
//        super(new BorderLayout());
//
//        // Create a TileFactoryInfo for OpenStreetMap
//        TileFactoryInfo info = new OSMTileFactoryInfo();
//        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
//        tileFactory.setThreadPoolSize(8);
//
//        // Setup local file cache
//        File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
//        LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir, false);
//
//        // Setup JXMapViewer
//        JXMapViewer mapViewer = new JXMapViewer();
//        mapViewer.setTileFactory(tileFactory);
//
//        // Set the focus
//        mapViewer.setZoom(7);
//        mapViewer.setAddressLocation(initialPosition);
//
//        // Add interactions
//        MouseInputListener mia = new PanMouseInputListener(mapViewer);
//        mapViewer.addMouseListener(mia);
//        mapViewer.addMouseMotionListener(mia);
//
//        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
//
//        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
//
//        mapViewer.addKeyListener(new PanKeyListener(mapViewer));
//
//        // Add a selection painter
//        SelectionAdapter sa = new SelectionAdapter(mapViewer);
//        SelectionPainter sp = new SelectionPainter(sa);
//        mapViewer.addMouseListener(sa);
//        mapViewer.addMouseMotionListener(sa);
//        mapViewer.setOverlayPainter(sp);
//
//        add(mapViewer, BorderLayout.CENTER);
//    }
//
//    public static class SelectionPainter implements Painter<Object> {
//
//        private Color fillColor = new Color(128, 192, 255, 128);
//        private Color frameColor = new Color(0, 0, 255, 128);
//
//        private SelectionAdapter adapter;
//
//        /**
//         * @param adapter the selection adapter
//         */
//        public SelectionPainter(SelectionAdapter adapter) {
//            this.adapter = adapter;
//        }
//
//        @Override
//        public void paint(Graphics2D g, Object t, int width, int height) {
//            Rectangle rc = adapter.getRectangle();
//
//            if (rc != null) {
//                g.setColor(frameColor);
//                g.draw(rc);
//                g.setColor(fillColor);
//                g.fill(rc);
//            }
//        }
//    }
//
//    /**
//     * Creates a selection rectangle based on mouse input Also triggers repaint
//     * events in the viewer
//     *
//     * @author Martin Steiger
//     */
//    public static class SelectionAdapter extends MouseAdapter {
//
//        private boolean dragging;
//        private JXMapViewer viewer;
//
//        private Point2D startPos = new Point2D.Double();
//        private Point2D endPos = new Point2D.Double();
//
//        /**
//         * @param viewer the jxmapviewer
//         */
//        public SelectionAdapter(JXMapViewer viewer) {
//            this.viewer = viewer;
//        }
//
//        @Override
//        public void mousePressed(MouseEvent e) {
//            if (e.getButton() != MouseEvent.BUTTON3) {
//                return;
//            }
//
//            startPos.setLocation(e.getX(), e.getY());
//            endPos.setLocation(e.getX(), e.getY());
//
//            dragging = true;
//        }
//
//        @Override
//        public void mouseDragged(MouseEvent e) {
//            if (!dragging) {
//                return;
//            }
//
//            endPos.setLocation(e.getX(), e.getY());
//
//            viewer.repaint();
//        }
//
//        @Override
//        public void mouseReleased(MouseEvent e) {
//            if (!dragging) {
//                return;
//            }
//
//            if (e.getButton() != MouseEvent.BUTTON3) {
//                return;
//            }
//
//            viewer.repaint();
//
//            dragging = false;
//        }
//
//        /**
//         * @return the selection rectangle
//         */
//        public Rectangle getRectangle() {
//            if (dragging) {
//                int x1 = (int) Math.min(startPos.getX(), endPos.getX());
//                int y1 = (int) Math.min(startPos.getY(), endPos.getY());
//                int x2 = (int) Math.max(startPos.getX(), endPos.getX());
//                int y2 = (int) Math.max(startPos.getY(), endPos.getY());
//
//                return new Rectangle(x1, y1, x2 - x1, y2 - y1);
//            }
//
//            return null;
//        }
//
//    }
//}
