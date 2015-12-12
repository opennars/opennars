//package nars.nal;
//
//import nars.Global;
//import nars.Op;
//import nars.term.Term;
//import nars.term.compound.Compound;
//import nars.term.variable.Variable;
//import org.jgrapht.traverse.DepthFirstIterator;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.util.Set;
//
//import static nars.$.$;
//
///**
// * Created by me on 12/11/15.
// */
//public class VariableDependenciesTest {
//
//
//    @Test public void testPattern1() {
//        testPattern("((%A ==> (%P --> %M)), (%S --> %M))");
//    }
//    @Test public void testPattern3() {
//        testPattern("((%a ==> {%x,%y,%z})), (%a --> %z))");
//    }
//    @Test public void testPattern4() throws IOException {
//        PatternIndex.VariableDependencies v;
//        v = testPattern("(( {%x,%y,%z}, %z )), (%a --> %z))");
//
//        /*new DOTExporter()
//                .export(new FileWriter("/tmp/x.dot"), v);*/
//    }
//
//    @Test public void testPattern2() {
//        testPattern("(%y, (%x, %y))");
//    }
//
//    public PatternIndex.VariableDependencies testPattern(String pattern) {
//        PatternIndex.VariableDependencies v = new PatternIndex.VariableDependencies(
//            $(pattern), Op.VAR_PATTERN
//        );
//
//        System.out.println(pattern);
//
//
//        Set<Variable> matched = Global.newHashSet(1);
//
//        //new BreadthFirstIterator(v).forEachRemaining(a -> {
//        new DepthFirstIterator<Term,String>(v).forEachRemaining(a -> {
//        //v.iterator().forEachRemaining(a -> {
//
//            if (a.equals(Op.Imdex)) return; //skip
//
//            if (a instanceof Compound) {
//                System.out.println("match Compound: " + a);
//            } else if ((a.op() == v.type)) {
//
//                if (a instanceof PatternIndex.VariableDependencies.RematchedPatternVariableIndex)
//                    System.out.println("match Variable++: " + a);
//                else
//                    System.out.println("match Variable  : " + a);
//
//                matched.add((Variable) a);
//            } else {
//                System.out.println("match Constant: " + a);
//            }
//
//
//        });
//        System.out.print("\n\t");
//
////        //new DepthFirstIterator(v).forEachRemaining(a -> {
////        v.iterator().forEachRemaining(a -> {
////            System.out.print(a + " , ");
////        });
////
////        System.out.println("\n");
//
//        return v;
//
//    }
//
////    private static class MyStringNameProvider extends StringNameProvider {
////        @Override
////        public String getVertexName(Object vertex) {
////            return super.getVertexName(vertex)
////                    .replace("%","aa")
////                    .replace("_","zz")
////                    .replace("(","a")
////                    .replace(")","b")
////                    .replace("{","c")
////                    .replace("}","d")
////                    .replace(",","e");
////        }
////    }
//}