/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output.graph;

/**
 *
 * @author me
 */

import automenta.vivisect.dimensionalize.FastOrganicLayout;
import automenta.vivisect.dimensionalize.HyperassociativeLayout;
import automenta.vivisect.graph.AnimatingGraphVis;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.GraphDisplays;
import automenta.vivisect.swing.NSlider;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import nars.io.events.EventEmitter.EventObserver;
import nars.io.events.Events.CyclesEnd;
import nars.io.events.Events.ResetEnd;
import nars.main.NAR;
import nars.gui.output.graph.layout.CircleLayout;
import nars.gui.util.DefaultGraphizer;
import nars.gui.util.NARGraph;
import nars.gui.graph.InheritanceGraph;
import nars.gui.graph.ImplicationGraph;
import nars.gui.output.graph.layout.SpiralLayout;
import nars.language.Interval.PortableDouble;
import org.jgrapht.Graph;

/**
 *
 */
public class NARGraphVis extends AnimatingGraphVis<Object,Object> implements EventObserver {
        
    
    final AtomicReference<Graph> displayedGraph = new AtomicReference();
    private final NAR nar;
    
    private final GraphDisplays displays;
    private NARGraphDisplay style;
    private GraphDisplay layout;
    private JPanel modePanelHolder;
    
    public static interface GraphMode {
        public Graph nextGraph();
        default public void stop() {
        }
        
        public JPanel newControlPanel();        
    }

    public abstract class MinPriorityGraphMode implements GraphMode {
        float minPriority = 0;

        @Override
        public JPanel newControlPanel() {
            JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT));

