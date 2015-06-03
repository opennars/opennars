/*
 * Copyright 2006 - 2013
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.stream.thread;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.graphstream.graph.Graph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.stream.Replayable;
import org.graphstream.stream.Replayable.Controller;
import org.graphstream.stream.Sink;
import org.graphstream.stream.Source;
import org.graphstream.stream.SourceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter that allows to pass graph events between two threads without explicit
 * synchronization.
 * 
 * <p>
 * This filter allows to register it as an output for some source of events in a
 * source thread (hereafter called the input thread) and to register listening
 * outputs in a destination thread (hereafter called the sink thread).
 * </p>
 * 
 * <pre>
 *                       |
 *   Source ---> ThreadProxyFilter ----> Sink
 *  Thread 1             |              Thread 2
 *                       |
 * </pre>
 * 
 * <p>
 * In other words, this class allows to listen in a sink thread graph events
 * that are produced in another source thread without any explicit
 * synchronization on the source of events.
 * </p>
 * 
 * <p>
 * The only restriction is that the sink thread must regularly call the
 * {@link #pump()} method to dispatch events coming from the source to all sinks
 * registered (see the explanation in {@link org.graphstream.stream.ProxyPipe}).
 * </p>
 * 
 * <p>
 * You can register any kind of input as source of event, but if the input is a
 * graph, then you can choose to "replay" all the content of the graph so that
 * at the other end of the filter, all outputs receive the complete content of
 * the graph. This is the default behavior if this filter is constructed with a
 * graph as input.
 * </p>
 */
public class ThreadProxyPipe extends SourceBase implements ProxyPipe {

    /**
     * class level logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ThreadProxyPipe.class);

	/**
	 * Proxy id.
	 */
	private String id;

	/**
	 * The event sender name, usually the graph name.
	 */
	private String from;

	/**
	 * The message box used to exchange messages between the two threads.
	 */
    private final BlockingQueue<GraphMessage> queue;

	/**
	 * Used only to remove the listener. We ensure this is done in the source
	 * thread.
	 */
	protected Source input;

	/**
	 * Signals that this proxy must be removed from the source input.
	 */
	protected boolean unregisterWhenPossible = false;

	public ThreadProxyPipe() {
        super(ThreadProxyPipe.class.getSimpleName() + "#" + UUID.randomUUID());
		this.queue = new LinkedBlockingQueue<>(); 
		this.from = "<in>";
		this.input = null;
	}

	/**
	 * 
	 * @param input
	 *            The source of events we listen at.
	 * 
	 * @deprecated Use the default constructor and then call the
	 *             {@link #init(Source)} method.
	 */
	@Deprecated
	public ThreadProxyPipe(Source input) {
		this(input, null, input instanceof Replayable);
	}

	/**
	 * 
	 * @param input
	 * @param replay
	 * 
	 * @deprecated Use the default constructor and then call the
	 *             {@link #init(Source)} method.
	 */
	@Deprecated
	public ThreadProxyPipe(Source input, boolean replay) {
		this(input, null, replay);
	}

	/**
	 * 
	 * @param input
	 * @param initialListener
	 * @param replay
	 * 
	 * @deprecated Use the default constructor and then call the
	 *             {@link #init(Source)} method.
	 */
	@Deprecated
	public ThreadProxyPipe(Source input, Sink initialListener, boolean replay) {
		this();

		if (initialListener != null)
			addSink(initialListener);

		this.init(input, replay);
	}

	public void init() {
		init(null, false);
	}

	/**
	 * Init the proxy. If there are previous events, they will be cleared.
	 * 
	 * @param source
	 *            source of the events
	 */
	public void init(Source source) {
		init(source, source instanceof Replayable);
	}

	/**
	 * Init the proxy. If there are previous events, they will be cleared.
	 * 
	 * @param source
	 *            source of the events
	 * @param replay
	 *            true if the source should be replayed. You need a
	 *            {@link org.graphstream.stream.Replayable} source to enable
	 *            replay, else nothing happens.
	 */
	public void init(final Source source, final boolean replay) {		
        if (this.input != null) {
            this.input.removeSink(this);
        }
        this.input = source;
        this.queue.clear();
		
		if (source != null) {
			if (source instanceof Graph)
				this.from = ((Graph) source).getId();

			this.input.addSink(this);

			if (replay && source instanceof Replayable) {
				Replayable r = (Replayable) source;
				Controller rc = r.getReplayController();

				rc.addSink(this);
				rc.replay();
			}
		}
	}

	@Override
	public String toString() {
		return String.format("thread-proxy(from %s)", from);
	}

