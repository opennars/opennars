package nars.guifx.demo;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import nars.$;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.graph2.GraphSource;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.impl.BlurCanvasEdgeRenderer;
import nars.guifx.graph2.scene.DefaultVis;
import nars.guifx.graph2.source.DefaultGrapher;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.nal.DerivationRules;
import nars.nal.nal1.Inheritance;
import nars.nal.nal4.Product;
import nars.nar.Default2;
import nars.term.Term;
import nars.term.Termed;
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

    static {
        DerivationRules.maxVarArgsToMatch = 2;
    }

    public static SpaceGrapher newGrapher() {


        SpaceGrapher g = new DefaultGrapher(

                new JGraphSource<Term, Product> (
                    //newExampleGraph()
                    newExampleTermLinkGraph()
                ) {

                    @Override
                    public void start(SpaceGrapher g) {
                        super.start(g);

//                        new Thread(() -> {
//
//                            while (true) {
//                                Util.pause(100);
//
//                                updateGraph(g);
//                            }
//                        }).start();
                    }

                    @Override
                    public void updateNode(SpaceGrapher sg, Termed s, TermNode sn) {

                        graph.outgoingEdgesOf(s.getTerm()).forEach(e -> {

                            Term t = e.term(1);//other term

                            TermNode tn = sg.getTermNode(t);
                            if (tn == null) return;

                            TermEdge ee = getConceptEdge(sg, sn, tn, (S, N) -> {

                                return new TermEdge(S, N) {

                                    @Override
                                    public double getWeight() {
                                        return Math.random() * 0.75;
                                    }
                                };
                            });

                            if (ee != null) {

                                //ee.linkFrom(tn, link);
                            }


                        });
                    }

                },

                128,

                new DefaultVis() {

                    @Override
                    public TermNode newNode(Termed term) {
                        TermNode t = new TermNode(term);
                        t.priNorm = 0.25f;
                        Button b = new Button(term.toString());
                        b.setScaleX(0.1);
                        b.setScaleY(0.1);
                        t.getChildren().add(b);
                        return t;
                    }
                },


                new BlurCanvasEdgeRenderer()
        );


        return g;
    }

    private static DirectedGraph<Term, Inheritance> newExampleGraph() {
        DirectedGraph<Term, Inheritance> g = new SimpleDirectedGraph<Term, Inheritance>(new EdgeFactory<Term, Inheritance>() {
            @Override
            public Inheritance createEdge(Term s, Term t) {
                return $.inh(s, t);
            }
        });


        int n = 16;
        new ScaleFreeGraphGenerator(n).generateGraph(g,
                new VertexFactory<Term>() {
                    int i = 0;

                    @Override public Term createVertex() {
                        i++;
                        return $("x" + i);
                    }
                }, new HashMap());

        return g;

    }

    private static DirectedGraph<Term, Product> newExampleTermLinkGraph() {

        NAR n = new Default2(100, 3, 3, 3);
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
            gggg.start(35);
            b.show();

//            n.spawnThread(250, x -> {
//
//            });
        });


//        TextOutput.out(n);
//        new Thread(() -> n.loop(185)).start();


    }

    private static class JGraphSource<V extends Termed, E> extends GraphSource {


        DirectedGraph<V, E> graph;
        private SpaceGrapher<V, TermNode<V>> grapher;

        public JGraphSource(DirectedGraph<V, E> initialGraph) {
            this.graph = initialGraph;
        }

        @Override
        public void start(SpaceGrapher g) {
            this.grapher = g;
            super.start(g);

            new Animate(35, a -> {

                setUpdateable();
                updateGraph(g);

            }).start();

        }

        public void setGraph(DirectedGraph<V, E> initialGraph) {
            this.graph = initialGraph;
            updateGraph(grapher);
        }


        @Override
        public void updateGraph(SpaceGrapher g) {

            if (graph == null) return;

            if (!g.isReady())
                return;

            if (this.canUpdate()) {

                if (graph == null) {
                    //setvertices empty array?
                    return;
                }

                g.setVertices(graph.vertexSet());
            }
        }

    }
}
