///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.demo.swing;
//
//import automenta.netention.demo.Demo;
//import automenta.netention.demo.Demo;
//import automenta.spacegraph.Surface;
//import com.syncleus.dann.graph.DirectedEdge;
//
///**
// *
// * @author seh
// */
//abstract public class RunSpikingGraph<N, E extends DirectedEdge<N>> extends Surface implements Demo {
//
////    float neuronDT = 0.02f;
////
////    public static void main(String[] args) {
////
////        SwingWindow sw = new SwingWindow(new RunSpikingGraph().newPanel(), 400, 400, true);
////
////    }
////
////    public static class ActivationHistory {
////
////        public final Map<RealtimeNeuron, LinkedList<Double>> activation = new HashMap();
////        private final int historySize;
////
////        public ActivationHistory(int historySize) {
////            super();
////
////            this.historySize = historySize;
////        }
////
////        public void update(RealtimeBrain n) {
////            for (RealtimeNeuron rn : n.getNodes()) {
////                double a = rn.getActivation();
////                LinkedList<Double> ll = activation.get(rn);
////                if (ll == null) {
////                    ll = new LinkedList();
////                    activation.put(rn, ll);
////                } else {
////                    if (ll.size() > historySize) {
////                        ll.removeFirst();
////                    }
////                }
////                ll.addLast(a);
////            }
////
////            //TODO remove entries in activation that are not present in 'n'
////        }
////    }
////
////    public JPanel newPanel() {
////        ConcurrentContext.setConcurrency(Runtime.getRuntime().availableProcessors());
////
////        final AbstractRealtimeBrain spiker = new AbstractRealtimeBrain();
////        final ActivationHistory activationHistory = new ActivationHistory(120);
////
////        final int numNeurons = 156;
////        //int numSynapses = 4;
////        {
////            for (int n = 0; n < numNeurons; n++) {
////                spiker.addNeuron(new IzhikevichNeuron());
////            }
////            List<RealtimeNeuron> n = new LinkedList(spiker.getNodes());
////            for (int a = 0; a < numNeurons; a++) {
////                for (int b = 0; b < numNeurons; b++) {
////                    if (a != b) {
////                        if (Math.random() < 0.01) {
////                            RealtimeNeuron s = n.get(a);
////                            RealtimeNeuron t = n.get(b);
////                            spiker.addSynapse(new ShortTermPlasticitySynapse(s, t, new JumpAndDecay(), 1.0));
////                        }
////                    }
////                }
////            }
////        }
////
////
////        int numDimensions = 3;
////
////        System.out.println(spiker.getNodes().size() + " : " + spiker.getEdges().size());
////        final GraphCanvas<RealtimeNeuron, RealtimeSynapse> graphCanvas = new GraphCanvas<RealtimeNeuron, RealtimeSynapse>(spiker, numDimensions) {
////
////            float t = 0;
////
////            @Override
////            public void display(GLAutoDrawable g) {
////                for (RealtimeSynapse s : edgeLines.keySet()) {
////                    Curve c = edgeLines.get(s);
////                    c.setLineWidth((float) Math.abs(s.getStrength()));
////                    float cr = (float)(0.1f + Math.abs(s.getStrength())/2.0f);
////                    float cg = cr;
////                    float cb = cr;
////                    c.setColor(cr, cg, cb);
////                }
////
////                super.display(g);
////
////                spiker.update(neuronDT);
////                t += neuronDT;
////
////                activationHistory.update(spiker);
////                List<RealtimeNeuron> ll = new LinkedList(spiker.getNodes());
////                for (RealtimeNeuron rn : ll) {
////                    rn.setActivation((2 * Math.random() - 1.0) * 10.0 + rn.getActivation());
////                }
////                //RealtimeNeuron sn = spiker.getNodes().iterator().next();
////                //System.out.println(sn.getActivation());
////                //
////                //sn.setActivation(Math.cos(t) * 100.0);
////            }
////
////            @Override
////            public Rect newNodeRect(RealtimeNeuron n) {
////                if (n instanceof SpikingNeuron) {
////                    final SpikingNeuron sn = (SpikingNeuron) n;
////                    WideIcon i = new WideIcon("" /*bp.toString()*/, new Vec4f(Color.WHITE), new Vec4f(Color.WHITE)) {
////
////                        protected void fillRect(GL2 gl, float x1, float y1, float x2, float y2, float r, float g, float b) {
////                            final float w = Math.abs(x2 - x1);
////                            final float h = Math.abs(y2 - y1);
////                            final float dz = 0.1f;
////                            final float cx = (x1 + x2) / 2.0f;
////                            final float cy = (y1 + y2) / 2.0f;
////                            gl.glColor3f(r, g, b);
////                            gl.glBegin(GL2.GL_QUADS);
////                            {
////                                //Front
////                                //gl.glNormal3f(0, 0, 1); {
////                                gl.glVertex3f(cx + -w, cy + -h, dz);
////                                gl.glVertex3f(cx + w, cy + -h, dz);
////                                gl.glVertex3f(cx + w, cy + h, dz);
////                                gl.glVertex3f(cx + -w, cy + h, dz);
////                                //}
////                            }
////                            gl.glEnd();
////                        }
////
////                        @Override
////                        public void drawFront(GL2 gl) {
////                            LinkedList<Double> al = activationHistory.activation.get(sn);
////                            if (al != null) {
////                                int n = al.size();
////                                if (n > 0) {
////                                    float x = -1;
////                                    float dx = 2.0f / ((float) al.size());
////                                    for (int i = 0; i < n; i++) {
////                                        float fv = al.get(i).floatValue();
////                                        float h = Math.abs(fv) / 100.0f;
////
////                                        float r, g, b;
////                                        if (fv < 0) {
////                                            r = h;
////                                            g = 0.5f;
////                                            b = 0.5f;
////                                        }
////                                        else {
////                                            r = 0.5f;
////                                            b = h;
////                                            g = 0.5f;
////                                        }
////                                        fillRect(gl, x, -h / 2.0f, x + dx, h / 2.0f, r, g, b);
////                                        x += dx;
////                                    }
////                                }
////                            }
////                        }
////                    };
////                    float s = 1.0f;
////                    i.getSize().set(s * 2f, s, s);
////                    return i;
////                } else {
////                    return super.newNodeRect(n);
////                }
////            }
////
////            @Override
////            protected void updateRect(RealtimeNeuron n, Rect r) {
////                final Vec4f green = new Vec4f(Color.GREEN);
////                final Vec4f blue = new Vec4f(Color.BLACK);
////                if (n instanceof SpikingNeuron) {
////                    SpikingNeuron sn = (SpikingNeuron) n;
////                    float size = Math.abs(((float) sn.getActivation()) / 300.0f);
////                    r.setBackgroundColor(sn.getActivation() < 0 ? blue : green);
////                    r.getSize().set(size, size, size);
////                    //System.out.println(r.getSize());
////                } else {
////                    super.updateRect(n, r);
////                }
////            }
////
////            public float getBPSize(BusinessPerformance bp) {
////                float lowest = bp.getBusiness().getLow();
////                float highest = bp.getBusiness().getHigh();
////                float s = 0.05F + 0.25F * (bp.high - lowest) / (highest - lowest);
////                return s;
////            }
////
////            public Vec4f getBPColor(BusinessPerformance bp) {
////                float lowest = bp.getBusiness().getLow();
////                float highest = bp.getBusiness().getHigh();
////                float r = (bp.high - lowest) / (highest - lowest);
////                float g = 0.1F;
////                float b = 0.1F;
////                Vec4f v = new Vec4f(r, g, b, 1.0f);
////                return v;
////            }
////        };
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
////        return "Spiking Neural Network";
////    }
////
////    @Override
////    public String getDescription() {
////        return ".." /* izhicheck network intro */;
////    }
//
//}
