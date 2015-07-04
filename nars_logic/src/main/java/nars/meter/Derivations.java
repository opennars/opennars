package nars.meter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import nars.Events;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.event.NARReaction;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operator;
import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.term.transform.TermVisitor;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jgrapht.ext.*;
import org.jgrapht.graph.DirectedMaskSubgraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.MaskFunctor;

import java.io.FileWriter;
import java.io.PrintStream;
import java.util.*;

/**
 * table for holding derivation results for online & offline
 * testing and analysis
 *
 * associates an input condition to different instances of output results
 *
 * input:
 *      concept
 *      tasklink
 *      termlink (optional)
 *
 * output(s):
 *      list of new tasks derived
 *
 * terms are stored with their names normalized to generic values, ex;
 * <(*,<A --> B>,(--,C)) --> D>
 *
 * TODO add all removed tasks and their reasons as extra metadata
 */
public class Derivations extends DirectedMultigraph {


    public final Multimap<Premise, Set<TaskResult>> premiseResult;
    Map<Object,Double> edgeWeights = Global.newHashMap();

    private final boolean includeDerivedBudget;
    private final boolean includeDerivedTruth;



    public Derivations(boolean includeDerivedBudget, boolean includeDerivedTruth) {
        super((Class)null);

        premiseResult = MultimapBuilder.treeKeys().hashSetValues().build();
        this.includeDerivedBudget = includeDerivedBudget;
        this.includeDerivedTruth = includeDerivedTruth;
    }

    abstract public static class Keyed implements Comparable<Keyed> {

        abstract public String name();

        @Override
        public String toString() {
            return name();
        }

        @Override
        public int hashCode() {
            return name().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Keyed) {
                return name().equals(((Keyed) obj).name());
            }
            return false;
        }