	/**
	 * Ask the proxy to unregister from the event input source (stop receive
	 * events) as soon as possible (when the next event will occur in the
	 * graph).
	 */
	public void unregisterFromSource() {
		unregisterWhenPossible = true;
	}

    @Override
	public void pump() {
       this.pump(-1);
	}
    
    @Override
    public void pump(final int maxEvents) {
        int num = 0;
        while (!this.queue.isEmpty())
        {
            final GraphMessage msg = this.queue.poll();
            if (msg != null)
            {
                num++;
                this.processMessage(msg.event, msg.data);
                if (maxEvents > 0 && maxEvents <= num) { 
                    break;
                }
            }
        }
    }

	@Override
	public void blockingPump() throws InterruptedException {
		blockingPump(0);
	}

    @Override
	public void blockingPump(final long timeout) throws InterruptedException {
        try
        {
            final GraphMessage msg;
            if (timeout > 0) { 
                msg = this.queue.poll(timeout, TimeUnit.MILLISECONDS);
            } else { 
                msg = this.queue.take();                
            }
            if (msg != null)
            {
                this.processMessage(msg.event, msg.data);
            }
        }
        catch (final InterruptedException e)
        {
            logger.warn("Unable to poll graph pipe event.", e);
        }		
        this.pump();
	}

	public boolean hasPostRemaining() {
		return !this.queue.isEmpty();
	}

	
	private static enum GraphEvents {
		ADD_NODE, DEL_NODE, ADD_EDGE, DEL_EDGE, STEP, CLEARED, ADD_GRAPH_ATTR, CHG_GRAPH_ATTR, DEL_GRAPH_ATTR, ADD_NODE_ATTR, CHG_NODE_ATTR, DEL_NODE_ATTR, ADD_EDGE_ATTR, CHG_EDGE_ATTR, DEL_EDGE_ATTR
	};
    
    
    private static class GraphMessage { 
       private final GraphEvents event;
       private final Object[] data;
       public GraphMessage(final GraphEvents e, final Object[] d) {
           this.event = e;
           this.data = d;
       }
    }
    

    private boolean maybeUnregister() {
		if (unregisterWhenPossible) {
			if (input != null)
				input.removeSink(this);
			return true;
		}

		return false;
	}

	private boolean post(final GraphEvents event, final Object... data) {
        if (null == event) {
            return false;
        }
        if (null == data || data.length <= 0) {
            return false;
        }
        try { 
            this.queue.put(new GraphMessage(event, data));
            return true;
        } catch (final Exception e) {
            logger.warn("Unable to added graph message to pipe.", e);
            return true;
        }
	}

    @Override
	public void edgeAttributeAdded(String graphId, long timeId, String edgeId,
			String attribute, Object value) {
		if (maybeUnregister())
			return;

		post(GraphEvents.ADD_EDGE_ATTR, graphId, timeId, edgeId, attribute, value);
	}

    @Override
	public void edgeAttributeChanged(String graphId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		if (maybeUnregister())
			return;

		post(GraphEvents.CHG_EDGE_ATTR, graphId, timeId, edgeId, attribute, oldValue, newValue);
	}

    @Override
	public void edgeAttributeRemoved(String graphId, long timeId,
			String edgeId, String attribute) {
		if (maybeUnregister())
			return;

		post(GraphEvents.DEL_EDGE_ATTR, graphId, timeId, edgeId, attribute);
	}

    @Override
	public void graphAttributeAdded(String graphId, long timeId,
			String attribute, Object value) {
		if (maybeUnregister())
			return;

		post(GraphEvents.ADD_GRAPH_ATTR, graphId, timeId, attribute, value);
	}

    @Override
	public void graphAttributeChanged(String graphId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		if (maybeUnregister())
			return;

		post(GraphEvents.CHG_GRAPH_ATTR, graphId, timeId, attribute, oldValue, newValue);
	}

    @Override
	public void graphAttributeRemoved(String graphId, long timeId,
			String attribute) {
		if (maybeUnregister())
			return;

		post(GraphEvents.DEL_GRAPH_ATTR, graphId, timeId, attribute);
	}

    @Override
	public void nodeAttributeAdded(String graphId, long timeId, String nodeId,
			String attribute, Object value) {
		if (maybeUnregister())
			return;

		post(GraphEvents.ADD_NODE_ATTR, graphId, timeId, nodeId, attribute, value);
	}

    @Override
	public void nodeAttributeChanged(String graphId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		if (maybeUnregister())
			return;

		post(GraphEvents.CHG_NODE_ATTR, graphId, timeId, nodeId, attribute, oldValue, newValue);
	}

    @Override
	public void nodeAttributeRemoved(String graphId, long timeId,
			String nodeId, String attribute) {
		if (maybeUnregister())
			return;

		post(GraphEvents.DEL_NODE_ATTR, graphId, timeId, nodeId, attribute);
	}

