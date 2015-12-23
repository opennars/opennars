package nars.guifx.demo;

import com.google.common.collect.Lists;
import javafx.scene.Node;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.guifx.graph2.ConceptsSource;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.impl.BlurCanvasEdgeRenderer;
import nars.guifx.graph2.scene.DefaultNodeVis;
import nars.guifx.graph2.source.DefaultGrapher;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.guifx.util.TabX;
import nars.nar.Default;
import nars.term.Termed;
import nars.term.compound.Compound;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by me on 8/15/15.
 */
public class NARGraph1Test {

    public static class ConceptNeighborhoodSource extends ConceptsSource {

        private final ArrayList<Termed> roots;
        int termLinkNeighbors = 16;

        public ConceptNeighborhoodSource(NAR nar, Concept... c) {
            super(nar);
            this.roots = Lists.newArrayList(c);
        }

        final Set<Termed> conceptsSet = Global.newHashSet(1);

        @Override
        public void commit() {

            roots.forEach(r -> {
                conceptsSet.add(r);
                if (!(r instanceof Concept)) return;

                Concept c = (Concept)r;
                c.getTaskLinks().forEach(termLinkNeighbors, n-> {
                    Termed<Compound> tn = n.get();
                    if (tn instanceof Concept) {
                        conceptsSet.add(tn);
                    } else {
                        //System.out.println("non-Concept TaskLink target: " + tn + " " + tn.getClass());
                        conceptsSet.add( nar.concept( tn ) );
                    }
                });
                c.getTermLinks().forEach(termLinkNeighbors, n-> {
                    if (n instanceof Concept) {
                        conceptsSet.add(n);
                    } else {
                        //System.out.println("non-Concept TermLink target: " + n + " " + n.getClass());
                        conceptsSet.add( nar.concept( n.term() ) );
                    }
                });
                        //concepts::add);
            });

            commit(conceptsSet);

            //System.out.println(concepts);

            conceptsSet.clear();

        }
    }

    public static SpaceGrapher newGraph(Default n) {


//        new BagForgettingEnhancer(n.memory, n.core.concepts(),
//                0f, 0.8f, 0.8f);
        n.memory.taskLinkForgetDurations.setValue(16);
        n.memory.termLinkForgetDurations.setValue(16);

//        n.memory.conceptForgetDurations.setValue(8);
//        n.memory.termLinkForgetDurations.setValue(12);
//        n.memory.taskLinkForgetDurations.setValue(12);

//        //n.input(new File("/tmp/h.nal"));
//        n.input("<hydochloric --> acid>.");
//        n.input("<#x-->base>. %0.65%");
//        n.input("<neutralization --> (acid,base)>. %0.75;0.90%");
//        n.input("<(&&, <#x --> hydochloric>, eat:#x) --> nice>. %0.75;0.90%");
//        n.input("<(&&,a,b,ca)-->#x>?");

        n.input("<a --> b>.");
        n.input("<b --> c>.");
        n.frame(5);


        DefaultGrapher g = new DefaultGrapher(

                //new ConceptsSource(n),
                new ConceptNeighborhoodSource(n,
                        n.concept("<a --> b>"),
                        n.concept("<b --> c>")
                ),

                new DefaultNodeVis() {

                    @Override
                    public TermNode newNode(Termed term) {
                        return new LabeledCanvasNode(term, 32, e-> { }, e-> { }) {
                            @Override
                            protected Node newBase() {
                                SubButton s = SubButton.make(n, (Concept) term);

                                s.setScaleX(0.02f);
                                s.setScaleY(0.02f);
                                s.shade(1f);

                                s.setManaged(false);
                                s.setCenterShape(false);

                                return s;
                            }
                        };
                        //return new HexTermNode(term.term(), 32, e-> { }, e-> { });
                        //return super.newNode(term);
                    }
                },
                //new DefaultNodeVis.HexTermNode(),

                (A, B) -> {
                    return new TermEdge(A, B) {
                        @Override
                        public double getWeight() {
                            //return ((Concept)A.term).getPriority();
                            return pri;
                        }
                    };
                    //return $.pro(A.getTerm(), B.getTerm());
                },

                new BlurCanvasEdgeRenderer()
        );

        //g.setLayout(HyperassociativeMap2D.class);
        //g.pan(2000,2000);



        return g;
    }

    public static void main(String[] args)  {


        Default n = new Default(1024,1,1,2);

        NARide.show(n.loop(), ide -> {

            //ide.addView(new IOPane(n));
            ide.content.getTabs().setAll(
                    new TabX("Graph", newGraph(n)));


            ide.setSpeed(150);
            //n.frame(5);

        });

//        NARfx.run((a,b)-> {
//            b.setScene(
//                new Scene(newGraph(n), 600, 600)
//            );
//            b.show();
//
//            n.spawnThread(250, x -> {
//
//            });
//        });





//        TextOutput.out(n);
//        new Thread(() -> n.loop(185)).start();


    }

}
