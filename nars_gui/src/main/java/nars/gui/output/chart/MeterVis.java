package nars.gui.output.chart;


import automenta.vivisect.Video;
import automenta.vivisect.swing.PCanvas;
import automenta.vivisect.timeline.AxisPlot;
import automenta.vivisect.timeline.AxisPlot.MultiChart;
import automenta.vivisect.timeline.LineChart;
import automenta.vivisect.timeline.TimelineVis;
import nars.event.EventEmitter;
import nars.core.Events.FrameEnd;
import nars.core.NAR;
import nars.io.meter.Metrics;
import nars.io.meter.Signal;
import nars.io.meter.SignalData;
import nars.io.meter.TemporalMetrics;
import reactor.event.registry.Registration;
import reactor.function.Consumer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.*;
import java.util.List;

import static reactor.event.selector.Selectors.T;

public class MeterVis extends TimelineVis {

    private final EventEmitter nar;

    @Deprecated public class DataChart {
        
        public final SignalData data;
        public AxisPlot chart;

        public DataChart(String id) {
            this.data = meters.newSignalData(id);
            this.chart = displayedCharts.get(id);
            
            if (chart!=null)
                chart.setOverlayEnable(true);
        }        

        private DataChart(Signal s) {
            this(s.id);
        }
    }
    
    final Map<String, AxisPlot> displayedCharts = new HashMap();
    final Metrics meters;
    final Map<String, DataChart> charts;
    
    
    public static final Font monofontLarge = Video.monofont.deriveFont(Font.PLAIN, 18f);
    public static final Font monofontSmall = Video.monofont.deriveFont(Font.PLAIN, 13f);
    int nextChartTime = 0;
    

    
    public MeterVisPanel newPanel() {
        return new MeterVisPanel();
    }
    public MeterVisPanel newPanel(int w, int h) {
        MeterVisPanel m = new MeterVisPanel();
        m.setMinimumSize(new Dimension(w,h));
        return m;
    }

    
    public MeterVis(NAR n, TemporalMetrics<Object> metrics) {
        this(n.memory.event, metrics);
    }
    
    /**
     *
     * @param title
     * @param historySize
     * @param chartType use Chart.BAR, Chart.LINE, Chart.PIE, Chart.AREA,
     * Chart.BAR_CENTERED
     */
    public MeterVis(EventEmitter nar, TemporalMetrics<Object> meters) {
        super(meters.newSignalData("time"));

        this.nar = nar;        
        this.meters = meters;

        charts = new TreeMap();
        
        List<AxisPlot> c = new ArrayList();
        List<SignalData> signals = meters.getSignalDatas();
        for (SignalData s : signals) {
            charts.put(s.getID(), new DataChart(s.signal));                
            c.add(new LineChart(s).height(10));
        }            
        setCharts(c);
        

        
        
    }
    


    public DataChart getDisplayedChart(String id) {
        if (id.contains("#")) {
            String baseName = id.split("#")[0];
            return getDisplayedChart(baseName);
        }
        DataChart c = charts.get(id);
        if (c!=null) {
            if (c instanceof MultiChart)
                ((MultiChart)c).getData().add(c.data);
            else
                throw new RuntimeException(c + " does not support multiple datas");
        }
        else {
            /*c = meters.newDefaultChart(id, data);
            
            if (c == null)
                c = new LineChart(data);
            
            displayedCharts.put(id, c);
            addChart(c);            */
        }
        return c;
    }    
    
    
    public class MeterVisPanel extends PCanvas implements Consumer<Object> {
        
        private final Registration framer;
        
        public MeterVisPanel() {
            super(MeterVis.this);
            
            //noLoop();
            //TODO disable event when window hiden
            framer = nar.on(T(FrameEnd.class), this);
            
            final MouseAdapter m = newMouseDragPanScale(this);
            addMouseMotionListener(m);
            addMouseListener(m);
        }

        @Override
        public void accept(Object t) {
            //if (event == FrameEnd.class) {
                updateNext();
                predraw();
            //}
        }
        
        
        
    }
}
