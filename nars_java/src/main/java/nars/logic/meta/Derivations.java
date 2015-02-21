package nars.logic.meta;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import nars.core.Events;
import nars.core.NAR;
import nars.event.AbstractReaction;
import nars.logic.NAL;
import nars.logic.entity.*;
import nars.logic.nal7.Interval;
import nars.logic.nal7.Tense;
import nars.logic.nal8.Operator;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
 */
public class Derivations extends DirectedMultigraph {


    public final Multimap<Premise, String> premiseResult;

    private final boolean includeDerivedBudget;
    private final boolean includeDerivedTruth;

    StringBuilder tempResultString = new StringBuilder(128);

    public Derivations(boolean includeDerivedBudget, boolean includeDerivedTruth) {
        super((Class)null);

        premiseResult = MultimapBuilder.treeKeys().treeSetValues().build();
        this.includeDerivedBudget = includeDerivedBudget;
        this.includeDerivedTruth = includeDerivedTruth;
    }


    public static class Premise implements Comparable<Premise> {

        private final String conceptKey;
        private final String taskLinkKey;
        private final String termLinkKey;
        public final String key;

        public Premise(Term concept, TaskLink tasklink, TermLink termlink, Map<Term, Integer> unique, long now, boolean truth, boolean budget) {
            this.conceptKey = genericString(concept.getTerm(), unique);
            this.taskLinkKey = genericString(tasklink.getTask(), unique, now, truth, budget);
            this.termLinkKey = termlink == null ? "" : genericString(termlink.getTerm(), unique);
            this.key = (conceptKey + ' ' + taskLinkKey + ' ' + termLinkKey).trim();
        }

        @Override
        public String toString() {
            return key;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Premise) {
                return key.equals(((Premise)obj).key);
            }
            return false;
        }

        @Override
        public int compareTo(Premise o) {
            return key.compareTo(o.key);
        }
    }

    public Premise newPremise(Term concept, TaskLink tasklink, TermLink termlink, Map<Term, Integer> unique, long now) {
        return new Premise(concept, tasklink, termlink, unique, now, includeDerivedTruth, includeDerivedBudget);
    }

    public AbstractReaction record(NAR n) {
        return new AbstractReaction(n, Events.TermLinkTransform.class,
                Events.ConceptFired.class, Events.TermLinkSelected.class, Events.TaskDerive.class) {

            @Override
            public void event(Class event, Object[] args) {

                if (event == Events.TermLinkTransform.class) {
                    TaskLink tl = (TaskLink)args[0];
                    Concept c = (Concept)args[1];
                    NAL n = (NAL)args[2];
                    record(c, tl, null, n.getNewTasks(), n.time());
                }

                else if (event == Events.TermLinkSelected.class) {
                    TermLink termlink = (TermLink)args[0];
                    TaskLink tasklink = (TaskLink)args[1];
                    Concept c = (Concept)args[2];
                    NAL n = (NAL)args[3];
                    int taskStart = (int) args[4];
                    int taskEnd = (int) args[5];

                    record(c, tasklink, termlink, getTasks(n, taskStart, taskEnd), n.time());
                }

                else if (event == Events.TaskDerive.class) {
                    //System.out.println(args[0]);
                }
            }
        };
    }

    static Iterable<Task> getTasks(NAL n, int taskStart, int taskEnd) {
        return Iterables.limit(Iterables.skip(n.getNewTasks(), taskStart), taskEnd - taskStart);    }




    public static String genericString(Task t, Map<Term,Integer> unique, long now, boolean includeDerivedTruth, boolean includeDerivedBudget) {
        StringBuilder tempTaskString = new StringBuilder(128);

        String s = genericString(t.sentence, unique, now, includeDerivedTruth);

        if (includeDerivedBudget)
            tempTaskString.append(t.budget.toStringExternal1(false));

        tempTaskString.append(s);

        return tempTaskString.toString();
    }

    public synchronized void record(Concept c, TaskLink tasklink, TermLink termlink, Iterable<Task> result, long now) {
        tempResultString.setLength(0);

        Map<Term,Integer> unique = new HashMap();
        Premise derivationClass = newPremise(c.getTerm(), tasklink, termlink, unique, now);

        for (Task t : result) {
            tempResultString.append(genericString(t, unique, now, includeDerivedTruth, includeDerivedBudget)).append("  ");

        }

        String r = tempResultString.toString();

        premiseResult.put(derivationClass, r);
    }

    public static String genericLiteral(Term c, Map<Term, Integer> unique) {
        c.recurseTerms(new Term.TermVisitor() {
            @Override public void visit(Term t, Term superterm) {
                if ((t.getClass() == Term.class) || (t instanceof Variable)) {
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
        String t = genericString(s.term, unique);
        t += s.punctuation;

        if (!s.isEternal())
            t += Tense.tenseRelative(s.getOccurrenceTime(), now);

        if (includeTruth)
            t += (s.truth.toStringExternal1());

        return t;
    }

    public static String genericString(Term t, Map<Term,Integer> _unique) {
        Map<Term, Integer> unique;
        if (_unique == null)
            unique = new HashMap();
        else
            unique = _unique;

        if (t.getClass() == Term.class) {
            //atomic term
            return genericLiteral(t, unique);
        }
        else if (t instanceof Operator) {
            return t.toString();
        }
        else if (t instanceof Variable) {
            return t.toString();
        }
        else if (t instanceof CompoundTerm) {
            return genericLiteral(t, unique);
        }
        else if (t instanceof Interval) {
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
            Collection<String> l = premiseResult.get(k);
            p.println();
            p.print(k);
            p.print(": ");
            if (l.isEmpty())
                p.println("null");
            else {
                p.println();
                for (String t : l) {
                    p.print('\t');
                    t = t.trim();
                    if (t.isEmpty())
                        p.println("null");
                    else
                        p.println(t);
                }
            }
        }
    }
}
