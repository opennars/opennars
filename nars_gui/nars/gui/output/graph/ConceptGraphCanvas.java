package nars.gui.output.graph;

import java.util.Comparator;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.language.Term;
import nars.util.NARGraph;
import nars.util.sort.IndexedTreeSet;



public class ConceptGraphCanvas extends ProcessingGraphCanvas {

    private final NAR nar;

    //ty = -((((Bag<Concept>)nar.memory.concepts).levels - level) * maxNodeSize * 3.5f);
    //double radius = ((((Bag<Concept>)nar.memory.concepts).levels - level)+8);
    //TEMPORARY
    //gridsort
    //terms equal to concept, ordinarily displayed as subsequent nodes
    //should just appear at the same position as the concept
    //lastTermVertex.visible = false;
    public ConceptGraphCanvas(NAR nar) {
        this.nar = nar;
    }
    //TODO genrealize to DirectedMultigraph

    public void position(ProcessingGraphCanvas.VertexDisplay v, float level, float index, float priority) {
        float LEVELRAD = maxNodeSize * 2.5f;
        if (mode == 2) {
            v.tx = ((float) Math.sin(index / 10d) * LEVELRAD) * 5 * ((10 + index) / 20);
            //ty = -((((Bag<Concept>)nar.memory.concepts).levels - level) * maxNodeSize * 3.5f);
            v.ty = (1.0f - priority) * LEVELRAD * 150;
        } else if (mode == 1) {
            //double radius = ((((Bag<Concept>)nar.memory.concepts).levels - level)+8);
            double radius = (1.0 - priority) * LEVELRAD + 8;
            float angle = index; //TEMPORARY
            v.tx = (float) (Math.cos(angle / 3.0) * radius) * LEVELRAD;
            v.ty = (float) (Math.sin(angle / 3.0) * radius) * LEVELRAD;
        } else if (mode == 0) {
            //gridsort
            v.tx = index * LEVELRAD;
            v.ty = (1.0f - priority) * LEVELRAD * 100;
        }
    }
    final IndexedTreeSet<Concept> sortedConcepts = new IndexedTreeSet(new Comparator<Concept>() {
        @Override
        public int compare(Concept o1, Concept o2) {
            return o1.getKey().toString().compareTo(o2.getKey().toString());
        }
    });

    @Override
    protected boolean hasUpdate() {
        if (nar.getTime() != lasttime) {
            lasttime = nar.getTime();
            return true;
        }
        return false;
    }

    //TODO genrealize to DirectedMultigraph
    public NARGraph getGraph() {
        final Sentence currentBelief = nar.memory.getCurrentBelief();
        final Concept currentConcept = nar.memory.getCurrentConcept();
        final Task currentTask = nar.memory.getCurrentTask();
        if (mode == 0) {
            sortedConcepts.clear();
            sortedConcepts.addAll(nar.memory.getConcepts());
        }
        return new NARGraph().add(nar, new NARGraph.ExcludeBelowPriority(minPriority), new NARGraph.DefaultGraphizer(showBeliefs, true, showBeliefs, true, false) {
            float level;
            float index = 0;
            int levelContents = 0;
            private float priority;
            Term lastTerm = null;
            ProcessingGraphCanvas.VertexDisplay lastTermVertex = null;

            public void preLevel(NARGraph g, int l) {
                if (!compressLevels) {
                    level = l;
                }
                levelContents = 0;
                if (mode == 1) {
                    index = 0;
                }
            }

            public void postLevel(NARGraph g, int l) {
                if (compressLevels) {
                    if (levelContents > 0) {
                        level--;
                    }
                }
            }

            @Override
            public void onConcept(NARGraph g, Concept c) {
                super.onConcept(g, c);
                priority = c.getPriority();
                level = (float) (priority * 100.0);
                if (mode == 0) {
                    index = sortedConcepts.entryIndex(c);
                } else {
                    if ((lastTerm != null) && (c.term.equals(lastTerm))) {
                        //terms equal to concept, ordinarily displayed as subsequent nodes
                        //should just appear at the same position as the concept
                        //lastTermVertex.visible = false;
                        position(lastTermVertex, level, index, priority);
                        lastTermVertex.visible = false;
                    } else {
                        index++;
                    }
                }
                ProcessingGraphCanvas.VertexDisplay d = updateVertex(c);
                position(d, level, index, priority);
                deadVertices.remove(c);
                if (currentConcept != null) {
                    if (c.equals(currentConcept)) {
                        d.boost = 1.0f;
                    }
                }
                levelContents++;
                lastTerm = null;
                lastTermVertex = null;
            }

            @Override
            public void onTerm(Term t) {
                index++;
                ProcessingGraphCanvas.VertexDisplay d = updateVertex(t);
                position(d, level, index, priority);
                deadVertices.remove(d);
                lastTerm = t;
                lastTermVertex = d;
                levelContents++;
            }

            @Override
            public void onBelief(Sentence kb) {
                index += 0.25f;
                ProcessingGraphCanvas.VertexDisplay d = updateVertex(kb);
                position(d, level, index, priority);
                deadVertices.remove(kb);
                if (currentBelief != null) {
                    if (kb.equals(currentBelief)) {
                        d.boost = 1.0f;
                    }
                }
                levelContents++;
                lastTerm = null;
                lastTermVertex = null;
            }

            @Override
            public void onQuestion(Task t) {
                index += 0.25f;
                ProcessingGraphCanvas.VertexDisplay d = updateVertex(t);
                position(d, level, index, priority);
                deadVertices.remove(t);
                if (currentTask != null) {
                    if (t.equals(currentTask)) {
                        d.boost = 1.0f;
                    }
                }
                levelContents++;
                lastTerm = null;
                lastTermVertex = null;
            }
        });
    }
}
