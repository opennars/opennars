package nars.gui.output.graph.deprecated;

//package nars.gui.output.graph;
//
//
//
//import automenta.vivisect.graph.ProcessingGraphCanvas;
//import java.other.Comparator;
//import nars.core.NAR;
//import nars.logic.entity.Concept;
//import nars.logic.entity.Sentence;
//import nars.logic.entity.Task;
//import nars.gui.NARSwing;
//import nars.logic.entity.Term;
//import nars.other.DefaultGraphizer;
//import nars.other.NARGraph;
//import nars.other.sorted.IndexedTreeSet;
//
//
//
//public class ConceptGraphCanvas extends ProcessingGraphCanvas {
//
//    public int mode = 2;
//
//    boolean showBeliefs = false;
//    boolean showTermlinks = false;
//    boolean showTasklinks = false;
//    float spacing = 100f;
//    boolean compressLevels = true;
//
//    float minPriority = 0;
//
//    long lasttime = -1;
//    private final NAR nar;
//
//    //ty = -((((Bag<Concept>)nar.memory.concepts).levels - level) * maxNodeSize * 3.5f);
//    //double radius = ((((Bag<Concept>)nar.memory.concepts).levels - level)+8);
//    //TEMPORARY
//    //gridsort
//    //terms equal to concept, ordinarily displayed as subsequent nodes
//    //should just appear at the same position as the concept
//    //lastTermVertex.visible = false;
//    public ConceptGraphCanvas(NAR nar) {
//        super(new NARGraphDisplay());        
//        this.nar = nar;        
//    }
//    
//    public void position(VertexDisplay v, float level, float index, float priority) {
//        
//        if (mode == 3) {
//            v.tx = ((float) Math.sin(index / 10d) * spacing) * 5 * ((10 + index) / 20);
//            //ty = -((((Bag<Concept>)nar.memory.concepts).levels - level) * maxNodeSize * 3.5f);
//            v.ty = (1.0f - priority) * spacing * 150;            
//        } else if (mode == 1) {
//            //double radius = ((((Bag<Concept>)nar.memory.concepts).levels - level)+8);
//            double radius = (1.0 - priority) * spacing + 8;
//            float angle = index; //TEMPORARY
//            v.tx = (float) (Math.cos(angle / 3.0) * radius) * spacing;
//            v.ty = (float) (Math.sin(angle / 3.0) * radius) * spacing;
//        } else if (mode == 2) {
//            //double radius = ((((Bag<Concept>)nar.memory.concepts).levels - level)+8);
//            double radius = (1.0 - priority) * spacing + 8;
//            float angle = NARSwing.hashFloat(v.vertex.hashCode()) * ((float)Math.PI*2f);
//            v.tx = (float) (Math.cos(angle) * radius) * spacing;
//            v.ty = (float) (Math.sin(angle) * radius) * spacing;
//        } else if (mode == 0) {
//            //gridsort
//            v.tx = index * spacing;
//            v.ty = (1.0f - priority) * spacing * 100;
//        }
//    }
//    final IndexedTreeSet<Concept> sortedConcepts = new IndexedTreeSet(new Comparator<Concept>() {
//        @Override
//        public int compare(Concept o1, Concept o2) {
//            return o1.name().toString().compareTo(o2.name().toString());
//        }
//    });
//
//    @Override
//    protected boolean hasUpdate() {
//        if (nar.time() != lasttime) {
//            lasttime = nar.time();
//            return true;
//        }
//        return false;
//    }
//
//    //TODO genrealize to DirectedMultigraph
//    public NARGraph getGraph() {
//        //final Sentence currentBelief = nar.memory.getCurrentBelief();
//        //final Concept currentConcept = nar.memory.getCurrentConcept();
//        //final Task currentTask = nar.memory.getCurrentTask();
//        if (mode == 0) {
//            sortedConcepts.clear();
//            for (Concept c : nar.memory.concepts)
//                sortedConcepts.add(c);
//        }
//        return new NARGraph().add(nar, new NARGraph.ExcludeBelowPriority(minPriority), 
//                new DefaultGraphizer(showBeliefs, showBeliefs, false, false, 0, showTermlinks, showTasklinks) { 
//            float level;
//            float index = 0;
//            int levelContents = 0;
//            private float priority;
//            Term lastTerm = null;
//            VertexDisplay lastTermVertex = null;
//
//            public void preLevel(NARGraph g, int l) {
//                if (!compressLevels) {
//                    level = l;
//                }
//                levelContents = 0;
//                if (mode == 1) {
//                    index = 0;
//                }
//            }
//
//            public void postLevel(NARGraph g, int l) {
//                if (compressLevels) {
//                    if (levelContents > 0) {
//                        level--;
//                    }
//                }
//            }
//
//            @Override
//            public void onConcept(NARGraph g, Concept c) {
//                super.onConcept(g, c);
//                priority = c.getPriority();
//                level = (float) (priority * 100.0);
//                if (mode == 0) {
//                    index = sortedConcepts.entryIndex(c);
//                } else {
//                    if ((lastTerm != null) && (c.term.equals(lastTerm))) {
//                        //terms equal to concept, ordinarily displayed as subsequent nodes
//                        //should just appear at the same position as the concept
//                        //lastTermVertex.visible = false;
//                        position(lastTermVertex, level, index, priority);
//                        lastTermVertex.radius = 0;
//                    } else {
//                        index++;
//                    }
//                }
//                VertexDisplay d = updateVertex(c);
//                position(d, level, index, priority);
//                resurrectVertex(c);                
//                
//                /*if (currentConcept != null) {
//                    if (c.equals(currentConcept)) {
//                        d.boost = 1.0f;
//                    }
//                }*/
//                levelContents++;
//                lastTerm = null;
//                lastTermVertex = null;
//            }
//
//            @Override
//            public void onTerm(Term t) {
//                index++;
//                //VertexDisplay d = updateVertex(t);
//                //position(d, level, index, priority);
//                //deadVertices.remove(d);
//                //lastTerm = t;
//                //lastTermVertex = d;
//                levelContents++;
//            }
//
////            @Override
////            public void onTask(Task t) {
////                VertexDisplay d = updateVertex(t);
////                position(t, t.getPriority(), index++, t.getPriority());
////            }
////
//
//            @Override
//            public void onTask(Task t) {
//                VertexDisplay d = updateVertex(t);
//                position(d, t.getPriority(), index++, t.getPriority());
//            }
//            
//            
//            @Override
//            public void onBelief(Sentence kb) {
//                index += 0.25f;
//                //VertexDisplay d = updateVertex(kb);
//                //position(d, level, index, priority);
//                //deadVertices.remove(kb);
//                /*if (currentBelief != null) {
//                    if (kb.equals(currentBelief)) {
//                        d.boost = 1.0f;
//                    }
//                }*/
//                levelContents++;
//                lastTerm = null;
//                lastTermVertex = null;
//            }
//
//            @Override
//            public void onQuestion(Task t) {
//                index += 0.25f;
//                //VertexDisplay d = updateVertex(t);
//                //position(d, level, index, priority);
//                //deadVertices.remove(t);
//                /*if (currentTask != null) {
//                    if (t.equals(currentTask)) {
//                        d.boost = 1.0f;
//                    }
//                }*/
//                levelContents++;
//                lastTerm = null;
//                lastTermVertex = null;
//            }
//        });
//    }
// }
