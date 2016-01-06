//package ca.nengo.model.impl;
//
//import automenta.vivisect.dimensionalize.HyperassociativeMap;
//import ca.nengo.ui.lib.world.PaintContext;
//import ca.nengo.ui.lib.world.WorldObject;
//import ca.nengo.ui.model.widget.UIProbe;
//import ca.nengo.ui.model.widget.Widget;
//import nars.core.Parameters;
//import org.apache.commons.math3.linear.ArrayRealVector;
//
//import java.awt.*;
//import java.awt.geom.Rectangle2D;
//import java.util.Map;
//
//
//public class ObjectGraphNode extends ObjectNode {
//
//    protected double childlayoutTime = 0.25f; //in seconds
//
//    public ObjectGraphNode(String name, Object object) {
//        super(name, object);
//    }
//
//    @Override
//    public UIObjectGraphNode newUI(double width, double height) {
//        return new UIObjectGraphNode();
//    }
//
//    public class UIObjectGraphNode extends UIObjectNode {
//
//        public HyperassociativeMap<String,String> bodyLayout;
//        private Map<WorldObject, ArrayRealVector> pos;
//        private Map<String,WorldObject> graphChildren = Parameters.newHashMap();
//
//        public void randomize() {
//            bodyLayout.reset();
//        }
//
//        //@Override
//        public void layoutChildren() {
//            //super.layoutChildren();
//
//
//		/*
//         * layout widgets such as Origins and Terminations
//		 */
//            Rectangle2D bounds = getIcon().localToParent(getIcon().getBounds());
//
//            double offsetX = bounds.getX();
//            double offsetY = bounds.getY();
//
//            double centerX = offsetX + bounds.getWidth() / 2f;
//            double centerY = offsetY + bounds.getHeight() / 2f;
//
//            double termX = -20 + bounds.getX();
//            double termY = getIcon().getHeight() + offsetY;
//
//            double originX = getIcon().getWidth() + 5 + offsetX;
//            double originY = termY;
//
//            double probeY = 0;
//
//
//            if (getChildrenCount() == 0) {
//
//                return;
//            }
//
//            if (bodyLayout == null) {
//
//
//                addChild(commonIn);
//                addChild(commonOut);
//
//
//                bodyLayout = new HyperassociativeMap<String,String>(bodyGraph, HyperassociativeMap.Euclidean, 2) {
//
//                    @Override
//                    public double getSpeedFactor() {
//                        return 1f;
//                    }
//
//
//                    @Override
//                    public double getSpeedFactor(String o) {
//
//                        /*if (o.equals(getName())) {
//                            return 0;
//                        }*/
//                        //if (o.equals(commonIn.getName())) return 0;
//                        //if (o.equals(commonOut.getName())) return 0;
//                        return super.getSpeedFactor(o);
//                    }
//
//                    @Override
//                    public double getRadius(String o) {
//
//                        if (o.equals(getName())) {
//                            return 500d;
//                        }
////                        if (o instanceof WorldObject) {
////                            WorldObject w = (WorldObject)o;
////                            return Math.max(w.getWidth(), w.getHeight()) * 1f;
////                        }
//                        return 10d;
//                    }
//
//                };
//                bodyLayout.getPosition(getName()).set(0);
//                bodyLayout.setEquilibriumDistance(20);
//                bodyLayout.setMaxRepulsionDistance(1000);
//
//
//            }
//
//
//		/*
//		 * Lays out origin objects
//		 */
//            for (WorldObject wo : getChildren()) {
//
//                if (wo instanceof UIProbe) {
//                    UIProbe probe = (UIProbe) wo;
//
//                    probe.setOffset(getWidth() * (1f / 4f), probeY + getHeight() * (1f / 4f));
//                    probeY += probe.getHeight() + 5;
//
//                } else if (wo instanceof Widget) {
//                    Widget widget = (Widget) wo;
//                    if (widget.getParent() == null) {
//					/*
//					 * Check to see that the origin has not been removed from
//					 * the world
//					 */
//
//                    } else {
//
//                        double scale = widget.getScale();
//
//                        if (!(widget).isWidgetVisible()) {
//                            double x = centerX - widget.getWidth() * scale / 2f;
//                            double y = centerY - widget.getHeight() * scale / 2f;
//
//                            widget.setOffset(x, y);
//
//                            widget.setVisible(false);
//                            widget.setPickable(false);
//                            widget.setChildrenPickable(false);
//
//                        } else {
//                            widget.setVisible(true);
//                            widget.setPickable(true);
//                            widget.setChildrenPickable(true);
//
//
//                            //TODO remove removed widgets
//
//                        }
//                    }
//
//                }
//            }
//
//
//
//            /*if (bodyLayout.isAlignable())*/ {
//                layoutGraph();
//
//            }
//
//        }
//
//
//
//        protected void layoutGraph() {
//            //bodyLayout.resetLearning();
//            bodyLayout.run(16);
//
//            final double w = getWidth();
//            final double h = getHeight();
//            //final double s = Math.max(w, h);
//
//
//            if (pos == null)
//                pos = Parameters.newHashMap(bodyGraph.vertexSet().size());
//            else
//                pos.clear();
//
//            double minX=Double.POSITIVE_INFINITY, maxX=Double.NEGATIVE_INFINITY, minY=Double.POSITIVE_INFINITY, maxY=Double.NEGATIVE_INFINITY;
//
//            for (WorldObject wo : getChildren()) {
//
//                ArrayRealVector p = bodyLayout.getPosition(wo.getName());
//
//                if (wo == getIcon()/* || wo == commonIn || wo == commonOut*/) {
//                    //force set the map's value to the fixed position
//                    p.setEntry(0, 0); //wo.getX());
//                    p.setEntry(1, 0); //wo.getY());
//                    continue;
//                }
//
//                double x = p.getEntry(0);
//                double y = p.getEntry(1);
//                if (x < minX) minX = x;
//                if (y < minY) minY = y;
//                if (x > maxX) maxX = x;
//                if (y > maxY) maxY = y;
//
//                pos.put(wo, p);
//            }
//
//            if ((minX==maxX) && (minY==maxY))
//                return;
//
//            double bw = maxX - minX;
//            double cx = 0.5 * (maxX + minX);
//            double bh = maxY - minY;
//            double cy = 0.5 * (maxY + minY);
//            double r = Math.max(getWidth(), getHeight()) * 2;
//
//            for (Map.Entry<WorldObject, ArrayRealVector> e : pos.entrySet()) {
//                WorldObject wo = e.getKey();
//                ArrayRealVector p = e.getValue();
//                double x = (p.getEntry(0)-cx) / bw;
//                double y = (p.getEntry(1)-cy) / bh;
//
//                /*double ww = w/2 * 1.25f;
//                if (wo instanceof UITarget && x > -ww) x = -ww;
//                else if (wo instanceof UISource && x < ww) x = ww;*/
//
//                //wo.animateToPosition(x* r, y* r, childlayoutTime);
//                wo.setOffset(x* r, y* r);
//
//                //POLAR:
//                /*
//                if (wo == commonIn || wo == commonOut) {
//                    p.setEntry(1, 0);
//                }
//                x *= Math.PI;
//                double r = Math.max(getWidth(), getHeight())/2.0;
//                y *= r/20.0;
//                wo.animateToPosition((r+y) * Math.cos(x), (r+y) * Math.sin(x), childlayoutTime);
//                */
//
//
//            }
//
//        }
//
//        @Override
//        public void paint(PaintContext paintContext) {
//
//
//            Graphics2D g = paintContext.getGraphics();
//
//            g.setStroke(new BasicStroke(1));
//            g.setColor(new Color(255,200,0,128));
//
//
//            //render edges
//            for (Object a : bodyGraph.edgeSet()) {
//                String src = (String) bodyGraph.getEdgeSource(a);
//                WorldObject sw = getGraphChild(src); //this function can be accelerated if children are stored as a map
//                if (sw == null) continue;
//                String tgt = (String) bodyGraph.getEdgeTarget(a);
//                WorldObject tw = getGraphChild(tgt); //this function can be accelerated if children are stored as a map
//                if (tw == null) continue;
//
//                //System.out.println(src + " " + sw.getX() + " " + sw.getY() + " " + tgt + " " + tw.getX());
//                g.drawLine(
//                        (int) (sw.getOffset().getX()),
//                        (int) (sw.getOffset().getY()),
//                        (int) (tw.getOffset().getX()),
//                        (int) (tw.getOffset().getY()) );
//
//            }
//
//            super.paint(paintContext);
//
//        }
//
//
//        private WorldObject getGraphChild(String src) {
//            if (graphChildren.containsKey(src)) {
//                return graphChildren.get(src);
//            }
//            WorldObject r = getChild(src, null);
//            graphChildren.put(src, r);
//            return r;
//        }
//
//    }
//
// }
