package nars.analyze.cfg;

import nars.analyze.cfg.method.CGMethod;
import nars.analyze.cfg.method.MethodCallGraph;
import org.jgrapht.alg.KShortestPaths;

/**
 * Created by me on 1/15/15.
 */
public class MethodCFG {
    public static void main(String[] args) throws ClassNotFoundException {



        MethodCallGraph g = new MethodCallGraph()
                .addClasses("nars.logic", true)
                .addClasses("nars.logic.reason", true)
                .addClasses("nars.logic.reason.concept", true)
                .addClass("nars.logic.reason.ConceptFire")
                .addClass("nars.logic.reason.concept.ConceptFireTask")
                .addClass("nars.logic.reason.concept.ConceptFireTaskTerm")
                .addClass("nars.logic.NAL")
                ;

        System.out.println(g.vertexSet().size() + " vert , "  + g.edgeSet().size() + " edge");
        //System.out.println(g);


        System.out.println("Methods: ");
        list("\tMETHOD ", g.vertexSet());

        System.out.println("Entry methods: ");
        list("\tENTRYMETHOD ", g.getEntryPoints());

        /*System.out.println(" Exit methods: ");
        list("\tEXITMETHOD ", g.getExitPoints());*/


        CGMethod fireConcept = g.method("nars.logic.reason.ConceptFire#reason([])");
        CGMethod derivedTask = g.method("nars.logic.NAL#deriveTask([nars.logic.entity.Task, boolean, boolean, nars.logic.entity.Task, nars.logic.entity.Sentence, nars.logic.entity.Sentence, nars.logic.entity.Task])");
        //CGMethod addTask = g.method("nars.logic.NAL#addTask([nars.logic.entity.Task, java.lang.String])");

        KShortestPaths fromFireConcept = new KShortestPaths(g, fireConcept, 500, 500);

        System.out.println(fireConcept + " to " + derivedTask);
        list("  DERIVATION_EDGE ", fromFireConcept.getPaths(derivedTask));

        //System.out.println("FireConcept to NAL.addTask:");
        //list(fromFireConcept.getPaths(addTask));

        //new NWindow("methods", new JGraphXGraphPanel(g)).show(500,500,true);
        System.out.println(g.vertexSet().size() + "V|" + g.edgeSet().size() + "E");
    }

    public static void list(String prefix, Iterable x) {
        if (x != null)
            for (Object y : x) {
                System.out.println(prefix + y);
            }
        System.out.println();
    }
}
