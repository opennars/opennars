//package nars.gui.output.chart;
//
//
//import ca.nengo.model.StructuralException;
//import ca.nengo.model.impl.DefaultNetwork;
//import ca.nengo.ui.model.plot.LinePlot;
//import nars.NAR;
//import nars.event.FrameReaction;
//import nars.event.NARReaction;
//import nars.util.meter.Signal;
//import nars.util.meter.TemporalMetrics;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//public class MeterNode extends DefaultNetwork {
//
//    private final TemporalMetrics<Object> data;
//    private final NAR nar;
//    private NARReaction frameHandler;
//
//    Map<Signal, LinePlot> plots = new LinkedHashMap<>();
//    private boolean updatePending = false;
//
//    public MeterNode(NAR nar, TemporalMetrics m) {
//        super();
//
//        this.nar = nar;
//        this.data = m;
//
//        double y = 0;
//        double height = 64;
//        double margin = 4;
//        double width = 128;
//        for (Signal s : data.getSignals()) {
//            LinePlot l = new LinePlot(s.id, width, height);
//            plots.put(s, l);
//
//            try {
//                addNode(l);
//            } catch (StructuralException e) {
//
//
//            }
//
//            l.move(0,y);
//
//            y+=height+margin;
//
////            l.move(0,y);
////
////            final double Y  =y;
////            l.ui.animateToPosition(0, Y, 0);
//
//
//        }
//
//
//        visibility(true); //TODO use real awearness of visibliity
//
//
//    }
//
//
//
//
//    protected void visibility(boolean appearedOrDisappeared) {
//        if (appearedOrDisappeared) {
//            if (frameHandler !=null)
//                frameHandler.off();
//
//
//            this.frameHandler = new FrameReaction(nar) {
//
//
//                @Override public void onFrame() {
//                    /*if (!updatePending) {
//                        updatePending = true;
//                        SwingUtilities.invokeLater(MeterNode.this::updateMeter);
//                    }*/
//                    updateMeter();
//                }
//            };
//
//            updateMeter();
//        }
//        else {
//            if (this.frameHandler !=null) {
//                frameHandler.off();
//                frameHandler = null;
//            }
//        }
//    }
//
//
//
//    public void updateMeter() {
//        updatePending = false;
//
//        if (data.numRows() == 0) return;
//
//        Object[] n = data.rowLast();
//
//        for (Signal s : data.getSignals()) {
//            LinePlot lp = plots.get(s);
//            Object x = n[data.getSignalIndex().get(s.id)];
//            if (x instanceof Number)
//                lp.push(((Number) x).doubleValue());
//        }
//
//
//    }
//
//
// }
