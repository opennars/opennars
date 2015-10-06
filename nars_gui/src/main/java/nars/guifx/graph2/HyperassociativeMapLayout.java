package nars.guifx.graph2;

import automenta.vivisect.dimensionalize.HyperassociativeMap;

import java.util.function.Consumer;

/**
 * Created by me on 9/6/15.
 */
public class HyperassociativeMapLayout extends HyperassociativeMap<TermNode, TermEdge> {
    double scaleFactor = 1;
    private TermNode[] termList;

    public HyperassociativeMapLayout() {
        this(2);
    }

    public HyperassociativeMapLayout(int dim) {
        super(dim);
    }

    @Override
    public void init(TermNode n) {
        n.move(Math.random()*200,Math.random()*200);
    }

    @Override
    public void run(NARGraph graph, int i) {

        init();

        this.termList = graph.displayed;

        align(i);

        apply();
    }

    protected void init() {
        resetLearning();
        setLearningRate(0.4f);
        setRepulsiveWeakness(10.0);
        setAttractionStrength(15.0);
        setMaxRepulsionDistance(150);
        setEquilibriumDistance(0.02f);
    }

    @Override
    public void getPosition(final TermNode node, final double[] v) {
        node.getPosition(v);
    }

            /*
            @Override
            public double getEdgeWeight(TermEdge termEdge) {
                ///doesnt do anything in this anymore
            }
            */

            /*@Override
            public double getRadius(TermNode termNode) {
                return super.getRadius(termNode);
            }*/

    @Override
    public boolean normalize() {
        return false;
    }


    @Override
    public double getRadius(TermNode termNode) {

        return termNode.priNorm * 0.025;

    }

    @Override
    public double getSpeedFactor(TermNode termNode) {
        //return 120 + 120 / termNode.width(); //heavier is slower, forcing smaller ones to move faster around it
        return scaleFactor * 1.5f;
    }

    @Override
    public void apply(final TermNode node, final double[] dataRef) {

        node.move(dataRef[0], dataRef[1]);//, 1.0, 0);
    }

    @Override
    protected TermNode[] getVertices() {
        scaleFactor = 150 + 70 * Math.sqrt(1 + termList.length);
        setScale(scaleFactor);


        //TODO avoid copying
        return termList;
    }

    @Override
    protected void edges(final TermNode nodeToQuery, Consumer<TermNode> updateFunc) {
//                    for (final TermEdge e : edges.row(nodeToQuery.term).values()) {
//                        updateFunc.accept(e.otherNode(nodeToQuery));
//                    }

        for (final TermEdge e : nodeToQuery.getEdges()) {
            if (e!=null && e.visible)
                updateFunc.accept(e.otherNode(nodeToQuery));
        }

            /*edges.values().forEach(e ->
                    updateFunc.accept(e.otherNode(nodeToQuery)));*/

    }

}
