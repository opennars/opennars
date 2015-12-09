package objenome.evolve;


import objenome.op.DoubleVariable;
import objenome.op.Node;
import objenome.op.VariableNode;
import objenome.op.math.*;
import objenome.solver.Civilization;
import objenome.solver.EGoal;
import objenome.solver.evolve.RandomSequence;
import objenome.solver.evolve.TypedOrganism;
import org.junit.Test;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * https://github.com/apache/commons-math/blob/master/src/test/java/org/apache/commons/math4/genetics/GeneticAlgorithmTestBinary.java
 */
public class CivilizationTest extends Civilization {

    IdentityHashMap uniqueVar = new IdentityHashMap();
    IdentityHashMap uniqueVarNode = new IdentityHashMap();

    @Test
    public void testUniqueVariableContext() {
        int threads = 2;
        int individuals = 64;


        add(new TestGoal());

        //run();

        System.out.println(uniqueVar);
    }

    @Override
    public List<Node> getOperators(RandomSequence random) {
        List<Node> l = new ArrayList();

        l.add( new DoubleERC(random, -1.0, 2.0, 2));

        l.add(new Add());
        l.add(new Subtract());
        l.add(new Multiply());
        l.add(new DivisionProtected());


        l.add(new VariableNode(new DoubleVariable("a")));
        l.add(new VariableNode(new DoubleVariable("b")));

        return l;
    }

    public CivilizationTest() {
        super(1, 4, 8);
    }

    public class TestGoal extends EGoal<TypedOrganism> {

        public TestGoal() {
            super("TestGoal");
        }


        @Override
        public double cost(TypedOrganism leakProgram) {

            Set<VariableNode> vars = leakProgram.getRoot().newVariableSet();

            synchronized (uniqueVar) {
                for (VariableNode vn : vars) {
                    Object duplicate = uniqueVarNode.put(vn, leakProgram);
                    assertEquals("two organism instances should never have equal variable nodes or variables", null, duplicate);

                    Object duplicate2 = uniqueVar.put(vn.getVariable(), leakProgram);
                    assertEquals("two organism instances should never have equal variable nodes or variables", null, duplicate2);
                }
            }

            double d = leakProgram.eval();

            return Math.sin(d);

            //bind all program variables to this instance
//            Node root = (leakProgram.getRoot());
//            Map<String, Variable> vars = new HashMap();
//            for (Object o : root.listVariables()) {
//                VariableNode v = (VariableNode)o;
//                vars.put(v.getIdentifier(), v);
//            }

        }

    }

}

