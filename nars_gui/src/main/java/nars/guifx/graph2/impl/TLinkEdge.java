package nars.guifx.graph2.impl;

import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;

/**
 * Represents an aggregation of all termlinks and tasklinks sourcing from a Concept
 */
public class TLinkEdge<N extends TermNode> extends TermEdge {

    private double w;

    public TLinkEdge(N aSrc, N bSrc) {
        super(aSrc, bSrc);
    }


    @Override
    public double getWeight() {
        return 1.0;
    }

//    public TermLink termLinkAB = null;
//    public TermLink termLinkBA = null;
//    public TaskLink taskLinkAB = null;
//    public TaskLink taskLinkBA = null;



//    final public float termLinkFrom(TermNode src) {
//        TermLink tl = (src == aSrc) ? termLinkAB : termLinkBA;
//        if (tl == null) return 0;
//        return tl.getPriority();
//    }

//    final public float taskLinkFrom(TermNode src) {
//        TaskLink tl = (src == aSrc) ? taskLinkAB : taskLinkBA;
//        if (tl == null) return 0;
//        return tl.getPriority();
//    }

//    final public void linkFrom(TermNode src, TLink link) {
//
//        if (link instanceof TermLink) {
//            TermLink tl = (TermLink) link;
//            if (src == aSrc)
//                termLinkAB = tl;
//            else
//                termLinkBA = tl;
//        } else {
//            TaskLink tl = (TaskLink) link;
//            if (src == aSrc)
//                taskLinkAB = tl;
//            else
//                taskLinkBA = tl;
//        }
//
//    }

}
