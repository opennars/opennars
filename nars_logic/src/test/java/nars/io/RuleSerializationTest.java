package nars.io;

import nars.nal.Deriver;
import nars.nal.TaskRule;
import nars.term.Compound;
import org.junit.Assert;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 9/7/15.
 */
public class RuleSerializationTest extends AbstractSerializationTest<Collection<TaskRule>,Collection<TaskRule>> {


    public RuleSerializationTest() {
        super(Deriver.standard);
    }



    @Override
    Collection<TaskRule> parse(Collection<TaskRule> input) {
        return input;
    }

    @Override
    protected Collection<TaskRule> post(Collection<TaskRule> deserialized) {
        return deserialized;
    }

    @Override
    public void testEquality(Collection<TaskRule> aa, Collection<TaskRule> bb)  {

        assertTrue(aa!=bb);

        Iterator xa = aa.iterator();
        Iterator xb = bb.iterator();

        Assert.assertEquals(aa.size(), bb.size());


        while (xa.hasNext()) {
            TaskRule a = (TaskRule) xa.next();

            TaskRule b = (TaskRule) xb.next();

            assertTrue(a!=b);

//            if (!a.equals(b)) {
//                System.out.println(a +  "\t\t" + b + "\n\n");
//
//
//                /*try {
//                    System.out.println(JSON.omDeep.writeValueAsString(a));
//                    System.out.println(JSON.omDeep.writeValueAsString(b));
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                }*/
//            }

            Assert.assertEquals(a.toString(), b.toString());

            if (!((Compound)a.getTerm()).term(0).equals(
                    ((Compound)b.getTerm()).term(0) ))
                System.err.println("what");

            Assert.assertEquals(
                    ((Compound)a.getTerm()).term(0),
                    ((Compound)b.getTerm()).term(0)
            );

            Assert.assertEquals(a + " equals hash " + b,
                    a.subterms().hashCode(), a.subterms().hashCode());

            Assert.assertEquals(a + " equals hash " + b,
                    a.hashCode(), b.hashCode());

            Assert.assertArrayEquals(a.postPreconditions, b.postPreconditions);
            //postconditions will eventually be backed by proper terms, until then, it is enough for preconditions to match
            //assertArrayEquals(a.postconditions, b.postconditions);
            Assert.assertEquals(a.postconditions.length, b.postconditions.length);


            Assert.assertEquals(a, b);
        }


    }


}