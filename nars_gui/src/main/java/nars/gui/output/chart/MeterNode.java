package nars.gui.output.chart;


import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.ui.model.plot.LinePlot;
import nars.NAR;
import nars.event.AbstractReaction;
import nars.event.FrameReaction;
import nars.io.meter.Signal;
import nars.io.meter.TemporalMetrics;

import java.util.LinkedHashMap;
import java.util.Map;

public class MeterNode extends DefaultNetwork {

    private final TemporalMetrics<Object> data;
    private final NAR nar;
    private AbstractReaction frameHandler;

    Map<Signal, LinePlot> plots = new LinkedHashMap<>();

    public MeterNode(NAR nar, TemporalMetrics m) {
        super();

        this.nar = nar;
        this.data = m;

        double y = 0;
        double height = 64;
        double margin = 4;
        double width = 128;
        for (Signal s : data.getSignals()) {
            LinePlot l = new LinePlot(s.id, width, height);
            plots.put(s, l);

            try {
                addNode(l);
            } catch (StructuralException e) {


            }

            l.move(0,y);
            System.out.println(l);

            y+=height+margin;

//            l.move(0,y);
//
//            final double Y  =y;
//            l.ui.animateToPosition(0, Y, 0);


        }


        visibility(true); //TODO use real awearness of visibliity


    }




    protected void visibility(boolean appearedOrDisappeared) {
        if (appearedOrDisappeared) {
            if (frameHandler !=null)
                frameHandler.off();


            this.frameHandler = new FrameReaction(nar) {
                @Override public void onFrame() {
                    updateMeter();
                }
            };

            updateMeter();
        }
        else {
            if (this.frameHandler !=null) {
                frameHandler.off();
                frameHandler = null;
            }
        }
    }



    public void updateMeter() {

        if (data.numRows() == 0) return;

        Object[] n = data.rowLast();

        for (Signal s : data.getSignals()) {
            LinePlot lp = plots.get(s);
            Object x = n[data.getSignalIndex().get(s.id)];
            if (x instanceof Number)
                lp.push(((Number) x).doubleValue());
        }
    }


}
