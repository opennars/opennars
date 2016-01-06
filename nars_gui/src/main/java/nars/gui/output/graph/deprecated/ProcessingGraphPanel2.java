//package nars.gui.output.graph.deprecated;
//
//import automenta.vivisect.swing.NPanel;
//import automenta.vivisect.swing.NSlider;
//import com.mxgraph.layout.mxCompactTreeLayout;
//import com.mxgraph.layout.mxFastOrganicLayout;
//import nars.nal.entity.Compound;
//import nars.nal.entity.Concept;
//import nars.nal.entity.Sentence;
//import nars.nal.entity.Term;
//import nars.util.graph.NARGraph;
//import org.jgrapht.ext.JGraphXAdapter;
//import org.jgrapht.graph.DirectedMultigraph;
//
//import javax.swing.*;
//import javax.swing.event.AncestorEvent;
//import javax.swing.event.AncestorListener;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//abstract public class ProcessingGraphPanel2<V,E> extends NPanel {
//
//    public PGraphPanel<V,E> app = null;
//    float edgeDistance = 10;
//    protected boolean showSyntax;
//
//    private final List<Object> items;
//    private int sentenceIndex = -1;
//    String layoutMode;
//    int maxItems = -1;
//
//    public ProcessingGraphPanel2() {
//        this(new ArrayList());
//    }
//
//    @Deprecated public ProcessingGraphPanel2(List<Object> sentences) {
//        super();
//
//        this.items = sentences;
//
//        app = new PGraphPanel<V,E>() {
//
//            @Override public int edgeColor(E edge) {
//                return ProcessingGraphPanel2.this.edgeColor(edge);
//            }
//
//            @Override public float edgeWeight(E edge) {
//                return ProcessingGraphPanel2.this.edgeWeight(edge);
//            }
//
//            @Override public int vertexColor(V vertex) {
//                return ProcessingGraphPanel2.this.vertexColor(vertex);
//            }
//        };
//
//
//        try {
//            app.init();
//        }
//        catch (Throwable t) { /* ignore this */ }
//
//
//
//        //this.setSize(1000, 860);//initial size of the window
//        this.setVisible(true);
//
//
//        setLayout(new BorderLayout());
//
//        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
//
//        final JComboBox layoutSelect = new JComboBox();
//        layoutSelect.addItem("Graph");
//        layoutSelect.addItem("Tree");
//        layoutSelect.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                layoutMode = layoutSelect.getSelectedItem().toString();
//                needLayout = true;
//                update();
//                redraw();
//            }
//        });
//        layoutMode = layoutSelect.getSelectedItem().toString();
//        menu.add(layoutSelect);
//
//        final JCheckBox beliefsEnable = new JCheckBox("Syntax");
//        beliefsEnable.addActionListener(new ActionListener() {
//            @Override public void actionPerformed(ActionEvent e) {
//                showSyntax = beliefsEnable.isSelected();
//                update();
//                redraw();
//            }
//        });
//        menu.add(beliefsEnable);
//
//        NSlider nodeSize = new NSlider(app.nodeSize, 1, app.maxNodeSize) {
//            @Override
//            public void onChange(float v) {
//                app.nodeSize = v;
//                redraw();
//            }
//        };
//        nodeSize.setPrefix("DDNode Size: ");
//        nodeSize.setPreferredSize(new Dimension(125, 25));
//        menu.add(nodeSize);
//
//        NSlider edgeDist = new NSlider(edgeDistance, 1, 100) {
//            @Override
//            public void onChange(float v) {
//                edgeDistance = v;
//                ProcessingGraphPanel2.this.update();
//            }
//        };
//        edgeDist.setPrefix("Separation: ");
//        edgeDist.setPreferredSize(new Dimension(125, 25));
//        menu.add(edgeDist);
//
//        /*
//        if (sentences.size() > 1) {
//            final JTextField ssl = new JTextField();
//            final JSlider indexSlider = new JSlider(-1, sentences.size()-1, -1);
//            indexSlider.setSnapToTicks(true);
//            indexSlider.setMajorTickSpacing(1);
//            indexSlider.setMinorTickSpacing(1);
//            indexSlider.addChangeListener(new ChangeListener() {
//                @Override
//                public void stateChanged(ChangeEvent e) {
//                    int i = indexSlider.getValue();
//                    sentenceIndex = i;
//                    if (i == -1) {
//                        update();
//                        ssl.setText("All Sentences");
//                    }
//                    else {
//                        update();
//                        ssl.setText(ProcessingGraphPanel.this.items.get(i).toString());
//                    }
//                }
//            });
//            menu.add(indexSlider);
//            menu.add(ssl);
//        }
//        */
//
//        if ((getWidth() > 0) && (getHeight() > 0))
//            app.setSize(getWidth(), getHeight());
//
//        add(menu, BorderLayout.NORTH);
//        add(app, BorderLayout.CENTER);
//
//        validate();
//
//        addAncestorListener(new AncestorListener() {
//
//            @Override
//            public void ancestorAdded(AncestorEvent event) {
//                redraw();
//            }
//
//            @Override
//            public void ancestorRemoved(AncestorEvent event) {
//            }
//
//            @Override
//            public void ancestorMoved(AncestorEvent event) {
//            }
//        });
//        addComponentListener(new ComponentAdapter() {
//
//
//            @Override
//            public void componentShown(ComponentEvent e) {
//                redraw();
//            }
//
//            @Override
//            public void componentResized(ComponentEvent e) {
//                SwingUtilities.invokeLater(new Runnable() {
//                    @Override public void run() {
//                        app.setSize(getWidth(), getHeight());
//                        redraw();
//                    }
//                });
//            }
//
//        });
//
//        update();
//        redraw();
//
//    }
//
//    abstract public int edgeColor(E edge);
//    abstract public float edgeWeight(E edge);
//    abstract public int vertexColor(V vertex);
//
//    @Override protected void visibility(boolean appearedOrDisappeared) {
//        if (!appearedOrDisappeared) {
//            //app.stop();
//            //app = null;
//        }
//        else {
//            //app = new papplet();
//        }
//    }
//
//
//    public void addItem(Object o) {
//        if ((maxItems > 0) && (items.size()+1 == maxItems)) {
//            items.remove(0);
//        }
//        items.add(o);
//        update();
//        redraw();
//    }
//
//
//    public NARGraph.Filter newSelectedGraphFilter() {
//
//        final List<Object> selected = getItems();
//
//        final Set<Term> include = new HashSet();
//        for (final Object s : selected) {
//            if (s instanceof Sentence) {
//                Term t = ((Sentence)s).term;
//                include.add(t);
//                if (t instanceof Compound) {
//                    Compound ct = (Compound)t;
//                    include.addAll(ct.getContainedTerms());
//                }
//            }
//        }
//
//        return new NARGraph.Filter() {
//
//            @Override
//            public boolean includePriority(float l) {  return true; }
//
//            @Override
//            public boolean includeConcept(final Concept c) {
//
//                final Term t = c.term;
//
//
//                return include.contains(t);
//            }
//
//        };
//    }
//
//
//    public List<Object> getItems() {
//        List<Object> displayed;
//        if (sentenceIndex == -1) {
//            displayed = items;
//        }
//        else {
//            displayed = new ArrayList(1);
//            displayed.add(items.get(sentenceIndex));
//        }
//        return displayed;
//    }
//
//    public void setMaxItems(int maxItems) {
//        this.maxItems = maxItems;
//    }
//
//    abstract public DirectedMultigraph getGraph();
//
//    public void update() {
//        app.graph = getGraph();
//
//
//
//        needLayout = true;
//
//    }
//
//    boolean needLayout = true;
//
//    public boolean redraw() {
//        if (app == null) return false;
//        if (app.graph == null) {
//            update();
//        }
//
//        app.drawn = false;
//
//
//        if (needLayout) {
//
//            needLayout = false;
//
//            // create a visualization using JGraph, via an adapter
//            JGraphXAdapter graphAdapter = new JGraphXAdapter(app.graph);
//            app.graphAdapter = graphAdapter;
//
//
//            /*
//             */
//            switch (layoutMode) {
//                case "Graph":
//                    mxFastOrganicLayout l = new mxFastOrganicLayout(graphAdapter);
//                    //new mxCompactTreeLayout(jgxAdapter);
//                    //new mxCircleLayout(jgxAdapter);
//                    l.setForceConstant(edgeDistance*10f);
//                    l.execute(graphAdapter.getDefaultParent());
//
//                    break;
//                case "Tree":
//                    mxCompactTreeLayout layout2 =  new mxCompactTreeLayout(graphAdapter);
//                    layout2.setUseBoundingBox(true);
//                    layout2.setResizeParent(true);
//                    layout2.setLevelDistance((int)(edgeDistance*1.5f));
//                    layout2.setNodeDistance((int)(0.2f * edgeDistance*edgeDistance*2f));
//                    layout2.setInvert(true);
//                    layout2.execute(graphAdapter.getDefaultParent());
//                    break;
//            }
//
//        }
//
//
//
//
//
//        /*
//        HyperOrganicLayout layout =
//                //new mxCompactTreeLayout(jgxAdapter);
//                new HyperOrganicLayout(jgxAdapter);
//                //new mxCircleLayout(jgxAdapter);
//        layout.setEdgeLengthCostFactor(0.001);*/
//
//        app.updating = false;
//
//        app.redraw();
//
//        return true;
//    }
//
// }
