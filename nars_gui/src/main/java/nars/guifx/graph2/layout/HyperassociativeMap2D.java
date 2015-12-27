package nars.guifx.graph2.layout;

import automenta.vivisect.dimensionalize.HyperassociativeMap;
import com.gs.collections.impl.map.mutable.primitive.ObjectDoubleHashMap;
import javafx.beans.property.SimpleDoubleProperty;
import nars.data.Range;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.term.Termed;

/**
 * Created by me on 9/6/15.
 */
public class HyperassociativeMap2D extends HyperassociativeMap<Termed,TermNode> implements IterativeLayout {
    double scaleFactor = 1;
    private TermNode[] termList = null;


    //TODO equilibrum distance, speed, etc

    @Range(min = 0.1, max = 10)
    public final SimpleDoubleProperty attractionStrength = new SimpleDoubleProperty(1);
    @Range(min = 0.1, max = 10)
    public final SimpleDoubleProperty repulseWeakness = new SimpleDoubleProperty(3);
    @Range(min = 0, max = 40)
    public final SimpleDoubleProperty nodeSpeed = new SimpleDoubleProperty(2.0);
    @Range(min = 0.05, max = 1)
    public final SimpleDoubleProperty equilibriumDistance = new SimpleDoubleProperty(0.5);

    @Range(min = 1, max = 100)
    public final SimpleDoubleProperty scale = new SimpleDoubleProperty(1.0);

    private float _nodeSpeed;
    private SpaceGrapher graph;


    public HyperassociativeMap2D() {
        this(2);
    }

    public HyperassociativeMap2D(int dim) {
        this(dim, 1.0);
    }

    public HyperassociativeMap2D(int dim, double eqDist) {
        super(dim, eqDist,
                //Manhattan);
                Euclidean);

        //reusedCurrentPosition = new ArrayRealVector(dim);
    }



    @Override
    public void init(TermNode n) {
        float scale = this.scale.floatValue();

        n.move(-scale/2 + Math.random() * scale,
                -scale/2 + Math.random() * scale);
    }

    @Override public void getPosition(TermNode node, double[] v) {
        if (node == null) return; //shouldnt happen
        node.getPosition(v);
    }

    @Override
    public void move(TermNode node, double v0, double v1) {
        node.move(v0, v1);
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
    public void run(SpaceGrapher graph, int i) {

        init();

        termList = graph.displayed;
        this.graph = graph;

        align(i);

        apply();
    }

    protected void init() {
        resetLearning();
        setLearningRate(0.6f);
        setRepulsiveWeakness(repulseWeakness.get());
        setAttractionStrength(attractionStrength.get());
        setMaxRepulsionDistance(2000);
        setEquilibriumDistance(equilibriumDistance.floatValue());
        _nodeSpeed = nodeSpeed.floatValue();
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

        return termNode.priNorm * 1 * scaleFactor;

    }

    @Override
    protected void edges(TermNode t, ObjectDoubleHashMap<TermNode> neighbors) {

        for (TermEdge e : t.getEdges())
            neighbors.put(graph.getTermNode(e.bSrc.getTerm()), e.getWeight());
    }

//    @Override
//    public double getSpeedFactor(TermNode<N> termNode) {
//        //return 120 + 120 / termNode.width(); //heavier is slower, forcing smaller ones to move faster around it
//        return scaleFactor * 1.5f;
//    }


    @Override
    public void apply(TermNode node, double[] dataRef) {

        node.move(dataRef[0], dataRef[1], _nodeSpeed);
    }

    @Override
    protected TermNode[] getVertices() {
        scaleFactor = 35 + 3 * Math.sqrt(1 + termList.length);
        setScale(scaleFactor);


        //TODO avoid copying
        return termList;
    }
}

//    @Override
//    protected void edges(TermNode nodeToQuery, Consumer<N> updateFunc) {
//
//    }
//
//    @Override
//    protected void edges(final TermNode nodeToQuery, Consumer<TermNode<N>> updateFunc) {
////                    for (final TermEdge e : edges.row(nodeToQuery.term).values()) {
////                        updateFunc.accept(e.otherNode(nodeToQuery));
////                    }
//
//        for (final TermEdge e : nodeToQuery.getEdges()) {
//            if (e!=null && e.visible)
//                updateFunc.accept(e.otherNode(nodeToQuery));
//        }
//
//            /*edges.values().forEach(e ->
//                    updateFunc.accept(e.otherNode(nodeToQuery)));*/
//
//    }
//
//}
