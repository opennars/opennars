package nars.guifx.graph2;

import nars.NAR;
import nars.guifx.IOPane;
import nars.guifx.NARide;
import nars.guifx.graph2.layout.HyperassociativeMap1D;
import nars.guifx.util.TabX;
import nars.nar.Default;
import nars.term.Term;

import java.io.IOException;

/**
 * Created by me on 10/2/15.
 */
public class WordCloud extends DefaultNARGraph {

    public WordCloud(NAR nar) {
        super(nar, new V(), 64);
        layoutType.setValue(HyperassociativeMap1D.class);
    }
    public static void main(String[] args) throws IOException {


        NAR n = new Default(256, 1,2,2);

        NARide.show(n.loop(), ide -> {

            n.input("a:b.");
            n.input("b:c.");
            n.input("c:d.");
            n.frame(10);

            ide.content.getTabs().setAll(new TabX("Graph", new WordCloud(n)));
            ide.addView(new IOPane(n));


            n.frame(5);

        });

    }

    static class V extends HexagonsVis  {

        public V() {
            minSize = 32;
            maxSize = 64;
        }

        @Override
        public HexTerm2Node newNode(Term term) {
            return super.newNode(term);
        }

//            @Override
//            public void accept(TermNode t) {
//
//            }

    }


}
