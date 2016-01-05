package nars.cfg;

import nars.cfg.bytecode.ControlFlowGraph;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.IOException;
import java.util.List;

public enum BytecodeCFG {
    ;

    public static void main(String[] arg) throws IOException, AnalyzerException {

        /*

SootClass c = Scene.v().loadClassAndSupport("MyClass");
c.setApplicationClass();
SootMethod m = c.getMethodByName("main");
Body b = m.retrieveActiveBody();
UnitGraph g = new BriefUnitGraph(b);
         */


        /*
        DependencyVisitor v = new DependencyVisitor();
        //new ClassReader(f.getInputStream(e)).accept(v, 0);
        new ClassReader("nars.logic.FireConcept").accept(v, 0);

        System.out.println(v.getGlobals());
        System.out.println(v.getPackages());
        */

        ClassNode cn = new ClassNode();
        new ClassReader("nars.nal.FireConcept").accept(cn, 0);
        List<?> methods = cn.methods;
        for (Object method1 : methods) {

            MethodNode method = (MethodNode) method1;
            ControlFlowGraph cfg = ControlFlowGraph.create(null, cn, method);
            System.out.println(cfg);


            /*
            Analyzer a=new Analyzer(new BasicInterpreter()){
                @Override protected void newControlFlowEdge(    int src,    int dst){
                    System.out.println(src + " " + dst);
                    //controlflow.addFlow(src,dst);
                    //if (src > dst) {
                    //    controlflow.getFlow(src).setIsWhile(true);
                    //}
                }
            }
            a.analyze(cn.name,method);
            */
        }


    }
}
