package nars.guifx.graph2.layout;

import automenta.vivisect.dimensionalize.HyperassociativeMap;
import javafx.beans.property.SimpleDoubleProperty;
import nars.guifx.annotation.Range;
import nars.guifx.graph2.NARGraph;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;

import java.util.function.Consumer;

/**
 * Created by me on 9/6/15.
 */
public class HyperassociativeMap2D extends HyperassociativeMap<TermNode, TermEdge> {
    double scaleFactor = 1;
    private TermNode[] termList;


    //TODO equilibrum distance, speed, etc

    @Range(min=1, max=20)
    public final SimpleDoubleProperty attractionStrength = new SimpleDoubleProperty(15.0);
    @Range(min=1, max=20)
    public final SimpleDoubleProperty repulseWeakness = new SimpleDoubleProperty(10.0);

    public HyperassociativeMap2D() {
        this(2);
    }

    public HyperassociativeMap2D(int dim) {
        super(dim, 1.0,
                //Manhattan
                Euclidean);

        //reusedCurrentPosition = new ArrayRealVector(dim);
    }

    @Override
    public void init(TermNode n) {
        n.move(Math.random()*200,Math.random()*200);
    }


//
//    private final ArrayRealVector reusedCurrentPosition;
//
//    //this assumes single-thread usage so we reuse the vector */
//    @Override protected final ArrayRealVector getCurrentPosition(TermNode n) {
//        ArrayRealVector rcp = reusedCurrentPosition;
//        n.getPosition(rcp.getDataRef());
//        return rcp;
//    }


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
        setRepulsiveWeakness(repulseWeakness.get());
        setAttractionStrength(attractionStrength.get());
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
