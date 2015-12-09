package nars.guifx.demo;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import nars.$;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.impl.BlurCanvasEdgeRenderer;
import nars.guifx.graph2.scene.DefaultNodeVis;
import nars.guifx.graph2.source.DefaultGrapher;
import nars.guifx.graph2.source.JGraphSource;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.nal.nal1.Inheritance;
import nars.nar.Default;
import nars.term.Term;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.util.graph.TermLinkGraph2;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.ScaleFreeGraphGenerator;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.HashMap;

import static nars.$.$;

/**
 * Created by me on 8/15/15.
 */
public class GraphPaneTest {



    public static SpaceGrapher newGrapher() {


        SpaceGrapher<Term, TermNode<Term>> g = new DefaultGrapher<>(

                new JGraphSource<Term, Compound>(
                        //newExampleGraph()
                        newExampleTermLinkGraph()

                ) {

                    @Override
                    public Term getTargetVertex(Compound edge) {
                        return edge.term(1);
                    }

                },

                128,

                new DefaultNodeVis() {

                    @Override
                    public TermNode newNode(Termed term) {
                        //System.out.println("new node: " + term);
                        TermNode t = new TermNode(term, 8);
                        t.priNorm = 0.25f;
                        Button b = new Button(term.toString());
                        b.setScaleX(0.1);
                        b.setScaleY(0.1);
                        t.getChildren().add(b);
                        return t;
                    }
                },

                (TermNode<Term> S, TermNode<Term> T) -> new TermEdge(S, T) {
                    @Override
                    public double getWeight() {
                        return Math.random() * 0.75;
                    }
                },

                new BlurCanvasEdgeRenderer()
        );


        return g;
    }

    private static DirectedGraph<Term, Inheritance> newExampleGraph() {
        DirectedGraph<Term, Inheritance> g = new SimpleDirectedGraph<>((EdgeFactory<Term, Inheritance>) $::inh);


        int n = 16;
        new ScaleFreeGraphGenerator(n).generateGraph(g,
                new VertexFactory<Term>() {
                    int i = 0;

                    @Override
                    public Term createVertex() {
                        i++;
                        return $("x" + i);
                    }
                }, new HashMap());

        return g;

    }

    private static DirectedGraph<Term, Compound> newExampleTermLinkGraph() {

        NAR n = new Default(100, 3, 3, 3);
        n.input("a:b.");
        n.input("b:c.");
        n.input("c:(d,a)!");
        n.frame(4);
        return new TermLinkGraph2(n);
    }

    public static void main(String[] args) {


        NARfx.run((a, b) -> {
            SpaceGrapher gggg = newGrapher();
            b.setScene(
                    new Scene(gggg, 800, 800)
            );
            b.show();
            gggg.start(35);

//            n.spawnThread(250, x -> {
//
//            });
        });


//        TextOutput.out(n);
//        new Thread(() -> n.loop(185)).start();


    }

}
