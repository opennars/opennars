//package nars.rl.example;
//
//import jurls.reinforcementlearning.domains.RLEnvironment;
//import jurls.reinforcementlearning.domains.wander.Curiousbot;
//import nars.Global;
//import nars.NAR;
//import nars.NARSeed;
//import nars.Video;
//import nars.concept.BeliefTable;
//import nars.gui.NARSwing;
//import nars.nar.Default;
//import nars.rl.AEPerception;
//import nars.rl.Perception;
//import nars.rl.QLAgent;
//import nars.rl.RawPerception;
//
//import javax.swing.*;
//import java.awt.*;
//
///**
// * TODO add parameters determining what sensor input is exposed to NARS
// * --how many SOM nodes (# closest), confidence inverse proportional to distance
// * --raw input?  if so, allow segmentation (ex: columns size to form products for each row)
// * TODO measure action coherence as % similarlity of chosen execution to rejected executions
// * TODO use concept desire in accumulating votes\
// * TODO abstract the voting execution into an abstract Operator
// */
//public class RunQLAgent extends JPanel {
//
//
//    private final QLAgent agent;
//
//
//    //    /** growing neural gas */
////    public static class GNGPerception implements Perception {
////
////        private NeuralGasNet som = null;
////        private final int nodes;
////        private QLAgent agent;
////
////        public GNGPerception(int nodes) {
////            this.nodes = nodes;
////        }
////
////        @Override
////        public int init(RLEnvironment env, QLAgent agent) {
////
////            this.agent = agent;
////
////            som = new NeuralGasNet(env.inputDimension(), nodes);
////            return nodes;
////        }
////
////
////        @Override
////        public void perceive(double[] input, double t) {
////            Node closest = som.learn(input);
////            double d = closest.getLocalDistance();
////            float conf = (float) (1.0f / (1.0f + d)); //TODO normalize against input mag?
////
////            //perception input
////
////            //System.out.println(closest.id + "<" + Texts.n4(closest.getLocalError()) + ">: " + Arrays.toString(input) + " -> " + Arrays.toString(closest.getDataRef()));
////
////            agent.learn(closest.id, reward, conf);
////        }
////
////        /*
////        public void newVisWindow() {
////
////        new GraphPanelNengo<Node,Connection>(ql.som) {
////
////            @Override
////            public Color getEdgeColor(UIEdge<? extends UIVertex> v) {
////
////                Node a = (Node)v.getSource().vertex;
////                Node b = (Node)v.getTarget().vertex;
////                float dist = (float)a.getDistance(b);
////                float o = 1f / (1f + dist);
////                return new Color(o, 0.25f, 1f - o, 0.25f + 0.75f * o);
////            }
////
////        }.newWindow(800, 600);
////
////        }*/
////    }
////
//
//    //DBSCAN?
//    //...
//    //Embedded NAR?
//
//
//    public final NAR nar;
//
//
//    public RunQLAgent(RLEnvironment env, NARSeed dd, Perception... p) {
//        super();
//
//        nar = new NAR(dd);
//
//        agent = new QLAgent(nar, "O", "<n --> [g]>", env, p) {
//
//            final QVis qvis = new QVis(this) {
//
//                @Override
//                public void frame() {
//                    super.frame();
//                }
//
//                @Override
//                public void run() {
//                    super.run();
//                    env.component().repaint();
//                }
//            };
//
//
//            @Override
//            public void onFrame() {
//                super.onFrame();
//
//                qvis.frame();
//            }
//
//        };
//
//
//
//
//
//        Video.themeInvert();
//        NARSwing s = new NARSwing(nar);
//
//        new Frame().setVisible(true);
//        this.setIgnoreRepaint(true);
//
//
//
//    }
//
//
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String args[]) {
//
//
//        /* Create and display the form */
//        //RLEnvironment d = new PoleBalancing2D();
//        //RLEnvironment d = new Follow1D();
//        //RLEnvironment d = new Follow1DTwoPoint();
//        //RLEnvironment d = new Follow1DThreePoint(0.02, 0.1);
//
//        RLEnvironment d = new Curiousbot();
//
//        //RLEnvironment d = new Tetris(10, 14);
//        //RLEnvironment d = new Tetris(10, 8);
//
//        d.newWindow();
//
//        Global.DEBUG = false;
//        //Global.TRUTH_EPSILON = 0.01f;
//        //Global.BUDGET_EPSILON = 0.02f;
//
//        int concepts = 1024;
//        int conceptsPerCycle = 64;
//        final int cyclesPerFrame = 5;
//
//
//        //Solid dd = new Solid(100, concepts, 1, 1, 1, 8);
//
//        Default dd = new Default(concepts, conceptsPerCycle, 4) {
//
////            @Override
////            public Memory.DerivationProcessor getDerivationProcessor() {
////                //return new Memory.ConstantLeakyDerivations(0.95f, 0.95f);
////                return new Memory.DerivationProcessor() {
////
////                    @Override
////                    public boolean process(Task derived) {
////                        float amp = 1f;
////
////                        switch (derived.getPunctuation()) {
////                            case '?': amp = 0.6f; break;
////                            case '@': amp = 0.6f; break;
////
////                            case '!': amp = 0.75f; break;
////
////                            case '.': amp = 0.4f; break;
////                        }
////                        derived.setPriority(derived.getPriority() * amp);
////                        return true;
////                    }
////                };
////            }
//
////            /** ranks beliefs by recency. the relevance decays proportional to delta time from now divided by window length (in cycles) */
////            public float rankBeliefRecent(final Sentence s, final long now, final float window, final float eternalWindow) {
////                final float confidence = s.truth.getConfidence();
////
////                //final float originality = s.stamp.getOriginality();
////                final float w, when;
////                if (s.isEternal()) {
////                    w = eternalWindow;
////                    when = s.getCreationTime();
////                }
////                else {
////                    w = window;
////                    when = s.getOccurrenceTime();
////                }
////                float timeRelevance = 1f / (1f+ Math.abs(now - when)/w);
////                //return or(confidence, Math.min(originality, timeRelevance));
////                return or(confidence, timeRelevance);
////            }
//
//
////            @Override
////            protected Concept newConcept(Term t, Budget b, Bag<Sentence, TaskLink> taskLinks, Bag<TermLinkKey, TermLink> termLinks, Memory m) {
////                return new DefaultConcept(t, b, taskLinks, termLinks, m) {
//////                    @Override
//////                    public float rankBelief(Sentence s, long now) {
//////                        return rankBeliefRecent(s, now, cyclesPerFrame * 200, cyclesPerFrame * 1000);
//////                    }
////                };
////            }
//
//
//            @Override
//            public BeliefTable.RankBuilder newConceptBeliefGoalRanking() {
//                return (c, b) -> {
//                    return new BeliefTable.BeliefConfidenceAndCurrentTime(c);
//                };
//            }
//        };
//
//
//
//        dd.setInternalExperience(null);
//
//        dd.inputsMaxPerCycle.set(10);
//
//
//        dd.setCyclesPerFrame(cyclesPerFrame);
//        dd.conceptForgetDurations.set(2f * 1f);
//        //dd.duration.set(3 * cyclesPerFrame);         //nar.param.duration.setLinear
//        dd.duration.set(5);
//        dd.shortTermMemoryHistory.set(3);
//        dd.executionThreshold.set(0);
//        dd.outputVolume.set(5);
//
//        RunQLAgent a = new RunQLAgent(d, dd,
//                new RawPerception("L", 0.6f)
//                //new RawPerception.BipolarDirectPerception("L", 0.1f)
//
//                ,new AEPerception("A", 0.5f, 16, 1).setLearningRate(0.05).setSigmoid(false)
//                //new AEPerception("B", 0.2f, 8, 1).setLearningRate(0.02).setSigmoid(false)
//
//                /*new RawPerception("P", 0.8f) {
//                    @Override
//                    public float getFrequency(double d) {
//                        // pos or negative binary
//                        if (d > 0) return 1;
//                        return 0;
//                    }
//                },*/
//                //,new HaiSOMPerception("B", 2, 0.2f)
//        );
//
//        a.agent.setQLFactor(0.5f, 0.5f);
//        a.agent.setInputGain(1.0f);
//
//        a.agent.ql.brain.setEpsilon(0.12);
//
//
//
//    }
//
//
//
//}