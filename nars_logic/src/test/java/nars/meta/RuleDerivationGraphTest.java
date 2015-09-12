package nars.meta;

import com.google.common.collect.Sets;
import nars.meter.NARComparator;
import nars.nal.Deriver;
import nars.nar.Default;
import nars.nar.NewDefault;
import org.apache.commons.math3.stat.Frequency;
import org.junit.Test;

import java.util.*;

import static org.jgroups.util.Util.assertEquals;

/**
 * Created by me on 8/15/15.
 */
public class RuleDerivationGraphTest {

    @Test
    public void testRuleStatistics() {
        Deriver d = NewDefault.der;

        List<TaskRule> R = d.rules;
        int registeredRules = R.size();


        Frequency f = new Frequency();
        for (TaskRule t : R)
            f.addValue(t);
        Iterator<Map.Entry<Comparable<?>, Long>> ii = f.entrySetIterator();
        while (ii.hasNext()) {
            Map.Entry<Comparable<?>, Long> e = ii.next();
            if (e.getValue() > 1) {
                System.err.println("duplicate: " + e);
            }
        }
        System.out.println("total: " + f.getSumFreq() + ", unique=" + f.getUniqueCount());

        HashSet<TaskRule> setRules = Sets.newHashSet(R);

        assertEquals("no duplicates", registeredRules, setRules.size());

        Set<PreCondition> preconds = new HashSet();
        int totalPrecond = 0;
        for (TaskRule t : R) {
            for (PreCondition p : t.preconditions) {
                totalPrecond++;
                preconds.add(p);
            }
        }
        System.out.println("total precondtions = " + totalPrecond + ", unique=" + preconds.size());

        //preconds.forEach(p -> System.out.println(p));



        for (TaskRule s : R) {
            System.out.println(s);
        }
    }

    @Test public void testPostconditionSingletons() {
//        System.out.println(PostCondition.postconditions.size() + " unique postconditions " + PostCondition.totalPostconditionsRequested);
//        for (PostCondition p : PostCondition.postconditions.values()) {
//            System.out.println(p);
//        }

    }

    @Test
    public void testDerivationComparator() {

        NARComparator c = new NARComparator(
                new Default(),
                new Default()
        ) {


        };
        c.input("<x --> y>.\n<y --> z>.\n");



        int cycles = 64;
        for (int i = 0; i < cycles; i++) {
            if (!c.areEqual()) {

                /*System.out.println("\ncycle: " + c.time());
                c.printTasks("Original:", c.a);
                c.printTasks("Rules:", c.b);*/

//                System.out.println(c.getAMinusB());
//                System.out.println(c.getBMinusA());
            }
            c.frame(1);
        }

        System.out.println("\nDifference: " + c.time());
        System.out.println("Original - Rules:\n" + c.getAMinusB());
        System.out.println("Rules - Original:\n" + c.getBMinusA());

    }
}
