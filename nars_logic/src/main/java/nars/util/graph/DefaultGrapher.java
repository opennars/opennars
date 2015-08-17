package nars.util.graph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Termed;
import nars.util.data.CuckooMap;
import nars.util.data.id.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class DefaultGrapher implements NARGraph.Grapher {

    private final boolean includeBeliefs;
    private final boolean includeQuestions;
    private final boolean includeTermLinks;
    private final boolean includeTaskLinks;
    public final List<TermLink> termLinks = new ArrayList();
    public final Multimap<TaskLink, Concept> taskLinks = ArrayListMultimap.create();

    //public final Map<Term, Concept> terms = new CuckooMap();
    public final Map<Sentence, Concept> sentenceTerms = new CuckooMap();
    private final boolean includeTermContent;
    private final boolean includeDerivations;
    
    @Deprecated protected int includeSyntax; //how many recursive levels to decompose per Term
    private float minPriority = 0;
    private NARGraph graph;

    //addVertex(g,c);
    //addVertex(g,belief);
    //addEdge(g,belief, c, new SentenceContent());
    //TODO extract to onBelief
    //TODO check if kb.getContent() is never distinct from c.getTerm()
    /*if (c.term.equals(belief.content)) {
    continue;
    }
    addTerm(g, belief.content);
    addEdge(g,term, belief.content, new TermBelief());*/
    //TODO extract to onQuestion
    //TODO q.getParentBelief()
    //TODO q.getParentTask()
    //avoid loops

    public DefaultGrapher() {
        this(false, false, false, false,0,false,false);
    }
    
    
    public DefaultGrapher(boolean includeBeliefs, boolean includeDerivations, boolean includeQuestions, boolean includeTermContent, int includeSyntax, boolean includeTermLinks, boolean includeTaskLinks) {
        this.includeBeliefs = includeBeliefs;
        this.includeQuestions = includeQuestions;
        this.includeTermContent = includeTermContent;
        this.includeDerivations = includeDerivations;
        this.includeSyntax = includeSyntax;
        this.includeTermLinks = includeTermLinks;
        this.includeTaskLinks = includeTaskLinks;
    }
    //if (terms.put(t)) {
    //}

//    protected static Object v(final Object o) {
//        if (o instanceof Concept)
//            return ((Concept)o).term;
//        return o;
//    }

    public Object addVertex(Named o) {
        if (graph.addVertex(o))
            return o;
        return null;
    }
    public Object addEdge(NARGraph g, Named source, Named target, Object edge) {
        addVertex(source);
        addVertex(target);
        if (g.addEdge(source, target, edge))
            return edge;
        return null;
    }

    @Override
    public void onTime(NARGraph g, long time) {
        //terms.clear();
        sentenceTerms.clear();
        termLinks.clear();
    }


    public void onTerm(Term t) {
        

    }

    /** return true if the edge to the task should be included */
    public boolean onTask(Task t) {
        return true;
    }

    public Sentence onBelief(Sentence kb) {
        return kb;
    }

    public void onQuestion(Task q) {
    }


    void onConcept(Concept c, boolean forceUpdate) {
        if (addVertex(c)==null && !forceUpdate)
            return; //already added

        aroundConcept(c, forceUpdate);

    }

    void aroundConcept(Concept c, boolean forceUpdate) {
        @Deprecated NARGraph g = this.graph;

        if (includeTermLinks) {
            for (TermLink x : c.getTermLinks()) {
                termLinks.add(x);
            }
        }
        if (includeTaskLinks) {
            for (TaskLink x : c.getTaskLinks()) {
                taskLinks.put(x, c);
            }
        }
        //TERM and Concept share the same hash, equality check, etc.. so they will be seen as the same vertex
        //that's why this isnt necessar and will cause a graph error
        if (includeTermContent) {
            //addVertex(g,t);
            addEdge(g,c, c.getTerm(), new NARGraph.TermContent());
        }
        if (includeBeliefs) {
            for (final Task belief : c.getBeliefs()) {
                sentenceTerms.put(onBelief(belief), c);
            }
        }
        if (includeQuestions) {
            for (final Task q : c.getQuestions()) {
                if (c.getTerm().equals(q.getTerm())) {
                    continue;
                }
                //TODO extract to onQuestion
                //addVertex(g,q);
                //TODO q.getParentBelief()
                //TODO q.getParentTask()
                addEdge(g,c, q, new NARGraph.TermQuestion());
                onQuestion(q);
            }
        }
    }

    void recurseTermComponents(NARGraph g, Compound c, int level) {
        for (Term b : c.term) {
            //addVertex(g,b);

            if (!includeTermContent) {
                addEdge(g,c, b, new NARGraph.TermContent());
            }
            if ((level > 1) && (b instanceof Compound)) {
                recurseTermComponents(g, (Compound) b, level - 1);
            }
        }

    }

    @Override
    public void finish() {

//        if (includeSyntax > 0) {
//            for (final Term a : terms.keySet()) {
//                if (a instanceof CompoundTerm) {
//                    CompoundTerm c = (CompoundTerm) a;
//                    addVertex(g,c.operate());
//                    addEdge(g,c.operate(), c, new NARGraph.TermType());
//                    if (includeSyntax - 1 > 0) {
//                        recurseTermComponents(g, c, includeSyntax - 1);
//                    }
//                }
//            }
//        }

        /*
        if (includeTermContent) {
            for (final Term a : terms.keySet()) {
                for (final Term b : terms.keySet()) {
                    if (a == b) {
                        continue;
                    }
                    if (a.containsTerm(b)) {
                        addVertex(g,a);
                        addVertex(g,b);
                        addEdge(g,a, b, new NARGraph.TermContent());
                    }
                    if (b.containsTerm(a)) {
                        addVertex(g,a);
                        addVertex(g,b);
                        addEdge(g,b, a, new NARGraph.TermContent());
                    }
                }
            }
        }
        */

        ///TODO do this some other way

//        if (includeDerivations && includeBeliefs) {
//            for (final Sentence derivedSentence : sentenceTerms.keySet()) {
//                Concept derived = sentenceTerms.get(derivedSentence);
//                final Collection<Term> schain = derivedSentence.stamp.getChain();
//                for (final Sentence deriverSentence : sentenceTerms.keySet()) {
//                    if (derivedSentence == deriverSentence) {
//                        continue;
//                    }
//                    final Concept deriver = sentenceTerms.get(deriverSentence);
//                    if (derived == deriver) {
//                        continue;
//                    }
//                    final Collection<Term> tchain = deriverSentence.stamp.getChain();
//                    if (schain.contains(deriverSentence.term)) {
//                        addVertex(g,derived);
//                        addVertex(g,deriver);
//                        addEdge(g,deriver, derived, new NARGraph.TermDerivation());
//                    }
//                    if (tchain.contains(derivedSentence.term)) {
//                        addVertex(g,derived);
//                        addVertex(g,deriver);
//                        addEdge(g,derived, deriver, new NARGraph.TermDerivation());
//                    }
//                }
//            }
//        }

        if (includeTermLinks) {
            int nt = termLinks.size();
            for (int i = 0; i < nt; i++) {
                TermLink t = termLinks.get(i);
                if (t.getPriority() < minPriority) continue;
                Term to = t.getTarget().getTerm();
                if (to != null) {
                    //addEdge(graph, t.getSource(), to, new NARGraph.TermLinkEdge(t));
                }
            }
        }
        if (includeTaskLinks) {

            for (final Map.Entry<TaskLink, Concept> et : taskLinks.entries()) {

                final TaskLink t = et.getKey();
                if (t.getPriority() < minPriority) continue;
                final Concept from = et.getValue();
                if (t.targetTask != null && t.targetTask.getPriority() > minPriority) {
                    final Task theTask = t.targetTask;
                    if (onTask(theTask)) {

                        if (graph == null || !graph.containsVertex(theTask)) {
                            //on adding theTask once
                            Term taskTerm = theTask.getTerm();
                            if (taskTerm != null) {
                                addEdge(graph, theTask, taskTerm, new NARGraph.TaskLinkEdge(t));
                            }
                        }

                        addEdge(graph, from, theTask, new NARGraph.TaskLinkEdge(t));

                    }



                }
            }
        }
    }

    @Override
    public void setMinPriority(float minPriority) {
        this.minPriority = minPriority;
    }

    public void setShowSyntax(boolean showSyntax) {
        this.includeSyntax = showSyntax ? 1 : 0;
    }

    @Override
    public DefaultGrapher on(NARGraph g, Object vertex) {
        //return on(g, vertex);
        return this;
    }

    /** updates an object that may be in the graph */
    public DefaultGrapher on(NARGraph g, Object vertex, boolean aroundOnly) {
        this.graph = g;

        //if (!graph.containsVertex(v(vertex))) {
            if (vertex instanceof Concept) {
                if (aroundOnly)
                    aroundConcept((Concept) vertex, true);
                else
                    onConcept((Concept) vertex, true);
            } else if (vertex instanceof Termed) {
                if (!aroundOnly)
                    onTerm(((Termed) vertex).getTerm());
            }
        //}
        return this;
    }
}
