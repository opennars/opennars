package nars.gui.output.timeline;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import nars.core.EventEmitter.Observer;
import nars.core.Events.CycleEnd;
import nars.core.NAR;
import nars.gui.NPanel;
import nars.gui.output.chart.TimeSeries;
import nars.gui.output.timeline.BarChart;
import nars.gui.output.timeline.Chart;
import nars.gui.output.timeline.LineChart;
import nars.gui.output.timeline.StackedPercentageChart;
import nars.util.NARTrace;

/**
 *
 * @author me
 */


public class TimelinePanel extends NPanel implements Observer {
    
    private List<Chart> charts;
    private final Timeline2DCanvas timeline;
    private final JPanel controls;
    private final NAR nar;
    private final NARTrace trace;

    public TimelinePanel(NAR n) {
        super(new BorderLayout());
        
        this.nar = n;
        this.trace = new NARTrace(n);
        trace.setActive(false);
        
        
        charts = new ArrayList();

               
        this.timeline = new Timeline2DCanvas(charts);
        
        
        this.controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));
        
        JCheckBox enableBox = new JCheckBox("Enable");
        enableBox.setSelected(true); //TODO make functional
        enableBox.setEnabled(false);
        controls.add(enableBox);

        add(timeline, BorderLayout.CENTER);
        add(controls, BorderLayout.EAST);
        
        addChartControls();
        
        n.on(CycleEnd.class, this);
    }

    abstract public class ChartButton extends JToggleButton {
        private Chart chart;

        public ChartButton(String text) {
            super(text);
            addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (isSelected())
                        enableChart();
                    else
                        disableChart();
                }
            });
        }
    
        public void enableChart() {
            chart = newChart();      
            charts.add(chart);
        }
        
        public void disableChart() {
            if (chart!=null) {
                charts.remove(chart);
                chart = null;
            }
        }
        
        abstract public Chart newChart();
    }
    
    protected void addChartControls() {
        controls.add(new ChartButton("All Events") {
            @Override public Chart newChart() {
                return new EventChart(trace, false, true, false).height(3);
            }            
        });
        controls.add(new ChartButton("Concept Priority (mean)") {
            @Override public Chart newChart() {
                return new LineChart(trace, "concept.priority.mean").height(1);
            }            
        });
        controls.add(new ChartButton("Delta Concepts") {
            @Override public Chart newChart() {
                return new BarChart(new TimeSeries.FirstOrderDifferenceTimeSeries("d(concepts)", trace.charts.get("concept.count")));
            }            
        });
        controls.add(new ChartButton("Concept Priority Histogram (4 level)") {
            @Override public Chart newChart() {
                return new StackedPercentageChart(trace, "concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3").height(2);
            }            
        });
        controls.add(new ChartButton("Task: Novel Add, Immediate Processed") {
            @Override public Chart newChart() {
                return new LineChart(trace, "task.novel.add", "task.immediate_processed").height(3);
            }            
        });
        controls.add(new ChartButton("Task: Processed Goal, Question, Judgment") {
            @Override public Chart newChart() {
                return new LineChart(trace, "task.goal.process", "task.question.process", "task.judgment.process").height(3);
            }            
        });
        controls.add(new ChartButton("Emotion: Busy") {
            @Override public Chart newChart() {
                return new LineChart(trace, "emotion.busy").height(1);
            }            
        });
        controls.add(new ChartButton("Task: Executed") {
            @Override public Chart newChart() {
                return new BarChart(trace, "task.executed").height(2);
            }            
        });
        controls.add(new ChartButton("Plan Graph Components") {
            @Override public Chart newChart() {
                return new StackedPercentageChart(trace, "plan.graph.in.other.count", "plan.graph.in.operation.count", "plan.graph.in.interval.count").height(3);
            }            
        });
        controls.add(new ChartButton("Plan Graph Interval Magnitude (mean)") {
            @Override public Chart newChart() {
                return new LineChart(trace, "plan.graph.in.delay_magnitude.mean").height(1);
            }            
        });
        controls.add(new ChartButton("Plan Graph Vertices & Edges") {
            @Override public Chart newChart() {
                return new LineChart(trace, "plan.graph.edge.count", "plan.graph.vertex.count").height(2);
            }            
        });
        controls.add(new ChartButton("Executable & Planned Tasks") {
            @Override public Chart newChart() {
                return new StackedPercentageChart(trace, "plan.task.executable", "plan.task.planned").height(2);
            }            
        });
            
            
                           
            
        
        
    }
    
    @Override
    public void event(Class event, Object[] arguments) {
        if (event == CycleEnd.class) {
            timeline.updateNext();
        }
    }

    
    
    @Override
    protected void onShowing(boolean showing) {
        trace.setActive(showing);
    }
    
}
