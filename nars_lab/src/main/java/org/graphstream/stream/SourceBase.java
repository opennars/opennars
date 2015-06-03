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
package org.graphstream.stream;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import org.graphstream.graph.Element.AttributeChangeEvent;
import org.graphstream.stream.sync.SourceTime;

/**
 * Base implementation of an input that provide basic sink handling.
 * 
 * <p>
 * This implementation can register a set of graph sinks (or separate sets of
 * attributes or elements sinks) and provides protected methods to easily
 * broadcast events to all the sinks (beginning with "send").
 * </p>
 * 
 * <p>
 * Each time you want to produce an event toward all registered sinks, you call
 * one of the "send*" methods with correct parameters. The parameters of the
 * "send*" methods maps to the usual GraphStream events.
 * </p>
 * 
 * <p>
 * This class is "reentrant". This means that if a send*() method is called
 * during the execution of another or the same send*() method, the event is
 * deferred until the first send*() method is finished. This avoid recursive
 * loops if a sink modifies the input during event handling.
 * </p>
 */
public abstract class SourceBase implements Source {
	// Attribute

	public enum ElementType {
		NODE, EDGE, GRAPH
	};

	/**
	 * Set of graph attributes sinks.
	 */
	private final Set<AttributeSink> attrSinks = new HashSet<>();

	/**
	 * Set of graph elements sinks.
	 */
    private final Set<ElementSink> eltsSinks = new HashSet<>();

	/**
	 * A queue that allow the management of events (nodes/edge
	 * add/delete/change) in the right order.
	 */
    private final Queue<GraphEvent> eventQueue = new ArrayDeque<>();

	/**
	 * A boolean that indicates whether or not an Sink event is being sent
	 * during another one.
	 */
    private boolean eventProcessing = false;

	/**
	 * Id of this source.
	 */
    protected String sourceId;
	
	/**
	 * Time of this source.
	 */
    protected SourceTime sourceTime;

	// Construction

	protected SourceBase() {
		this(SourceBase.class.getSimpleName() + "#" + UUID.randomUUID());
	}

	protected SourceBase(String sourceId) {
		this.sourceId = sourceId;
		this.sourceTime = new SourceTime(sourceId);
	}

	// Access

	public Iterable<AttributeSink> attributeSinks() {
		return this.attrSinks;
	}

	public Iterable<ElementSink> elementSinks() {
		return this.eltsSinks;
	}
    
    public String getId() { 
        return this.sourceId;
    }

	// Command

    @Override
	public void addSink(Sink sink) {
		addAttributeSink(sink);
		addElementSink(sink);
	}