    @Override
	public void edgeAdded(String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		if (maybeUnregister())
			return;

		post(GraphEvents.ADD_EDGE, graphId, timeId, edgeId, fromNodeId, toNodeId, directed);
	}
    
    @Override
	public void edgeRemoved(String graphId, long timeId, String edgeId) {
		if (maybeUnregister())
			return;

		post(GraphEvents.DEL_EDGE, graphId, timeId, edgeId);
	}

    @Override
	public void graphCleared(String graphId, long timeId) {
		if (maybeUnregister())
			return;

		post(GraphEvents.CLEARED, graphId, timeId);
	}

    @Override
	public void nodeAdded(String graphId, long timeId, String nodeId) {
		if (maybeUnregister())
			return;

		post(GraphEvents.ADD_NODE, graphId, timeId, nodeId);
	}

    @Override
	public void nodeRemoved(String graphId, long timeId, String nodeId) {
		if (maybeUnregister())
			return;

		post(GraphEvents.DEL_NODE, graphId, timeId, nodeId);
	}

    @Override
	public void stepBegins(String graphId, long timeId, double step) {
		if (maybeUnregister())
			return;

		post(GraphEvents.STEP, graphId, timeId, step);
	}

	// MBoxListener

	private void processMessage(final GraphEvents e, final Object[] data) {
		String graphId, elementId, attribute;
		Long timeId;
		Object newValue, oldValue;

		switch (e) {
		case ADD_NODE:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			elementId = (String) data[2];

			sendNodeAdded(graphId, timeId, elementId);
			break;
		case DEL_NODE:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			elementId = (String) data[2];

			sendNodeRemoved(graphId, timeId, elementId);
			break;
		case ADD_EDGE:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			elementId = (String) data[2];

			String fromId = (String) data[3];
			String toId = (String) data[4];
			boolean directed = (Boolean) data[5];

			sendEdgeAdded(graphId, timeId, elementId, fromId, toId, directed);
			break;
		case DEL_EDGE:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			elementId = (String) data[2];

			sendEdgeRemoved(graphId, timeId, elementId);
			break;
		case STEP:
			graphId = (String) data[0];
			timeId = (Long) data[1];

			double step = (Double) data[2];

			sendStepBegins(graphId, timeId, step);
			break;
		case ADD_GRAPH_ATTR:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			attribute = (String) data[2];
			newValue = data[3];

			sendGraphAttributeAdded(graphId, timeId, attribute, newValue);
			break;
		case CHG_GRAPH_ATTR:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			attribute = (String) data[2];
			oldValue = data[3];
			newValue = data[4];

			sendGraphAttributeChanged(graphId, timeId, attribute, oldValue,
					newValue);
			break;
		case DEL_GRAPH_ATTR:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			attribute = (String) data[2];

			sendGraphAttributeRemoved(graphId, timeId, attribute);
			break;
		case ADD_EDGE_ATTR:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			elementId = (String) data[2];
			attribute = (String) data[3];
			newValue = data[4];

			sendEdgeAttributeAdded(graphId, timeId, elementId, attribute,
					newValue);
			break;
		case CHG_EDGE_ATTR:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			elementId = (String) data[2];
			attribute = (String) data[3];
			oldValue = data[4];
			newValue = data[5];

			sendEdgeAttributeChanged(graphId, timeId, elementId, attribute,
					oldValue, newValue);
			break;
		case DEL_EDGE_ATTR:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			elementId = (String) data[2];
			attribute = (String) data[3];

			sendEdgeAttributeRemoved(graphId, timeId, elementId, attribute);
			break;
		case ADD_NODE_ATTR:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			elementId = (String) data[2];
			attribute = (String) data[3];
			newValue = data[4];

			sendNodeAttributeAdded(graphId, timeId, elementId, attribute,
					newValue);
			break;
		case CHG_NODE_ATTR:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			elementId = (String) data[2];
			attribute = (String) data[3];
			oldValue = data[4];
			newValue = data[5];

			sendNodeAttributeChanged(graphId, timeId, elementId, attribute,
					oldValue, newValue);
			break;
		case DEL_NODE_ATTR:
			graphId = (String) data[0];
			timeId = (Long) data[1];
			elementId = (String) data[2];
			attribute = (String) data[3];

			sendNodeAttributeRemoved(graphId, timeId, elementId, attribute);
			break;
		case CLEARED:
			graphId = (String) data[0];
			timeId = (Long) data[1];

			sendGraphCleared(graphId, timeId);
			break;
		default:
            logger.warn(String.format("Unknown message %s.", e));
			break;
		}
	}
}