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
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A replayer that assumes both graph and ui are operating in the same local
 * thread.
 *
 * @author trajar
 */
public class LocalReplayer implements Replayer
{
    private static final Logger logger = LoggerFactory.getLogger(LocalReplayer.class);

    private final Graph destGraph;

    public LocalReplayer(final Graph dest)
    {
        this.destGraph = dest;
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
            Node destNode;
            try
            {
                destNode = this.destGraph.addNode(nodeId);
            }
            catch (final IdAlreadyInUseException e)
            {
                logger.debug("Node [" + node.getId() + "] already exists.", e);
                destNode = this.destGraph.getNode(nodeId);
            }
            if (destNode != null)
            {
                final Collection<String> attributes = node.getAttributeKeySet();
                if (attributes != null && !attributes.isEmpty())
                {
                    for (final String key : attributes)
                    {
                        destNode.addAttribute(key, node.getAttribute(key));
                    }
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
            Edge destEge;
            try
            {
                destEge = this.destGraph.addEdge(edgeId, edge.getSourceNode().getId(), edge.getTargetNode().getId(), edge.isDirected());
            }
            catch (final IdAlreadyInUseException e)
            {
                logger.debug("Edge [" + edge.getId() + "] already exists.", e);
                destEge = this.destGraph.getEdge(edgeId);
            }
            if (destEge != null)
            {
                final Collection<String> attributes = edge.getAttributeKeySet();
                if (attributes != null && !attributes.isEmpty())
                {
                    for (final String key : attributes)
                    {
                        destEge.addAttribute(key, edge.getAttribute(key));
                    }
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
                this.destGraph.addAttribute(key, graph.getAttribute(key));
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