    @Override
	public void addAttributeSink(AttributeSink sink) {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

			attrSinks.add(sink);

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new AddToListEvent<>(attrSinks, sink));
		}
	}

    @Override
	public void addElementSink(ElementSink sink) {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

			eltsSinks.add(sink);

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new AddToListEvent<>(eltsSinks, sink));
		}
	}

    @Override
	public void clearSinks() {
		clearElementSinks();
		clearAttributeSinks();
	}

    @Override
	public void clearElementSinks() {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

			eltsSinks.clear();

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new ClearListEvent<>(eltsSinks));
		}
	}

    @Override
	public void clearAttributeSinks() {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

			attrSinks.clear();

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new ClearListEvent<>(attrSinks));
		}
	}

    @Override
	public void removeSink(Sink sink) {
		removeAttributeSink(sink);
		removeElementSink(sink);
	}

    @Override
	public void removeAttributeSink(AttributeSink sink) {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

			attrSinks.remove(sink);

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new RemoveFromListEvent<>(attrSinks,
					sink));
		}
	}

    @Override
	public void removeElementSink(ElementSink sink) {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

			eltsSinks.remove(sink);

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue
					.add(new RemoveFromListEvent<>(eltsSinks, sink));
		}
	}

	/**
	 * Send a "graph cleared" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 */
	public void sendGraphCleared(String sourceId) {
		sendGraphCleared(sourceId, sourceTime.newEvent());
	}

	/**
	 * Send a "graph cleared" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 */
	public void sendGraphCleared(String sourceId, long timeId) {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

            for (final ElementSink sink : this.eltsSinks)
				sink.graphCleared(sourceId, timeId);

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new BeforeGraphClearEvent(sourceId, timeId));
		}
	}

	/**
	 * Send a "step begins" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The graph identifier.
	 * @param step
	 *            The step time stamp.
	 */
	public void sendStepBegins(String sourceId, double step) {
		sendStepBegins(sourceId, sourceTime.newEvent(), step);
	}

	/**
	 * Send a "step begins" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The graph identifier.
	 * @param timeId
	 * @param step
	 *            The step time stamp.
	 */
	public void sendStepBegins(String sourceId, long timeId, double step) {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

            for (final ElementSink sink : this.eltsSinks)
                sink.stepBegins(sourceId, timeId, step);

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new StepBeginsEvent(sourceId, timeId, step));
		}
	}

	/**
	 * Send a "node added" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param nodeId
	 *            The node identifier.
	 */
	public void sendNodeAdded(String sourceId, String nodeId) {
		sendNodeAdded(sourceId, sourceTime.newEvent(), nodeId);
	}

	/**
	 * Send a "node added" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param nodeId
	 *            The node identifier.
	 */
	public void sendNodeAdded(String sourceId, long timeId, String nodeId) {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

            for (final ElementSink sink : this.eltsSinks)
				sink.nodeAdded(sourceId, timeId, nodeId);

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new AfterNodeAddEvent(sourceId, timeId, nodeId));
		}
	}

	/**
	 * Send a "node removed" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The graph identifier.
	 * @param nodeId
	 *            The node identifier.
	 */
	public void sendNodeRemoved(String sourceId, String nodeId) {
		sendNodeRemoved(sourceId, sourceTime.newEvent(), nodeId);
	}

	/**
	 * Send a "node removed" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The graph identifier.
	 * @param timeId
	 * @param nodeId
	 *            The node identifier.
	 */
	public void sendNodeRemoved(String sourceId, long timeId, String nodeId) {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

            for (final ElementSink sink : this.eltsSinks)
				sink.nodeRemoved(sourceId, timeId, nodeId);

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new BeforeNodeRemoveEvent(sourceId, timeId, nodeId));
		}
	}

	/**
	 * Send an "edge added" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param edgeId
	 *            The edge identifier.
	 * @param fromNodeId
	 *            The edge start node.
	 * @param toNodeId
	 *            The edge end node.
	 * @param directed
	 *            Is the edge directed?.
	 */
	public void sendEdgeAdded(String sourceId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		sendEdgeAdded(sourceId, sourceTime.newEvent(), edgeId, fromNodeId,
				toNodeId, directed);
	}

	/**
	 * Send an "edge added" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param edgeId
	 *            The edge identifier.
	 * @param fromNodeId
	 *            The edge start node.
	 * @param toNodeId
	 *            The edge end node.
	 * @param directed
	 *            Is the edge directed?.
	 */
	public void sendEdgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

            for (final ElementSink sink : this.eltsSinks)
				sink.edgeAdded(sourceId, timeId, edgeId, fromNodeId, toNodeId, directed);

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new AfterEdgeAddEvent(sourceId, timeId, edgeId,
					fromNodeId, toNodeId, directed));
		}
	}

	/**
	 * Send a "edge removed" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param edgeId
	 *            The edge identifier.
	 */
	public void sendEdgeRemoved(String sourceId, String edgeId) {
		sendEdgeRemoved(sourceId, sourceTime.newEvent(), edgeId);
	}

	/**
	 * Send a "edge removed" event to all element sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param edgeId
	 *            The edge identifier.
	 */
	public void sendEdgeRemoved(String sourceId, long timeId, String edgeId) {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

            for (final ElementSink sink : this.eltsSinks)
				sink.edgeRemoved(sourceId, timeId, edgeId);

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new BeforeEdgeRemoveEvent(sourceId, timeId, edgeId));
		}
	}

	/**
	 * Send a "edge attribute added" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param edgeId
	 *            The edge identifier.
	 * @param attribute
	 *            The attribute name.
	 * @param value
	 *            The attribute value.
	 */
	public void sendEdgeAttributeAdded(String sourceId, String edgeId,
			String attribute, Object value) {
		sendAttributeChangedEvent(sourceId, edgeId, ElementType.EDGE,
				attribute, AttributeChangeEvent.ADD, null, value);
	}

	/**
	 * Send a "edge attribute added" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param edgeId
	 *            The edge identifier.
	 * @param attribute
	 *            The attribute name.
	 * @param value
	 *            The attribute value.
	 */
	public void sendEdgeAttributeAdded(String sourceId, long timeId,
			String edgeId, String attribute, Object value) {
		sendAttributeChangedEvent(sourceId, timeId, edgeId, ElementType.EDGE,
				attribute, AttributeChangeEvent.ADD, null, value);
	}

	/**
	 * Send a "edge attribute changed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param edgeId
	 *            The edge identifier.
	 * @param attribute
	 *            The attribute name.
	 * @param oldValue
	 *            The old attribute value.
	 * @param newValue
	 *            The new attribute value.
	 */
	public void sendEdgeAttributeChanged(String sourceId, String edgeId,
			String attribute, Object oldValue, Object newValue) {
		sendAttributeChangedEvent(sourceId, edgeId, ElementType.EDGE,
				attribute, AttributeChangeEvent.CHANGE, oldValue, newValue);
	}

	/**
	 * Send a "edge attribute changed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param edgeId
	 *            The edge identifier.
	 * @param attribute
	 *            The attribute name.
	 * @param oldValue
	 *            The old attribute value.
	 * @param newValue
	 *            The new attribute value.
	 */
	public void sendEdgeAttributeChanged(String sourceId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		sendAttributeChangedEvent(sourceId, timeId, edgeId, ElementType.EDGE,
				attribute, AttributeChangeEvent.CHANGE, oldValue, newValue);
	}

	/**
	 * Send a "edge attribute removed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param edgeId
	 *            The edge identifier.
	 * @param attribute
	 *            The attribute name.
	 */
	public void sendEdgeAttributeRemoved(String sourceId, String edgeId,
			String attribute) {
		sendAttributeChangedEvent(sourceId, edgeId, ElementType.EDGE,
				attribute, AttributeChangeEvent.REMOVE, null, null);
	}

	/**
	 * Send a "edge attribute removed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param edgeId
	 *            The edge identifier.
	 * @param attribute
	 *            The attribute name.
	 */
	public void sendEdgeAttributeRemoved(String sourceId, long timeId,
			String edgeId, String attribute) {
		sendAttributeChangedEvent(sourceId, timeId, edgeId, ElementType.EDGE,
				attribute, AttributeChangeEvent.REMOVE, null, null);
	}

	/**
	 * Send a "graph attribute added" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param attribute
	 *            The attribute name.
	 * @param value
	 *            The attribute value.
	 */
	public void sendGraphAttributeAdded(String sourceId, String attribute,
			Object value) {
		sendAttributeChangedEvent(sourceId, null, ElementType.GRAPH, attribute,
				AttributeChangeEvent.ADD, null, value);
	}

	/**
	 * Send a "graph attribute added" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param attribute
	 *            The attribute name.
	 * @param value
	 *            The attribute value.
	 */
	public void sendGraphAttributeAdded(String sourceId, long timeId,
			String attribute, Object value) {
		sendAttributeChangedEvent(sourceId, timeId, null, ElementType.GRAPH,
				attribute, AttributeChangeEvent.ADD, null, value);
	}

	/**
	 * Send a "graph attribute changed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param attribute
	 *            The attribute name.
	 * @param oldValue
	 *            The attribute old value.
	 * @param newValue
	 *            The attribute new value.
	 */
	public void sendGraphAttributeChanged(String sourceId, String attribute,
			Object oldValue, Object newValue) {
		sendAttributeChangedEvent(sourceId, null, ElementType.GRAPH, attribute,
				AttributeChangeEvent.CHANGE, oldValue, newValue);
	}

	/**
	 * Send a "graph attribute changed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param attribute
	 *            The attribute name.
	 * @param oldValue
	 *            The attribute old value.
	 * @param newValue
	 *            The attribute new value.
	 */
	public void sendGraphAttributeChanged(String sourceId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		sendAttributeChangedEvent(sourceId, timeId, null, ElementType.GRAPH,
				attribute, AttributeChangeEvent.CHANGE, oldValue, newValue);
	}

	/**
	 * Send a "graph attribute removed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param attribute
	 *            The attribute name.
	 */
	public void sendGraphAttributeRemoved(String sourceId, String attribute) {
		sendAttributeChangedEvent(sourceId, null, ElementType.GRAPH, attribute,
				AttributeChangeEvent.REMOVE, null, null);
	}

	/**
	 * Send a "graph attribute removed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param attribute
	 *            The attribute name.
	 */
	public void sendGraphAttributeRemoved(String sourceId, long timeId,
			String attribute) {
		sendAttributeChangedEvent(sourceId, timeId, null, ElementType.GRAPH,
				attribute, AttributeChangeEvent.REMOVE, null, null);
	}

	/**
	 * Send a "node attribute added" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param nodeId
	 *            The node identifier.
	 * @param attribute
	 *            The attribute name.
	 * @param value
	 *            The attribute value.
	 */
	public void sendNodeAttributeAdded(String sourceId, String nodeId,
			String attribute, Object value) {
		sendAttributeChangedEvent(sourceId, nodeId, ElementType.NODE,
				attribute, AttributeChangeEvent.ADD, null, value);
	}

	/**
	 * Send a "node attribute added" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param nodeId
	 *            The node identifier.
	 * @param attribute
	 *            The attribute name.
	 * @param value
	 *            The attribute value.
	 */
	public void sendNodeAttributeAdded(String sourceId, long timeId,
			String nodeId, String attribute, Object value) {
		sendAttributeChangedEvent(sourceId, timeId, nodeId, ElementType.NODE,
				attribute, AttributeChangeEvent.ADD, null, value);
	}

	/**
	 * Send a "node attribute changed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param nodeId
	 *            The node identifier.
	 * @param attribute
	 *            The attribute name.
	 * @param oldValue
	 *            The attribute old value.
	 * @param newValue
	 *            The attribute new value.
	 */
	public void sendNodeAttributeChanged(String sourceId, String nodeId,
			String attribute, Object oldValue, Object newValue) {
		sendAttributeChangedEvent(sourceId, nodeId, ElementType.NODE,
				attribute, AttributeChangeEvent.CHANGE, oldValue, newValue);
	}

	/**
	 * Send a "node attribute changed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param nodeId
	 *            The node identifier.
	 * @param attribute
	 *            The attribute name.
	 * @param oldValue
	 *            The attribute old value.
	 * @param newValue
	 *            The attribute new value.
	 */
	public void sendNodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		sendAttributeChangedEvent(sourceId, timeId, nodeId, ElementType.NODE,
				attribute, AttributeChangeEvent.CHANGE, oldValue, newValue);
	}

	/**
	 * Send a "node attribute removed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param nodeId
	 *            The node identifier.
	 * @param attribute
	 *            The attribute name.
	 */
	public void sendNodeAttributeRemoved(String sourceId, String nodeId,
			String attribute) {
		sendAttributeChangedEvent(sourceId, nodeId, ElementType.NODE,
				attribute, AttributeChangeEvent.REMOVE, null, null);
	}

	/**
	 * Send a "node attribute removed" event to all attribute sinks.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param timeId
	 * @param nodeId
	 *            The node identifier.
	 * @param attribute
	 *            The attribute name.
	 */
	public void sendNodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attribute) {
		sendAttributeChangedEvent(sourceId, timeId, nodeId, ElementType.NODE,
				attribute, AttributeChangeEvent.REMOVE, null, null);
	}

	/**
	 * Send a add/change/remove attribute event on an element. This method is a
	 * generic way of notifying of an attribute change and is equivalent to
	 * individual send*Attribute*() methods.
	 * 
	 * @param sourceId
	 *            The source identifier.
	 * @param eltId
	 *            The changed element identifier.
	 * @param eltType
	 *            The changed element type.
	 * @param attribute
	 *            The changed attribute.
	 * @param event
	 *            The add/change/remove action.
	 * @param oldValue
	 *            The old attribute value (null if the attribute is removed or
	 *            added).
	 * @param newValue
	 *            The new attribute value (null if removed).
	 */
	public void sendAttributeChangedEvent(String sourceId, String eltId,
			ElementType eltType, String attribute, AttributeChangeEvent event,
			Object oldValue, Object newValue) {
		sendAttributeChangedEvent(sourceId, sourceTime.newEvent(), eltId,
				eltType, attribute, event, oldValue, newValue);
	}

	public void sendAttributeChangedEvent(String sourceId, long timeId,
			String eltId, ElementType eltType, String attribute,
			AttributeChangeEvent event, Object oldValue, Object newValue) {
		if (!eventProcessing) {
			eventProcessing = true;
			manageEvents();

			if (event == AttributeChangeEvent.ADD) {
				if (eltType == ElementType.NODE) {
                    for (final AttributeSink sink : this.attrSinks)
                        sink.nodeAttributeAdded(sourceId, timeId, eltId, attribute, newValue);
				} else if (eltType == ElementType.EDGE) {
                    for (final AttributeSink sink : this.attrSinks)
                        sink.edgeAttributeAdded(sourceId, timeId, eltId, attribute, newValue);
				} else {
                    for (final AttributeSink sink : this.attrSinks)
                        sink.graphAttributeAdded(sourceId, timeId, attribute, newValue);
				}
			} else if (event == AttributeChangeEvent.REMOVE) {
				if (eltType == ElementType.NODE) {
                    for (final AttributeSink sink : this.attrSinks)
                        sink.nodeAttributeRemoved(sourceId, timeId, eltId, attribute);
				} else if (eltType == ElementType.EDGE) {
                    for (final AttributeSink sink : this.attrSinks)
                        sink.edgeAttributeRemoved(sourceId, timeId, eltId, attribute);
				} else {
                    for (final AttributeSink sink : this.attrSinks)
                        sink.graphAttributeRemoved(sourceId, timeId, attribute);
				}
			} else {
				if (eltType == ElementType.NODE) {
                    for (final AttributeSink sink : this.attrSinks)
                        sink.nodeAttributeChanged(sourceId, timeId, eltId, attribute, oldValue, newValue);
				} else if (eltType == ElementType.EDGE) {
                    for (final AttributeSink sink : this.attrSinks)
                        sink.edgeAttributeChanged(sourceId, timeId, eltId, attribute, oldValue, newValue);
				} else {
                    for (final AttributeSink sink : this.attrSinks)
                        sink.graphAttributeChanged(sourceId, timeId, attribute, oldValue, newValue);
				}
			}

			manageEvents();
			eventProcessing = false;
		} else {
			eventQueue.add(new AttributeChangedEvent(sourceId, timeId, eltId,
					eltType, attribute, event, oldValue, newValue));
		}
	}

	// Deferred event management

	/**
	 * If in "event processing mode", ensure all pending events are processed.
	 */
	private void manageEvents() {
		if (eventProcessing) {
			while (!eventQueue.isEmpty())
				eventQueue.remove().trigger();
		}
	}

	// Events Management

	/**
	 * Interface that provide general purpose classification for evens involved
	 * in graph modifications
	 */
	private abstract class GraphEvent {
        protected final String sourceId;
        protected final long timeId;

		GraphEvent(String sourceId, long timeId) {
			this.sourceId = sourceId;
			this.timeId = timeId;
		}

		abstract void trigger();
	}

	private class AfterEdgeAddEvent extends GraphEvent {
		private final String edgeId;
        private final String fromNodeId;
        private final String toNodeId;
        private final boolean directed;

		AfterEdgeAddEvent(String sourceId, long timeId, String edgeId,
				String fromNodeId, String toNodeId, boolean directed) {
			super(sourceId, timeId);
			this.edgeId = edgeId;
			this.fromNodeId = fromNodeId;
			this.toNodeId = toNodeId;
			this.directed = directed;
		}

        @Override
		void trigger() {
            for (final ElementSink sink : eltsSinks)
                sink.edgeAdded(sourceId, timeId, edgeId, fromNodeId, toNodeId, directed);
		}
	}

	private class BeforeEdgeRemoveEvent extends GraphEvent {
		private final String edgeId;

		BeforeEdgeRemoveEvent(String sourceId, long timeId, String edgeId) {
			super(sourceId, timeId);
			this.edgeId = edgeId;
		}

        @Override
		void trigger() {
            for (final ElementSink sink : eltsSinks)
                sink.edgeRemoved(sourceId, timeId, edgeId);
		}
	}

	private class AfterNodeAddEvent extends GraphEvent {
		private final String nodeId;

		AfterNodeAddEvent(String sourceId, long timeId, String nodeId) {
			super(sourceId, timeId);
			this.nodeId = nodeId;
		}

        @Override
		void trigger() {
            for (final ElementSink sink : eltsSinks)
                sink.nodeAdded(sourceId, timeId, nodeId);
		}
	}

	private class BeforeNodeRemoveEvent extends GraphEvent {
		private final String nodeId;

		BeforeNodeRemoveEvent(String sourceId, long timeId, String nodeId) {
			super(sourceId, timeId);
			this.nodeId = nodeId;
		}

        @Override
		void trigger() {
            for (final ElementSink sink : eltsSinks)
                sink.nodeRemoved(sourceId, timeId, nodeId);
		}
	}

	private class BeforeGraphClearEvent extends GraphEvent {
		BeforeGraphClearEvent(String sourceId, long timeId) {
			super(sourceId, timeId);
		}

        @Override
		void trigger() {
            for (final ElementSink sink : eltsSinks)
                sink.graphCleared(sourceId, timeId);
		}
	}

	private class StepBeginsEvent extends GraphEvent {
		private final double step;

		StepBeginsEvent(String sourceId, long timeId, double step) {
			super(sourceId, timeId);
			this.step = step;
		}

        @Override
		void trigger() {
            for (final ElementSink sink : eltsSinks)
                sink.stepBegins(sourceId, timeId, step);
		}
	}

	private class AttributeChangedEvent extends GraphEvent {
        private final ElementType eltType;
        private final String eltId;
        private final String attribute;
        private final AttributeChangeEvent event;
        private final Object oldValue;
		private final Object newValue;

		AttributeChangedEvent(String sourceId, long timeId, String eltId,
				ElementType eltType, String attribute,
				AttributeChangeEvent event, Object oldValue, Object newValue) {
			super(sourceId, timeId);
			this.eltType = eltType;
			this.eltId = eltId;
			this.attribute = attribute;
			this.event = event;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

        @Override
		void trigger() {
			switch (event) {
			case ADD:
				switch (eltType) {
				case NODE:
                    for (final AttributeSink sink : attrSinks)
                        sink.nodeAttributeAdded(sourceId, timeId, eltId, attribute, newValue);
					break;
				case EDGE:
                    for (final AttributeSink sink : attrSinks)
                        sink.edgeAttributeAdded(sourceId, timeId, eltId, attribute, newValue);
					break;
				default:
                    for (final AttributeSink sink : attrSinks)
						sink.graphAttributeAdded(sourceId, timeId, attribute, newValue);
				}
				break;
			case REMOVE:
				switch (eltType) {
				case NODE:
                    for (final AttributeSink sink : attrSinks)
                        sink.nodeAttributeRemoved(sourceId, timeId, eltId, attribute);
					break;
				case EDGE:
                    for (final AttributeSink sink : attrSinks)
                        sink.edgeAttributeRemoved(sourceId, timeId, eltId, attribute);
					break;
				default:
                    for (final AttributeSink sink : attrSinks)
                        sink.graphAttributeRemoved(sourceId, timeId, attribute);
				}
				break;
			default:
				switch (eltType) {
				case NODE:
                    for (final AttributeSink sink : attrSinks)
                        sink.nodeAttributeChanged(sourceId, timeId, eltId, attribute, oldValue, newValue);
					break;
				case EDGE:
                    for (final AttributeSink sink : attrSinks)
                        sink.edgeAttributeChanged(sourceId, timeId, eltId, attribute, oldValue, newValue);
					break;
				default:
                    for (final AttributeSink sink : attrSinks)
                        sink.graphAttributeChanged(sourceId, timeId, attribute, oldValue, newValue);
				}
			}
		}
	}

	private class AddToListEvent<T> extends GraphEvent {
		private final Collection<T> l;
        private final T obj;

		AddToListEvent(final Collection<T> l, T obj) {
			super(null, -1);
			this.l = l;
			this.obj = obj;
		}

        @Override
		void trigger() {
			l.add(obj);
		}
	}

    private class RemoveFromListEvent<T> extends GraphEvent {
        private final Collection<T> l;
		private final T obj;

		RemoveFromListEvent(Collection<T> l, T obj) {
			super(null, -1);
			this.l = l;
			this.obj = obj;
		}

        @Override
		void trigger() {
			l.remove(obj);
		}
	}

	private class ClearListEvent<T> extends GraphEvent {
        private final Collection<T> l;

		ClearListEvent(Collection<T> l) {
			super(null, -1);
			this.l = l;
		}

        @Override
		void trigger() {
			l.clear();
		}
	}
}