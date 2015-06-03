/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphstream.stream;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * A replayer that sends events to sink.
 *
 * @author trajar
 */
public class SinkReplayer implements Replayer
{
    private final String sourceId;

    private final Sink sink;

    private final AtomicInteger timer = new AtomicInteger();

    public SinkReplayer(final Sink sink)
    {
        this(SinkReplayer.class.getSimpleName() + "#" + UUID.randomUUID(), sink);
    }

    public SinkReplayer(final String id, final Sink sink)
    {
        if (null == id)
        {
            throw new IllegalArgumentException("Id canont be null.");
        }
        if (null == sink)
        {
            throw new IllegalArgumentException("Sink cannot be null.");
        }
        this.sourceId = id;
        this.sink = sink;
    }

    @Override
    public void replayNodes(final Iterable<Node> nodes)
    {
        if (null == nodes)
        {
            return;
        }

        for (final Node node : nodes)
        {
            final String nodeId = node.getId();
            this.sink.nodeAdded(sourceId, timer.incrementAndGet(), nodeId);
            final Collection<String> attributes = node.getAttributeKeySet();
            if (attributes != null && !attributes.isEmpty())
            {
                for (final String key : attributes)
                {
                    this.sink.nodeAttributeAdded(sourceId, timer.incrementAndGet(), nodeId, key, node.getAttribute(key));
                }
            }
        }
    }

    @Override
    public void replayEdges(final Iterable<Edge> edges)
    {
        if (null == edges)
        {
            return;
        }

        for (final Edge edge : edges)
        {
            final String edgeId = edge.getId();
            this.sink.edgeAdded(sourceId, timer.incrementAndGet(), edgeId, edge.getSourceNode().getId(), edge.getTargetNode().getId(), edge.isDirected());
            final Collection<String> attributes = edge.getAttributeKeySet();
            if (attributes != null && !attributes.isEmpty())
            {
                for (final String key : attributes)
                {
                    this.sink.edgeAttributeAdded(sourceId, timer.incrementAndGet(), edgeId, key, edge.getAttribute(key));
                }
            }
        }
    }

    @Override
    public void replayGraph(final Graph graph)
    {
        final Collection<String> attributes = graph.getAttributeKeySet();
        if (attributes != null && !attributes.isEmpty())
        {
            for (final String key : attributes)
            {
                this.sink.graphAttributeAdded(sourceId, timer.incrementAndGet(), key, graph.getAttribute(key));
            }
        }

        for (final Node node : graph)
        {
            this.replayNodes(Arrays.asList(node));
        }

        for (final Edge edge : graph.getEachEdge())
        {
            this.replayEdges(Arrays.asList(edge));
        }
    }
}