            /*NSlider maxLevels = new NSlider(1, 0, 1) {
                @Override
                public void onChange(float v) {
                    minPriority = (float) (1.0 - v);
                    setUpdateNext();
                }
            };
            maxLevels.setPrefix("Min Level: ");
            maxLevels.setPreferredSize(new Dimension(80, 25));*/
            //j.add(conceptPriSlider);  
            j.add(conceptPriSlider);
            return j;
        }
        
        
    }
    
    public final PortableDouble conceptPriorityThreshold = new PortableDouble(0.0);
    public final PortableDouble taskPriorityThreshold = new PortableDouble(0.1);
    public final PortableDouble nConcepts = new PortableDouble(0.004); //10000*0.004=40
    JTextField filterBox = new JTextField();
    NSlider conceptPriSlider = new NSlider(conceptPriorityThreshold, "ConcP", 0.0f, 1.0f);
    NSlider taskPriSlider = new NSlider(taskPriorityThreshold, "TaskP", 0.0f, 1.0f);
    NSlider nConceptsSlider = new NSlider(nConcepts, "number of Concepts: The maximum number of concepts (long slider for a good accuracy)", 0.0f, 1.0f);
    public class ConceptGraphMode extends MinPriorityGraphMode implements GraphMode {
        private boolean showBeliefs = false;    
        private boolean showQuestions = false;
        private boolean showTermContent = false;
        
        boolean showTaskLinks = false;
        boolean showTermLinks = true;

        @Override
        public Graph nextGraph() {
            return new NARGraph().add(nar, new NARGraph.ExcludeBelowPriority(minPriority), 
                    new DefaultGraphizer(showBeliefs, showBeliefs, showQuestions, showTermContent, 
                            0, showTermLinks, showTaskLinks, filterBox, conceptPriorityThreshold, taskPriorityThreshold, nConcepts));
        }

        @Override
        public JPanel newControlPanel() {
            JPanel j = super.newControlPanel();

            final JCheckBox termlinkEnable = new JCheckBox("TermLinks");
            termlinkEnable.setSelected(showTermLinks);
            termlinkEnable.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    showTermLinks = (termlinkEnable.isSelected());                
                    setUpdateNext();
                }
            });
            j.add(termlinkEnable);        

            final JCheckBox taskLinkEnable = new JCheckBox("TaskLinks");
            taskLinkEnable.setSelected(showTaskLinks);
            taskLinkEnable.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    showTaskLinks = (taskLinkEnable.isSelected());                
                    setUpdateNext();
                }
            });
            j.add(taskLinkEnable);

           /* final JCheckBox beliefsEnable = new JCheckBox("Beliefs");
            beliefsEnable.setSelected(showBeliefs);
            beliefsEnable.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    showBeliefs = (beliefsEnable.isSelected());
                    setUpdateNext();
                }
            });
            j.add(beliefsEnable);*/
            
            filterBox.setPreferredSize(new Dimension(100,20));
            j.add(filterBox);
            j.add(conceptPriSlider);
            j.add(taskPriSlider);
            j.add(nConceptsSlider);

            return j;
            
        }
        
    }
    public class InheritanceGraphMode extends MinPriorityGraphMode implements GraphMode {
        private InheritanceGraph ig;

        @Override
        public Graph nextGraph() {
            if (this.ig==null) {
                this.ig = new InheritanceGraph(nar, true, true, conceptPriorityThreshold);
                ig.start();
            }
            
            return ig;
        }        

        @Override
        public void stop() {
            if (ig!=null) {
                ig.stop();
                ig = null;
            }
        }
        
    }

    public class ImplicationGraphMode extends MinPriorityGraphMode implements GraphMode {
        private ImplicationGraph ig;

        @Override
        public Graph nextGraph() {
            if (this.ig==null) {
                this.ig = new ImplicationGraph(nar, true, true, conceptPriorityThreshold);
                ig.start();
            }
            
            return ig;
        }        

        @Override
        public void stop() {
            if (ig!=null) {
                ig.stop();
                ig = null;
            }
        }
        
    }
    
    public GraphMode mode = new ConceptGraphMode();
    
    boolean updateNextGraph = false;
            
    public NARGraphVis(NAR n) {
        super(null, new GraphDisplays());
        this.nar = n;
        this.displays = (GraphDisplays)getDisplay();
        NARGraphDisplay grap = new NARGraphDisplay();
        update(grap, new FastOrganicLayout());
    }
    
    public void update(NARGraphDisplay style, GraphDisplay layout) {
        this.style = style;
        this.layout = layout;
        displays.sequence.clear();
        displays.sequence.add(style);
        displays.sequence.add(layout);
        setUpdateNext();
    }

    @Override
    public void onVisible(boolean showing) {  
        nar.memory.event.set(this, showing, CyclesEnd.class, ResetEnd.class);        
        if (!showing) {
            mode.stop();
        }
    }

    @Override
    public void event(Class event, Object[] args) {
        if (event == CyclesEnd.class) {
            displayedGraph.set(nextGraph());
        }
        else if (event == ResetEnd.class) {
            displayedGraph.set(null);
        }
    }
        
            
    protected Graph nextGraph() {
        if (nar == null) return null;
        return mode.nextGraph();                
    }

    @Override
    public void setUpdateNext() {
        super.setUpdateNext();        
        updateNextGraph = true;
    }

    
    
    @Override
    public Graph<Object, Object> getGraph() {        
        if (displayedGraph == null)
            return null;
        
        
        if (updateNextGraph) {
            updateNextGraph = false;

            if (!nar.isRunning()) {
                //only update from here if NAR isnt running; otherwise a concurrency exception can occurr

                Graph ng = nextGraph();
                if (ng!=null)
                    displayedGraph.set(ng);            
            }
        }
        
        return displayedGraph.get();
    }
    

    
    public JPanel newLayoutPanel() {
        JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JComboBox layoutSelect = new JComboBox();
        layoutSelect.addItem("Organic");
        layoutSelect.addItem("Hyperassociative");
        layoutSelect.addItem("Circle");       
        layoutSelect.addItem("Circle (Half)");
        layoutSelect.addItem("Spiral");
        //layoutSelect.addItem("GridSort");
        //layoutSelect.addItem("Circle Anim");
        //layoutSelect.addItem("Grid");
        
        //modeSelect.setSelectedIndex(cg.mode);
        layoutSelect.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                switch (layoutSelect.getSelectedIndex()) {
                    case 0:
                        update(style, new FastOrganicLayout());     
                        break;
                    case 1:
                        update(style, new HyperassociativeLayout());
                        break;
                    case 2:
                        update(style, new CircleLayout(nar, 0f, 1f, 50));     
                        break;
                    case 3:
                        update(style, new CircleLayout(nar, 0.25f, 0.75f, 75));     
                        break;
                    case 4:
                        update(style, new SpiralLayout(nar, 75));     
                        break;
                }
//cg.mode = modeSelect.getSelectedIndex();
                setUpdateNext();
            }
        });
        j.add(layoutSelect);
        return j;
    }
    
    public void setMode(GraphMode g) {
        if (this.mode!=null) {
            this.mode.stop(); //stop existing
        }
        
        this.mode = g;
        
        modePanelHolder.removeAll();
        modePanelHolder.add(mode.newControlPanel());
        modePanelHolder.doLayout();
    }
    
    public JPanel newGraphPanel() {
        JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JComboBox modeSel = new JComboBox();
        modeSel.addItem("Concepts");
        modeSel.addItem("Inheritance");       
        modeSel.addItem("Implication");  
        //modeSelect.setSelectedIndex(cg.mode);
        modeSel.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                switch (modeSel.getSelectedIndex()) {
                    case 0:
                        setMode(new ConceptGraphMode());
                        break;
                    case 1:
                        setMode(new InheritanceGraphMode());
                        break;
                    case 2:
                        setMode(new ImplicationGraphMode());
                        break;

                }
                setUpdateNext();
            }
        });
        
        j.add(modeSel);
        
        modePanelHolder = new JPanel(new FlowLayout());
        modePanelHolder.add(mode.newControlPanel());
        
        j.add(modePanelHolder);
        return j;
    }
    
    public JPanel newStylePanel() {
        JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT));
        j.add(style.getControls());
        return j;
    }

    
    
    
    
}
