///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.gui.output.graph;
//
///**
// *
// * @author me
// */
//
//import automenta.vivisect.dimensionalize.FastOrganicLayout;
//import automenta.vivisect.graph.AnimatingGraphVis;
//import automenta.vivisect.graph.GraphDisplay;
//import automenta.vivisect.graph.GraphDisplays;
//import automenta.vivisect.swing.NSlider;
//import nars.Events.FrameEnd;
//import nars.Events.Restart;
//import nars.NAR;
//import nars.event.EventEmitter;
//import nars.event.Reaction;
//import nars.gui.output.graph.layout.HashPriorityPolarLayout;
//import nars.util.graph.*;
//import org.jgrapht.Graph;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// *
// */
//abstract public class NARGraphVis extends AnimatingGraphVis<Object,Object> implements Reaction {
//
//
//    final AtomicReference<Graph> displayedGraph = new AtomicReference();
//    public final NAR nar;
//
//    private final GraphDisplays displays;
//    private GraphDisplay style;
//    private GraphDisplay layout;
//    private JPanel modePanelHolder;
//
//    float updateCycleMS = 70;
//    long lastUpdateMS = -1;
//    private EventEmitter.Registrations reg = null;
//
//
//    public static interface GraphMode {
//        public Graph nextGraph();
//        default public void stop() {
//        }
//
//        public JPanel newControlPanel();
//    }
//
//    public abstract class MinPriorityGraphMode implements GraphMode {
//        float minPriority = 0;
//
//        @Override
//        public JPanel newControlPanel() {
//            JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT));
//
//            NSlider maxLevels = new NSlider(1, 0, 1) {
//                @Override
//                public void onChange(float v) {
//                    minPriority = (float) (1.0 - v);
//                    setUpdateNext();
//                }
//            };
//            maxLevels.setPrefix("Min Level: ");
//            maxLevels.setPreferredSize(new Dimension(80, 25));
//            j.add(maxLevels);
//            return j;
//        }
//
//
//    }
//
//    public class ConceptGraphMode extends MinPriorityGraphMode implements GraphMode {
//        private boolean showBeliefs = false;
//        private boolean showQuestions = false;
//        private boolean showTermContent = false;
//
//        boolean showTaskLinks = false;
//        boolean showTermLinks = true;
//
//        @Override
//        public Graph nextGraph() {
//            return new NARGraph().add(nar, new NARGraph.ExcludeBelowPriority(minPriority), new DefaultGrapher(showBeliefs, showBeliefs, showQuestions, showTermContent, 0, showTermLinks, showTaskLinks));
//        }
//
//        @Override
//        public JPanel newControlPanel() {
//            JPanel j = super.newControlPanel();
//
//            final JCheckBox termlinkEnable = new JCheckBox("TermLinks");
//            termlinkEnable.setSelected(showTermLinks);
//            termlinkEnable.addActionListener(new ActionListener() {
//                @Override public void actionPerformed(ActionEvent e) {
//                    showTermLinks = (termlinkEnable.isSelected());
//                    setUpdateNext();
//                }
//            });
//            j.add(termlinkEnable);
//
//            final JCheckBox taskLinkEnable = new JCheckBox("TaskLinks");
//            taskLinkEnable.setSelected(showTaskLinks);
//            taskLinkEnable.addActionListener(new ActionListener() {
//                @Override public void actionPerformed(ActionEvent e) {
//                    showTaskLinks = (taskLinkEnable.isSelected());
//                    setUpdateNext();
//                }
//            });
//            j.add(taskLinkEnable);
//
//            final JCheckBox beliefsEnable = new JCheckBox("Beliefs");
//            beliefsEnable.setSelected(showBeliefs);
//            beliefsEnable.addActionListener(new ActionListener() {
//                @Override public void actionPerformed(ActionEvent e) {
//                    showBeliefs = (beliefsEnable.isSelected());
//                    setUpdateNext();
//                }
//            });
//            j.add(beliefsEnable);
//
//            return j;
//
//        }
//
//    }
//
//    public class TaskGraphMode implements GraphMode {
//
//        public final TaskGraph taskGraph;
//
//        public TaskGraphMode() {
//            taskGraph = new TaskGraph(nar);
//        }
//
//        @Override
//        public Graph nextGraph() {
//            return taskGraph.get();
//
//        }
//
//        @Override
//        public JPanel newControlPanel() {
//            JPanel j = new JPanel();
//            return j;
//        }
//
//        @Override
//        public void stop() {
//            taskGraph.stop();
//        }
//    }
//
//    abstract public class StatementGraphMode extends MinPriorityGraphMode implements GraphMode {
//        private StatementGraph ig;
//
//        abstract StatementGraph newGraph(NAR nar);
//
//        @Override
//        public Graph nextGraph() {
//            if (this.ig==null) {
//                this.ig = newGraph(nar);
//                if (ig!=null)
//                    ig.start();
//            }
//
//            return ig;
//        }
//
//        @Override
//        public void stop() {
//            if (ig!=null) {
//                ig.stop();
//                ig = null;
//            }
//        }
//
//    }
//
//    public class InheritanceGraphMode extends StatementGraphMode {
//
//        @Override
//        StatementGraph newGraph(NAR nar) {
//            return new InheritanceGraph(nar, true, true);
//        }
//    }
//    public class ImplicationGraphMode extends StatementGraphMode {
//
//        @Override
//        StatementGraph newGraph(NAR nar) {
//            return new ImplicationGraph2(nar);
//        }
//    }
//
//
//    public GraphMode mode;
//
//    boolean updateNextGraph = false;
//
//    public NARGraphVis(NAR n) {
//        this(n, new NARGraphDisplay(n), new FastOrganicLayout());
//    }
//
//    public NARGraphVis(NAR n, GraphDisplay style, GraphDisplay layout) {
//        super(null, new GraphDisplays());
//        this.nar = n;
//        this.displays = (GraphDisplays)getDisplay();
//
//        this.mode = getInitialMode();
//
//        if (style != null)
//            update(style, layout);
//    }
//
//    abstract public GraphMode getInitialMode();
//
//    @Deprecated public void update(GraphDisplay style, GraphDisplay layout) {
//        this.style = style;
//        this.layout = layout;
//        displays.sequence.clear();
//        displays.sequence.add(style);
//        displays.sequence.add(layout);
//        setUpdateNext();
//    }
//    public void update(GraphDisplay... d) {
//        displays.sequence.clear();
//
//        for (GraphDisplay gd : d)
//            displays.sequence.add(gd);
//        setUpdateNext();
//    }
//
//    @Override
//    public void onVisible(boolean showing) {
//        if (showing) {
//            if (reg == null)
//                reg = nar.memory.event.on(this, FrameEnd.class, Restart.class);
//        }
//        else {
//            if (reg != null) {
//                reg.cancel();
//                mode.stop();
//                reg = null;
//            }
//        }
//    }
//
//    @Override
//    public void event(Class event, Object[] args) {
//        if (event == FrameEnd.class) {
//
//            long now = System.currentTimeMillis();
//
//            if (now - lastUpdateMS > updateCycleMS) {
//
//                displayedGraph.set(nextGraph());
//
//                lastUpdateMS = now;
//            }
//
//        }
//        else if (event == Restart.class) {
//            displayedGraph.set(null);
//        }
//    }
//
//
//    protected Graph nextGraph() {
//        if (nar == null) return null;
//        return mode.nextGraph();
//    }
//
//    @Override
//    public void setUpdateNext() {
//        super.setUpdateNext();
//        updateNextGraph = true;
//    }
//
//
//
//    @Override
//    public Graph<Object, Object> getGraph() {
//        if (displayedGraph == null)
//            return null;
//
//
//        if (updateNextGraph) {
//            updateNextGraph = false;
//
//            if (!nar.isRunning()) {
//                //only update from here if NAR isnt running; otherwise a concurrency exception can occurr
//
//                Graph ng = nextGraph();
//                if (ng!=null)
//                    displayedGraph.set(ng);
//            }
//        }
//
//        return displayedGraph.get();
//    }
//
//
//
//    public JPanel newLayoutPanel() {
//        JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        final JComboBox layoutSelect = new JComboBox();
//        layoutSelect.addItem("Organic");
//        layoutSelect.addItem("Circle Fixed");
//        layoutSelect.addItem("Circle Fixed (Half)");
//        layoutSelect.addItem("Hyperassociative");
//        //layoutSelect.addItem("GridSort");
//        //layoutSelect.addItem("Circle Anim");
//        //layoutSelect.addItem("Grid");
//
//        //modeSelect.setSelectedIndex(cg.mode);
//        layoutSelect.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                switch (layoutSelect.getSelectedIndex()) {
//                    case 0:
//                        update(style, new FastOrganicLayout());
//                        break;
//                    case 1:
//                        update(style, new HashPriorityPolarLayout(0f, 1f, 50));
//                        break;
//                    case 2:
//                        update(style, new HashPriorityPolarLayout(0.25f, 0.75f, 75));
//                        break;
//                    case 3:
//                        //update(style, new HyperassociativeLayout());
//                        break;
//
//                }
////cg.mode = modeSelect.getSelectedIndex();
//                setUpdateNext();
//            }
//        });
//        j.add(layoutSelect);
//        return j;
//    }
//
//    public void setMode(GraphMode g) {
//        if (this.mode!=null) {
//            this.mode.stop(); //stop existing
//        }
//
//        this.mode = g;
//
//        modePanelHolder.removeAll();
//        modePanelHolder.add(mode.newControlPanel());
//        modePanelHolder.doLayout();
//    }
//
//    public JPanel newGraphPanel() {
//        JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        final JComboBox modeSel = new JComboBox();
//        modeSel.addItem("Tasks");
//        modeSel.addItem("Concepts");
//        modeSel.addItem("Inheritance");
//        modeSel.addItem("Implication");
//
//        //modeSelect.setSelectedIndex(cg.mode);
//        modeSel.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                switch (modeSel.getSelectedIndex()) {
//                    case 0:
//                        setMode(new TaskGraphMode());
//                        break;
//                    case 1:
//                        setMode(new ConceptGraphMode());
//                        break;
//                    case 2:
//                        setMode(new InheritanceGraphMode());
//                        break;
//                    case 3:
//                        setMode(new ImplicationGraphMode());
//                        break;
//                }
//                setUpdateNext();
//            }
//        });
//
//        j.add(modeSel);
//
//        modePanelHolder = new JPanel(new FlowLayout());
//        modePanelHolder.add(mode.newControlPanel());
//
//        j.add(modePanelHolder);
//        return j;
//    }
//
//    public JPanel newStylePanel() {
//        JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        if (style instanceof NARGraphDisplay)
//            j.add( ((NARGraphDisplay)style).getControls());
//        return j;
//    }
//
//
//
//
//
// }
