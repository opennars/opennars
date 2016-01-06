//package nars.gui.output;
//
//import automenta.vivisect.swing.NPanel;
//import nars.util.event.Reaction;
//
///**
// *
// * @author me
// */
//
//
//public class TimelinePanel extends NPanel implements Reaction<Class> {
//
//    @Override
//    protected void visibility(boolean appearedOrDisappeared) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public void event(Class event, Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
////    private List<AxisPlot> charts;
////    private final TimelineVis timeline;
////    private final JPanel controls;
////    private final NAR nar;
////    private final NARTrace trace;
////    private final PCanvas canvas;
////
////    public TimelinePanel(NAR n, NARTrace trace) {
////        super(new BorderLayout());
////
////        this.nar = n;
////        this.trace = trace;
////        trace.setActive(false);
////
////
////        charts = new CopyOnWriteArrayList();
////
////
////        this.timeline = new TimelineVis(charts);
////
////
////        this.controls = new JPanel();
////        controls.setLayout(new WrapLayout());
////
////        JCheckBox enableBox = new JCheckBox("Enable");
////        enableBox.setSelected(true); //TODO make functional
////        enableBox.setEnabled(false);
////        controls.add(enableBox);
////
////        add(canvas = new PCanvas(timeline), BorderLayout.CENTER);
////        add(controls, BorderLayout.NORTH);
////
////        canvas.setZoom(1.5f);
////        addChartControls();
////
////        n.on(CycleEnd.class, this);
////    }
////
////    abstract public class ChartButton extends JToggleButton {
////        private AxisPlot chart;
////
////        public ChartButton(String text) {
////            super(text);
////            addActionListener(new ActionListener() {
////                @Override public void actionPerformed(ActionEvent e) {
////                    if (isSelected())
////                        enableChart();
////                    else
////                        disableChart();
////                }
////            });
////        }
////
////        public void enableChart() {
////            chart = newChart();
////            chart.setOverlayEnable(true);
////            charts.add(chart);
////        }
////
////        public void disableChart() {
////            if (chart!=null) {
////                charts.remove(chart);
////                chart = null;
////            }
////        }
////
////        abstract public AxisPlot newChart();
////    }
////
////    protected void addChartControls() {
////        controls.add(new ChartButton("All Events") {
////            @Override public AxisPlot newChart() {
////                return new EventChart(trace, false, true, false).height(3);
////            }
////        });
////        controls.add(new ChartButton("Concept Priority (mean)") {
////            @Override public AxisPlot newChart() {
////                return new LineChart(trace.getCharts("concept.priority.mean")).height(1);
////            }
////        });
//////        controls.add(new ChartButton("Delta Concepts") {
//////            @Override public Chart newChart() {
//////                return new BarChart(new TreeMLData.FirstOrderDifferenceTimeSeries("d(concepts)", trace.charts.get("concept.count")));
//////            }
//////        });
////        controls.add(new ChartButton("Concept Priority Histogram (4 level)") {
////            @Override public AxisPlot newChart() {
////                return new StackedPercentageChart(trace.getCharts("concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3")).height(2);
////            }
////        });
////        controls.add(new ChartButton("Task: Novel Add, Immediate Processed") {
////            @Override public AxisPlot newChart() {
////                return new LineChart(trace.getCharts("task.novel.add", "task.immediate_processed")).height(3);
////            }
////        });
////        controls.add(new ChartButton("Task: Processed Goal, Question, Judgment") {
////            @Override public AxisPlot newChart() {
////                return new LineChart(trace.getCharts("task.goal.process", "task.question.process", "task.judgment.process")).height(3);
////            }
////        });
////        controls.add(new ChartButton("Emotion: Busy") {
////            @Override public AxisPlot newChart() {
////                return new LineChart(trace.getCharts("emotion.busy")).height(1);
////            }
////        });
////        controls.add(new ChartButton("Task: Executed") {
////            @Override public AxisPlot newChart() {
////                return new BarChart(trace.getCharts("task.executed")[0]).height(2);
////            }
////        });
////        controls.add(new ChartButton("Plan Graph Components") {
////            @Override public AxisPlot newChart() {
////                return new StackedPercentageChart(trace.getCharts("plan.graph.in.other.count", "plan.graph.in.operation.count", "plan.graph.in.interval.count")).height(3);
////            }
////        });
////        controls.add(new ChartButton("Plan Graph Interval Magnitude (mean)") {
////            @Override public AxisPlot newChart() {
////                return new LineChart(trace.getCharts("plan.graph.in.delay_magnitude.mean")).height(1);
////            }
////        });
////        controls.add(new ChartButton("Plan Graph Vertices & Edges") {
////            @Override public AxisPlot newChart() {
////                return new LineChart(trace.getCharts("plan.graph.edge.count", "plan.graph.vertex.count")).height(2);
////            }
////        });
////        controls.add(new ChartButton("Executable & Planned Tasks") {
////            @Override public AxisPlot newChart() {
////                return new StackedPercentageChart(trace.getCharts("plan.task.executable", "plan.task.planned")).height(2);
////            }
////        });
////
////
////
////
////
////
////    }
////
////    @Override
////    public void event(Class event, Object[] arguments) {
////        if (event == CycleEnd.class) {
////            timeline.updateNext();
////        }
////    }
////
////
////
////    @Override
////    protected void onShowing(boolean showing) {
////        trace.setActive(showing);
////    }
//
// }
