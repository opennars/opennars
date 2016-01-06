//package nars.guifx.demo;
//
//import nars.NAR;
//import nars.guifx.IOPane;
//import nars.guifx.NARide;
//import nars.guifx.graph2.impl.CanvasEdgeRenderer;
//import nars.guifx.graph2.layout.ConceptComet;
//import nars.guifx.graph2.scene.DefaultVis;
//import nars.guifx.graph2.source.DefaultGrapher;
//import nars.guifx.graph2.source.SpaceGrapher;
//import nars.guifx.util.TabX;
//import nars.nal.DerivationRules;
//import nars.nar.Default;
//import nars.time.FrameClock;
//
///**
// * Created by me on 8/15/15.
// */
//public class NARTimeGraph1Test {
//
//    static {
//        DerivationRules.maxVarArgsToMatch = 2;
//    }
//
//    public static SpaceGrapher newGraph(NAR n) {
//
//        n.memory.conceptForgetDurations.setValue(6);
//        n.memory.termLinkForgetDurations.setValue(2);
//        n.memory.taskLinkForgetDurations.setValue(2);
//
//        //n.input(new File("/tmp/h.nal"));
//        n.input("<hydochloric --> acid>.");
//        n.input("<#x-->base>. %0.65%");
//        n.input("<neutralization --> (acid,base)>. %0.75;0.90%");
//        //n.input("<(&&, <#x --> hydochloric>, eat:#x) --> nice>. %0.75;0.90%");
//        //n.input("<(&&,a,b,ca)-->#x>?");
//
//        //n.frame(5);
//
//
//        SpaceGrapher<?,?> g = new DefaultGrapher(
//
//                new EventGraphSource(n),
//
//
//                64,
//
//                new DefaultVis(),
//
//
//                new CanvasEdgeRenderer()
//        );
//
//        g.layout.set(new ConceptComet(n));
//
//
//
//        return g;
//    }
//
//    public static void main(String[] args)  {
//
//
//        NAR n = new Default(512, 2,3,5, new FrameClock());
//
//        NARide.show(n.loop(), ide -> {
//
//            ide.content.getTabs().setAll(new TabX("Graph", newGraph(n)));
//            ide.addView(new IOPane(n));
//
//
//            //n.frame(5);
//
//        });
//
////        NARfx.run((a,b)-> {
////            b.setScene(
////                new Scene(newGraph(n), 600, 600)
////            );
////            b.show();
////
////            n.spawnThread(250, x -> {
////
////            });
////        });
//
//
//
//
//
////        TextOutput.out(n);
////        new Thread(() -> n.loop(185)).start();
//
//
//    }
//
// }
