package nars.util.graph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import nars.logic.entity.*;
import nars.util.data.CuckooMap;

import java.util.Map;



public class DefaultGrapher implements NARGraph.Grapher {

    private final boolean includeBeliefs;
    private final boolean includeQuestions;
    private final boolean includeTermLinks;
    private final boolean includeTaskLinks;
    public final Map<TermLink, Concept> termLinks = new CuckooMap();
    public final Multimap<TaskLink, Concept> taskLinks = ArrayListMultimap.create();

    public final Map<Term, Concept> terms = new CuckooMap();
    public final Map<Sentence, Concept> sentenceTerms = new CuckooMap();
    private final boolean includeTermContent;
    private final boolean includeDerivations;
    
    @Deprecated protected int includeSyntax; //how many recursive levels to decompose per Term
    private float minPriority = 0;

    //addVertex(g,c);
    //addVertex(g,belief);
    //g.addEdge(belief, c, new SentenceContent());
    //TODO extract to onBelief
    //TODO check if kb.getContent() is never distinct from c.getTerm()
    /*if (c.term.equals(belief.content)) {
    continue;
    }
    addTerm(g, belief.content);
    g.addEdge(term, belief.content, new TermBelief());*/
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

    public Object addVertex(NARGraph g, Object o) {
        if (g.addVertex(o))
            return o;
        return null;
    }
    public Object addEdge(NARGraph g, Object source, Object target, Object edge) {
        if (g.addEdge(source, target, edge))
            return edge;
        return null;
    }

    @Override
    public void onTime(NARGraph g, long time) {
        terms.clear();
        sentenceTerms.clear();
        termLinks.clear();
    }


    public void onTerm(NARGraph g, Term t) {
        

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

    @Override
    public void onConcept(NARGraph g, Concept c) {
        addVertex(g,c);
        
        Term t = c.term;
        
        terms.put(c.term, c);
                
        if (includeTermLinks) {
            for (TermLink x : c.termLinks) {
                termLinks.put(x, c);
            }
        }
        if (includeTaskLinks) {
            for (TaskLink x : c.taskLinks) {
                taskLinks.put(x, c);
            }
        }
        //TERM and Concept share the same hash, equality check, etc.. so they will be seen as the same vertex
        //that's why this isnt necessar and will cause a graph error
        if (includeTermContent) {
            addVertex(g,t);
            g.addEdge(c, c.term, new NARGraph.TermContent());
        }
        if (includeBeliefs) {
            for (final Sentence belief : c.beliefs) {
                sentenceTerms.put(onBelief(belief), c);
            }
        }
        if (includeQuestions) {
            for (final Task q : c.getQuestions()) {
                if (c.term.equals(q.getTerm())) {
                    continue;
                }
                //TODO extract to onQuestion
                addVertex(g,q);
                //TODO q.getParentBelief()
                //TODO q.getParentTask()
                g.addEdge(c, q, new NARGraph.TermQuestion());
                onQuestion(q);
            }
        }
    }

    void recurseTermComponents(NARGraph g, CompoundTerm c, int level) {
        for (Term b : c.term) {
            addVertex(g,b);

            if (!includeTermContent) {
                g.addEdge(c, b, new NARGraph.TermContent());
            }
            if ((level > 1) && (b instanceof CompoundTerm)) {
                recurseTermComponents(g, (CompoundTerm) b, level - 1);
            }
        }
    }

    @Override
    public void onFinish(NARGraph g) {
//        if (includeSyntax > 0) {
//            for (final Term a : terms.keySet()) {
//                if (a instanceof CompoundTerm) {
//                    CompoundTerm c = (CompoundTerm) a;
//                    addVertex(g,c.operator());
//                    g.addEdge(c.operator(), c, new NARGraph.TermType());
//                    if (includeSyntax - 1 > 0) {
//                        recurseTermComponents(g, c, includeSyntax - 1);
//                    }
//                }
//            }
//        }
        if (includeTermContent) {
            for (final Term a : terms.keySet()) {
                for (final Term b : terms.keySet()) {
                    if (a == b) {
                        continue;
                    }
                    if (a.containsTerm(b)) {
                        addVertex(g,a);
                        addVertex(g,b);
                        g.addEdge(a, b, new NARGraph.TermContent());
                    }
                    if (b.containsTerm(a)) {
                        addVertex(g,a);
                        addVertex(g,b);
                        g.addEdge(b, a, new NARGraph.TermContent());
                    }
                }
            }
        }

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
//                        g.addEdge(deriver, derived, new NARGraph.TermDerivation());
//                    }
//                    if (tchain.contains(derivedSentence.term)) {
//                        addVertex(g,derived);
//                        addVertex(g,deriver);
//                        g.addEdge(derived, deriver, new NARGraph.TermDerivation());
//                    }
//                }
//            }
//        }

        if (includeTermLinks) {
            for (TermLink t : termLinks.keySet()) {
                if (t.getPriority() < minPriority) continue;
                Concept from = termLinks.get(t);
                Concept to = terms.get(t.target);
                if (to != null) {
                    g.addEdge(from, to, new NARGraph.TermLinkEdge(t));
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
                    addVertex(g,theTask);


                    Term term = theTask.getTerm();
                    if (term != null) {
                        Concept c = terms.get(term);
                        if (c != null) {
                            addVertex(g,c);
                            g.addEdge(c, theTask, new NARGraph.TermContent());
                        }
                    }

                    if (onTask(theTask)) {
                        g.addEdge(from, theTask, new NARGraph.TaskLinkEdge(t));
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
}
