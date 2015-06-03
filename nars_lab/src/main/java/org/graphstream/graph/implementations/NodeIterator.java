//package org.graphstream.graph.implementations;
//
//import org.graphstream.graph.Node;
//
//import java.util.Iterator;
//import java.util.NoSuchElementException;
//
///**
// * Created by me on 6/3/15.
// */
//public class NodeIterator<T extends Node> implements Iterator<T> {
//    private AdjacencyListGraph nodes;
//    int iNext = 0;
//    int iPrev = -1;
//
//    public NodeIterator(AdjacencyListGraph nodes) {
//        this.nodes = nodes;
//    }
//
//    @Override
//    public boolean hasNext() {
//        return iNext < nodes.nodeCount;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public T next() {
//        if (iNext >= nodes.nodeCount)
//            throw new NoSuchElementException();
//        iPrev = iNext++;
//        return (T) nodes.nodeArray.get(iPrev);
//    }
//
//    @Override
//    public void remove() {
//        if (iPrev == -1)
//            throw new IllegalStateException();
//        nodes.removeNode(nodes.nodeArray.get(iPrev), true);
//        iNext = iPrev;
//        iPrev = -1;
//    }
//}
