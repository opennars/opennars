//package org.graphstream.graph.implementations;
//
//import org.graphstream.graph.Edge;
//
//import java.util.Iterator;
//import java.util.NoSuchElementException;
//
///**
// * Created by me on 6/3/15.
// */
//public class EdgeIterator<T extends Edge> implements Iterator<T> {
//    private AdjacencyListGraph nodes;
//    int iNext = 0;
//    int iPrev = -1;
//
//    public EdgeIterator(AdjacencyListGraph nodes) {
//        this.nodes = nodes;
//    }
//
//    @Override
//    public boolean hasNext() {
//        return iNext < nodes.edgeCount;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public T next() {
//        if (iNext >= nodes.edgeCount)
//            throw new NoSuchElementException();
//        iPrev = iNext++;
//        return (T) nodes.edgeArray.get(iPrev);
//    }
//
//    @Override
//    public void remove() {
//        if (iPrev == -1)
//            throw new IllegalStateException();
//        nodes.removeEdge(nodes.edgeArray.get(iPrev), true, true, true);
//        iNext = iPrev;
//        iPrev = -1;
//    }
//}
