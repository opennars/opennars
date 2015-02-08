package nars.core;


/**
 * Created by me on 2/6/15.
 */
public class RuleTest {

//    @Test public void testLeapsRules() throws Exception {
//
//        final org.drools.leaps.LeapsRuleBase rules = new org.drools.leaps.LeapsRuleBase();
//        //ruleBase.addPackage( this.pkg );
//
//        final Rule r = new Rule( "r" );
//
//        final Column c = new Column( 0 );
//
//
//        c.addConstraint(new BooleanCondition<Integer>() {
//            @Override
//            public boolean test(Integer integer) {
//                return integer > 0;
//            }
//        });
//
//
//        r.addPattern(c);
//
//        AtomicInteger hits = new AtomicInteger();
//
//        r.setConsequence(new AbstractConsequence() {
//            @Override
//            public void accept(Object o) {
//                //System.out.println("TRUE: " + o);
//                hits.incrementAndGet();
//            }
//        });
//
//        rules.addRule(r);
//
//        final WorkingMemory workingMemory = rules.newWorkingMemory();
//
//        workingMemory.assertObject( 4 );
//        workingMemory.assertObject( -4 );
//        workingMemory.assertObject( "c" );
//        workingMemory.assertObject( 1 );
//
//        workingMemory.fireAllRules();
//
//        assertEquals(2, hits.get());
//
//
//    }
//
//    @Test public void testRuleEngineSimple() {
//
//        AtomicInteger hits = new AtomicInteger();
//
//
//        RuleEngine r = new RuleEngine();
//        r.add(new BasicRule<Integer>() {
//
//            @Override
//            public boolean test(Integer i) {
//                return i > 0;
//            }
//
//            @Override
//            public void accept(Integer integer) {
//                hits.incrementAndGet();
//            }
//
//        });
//
//        r.start();
//        //WorkingMemoryLogger.out(r.state);
//
//        r.fire(1);
//        assertEquals(1, hits.get());
//        r.fire(0);
//        assertEquals(1, hits.get());
//        r.fire(2);
//        assertEquals(2, hits.get());
//        r.fire("a");
//        assertEquals(2, hits.get());
//    }
//
//    @Test public void testRuleEngineOr() {
//
//        AtomicInteger hits = new AtomicInteger();
//
//        BooleanCondition<Integer> a = new BooleanCondition<Integer>() {
//            @Override public boolean test(Integer i) {
//                //System.out.println("a?" + i);
//                return i == 1;
//            }
//        };
//        BooleanCondition<Integer> b = new BooleanCondition<Integer>() {
//            @Override public boolean test(Integer i) {
//                //System.out.println("b?" + i);
//                return i == 2;
//            }
//        };
//
//        RuleEngine r = new RuleEngine();
//
//        r.add(new OrRule<Integer>() {
//
//
//            @Override
//            public Object[] conditions() {
//                return new Object[] { a, b };
//            }
//
//            @Override
//            public void accept(Integer integer) {
//                hits.incrementAndGet();
//            }
//
//        });
//
//        r.start();
//
//        //WorkingMemoryLogger.out(r.state);
//
//
//        r.fire(1);
//        assertEquals(1, hits.get());
//        r.fire(0);
//        assertEquals(1, hits.get());
//        r.fire(2);
//        assertEquals(2, hits.get());
//        r.fire(3);
//        assertEquals(2, hits.get());
//    }
//
//    @Test public void testRuleEngineSharedCondition() {
//
//        AtomicInteger hits = new AtomicInteger();
//        AtomicInteger aaCount = new AtomicInteger();
//        AtomicInteger aCount = new AtomicInteger();
//
//        BooleanCondition<Integer> aa = new BooleanCondition<Integer>() {
//            @Override public boolean test(Integer i) {
//                System.out.println("aa?" + i);
//                //new Exception().printStackTrace();
//                aaCount.incrementAndGet();
//                return i == 1;
//            }
//
//            @Override
//            public String toString() { return "aCond";            }
//        };
////        EvalCondition a = new EvalCondition(new EvalExpression() {
////
////            @Override
////            public boolean evaluate(Tuple tuple, Declaration[] requiredDeclarations, WorkingMemory workingMemory) {
////                System.out.println("a? " + tuple);
////                aCount.incrementAndGet();
////                return true;
////            }
////        });
//
//
//        RuleEngine r = new RuleEngine();
//        r.start();
//
//        r.add(new OrRule<Integer>() {
//            @Override
//            public Object[] conditions() {
//                return new Object[] { aa };
//            }
//            @Override
//            public void accept(Integer integer) {
//                hits.incrementAndGet();
//            }
//        });
//        r.add(new BasicRule<Integer>() {
//
//            @Override
//            public boolean test(Integer integer) {
//                return false;
//            }
//
//            @Override
//            public void accept(Integer integer) {
//                hits.incrementAndGet();
//            }
//        });
//
//
//
//        WorkingMemoryLogger.out(r.state);
//
//        System.out.println(r.state);
//
//        System.out.println(hits + "; attepmts=" + aCount + " " + aaCount);
//        r.fire(1);
//        assertEquals(2, hits.get());
//        System.out.println(hits + "; attepmts=" + aCount + " " + aaCount);
//        r.fire(1);
//        System.out.println(hits + "; attepmts=" + aCount + " " + aaCount);
//
//
//    }
//
}
