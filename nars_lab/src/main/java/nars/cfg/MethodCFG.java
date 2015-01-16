package nars.cfg;

import nars.cfg.method.CGMethod;
import nars.cfg.method.MethodCallGraph;
import org.jgrapht.alg.KShortestPaths;

/**
 * Created by me on 1/15/15.
 */
public class MethodCFG {
    public static void main(String[] args) throws ClassNotFoundException {


        MethodCallGraph g = new MethodCallGraph()
                .addClass("nars.logic.FireConcept")
                .addClass("nars.logic.NAL")
                .addClass("nars.logic.StructuralRules")
                .addClass("nars.logic.RuleTables")
                .addClass("nars.logic.CompositionalRules")
                .addClass("nars.logic.nal1.LocalRules")
                .addClass("nars.logic.nal7.TemporalRules")
                ;

        System.out.println(g.vertexSet().size() + " vert , "  + g.edgeSet().size() + " edge");
        //System.out.println(g);


        System.out.println("Methods: ");
        list(g.vertexSet());

        System.out.println("Entry methods: ");
        list(g.getEntryPoints());

        System.out.println(" Exit methods: ");
        list(g.getExitPoints());


        CGMethod fireConcept = g.method("nars.logic.FireConcept#run([])");
        CGMethod derivedTask = g.method("nars.logic.NAL#derivedTask([nars.logic.entity.Task, boolean, boolean, nars.logic.entity.Task, nars.logic.entity.Sentence])");
        CGMethod addTask = g.method("nars.logic.NAL#addTask([nars.logic.entity.Task, java.lang.String])");

        KShortestPaths fromFireConcept = new KShortestPaths(g, fireConcept, 100, 100);

        System.out.println("FireConcept to NAL.derivedTask:");
        list(fromFireConcept.getPaths(derivedTask));

        System.out.println("FireConcept to NAL.addTask:");
        list(fromFireConcept.getPaths(addTask));

        //new NWindow("methods", new JGraphXGraphPanel(g)).show(500,500,true);
    }

    public static void list(Iterable x) {
        if (x != null)
            for (Object y : x)
                System.out.println(y);
        System.out.println();
    }
}
