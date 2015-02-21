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
public class Derivations {

    public final Multimap<String, String> data;
    private final boolean includeDerivedBudget;
    private final boolean includeDerivedTruth;

    public Derivations(boolean includeDerivedBudget, boolean includeDerivedTruth) {
        data = MultimapBuilder.treeKeys().treeSetValues().build();
        this.includeDerivedBudget = includeDerivedBudget;
        this.includeDerivedTruth = includeDerivedTruth;
    }

    public String getCase(Term concept, TaskLink tasklink, TermLink termlink, Map<Term,Integer> unique, long now) {
        return new StringBuilder()
                .append(concept == null ? '_' : genericString(concept.getTerm(), unique))
                .append("  ")
                .append(tasklink == null ? '_' : genericString(tasklink.getSentence(), unique, now))
                .append("  ")
                .append(termlink == null ? '_' : genericString(termlink.getTerm(), unique)).toString();

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
                    //TODO i broke this
                    //record(c, tl, null, n.produced, n.getTime());
                }

                else if (event == Events.TermLinkSelected.class) {
                    TermLink termlink = (TermLink)args[0];
                    TaskLink tasklink = (TaskLink)args[1];
                    Concept c = (Concept)args[2];
                    NAL n = (NAL)args[3];
                    int taskStart = (int) args[4];
                    int taskEnd = (int) args[5];

                    //TODO i broke this
                    Iterable<Task> theTasks = Iterables.limit(Iterables.skip(n.getNewTasks(), taskStart), taskEnd - taskStart);
                    record(c, tasklink, termlink, theTasks, n.time());
                }

                else if (event == Events.TaskDerive.class) {
                    //System.out.println(args[0]);
                }
            }
        };
    }



    StringBuilder resultString = new StringBuilder(128);
    StringBuilder sb = new StringBuilder(128);

    public synchronized String genericString(Task t, Map<Term,Integer> unique, long now) {
        String s = genericString(t.sentence, unique, now);

        sb.setLength(0);

        //TODO tense

        if (includeDerivedBudget)
            sb.append(t.budget.toStringExternal1(false));
        sb.append(s);
        if (includeDerivedTruth)
            sb.append(t.sentence.truth.toStringExternal1());

        return sb.toString();
    }

    public synchronized void record(Concept c, TaskLink tasklink, TermLink termlink, Iterable<Task> result, long now) {
        resultString.setLength(0);

        Map<Term,Integer> unique = new HashMap();
        String ca = getCase(c.getTerm(), tasklink, termlink, unique, now);

        for (Task t : result) {
            resultString.append(genericString(t, unique, now)).append("  ");

        }

        String r = resultString.toString();

        data.put(ca, r);
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

    public static String genericString(Sentence s, Map<Term,Integer> unique, long now) {
        String t = genericString(s.term, unique);
        t += s.punctuation;
        if (s.isEternal()) return t;
        return t + Tense.tenseRelative(s.getOccurrenceTime(), now);
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
        return data.toString();
    }

    public void print(PrintStream p) {
        for (String k : data.keySet()) {
            Collection<String> l = data.get(k);
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
