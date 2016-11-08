package nars.gui.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.CompoundTerm;
import nars.language.Term;



public class DefaultGraphizer implements NARGraph.Graphize {

    private final boolean includeBeliefs;
    private final boolean includeQuestions;
    private final boolean includeTermLinks;
    private final boolean includeTaskLinks;
    public final Map<TermLink, Concept> termLinks = new HashMap();
    public final Map<TaskLink, Concept> taskLinks = new HashMap();
    public final Map<Term, Concept> terms = new HashMap();
    public final Map<Sentence, Concept> sentenceTerms = new HashMap();
    private final boolean includeTermContent;
    private final boolean includeDerivations;
    
    @Deprecated protected int includeSyntax; //how many recursive levels to decompose per Term

    //g.addVertex(c);
    //g.addVertex(belief);
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

    public DefaultGraphizer() {
        this(false, false, false, false,0,false,false);
    }
    
    
    public DefaultGraphizer(boolean includeBeliefs, boolean includeDerivations, boolean includeQuestions, boolean includeTermContent, int includeSyntax, boolean includeTermLinks, boolean includeTaskLinks) {
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

    @Override
    public void onTime(NARGraph g, long time) {
        terms.clear();
        sentenceTerms.clear();
        termLinks.clear();
    }


    public void onTerm(NARGraph g, Term t) {
        

    }

    public void onTask(Task t) {
    }

    public void onBelief(Sentence kb) {
    }

    public void onQuestion(Task q) {
    }

    @Override
    public void onConcept(NARGraph g, Concept c) {
        g.addVertex(c);
        
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
        if (includeTermContent) {
            g.addVertex(t);
            g.addEdge(c, c.term, new NARGraph.TermContent());
            
        }
        if (includeBeliefs) {
            for (final Task beliefT : c.beliefs) {
                Sentence belief = beliefT.sentence;
                onBelief(belief);
                sentenceTerms.put(belief, c);
                //g.addVertex(c);
                //g.addVertex(belief);
                //g.addEdge(belief, c, new SentenceContent());
                //TODO extract to onBelief
                //TODO check if kb.getContent() is never distinct from c.getTerm()
                /*if (c.term.equals(belief.content)) {
                continue;
                }
                addTerm(g, belief.content);
                g.addEdge(term, belief.content, new TermBelief());*/
            }
        }
        if (includeQuestions) {
            for (final Task q : c.getQuestions()) {
                if (c.term.equals(q.getTerm())) {
                    continue;
                }
                //TODO extract to onQuestion
                g.addVertex(q);
                //TODO q.getParentBelief()
                //TODO q.getParentTask()
                g.addEdge(c, q, new NARGraph.TermQuestion());
                onQuestion(q);
            }
        }
    }

    void recurseTermComponents(NARGraph g, CompoundTerm c, int level) {
        for (Term b : c.term) {
            if (!g.containsVertex(b)) {
                g.addVertex(b);
            }
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
        if (includeSyntax > 0) {
            for (final Term a : terms.keySet()) {
                if (a instanceof CompoundTerm) {
                    CompoundTerm c = (CompoundTerm) a;
                    g.addVertex(c.operator());
                    g.addEdge(c.operator(), c, new NARGraph.TermType());
                    if (includeSyntax - 1 > 0) {
                        recurseTermComponents(g, c, includeSyntax - 1);
                    }
                }
            }
        }
        if (includeTermContent) {
            for (final Term a : terms.keySet()) {
                for (final Term b : terms.keySet()) {
                    if (a == b) {
                        continue;
                    }
                    if (a.containsTerm(b)) {
                        g.addVertex(a);
                        g.addVertex(b);
                        g.addEdge(a, b, new NARGraph.TermContent());
                    }
                    if (b.containsTerm(a)) {
                        g.addVertex(a);
                        g.addVertex(b);
                        g.addEdge(b, a, new NARGraph.TermContent());
                    }
                }
            }
        }
        if (includeDerivations && includeBeliefs) {
            for (final Map.Entry<Sentence, Concept> s : sentenceTerms.entrySet()) {
                final Sentence derivedSentence = s.getKey();
                final Collection<Term> schain = derivedSentence.stamp.getChain();
                final Concept derived = s.getValue();
                for (final Map.Entry<Sentence, Concept> t : sentenceTerms.entrySet()) {
                    if (s == t) {
                        continue;
                    }
                    final Sentence deriverSentence = t.getKey();
                    final Concept deriver = t.getValue();
                    if (derived == deriver) {
                        continue;
                    }
                    final Collection<Term> tchain = deriverSentence.stamp.getChain();
                    if (schain.contains(deriverSentence.term)) {
                        g.addVertex(derived);
                        g.addVertex(deriver);
                        g.addEdge(deriver, derived, new NARGraph.TermDerivation());
                    }
                    if (tchain.contains(derivedSentence.term)) {
                        g.addVertex(derived);
                        g.addVertex(deriver);
                        g.addEdge(derived, deriver, new NARGraph.TermDerivation());
                    }
                }
            }
        }
        if (includeTermLinks) {
            for (Map.Entry<TermLink, Concept> et : termLinks.entrySet()) {
                TermLink t = et.getKey();
                Concept from = et.getValue();
                Concept to = terms.get(t.target);
                if (to != null) {
                    g.addEdge(from, to, new NARGraph.TermLinkEdge(t));
                }
            }
        }
        if (includeTaskLinks) {
            for (Map.Entry<TaskLink, Concept> et : taskLinks.entrySet()) {
                TaskLink t = et.getKey();
                Concept from = et.getValue();
                if (t.targetTask != null) {
                    Task theTask = t.targetTask;
                    if (!g.containsVertex(theTask)) {
                        g.addVertex(theTask);
                        Term term = theTask.getTerm();
                        if (term != null) {
                            Concept c = terms.get(term);
                            if (c != null) {
                                if (g.containsVertex(c)) {
                                    g.addVertex(c);
                                }
                                g.addEdge(c, theTask, new NARGraph.TermContent());
                            }
                        }
                        onTask(theTask);
                    }
                    g.addEdge(from, t.targetTask, new NARGraph.TaskLinkEdge(t));
                }
            }
        }
    }

    public void setShowSyntax(boolean showSyntax) {
        this.includeSyntax = showSyntax ? 1 : 0;
    }
}