        @Override
        public int compareTo(Keyed o) {
            return name().compareTo(o.name());
        }

    }

    public static class Premise extends Keyed  {

        private final String conceptKey;
        private final String taskLinkKey;
        private final String termLinkKey;
        public final String key;

        public Premise(Term concept, TaskLink tasklink, TermLink termlink, Map<Term, Integer> unique, long now, boolean truth, boolean budget) {
            this.conceptKey = genericString(concept.getTerm(), unique);
            this.taskLinkKey = genericString(tasklink.getTask(), unique, now, truth, budget);
            this.termLinkKey = termlink == null ? "" : genericString(termlink.getTerm(), unique);
            this.key = "premise<" + (conceptKey + ' ' + taskLinkKey + ' ' + termLinkKey).trim() + '>';
        }

        public String name() { return key; }


    }


    public static class TaskResult extends Keyed {

        public final String key;

        public TaskResult(Task t, Map<Term, Integer> unique, long now, boolean includeDerivedTruth, boolean includeDerivedBudget) {
            key = "task<" + genericString(t, unique, now, includeDerivedTruth, includeDerivedBudget) + '>';
        }

        @Override
        public String name() {
            return key;
        }

    }


    public static class TermPattern extends Keyed {

        public final String key;

        public TermPattern(Term t, Map<Term, Integer> unique) {
            key = genericString(t, unique);
        }

        @Override
        public String name() {
            return key;
        }

    }

    public static class SentencePattern extends Keyed {

        public final String key;

        public SentencePattern(Sentence s, Map<Term, Integer> unique, long now, boolean includeDerivedTruth) {
            key = genericString(s, unique, now, includeDerivedTruth);
        }

        @Override
        public String name() {
            return key;
        }

    }

    public Premise newPremise(Term concept, TaskLink tasklink, TermLink termlink, Map<Term, Integer> unique, long now) {
        return new Premise(concept, tasklink, termlink, unique, now, includeDerivedTruth, includeDerivedBudget);
    }

    public NARReaction record(NAR n) {
        return new NARReaction(n, Events.TermLinkTransformed.class,
                Events.ConceptProcessed.class, Events.TermLinkSelected.class, Events.TaskDerive.class) {

            @Override
            public void event(Class event, Object[] args) {

                if (event == Events.TermLinkTransformed.class) {
                    TaskLink tl = (TaskLink)args[0];
                    Concept c = (Concept)args[1];
                    NAL n = (NAL)args[2];
                    result(c, tl, null, null /*n.getNewTasks()*/, n.time()); //TODO see if derivations can be noticed another way
                }

                /*else if (event == Events.TermLinkSelected.class) {
                    TermLink termlink = (TermLink)args[0];
                    ConceptProcess n = (ConceptProcess)args[1];
                    TaskLink tasklink = n.getCurrentTaskLink();
                    Concept c = n.getCurrentConcept();
                    int taskStart = (int) args[2];
                    int taskEnd = (int) args[3];

                    result(c, tasklink, termlink, getTasks(n, taskStart, taskEnd), n.time());
                }*/

                else if (event == Events.TaskDerive.class) {
                    //System.out.println(args[0]);
                }
            }
        };
    }

    static Iterable<Task> getTasks(NAL n, int taskStart, int taskEnd) {
        if (taskStart == taskEnd)
            return Collections.emptyList();
        return Iterables.limit(Iterables.skip(null /*n.getNewTasks()*/, taskStart), taskEnd - taskStart); //TODO see if derivations can be noticed another way
    }




    public static String genericString(Task t, Map<Term,Integer> unique, long now, boolean includeDerivedTruth, boolean includeDerivedBudget) {
        StringBuilder tempTaskString = new StringBuilder(128);

        String s = genericString(t.sentence, unique, now, includeDerivedTruth);

        if (includeDerivedBudget)
            tempTaskString.append(t.toStringExternalBudget1(false));

        tempTaskString.append(s);

        return tempTaskString.toString();
    }

    @Override
    public void setEdgeWeight(Object o, double weight) {
        edgeWeights.put(o, weight);
    }

    @Override
    public double getEdgeWeight(Object o) {
        return edgeWeights.get(o);
    }

    public Object newEdge(Keyed a, Keyed b) {
        Object edge = a.name() + "||" + b.name();

        if (containsEdge(edge)) {
            setEdgeWeight(edge, getEdgeWeight(edge)+1);
        }
        else {
            addEdge(a, b, edge);
            setEdgeWeight(edge, 1);
        }
        return edge;
    }

    public void result(Concept c, TaskLink tasklink, TermLink termlink, Iterable<Task> result, long now) {

        Map<Term,Integer> unique = Global.newHashMap();

        Premise premise = newPremise(c.getTerm(), tasklink, termlink, unique, now);
        addVertex(premise);

        TermPattern conceptTerm = addTermPattern(c.getTerm(), unique);
        newEdge(conceptTerm, premise);

        TermPattern taskLinkTerm = addTermPattern(tasklink.getTerm(), unique);
        SentencePattern taskLinkSentence = addSentencePattern(tasklink.getSentence(), unique, now);
        newEdge(taskLinkTerm, taskLinkSentence);
        newEdge(taskLinkSentence, premise);

        if (termlink!=null) {
            TermPattern termLinkTerm = addTermPattern(termlink.getTerm(), unique);
            newEdge(termLinkTerm, premise);
        }


        Set<TaskResult> resultSet = new TreeSet(); //order

        for (Task t : result) {

            TaskResult tr = new TaskResult(t, unique, now, includeDerivedTruth, includeDerivedBudget);
            addVertex(tr);

            //SentencePattern sp = addSentencePattern(t.sentence, unique, now);
            TermPattern tp = addTermPattern(t.getTerm(), unique);

            newEdge(premise, tr);
            newEdge(tr, tp);
            /*newEdge(tr, sp);
            newEdge(sp, tp);*/

            resultSet.add(tr);

        }

        premiseResult.put(premise, resultSet);

    }

    SentencePattern addSentencePattern(Sentence sentence, Map<Term, Integer> unique, long now) {
        SentencePattern s = new SentencePattern(sentence, unique, now, includeDerivedTruth);
        addVertex(s);
        return s;
    }
    TermPattern addTermPattern(Term term, Map<Term, Integer> unique) {
        TermPattern s = new TermPattern(term, unique);
        addVertex(s);
        return s;
    }

    public static String genericLiteral(Term c, Map<Term, Integer> unique) {
        c.recurseTerms(new TermVisitor() {
            @Override public void visit(Term t, Term superterm) {
                if (t instanceof Atom) {
                    if (!unique.containsKey(t))
                        unique.put(t, unique.size());
                }
            }
        });

        //TODO use a better generation method, replacement might cause error if term names contain common subsequences
        //maybe use a sorted Map so that the longest terms to be replaced are iterated first, so that a shorter subterm will not interfere with subsequent longer replacement
        String s = c.toString();
        for (Map.Entry<Term,Integer> e : unique.entrySet()) {
            String tn = e.getKey().toString();
            int i = e.getValue();
            if (i > 25) throw new RuntimeException("TODO support > 26 different unique atomic terms");
            String cc = String.valueOf((char) ('A' + i));
            s = s.replace(tn, cc); //this is replaceAll but without regex
        }
        return s;

    }

    public static String genericString(Sentence s, Map<Term,Integer> unique, long now, boolean includeTruth) {
        String t = genericString(s.getTerm(), unique);
        t += s.punctuation;

        if (!s.isEternal())
            t += Tense.tenseRelative(s.getOccurrenceTime(), now);

        if (includeTruth)
            t += (s.truth.toCharSequence());

        return t;
    }

    public static String genericString(Term t, Map<Term,Integer> _unique) {
        Map<Term, Integer> unique;
        if (_unique == null)
            unique = new HashMap();
        else
            unique = _unique;

        if (t.getClass() == Atom.class) {
            //atomic term
            return genericLiteral(t, unique);
        }
        else if (t instanceof Operator) {
            return t.toString();
        }
        else if (t instanceof Variable) {
            return t.toString();
        }
        else if (t instanceof Compound) {
            return genericLiteral(t, unique);
        }
        else if (t instanceof AbstractInterval) {
            //Interval, etc..
            return t.toString();
        }
        throw new RuntimeException("genericString Unhandled term: " + t);
    }

    @Override
    public String toString() {
        return premiseResult.toString();
    }

    public void print(PrintStream p) {

        for (Premise k : premiseResult.keySet()) {
            Collection<Set<TaskResult>> l = premiseResult.get(k);
            p.println();
            p.print(k);
            p.print(": ");
            if (l.isEmpty())
                p.println("null");
            else {
                p.println(l);
                /*
                for (TaskResult t : l) {
                    p.print('\t');
                    if (t.isEmpty())
                        p.println("null");
                    else
                        p.println(t);
                }*/
            }
        }

        p.println(vertexSet().size() + " " + edgeSet().size());


        SummaryStatistics s = new SummaryStatistics();
        for (Double d : edgeWeights.values())
            s.addValue(d);
        System.out.println("weights: " + s);

        GmlExporter gme = new GmlExporter(new IntegerNameProvider(), new StringNameProvider() {
            @Override
            public String getVertexName(Object vertex) {
                return super.getVertexName(vertex);
            }
        }, new IntegerEdgeNameProvider(), new StringEdgeNameProvider() {
            @Override
            public String getEdgeName(Object edge) {
                return super.getEdgeName(edge) + "\"\n\tweight \"" + getEdgeWeight(edge) ;
            }
        });
        gme.setPrintLabels(GmlExporter.PRINT_EDGE_VERTEX_LABELS);
        try {
            gme.export(new FileWriter("/tmp/g.gml"), weightAtleast(0.5 * (s.getMean() + s.getGeometricMean())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DirectedMaskSubgraph weightAtleast(double v) {
        MaskFunctor e = new MaskFunctor() {
            @Override
            public boolean isEdgeMasked(Object edge) {
                return getEdgeWeight(edge) < v;
            }

            @Override
            public boolean isVertexMasked(Object vertex) {
                return false;
            }
        };
        return new DirectedMaskSubgraph(this, e);
    }
}
