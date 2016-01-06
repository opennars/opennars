///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package automenta.vivisect.dimensionalize;
//
//import automenta.vivisect.graph.AbstractGraphVis;
//import automenta.vivisect.graph.EdgeVis;
//import automenta.vivisect.graph.GraphDisplay;
//import automenta.vivisect.graph.VertexVis;
//import nars.logic.entity.BudgetValue.Budgetable;
//import org.apache.commons.math3.linear.ArrayRealVector;
//
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * @author me
// */
//public class HyperassociativeLayout<V,E> implements GraphDisplay<V,E> {
//
//    HyperassociativeMap<V,E> h = null;
//    float scale = 200.0f, eqDistance = 1f;
//    boolean normalizing = true;
//
//    private AtomicBoolean newNode = new AtomicBoolean(false);
//
//    public void setScale(float scale) {
//        this.scale = scale;
//    }
//
//    public void setEdgeDistance(float eqDistance) {
//        this.eqDistance = eqDistance;
//    }
//
//    public void setNormalize(boolean normalizing) {
//        this.normalizing = normalizing;
//    }
//
//    @Override
//    public boolean preUpdate(AbstractGraphVis g) {
//
//
//        if (h == null)
//            h = new HyperassociativeMap<V,E>(g.getGraph(), HyperassociativeMap.Euclidean, 2) {
//                @Override
//                public ArrayRealVector newPosition(V node) {
//                    newNode.set(true);
//                    return super.newPosition(node);
//                }
//
//                @Override
//                public double getEdgeWeight(Object e) {
//                    if (e instanceof Budgetable) {
//                        return 1.0 + ((Budgetable) e).getBudget().getPriority() * 1.0;
//                    }
//                    return 1;
//                }
//
//                @Override
//                public boolean normalize() {
//                    return normalizing;
//                }
//
//
//                @Override
//                public double getRadius(Object n) {
//                    if (n instanceof Budgetable) {
//                        return 1.0 + ((Budgetable) n).getBudget().getPriority() * 1.0;
//                    }
//                    return 1;
//                }
//
//
//            };
//        else {
//            if (newNode.get()) {
//                h.resetLearning();
//                newNode.set(false);
//            }
//
//            h.setGraph(g.getGraph());
//        }
//        h.setEquilibriumDistance(eqDistance);
//        h.align();
//        return true;
//    }
//
//
//    @Override
//    public void vertex(AbstractGraphVis<V,E> g, VertexVis<V,E> v) {
//        if (h == null) return;
//        if (v == null) return;
//        if (v.vertex == null) return;
//
//        ArrayRealVector c = h.getPosition((V)v.vertex);
//        if (c == null) return;
//
//
//        if (canChangePosition(v.vertex)) {
//            double[] cc = c.getDataRef();
//            v.tx = (float) cc[0] * scale;
//            v.ty = (float) cc[1] * scale;
//        }
//
//    }
//
//    public boolean canChangePosition(Object vertex) {
//        return true;
//    }
//
//    @Override
//    public void edge(AbstractGraphVis g, EdgeVis e) {
//    }
//
// }
