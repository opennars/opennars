package nars.cfg;

import nars.cfg.callgraph.JCallGraph;
import nars.cfg.callgraph.model.CGClass;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

import java.util.List;

/**
 * Created by me on 1/15/15.
 */
public class CFG1 {
    public static void main(String[] args) throws ClassNotFoundException {
        JavaClass cl = Repository.lookupClass("nars.logic.FireConcept");
        List<CGClass> l = JCallGraph.callgraph(cl, ".*");
        System.out.println(l);

    }
}
