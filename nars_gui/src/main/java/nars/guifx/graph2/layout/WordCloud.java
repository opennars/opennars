package nars.guifx.graph2.layout;

import nars.guifx.graph2.NodeVis;

/**
 * TODO generic wordcloud/termcloud widget
 * If cells have rectangular backgrounds, then this
 * would appear as a sort of Tree Map diagram
 */
@SuppressWarnings("AbstractClassNeverImplemented")
public abstract class WordCloud implements IterativeLayout, NodeVis {

//    public WordCloud(NAR nar) {
//        super(nar, new V(), 64, new CanvasEdgeRenderer());
//        layoutType.setValue(HyperassociativeMap1D.class);
//    }
//    public static void main(String[] args) throws IOException {
//
//
//        NAR n = new Default(256, 1,2,2, new FrameClock());
//
//        NARide.show(n.loop(), ide -> {
//
//            n.input("a:b.");
//            n.input("b:c.");
//            n.input("c:d.");
//            n.frame(10);
//
//            ide.content.getTabs().setAll(new TabX("Graph", new WordCloud(n)));
//            ide.addView(new IOPane(n));
//
//
//            n.frame(5);
//
//        });
//
//    }
//
//    static class V extends DefaultVis  {
//
//        public V() {
//            minSize = 32;
//            maxSize = 64;
//        }
//
//        @Override
//        public HexTerm2Node newNode(Term term) {
//            return super.newNode(term);
//        }
//
////            @Override
////            public void accept(TermNode t) {
////
////            }
//
//    }


}
