/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphstream.stream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * An object capable of replaying graph events, useful in synchronizing the
 * graph and ui.
 *
 * @author trajar
 */
public interface Replayer
{
    void replayNodes(Iterable<Node> nodes);
    void replayEdges(Iterable<Edge> edges);
    void replayGraph(Graph graph);
}
