///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.demo.swing;
//
//import automenta.netention.graph.ValueEdge;
//import automenta.netention.impl.UnionGraph;
//import automenta.netention.demo.Demo;
//import automenta.netention.demo.Demo;
//import automenta.spacegraph.swing.SwingWindow;
//import automenta.spacegraph.Surface;
//import automenta.spacegraph.video.SGPanel;
//import automenta.spacegraph.math.linalg.Vec3f;
//import automenta.spacegraph.shape.Rect;
//import com.syncleus.dann.graph.DirectedEdge;
//import com.syncleus.dann.graph.rdf.MemoryRDFGraph;
//import com.syncleus.dann.graphicalmodel.bayesian.BayesianNode;
//import com.syncleus.dann.graphicalmodel.bayesian.MutableBayesianAdjacencyNetwork;
//import com.syncleus.dann.graphicalmodel.bayesian.SimpleBayesianEdge;
//import com.syncleus.dann.graphicalmodel.bayesian.SimpleBayesianNode;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.FlowLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.LinkedList;
//import java.util.List;
//import javax.media.opengl.GLAutoDrawable;
//import javax.swing.JButton;
//import javax.swing.JPanel;
//import javolution.context.ConcurrentContext;
//import org.openrdf.repository.Repository;
//import org.openrdf.repository.RepositoryConnection;
//import org.openrdf.repository.sail.SailRepository;
//import org.openrdf.rio.RDFFormat;
//import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
//import org.openrdf.sail.memory.MemoryStore;
//
///**
// *
// * @author seh
// */
//abstract public class RunGraphMix<N, E extends DirectedEdge<N>> extends Surface implements Demo {
//
////    float neuronDT = 0.02f;
////    private BayesNet belief;
////    private SimpleBayesianNode<Cause> cause1;
////    private SimpleBayesianNode<Cause> cause2;
////    private SimpleBayesianNode<Effect> effect1;
////    private SimpleBayesianNode<Effect> effect2;
////    private SimpleBayesianNode<Boolean> a;
////    private AbstractRealtimeBrain spiker;
////
////    static enum Cause {
////
////        A, B, C
////    }
////
////    static enum Effect {
////
////        D, E, F
////    }
////    private ArrayList<SimpleBayesianNode<Cause>> causes;
////    private ArrayList<SimpleBayesianNode<Effect>> effects;
////    private ArrayList<SimpleBayesianNode<Boolean>> b;
////    float spikerDT = 0.01f;
////
////    /**
////     * An example Bayesian Network, adapted directly from dANN's Unit Tests
////     * @author seh
////     */
////    public class BayesNet extends MutableBayesianAdjacencyNetwork {
////
//////        private static enum BooleanState {
//////
//////            TRUE, FALSE
//////        }
//////
//////        private static enum SeasonState {
//////
//////            WINTER, SUMMER, SPRING, FALL
//////        }
//////
//////        private static enum AgeState {
//////
//////            BABY, CHILD, TEENAGER, ADULT, SENIOR
//////        }
//////
//////        private static enum FeverState {
//////
//////            LOW, NONE, WARM, HOT
//////        }
//////        private static final Random RANDOM = new Random();
//////        //create nodes
//////        private BayesianNode<SeasonState> season = new SimpleBayesianNode<SeasonState>(SeasonState.WINTER, this);
//////        private BayesianNode<AgeState> age = new SimpleBayesianNode<AgeState>(AgeState.BABY, this);
//////        private BayesianNode<BooleanState> stuffyNose = new SimpleBayesianNode<BooleanState>(BooleanState.TRUE, this);
//////        private BayesianNode<FeverState> fever = new SimpleBayesianNode<FeverState>(FeverState.HOT, this);
//////        private BayesianNode<BooleanState> tired = new SimpleBayesianNode<BooleanState>(BooleanState.FALSE, this);
//////        private BayesianNode<BooleanState> sick = new SimpleBayesianNode<BooleanState>(BooleanState.FALSE, this);
////        public BayesNet() {
//////            MutableBayesianAdjacencyNetwork network = this;
//////
//////            //add nodes
//////            network.add(this.season);
//////            network.add(this.age);
//////            network.add(this.stuffyNose);
//////            network.add(this.fever);
//////            network.add(this.tired);
//////            network.add(this.sick);
//////
//////            {
//////                //others
////////            network.add(new SimpleBayesianNode<SeasonState>(SeasonState.FALL, this.network));
////////            network.add(new SimpleBayesianNode<SeasonState>(SeasonState.SPRING, this.network));
////////            network.add(new SimpleBayesianNode<SeasonState>(SeasonState.SUMMER, this.network));
//////            }
//////
//////            //connect nodes
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.season, this.stuffyNose));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.season, this.fever));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.season, this.tired));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.season, this.sick));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.age, this.stuffyNose));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.age, this.fever));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.age, this.tired));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.age, this.sick));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.tired, this.fever));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.tired, this.stuffyNose));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.tired, this.sick));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.stuffyNose, this.fever));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.stuffyNose, this.sick));
//////            network.add(new SimpleBayesianEdge<BayesianNode>(this.fever, this.sick));
//////
//////            //let the network learn
//////            for (int sampleCount = 0; sampleCount < 10; sampleCount++) {
//////                this.sampleState();
//////            }
//////            //lets check some probabilities
//////            final Set<BayesianNode> goals = new HashSet<BayesianNode>();
//////            goals.add(this.sick);
//////            final Set<BayesianNode> influences = new HashSet<BayesianNode>();
//////            influences.add(this.fever);
//////            sick.setState(BooleanState.TRUE);
//////            fever.setState(FeverState.LOW);
//////            final double lowPercentage = network.conditionalProbability(goals, influences);
//////            fever.setState(FeverState.NONE);
//////            final double nonePercentage = network.conditionalProbability(goals, influences);
//////            fever.setState(FeverState.WARM);
//////            final double warmPercentage = network.conditionalProbability(goals, influences);
//////            fever.setState(FeverState.HOT);
//////            final double hotPercentage = network.conditionalProbability(goals, influences);
//////
//////
//////            new GraphPanel(new JungGraph(network), 800, 600) {
//////            };
////        }
////
////        public void addAll(Collection<? extends BayesianNode> nodes) {
////            for (BayesianNode bn : nodes) {
////                add(bn);
////            }
////        }
////
////        private void sampleState() {
//////            final SeasonState seasonState = (SeasonState.values())[RANDOM.nextInt(SeasonState.values().length)];
//////            season.setState(seasonState);
//////
//////            final AgeState ageState = (AgeState.values())[RANDOM.nextInt(AgeState.values().length)];
//////            age.setState(ageState);
//////
//////            final BooleanState noseState = (BooleanState.values())[RANDOM.nextInt(BooleanState.values().length)];
//////            stuffyNose.setState(noseState);
//////
//////            final BooleanState tiredState = (BooleanState.values())[RANDOM.nextInt(BooleanState.values().length)];
//////            tired.setState(tiredState);
//////
//////
//////            fever.setState(FeverState.NONE);
//////            sick.setState(BooleanState.FALSE);
//////            learnStates();
//////            fever.setState(FeverState.NONE);
//////            sick.setState(BooleanState.FALSE);
//////            learnStates();
//////            fever.setState(FeverState.NONE);
//////            sick.setState(BooleanState.FALSE);
//////            learnStates();
//////            fever.setState(FeverState.NONE);
//////            sick.setState(BooleanState.FALSE);
//////            learnStates();
//////            fever.setState(FeverState.NONE);
//////            sick.setState(BooleanState.TRUE);
//////            learnStates();
//////
//////            fever.setState(FeverState.LOW);
//////            sick.setState(BooleanState.FALSE);
//////            learnStates();
//////            fever.setState(FeverState.LOW);
//////            sick.setState(BooleanState.FALSE);
//////            learnStates();
//////            fever.setState(FeverState.LOW);
//////            sick.setState(BooleanState.FALSE);
//////            learnStates();
//////            fever.setState(FeverState.LOW);
//////            sick.setState(BooleanState.TRUE);
//////            learnStates();
//////            fever.setState(FeverState.LOW);
//////            sick.setState(BooleanState.TRUE);
//////            learnStates();
//////
//////            fever.setState(FeverState.WARM);
//////            sick.setState(BooleanState.FALSE);
//////            learnStates();
//////            fever.setState(FeverState.WARM);
//////            sick.setState(BooleanState.FALSE);
//////            learnStates();
//////            fever.setState(FeverState.WARM);
//////            sick.setState(BooleanState.TRUE);
//////            learnStates();
//////            fever.setState(FeverState.WARM);
//////            sick.setState(BooleanState.TRUE);
//////            learnStates();
//////            fever.setState(FeverState.WARM);
//////            sick.setState(BooleanState.TRUE);
//////            learnStates();
//////
//////            fever.setState(FeverState.HOT);
//////            sick.setState(BooleanState.FALSE);
//////            learnStates();
//////            fever.setState(FeverState.HOT);
//////            sick.setState(BooleanState.TRUE);
//////            learnStates();
//////            fever.setState(FeverState.HOT);
//////            sick.setState(BooleanState.TRUE);
//////            learnStates();
//////            fever.setState(FeverState.HOT);
//////            sick.setState(BooleanState.TRUE);
//////            learnStates();
//////            fever.setState(FeverState.HOT);
//////            sick.setState(BooleanState.TRUE);
//////            learnStates();
////        }
////    }
////
////    public static void main(String[] args) {
////
////        SwingWindow sw = new SwingWindow(new RunGraphMix().newPanel(), 400, 400, true);
////
////    }
////
////    protected void updateBeliefs() {
////
////        {
////            cause1.setState(Math.random() > 0.3 ? Cause.A : Cause.B);
////            cause2.setState(Math.random() > 0.3 ? Cause.B : Cause.A);
////            a.setState(Math.random() > 0.5 ? true : false);
////            belief.learnStates();
////        }
////
////
////        {
////            spiker.update(spikerDT);
////            List<RealtimeNeuron> ll = new LinkedList(spiker.getNodes());
////            for (RealtimeNeuron rn : ll) {
////                rn.setActivation((2 * Math.random() - 1.0) * 10.0 + rn.getActivation());
////            }
////        }
////
////    }
////
////    public JPanel newPanel() {
////        ConcurrentContext.setConcurrency(Runtime.getRuntime().availableProcessors());
////
////
////        belief = new BayesNet();
////
////        a = new SimpleBayesianNode<Boolean>(true, belief);
////        belief.add(a);
////
////        cause1 = new SimpleBayesianNode<Cause>(Cause.A, belief);
////        cause2 = new SimpleBayesianNode<Cause>(Cause.B, belief);
////        belief.add(cause1);
////        belief.add(cause2);
////
////        effect1 = new SimpleBayesianNode<Effect>(Effect.D, belief);
////        effect2 = new SimpleBayesianNode<Effect>(Effect.E, belief);
////        belief.add(effect1);
////        belief.add(effect2);
////
////
////
////        belief.add(new SimpleBayesianEdge(cause1, effect1));
////        belief.add(new SimpleBayesianEdge(cause1, effect2));
////        belief.add(new SimpleBayesianEdge(cause2, effect1));
////        belief.add(new SimpleBayesianEdge(cause2, effect2));
////
////        belief.add(new SimpleBayesianEdge(cause1, a));
////        belief.add(new SimpleBayesianEdge(cause2, a));
////
////        belief.add(new SimpleBayesianEdge(a, effect1));
////        belief.add(new SimpleBayesianEdge(a, effect2));
////
////        int numDimensions = 2;
////
////        spiker = new AbstractRealtimeBrain();
////
////        final int numNeurons = 16;
////        //int numSynapses = 4;
////        {
////            for (int n = 0; n < numNeurons; n++) {
////                spiker.addNeuron(new IzhikevichNeuron());
////
////            }
////            List<RealtimeNeuron> n = new LinkedList(spiker.getNodes());
////            for (int a = 0; a < numNeurons; a++) {
////                for (int b = 0; b < numNeurons; b++) {
////                    if (a != b) {
////                        if (Math.random() < 0.5) {
////                            RealtimeNeuron s = n.get(a);
////                            RealtimeNeuron t = n.get(b);
////                            spiker.addSynapse(new ShortTermPlasticitySynapse(s, t, new JumpAndDecay(), 1.0));
////                        }
////                    }
////                }
////            }
////        }
////
////        MemoryRDFGraph rdf = new MemoryRDFGraph(8);
////        try {
////            Repository myRepository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
////            myRepository.initialize();
////
////            String rdfSource = "http://dbpedia.org/data/Semantic_Web.rdf";
////            RepositoryConnection con = myRepository.getConnection();
////
////            con.add(new URL(rdfSource), "", RDFFormat.RDFXML);
////            con.close();
////
////            rdf.add(myRepository);
////
////        } catch (Exception e) {
////            System.err.println(e);
////        }
////
////        UnionGraph ug = new UnionGraph(belief, spiker, rdf);
////        ug.add(new ValueEdge<Object, String>("x", spiker.getNodes().iterator().next(), a));
////        ug.add(new ValueEdge<Object, String>("y", rdf.getNodes().iterator().next(), a));
////
////        final GraphCanvas graphCanvas = new GraphCanvas(ug, numDimensions) {
////
////            float t = 0;
////
////            @Override
////            public void display(GLAutoDrawable g) {
////
////                updateBeliefs();
////
//////                for (Object o : edgeLines.keySet()) {
//////                    if (o instanceof BayesianEdge) {
//////                        BayesianEdge s = (BayesianEdge)o;
//////
//////                        Curve c = (Curve) edgeLines.get(s);
//////                        c.setLineWidth((float) Math.abs(s.getStrength()));
//////                        float cr = (float) (0.1f + Math.abs(s.getStrength()) / 2.0f);
//////                        float cg = cr;
//////                        float cb = cr;
//////                        c.setColor(cr, cg, cb);
//////                    }
//////                }
////
////                super.display(g);
////
////            }
////
////            @Override
////            public Rect newNodeRect(Object n) {
////                return super.newNodeRect(n);
////            }
////
////            @Override
////            protected void updateRect(Object n, Rect r) {
////                if (n instanceof BayesianNode) {
////                    BayesianNode sn = (BayesianNode) n;
////
////                    float size = Math.abs(((float) sn.stateProbability()));
////
////                    float nextSize = 0.9f * r.getSize().x() + 0.1f * size;
////                    r.getSize().set(nextSize, nextSize, nextSize);
////                    //System.out.println(r.getSize());
////                } else if (n instanceof RealtimeNeuron) {
////                    RealtimeNeuron sn = (RealtimeNeuron) n;
////
////                    float size = Math.abs(((float) sn.getActivation()) / 300.0f);
////
////                    float nextSize = 0.9f * r.getSize().x() + 0.1f * size;
////                    r.getSize().set(nextSize, nextSize, nextSize);
////                    //System.out.println(r.getSize());
////                } else {
////
////                    super.updateRect(n, r);
////                }
////            }
////        };
////
////        graphCanvas.setBackground(new Vec3f(0.5f, 0.5f, 0.5f));
////
////        SGPanel j = new SGPanel(graphCanvas);
////
////        JPanel panel = new JPanel(new BorderLayout());
////        panel.add(j, BorderLayout.CENTER);
////
////        JButton pb = new JButton("-");
////        pb.addActionListener(new ActionListener() {
////
////            @Override public void actionPerformed(ActionEvent e) {
////                double n = graphCanvas.hmap.getEquilibriumDistance() * 1.1;
////                graphCanvas.hmap.resetLearning();
////                graphCanvas.hmap.setEquilibriumDistance(n);
////            }
////        });
////        JButton mb = new JButton("+");
////        mb.addActionListener(new ActionListener() {
////
////            @Override public void actionPerformed(ActionEvent e) {
////                double n = graphCanvas.hmap.getEquilibriumDistance() * 0.9;
////                graphCanvas.hmap.resetLearning();
////                graphCanvas.hmap.setEquilibriumDistance(n);
////            }
////        });
////        graphCanvas.hmap.setEquilibriumDistance(0.5f);
////
////        JPanel px = new JPanel(new FlowLayout());
////        px.add(mb);
////        px.add(pb);
////
////        panel.add(px, BorderLayout.SOUTH);
////
////        return panel;
////    }
////
////    @Override
////    public String getName() {
////        return "Belief Network";
////    }
////
////    @Override
////    public String getDescription() {
////        return "..";
////    }
//
//}
