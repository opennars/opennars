/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphstream.stream;

import java.util.Arrays;
import java.util.Collection;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.thread.ThreadProxyPipe;

/**
 * A replayer this sends events through a proxy pipe.
 *
 * @author trajar
 */
public class ProxyPipeReplayer implements Replayer
{
    private final ThreadProxyPipe proxy;

    public ProxyPipeReplayer(final ThreadProxyPipe proxy)
    {
        this.proxy = proxy;
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
            this.proxy.sendNodeAdded(this.proxy.getId(), nodeId);
            final Collection<String> attributes = node.getAttributeKeySet();
            if (attributes != null && !attributes.isEmpty())
            {
                for (final String key : attributes)
                {
                    this.proxy.sendNodeAttributeAdded(this.proxy.getId(), nodeId, key, node.getAttribute(key));
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
            this.proxy.sendEdgeAdded(this.proxy.getId(), edgeId, edge.getSourceNode().getId(), edge.getTargetNode().getId(), edge.isDirected());
            final Collection<String> attributes = edge.getAttributeKeySet();
            if (attributes != null && !attributes.isEmpty())
            {
                for (final String key : attributes)
                {
                    this.proxy.sendEdgeAttributeAdded(this.proxy.getId(), edgeId, key, edge.getAttribute(key));
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
                this.proxy.sendGraphAttributeAdded(this.proxy.getId(), key, graph.getAttribute(key));
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
