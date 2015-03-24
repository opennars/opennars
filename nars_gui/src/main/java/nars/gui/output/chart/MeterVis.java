package nars.gui.output.chart;


import ca.nengo.ui.NengrowPanel;
import nars.NAR;
import nars.event.AbstractReaction;
import nars.event.CycleReaction;
import nars.io.meter.TemporalMetrics;

public class MeterVis extends NengrowPanel {

    private final TemporalMetrics meters;
    private final NAR nar;
    private final AbstractReaction cycleHandler;


    public MeterVis(NAR nar, TemporalMetrics m) {
        super();
        this.nar = nar;
        this.meters = m;
        this.cycleHandler = new CycleReaction(nar) {
            @Override public void onCycle() {
                updateMeter();
            }
        };


        setFPS(0);
        updateMeter();

    }

    public void updateMeter() {

    }


}
