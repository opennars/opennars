package ca.nengo.ui.lib.world.elastic;

import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.ObjectSet;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldGroundImpl;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.PXEdge;
import edu.uci.ics.jung.graph.impl.AbstractSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.visualization.Layout;
import nars.Global;
import org.piccolo2d.PNode;
import org.piccolo2d.util.PBounds;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;


@Deprecated public class ElasticGround extends WorldGroundImpl {

	public static final String ELASTIC_LENGTH_KEY = "elasticLength";

	private boolean childrenUpdatedFlag = false;
	private final ObjectSet<ElasticObject> elasticChildren = new ObjectSet<ElasticObject>();

	private ElasticLayoutRunner elasticLayoutThread;

	private final Map<PXEdge, AbstractSparseEdge> myEdgeMap = Global.newHashMap();

	private SparseGraph myGraph;

	private final Map<ElasticObject, ElasticVertex> myVertexMap = Global.newHashMap();

	public ElasticGround() {
		super();
        getPNode().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().compareTo(PNode.PROPERTY_CHILDREN) == 0) {
                    childrenUpdatedFlag = true;
                }
            }

        });
	}

	public void modifyEdgeDistances(ElasticObject obj, double delta) {
		for (PXEdge edge : getEdges()) {
			if (edge instanceof ElasticEdge) {
				ElasticEdge elasticEdge = (ElasticEdge) edge;

				if (obj == elasticEdge.getStartNode() || obj == elasticEdge.getEndNode()) {

					double distance = elasticEdge.getLength() + delta;

					if (distance < 100) {
						distance = 100;
					}

					elasticEdge.setLength(distance);

				}

			}

		}

	}

	@Override
	protected void prepareForDestroy() {
		setElasticEnabled(false);
		super.prepareForDestroy();
	}

	@Override
	public void childAdded(WorldObject wo) {
		super.childAdded(wo);
		if (wo instanceof ElasticObject) {
			elasticChildren.add((ElasticObject) wo);
		}
	}

	@Override
	public void childRemoved(WorldObject wo) {
		super.childRemoved(wo);
		if (wo instanceof ElasticObject) {
			if (!elasticChildren.remove(wo)) {
				Util.Assert(false);
			}
		}
	}

	public Iterable<ElasticObject> getElasticChildren() {
		return elasticChildren;
	}

	public Point2D getElasticPosition(ElasticObject node) {
		if (elasticLayoutThread != null) {
			ElasticVertex vertex = myVertexMap.get(node);
			if (vertex != null) {
				if (!elasticLayoutThread.isLocked(vertex)) {
					return elasticLayoutThread.getLocation(myVertexMap.get(node));
				}
			}
		}
		return node.getOffsetReal();
	}

	/**
	 * @return The current graph representation of the Ground.
	 */
	public SparseGraph getGraph() {
		return myGraph;
	}

	@Override
	public WorldImpl getWorld() {
		return super.getWorld();
	}

	public boolean isElasticMode() {
		if (elasticLayoutThread != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isPositionLocked(ElasticObject node) {
		if (elasticLayoutThread != null) {
			ElasticVertex vertex = myVertexMap.get(node);

			if (vertex != null) {
				return elasticLayoutThread.isLocked(vertex);
			}
		}
		return false;
	}

	public void setElasticEnabled(boolean enabled) {
		if (elasticLayoutThread != null) {
			elasticLayoutThread.stopLayout();
			elasticLayoutThread = null;
		}
		if (enabled) {
			myVertexMap.clear();
			myEdgeMap.clear();
			myGraph = null;
			elasticLayoutThread = new ElasticLayoutRunner(this);
			elasticLayoutThread.start();
		}

	}

	public void setElasticPosition(ElasticObject node, double x, double y) {
		boolean doRealMove = true;
		if (x == 0 || y == 0)
			return;

		if (elasticLayoutThread != null) {
			ElasticVertex vertex = myVertexMap.get(node);
			if (vertex != null) {

				elasticLayoutThread.forceMove(vertex, x, y);
				if (!elasticLayoutThread.isLocked(vertex)) {
					doRealMove = false;
				}
			}
		}
		if (doRealMove) {
			node.setOffsetReal(x, y);
		}
	}

	/**
	 * Locks the position of an elastic node so it isn't affected by the layout
	 * runner
	 * 
	 * @param node
	 * @param lockEnabled
	 */
	public void setPositionLocked(ElasticObject node, boolean lockEnabled) {
		if (elasticLayoutThread != null) {
			ElasticVertex vertex = myVertexMap.get(node);

			if (vertex == null) {
				// Try to update the layout and get the vertex again
				// Node might have been updated recently and not added to the
				// graph
				elasticLayoutThread.updateLayout();
				vertex = myVertexMap.get(node);
			}

			if (vertex != null) {
				if (lockEnabled) {
					elasticLayoutThread.lockVertex(vertex);
				} else {
					elasticLayoutThread.unlockVertex(vertex);
				}
			} else {
				// Util.Assert(false, "Vertex not found");
			}
		}

	}

	public void updateChildrenFromLayout(Layout layout, boolean animateNodes, boolean zoomToLayout) {
		/**
		 * Layout nodes
		 */
		boolean foundNode = false;

		double startX = Double.POSITIVE_INFINITY;
		double startY = Double.POSITIVE_INFINITY;
		double endX = Double.NEGATIVE_INFINITY;
		double endY = Double.NEGATIVE_INFINITY;



        if (!elasticChildren.isEmpty()) {

            final long now = System.currentTimeMillis();

            for (ElasticObject elasticObj : getElasticChildren()) {

                ElasticVertex vertex = myVertexMap.get(elasticObj);
                if (vertex != null) {

                    Point2D coord = layout.getLocation(vertex);

                    if (coord != null) {

                        foundNode = true;
                        double x = coord.getX();
                        double y = coord.getY();

                        if (elasticObj.isAnimating(now)) {
                            // If the object is being animated in another process,
                            // then we force update it's position in the layout
                            x = elasticObj.getOffsetReal().getX();
                            y = elasticObj.getOffsetReal().getY();
                            if (elasticLayoutThread != null) {
                                elasticLayoutThread.forceMove(vertex, x, y);
                            }
                        } else {
                            x = coord.getX();
                            y = coord.getY();
                            if (animateNodes) {
                                elasticObj.animateToPositionScaleRotation(x, y, 1, 0, 1000);
                            } else {
                                elasticObj.setOffsetReal(x, y);
                            }
                        }

                        if (x < startX) {
                            startX = x;
                        }
                        if (x + elasticObj.getWidth() > endX) {
                            endX = x + elasticObj.getWidth();
                        }

                        if (y < startY) {
                            startY = y;
                        }
                        if (y + elasticObj.getHeight() > endY) {
                            endY = y + elasticObj.getHeight();
                        }
                    }
                }
            }

        }

		if (zoomToLayout && foundNode) {
			PBounds fullBounds = new PBounds(startX, startY, endX - startX, endY - startY);
			getWorld().zoomToBounds(fullBounds);
		}

	}

	/**
	 * This method must be executed from the swing dispatcher thread because it
	 * must be synchronized with the Graphical children elements.
	 */
	public UpdateGraphResult updateGraph() {

		boolean graphChanged = false;
		if (myGraph == null) {
			graphChanged = true;
			childrenUpdatedFlag = true;
			myGraph = new SparseGraph();
		}
		Collection<ElasticVertex> addedVertexes = Collections.emptyList();
		if (childrenUpdatedFlag) {
			addedVertexes = new LinkedList<ElasticVertex>();
			childrenUpdatedFlag = false;

			/**
			 * Add vertices
			 */
			for (ElasticObject obj : getElasticChildren()) {

				if (!myVertexMap.containsKey(obj)) {
					ElasticVertex vertex = new ElasticVertex(obj);
					myGraph.addVertex(vertex);
					myVertexMap.put(obj, vertex);
					addedVertexes.add(vertex);

					graphChanged = true;
				}
			}

			/**
			 * Remove vertices
			 */
			List<ElasticObject> elasticObjToRemove = new ArrayList<ElasticObject>();
			for (ElasticObject elasticObj : myVertexMap.keySet()) {
				if (elasticObj.getParent() != this) {
					elasticObjToRemove.add(elasticObj);
				}
			}

			for (ElasticObject elasticObj : elasticObjToRemove) {
				myGraph.removeVertex(myVertexMap.get(elasticObj));
				myVertexMap.remove(elasticObj);
				graphChanged = true;
			}
		}

		/*
		 * TODO: Only update edges when they are changed. Have to figure out a
		 * way to know when edge start and end nodes are changed. This is tricky
		 * because the start and end nodes we're interested in are the Elastic
		 * Objects above the ground, which the dosen't know about
		 */

		/**
		 * Add edges
		 */

		for (PXEdge uiEdge : getEdges()) {

			if (uiEdge instanceof ElasticEdge) {
				ElasticEdge elasticEdge = (ElasticEdge) uiEdge;
				WorldObject startNode = uiEdge.getStartNode();
				WorldObject endNode = uiEdge.getEndNode();

				// Find the Elastic Objects which are ancestors of the start and
				// end
				// nodes
				while (startNode.getParent() != this && startNode != null) {
					startNode = startNode.getParent();
					if (startNode == null) {
						break;
					}
				}

				while (endNode.getParent() != this && endNode != null) {
					endNode = endNode.getParent();
					if (endNode == null) {
						break;
					}
				}

				if (startNode == null || endNode == null) {
					Util.Assert(false, "Edge nodes do not exist on this ground");
				} else if (!(startNode instanceof ElasticObject || endNode instanceof ElasticGround)) {
					/*
					 * The parent nodes are not elastic, we can ignore them
					 */
				} else if (startNode.getParent() == this && endNode.getParent() == this) {
					ElasticVertex startVertex = myVertexMap.get(startNode);
					ElasticVertex endVertex = myVertexMap.get(endNode);

					if (!(startVertex != null && endVertex != null)) {
						Util.Assert(false, "Could not find vertice");
					}

					AbstractSparseEdge jungEdge = myEdgeMap.get(uiEdge);

					boolean createJungEdge = false;
					if (jungEdge != null) {
						// find if an existing edge has changed
						boolean edgeChanged = false;

						if (uiEdge instanceof ElasticEdge) {
							Double length = (Double) jungEdge.getUserDatum(ELASTIC_LENGTH_KEY);
							if (length != ((ElasticEdge) uiEdge).getLength()) {
								edgeChanged = true;
							}
						} else if (jungEdge.getEndpoints().getFirst() != startVertex
								|| jungEdge.getEndpoints().getSecond() != endVertex) {
							edgeChanged = true;
						}

						if (edgeChanged) {
							myEdgeMap.remove(uiEdge);
							myGraph.removeEdge(jungEdge);
							graphChanged = true;

							// try to add the new changed one
							createJungEdge = true;

						}

					} else {
						createJungEdge = true;
					}

					if (createJungEdge) {
						// avoid recursive edges
						if (startVertex != endVertex) {
							if (uiEdge.isDirected()) {
								jungEdge = new DirectedSparseEdge(startVertex, endVertex);
							} else {
								jungEdge = new UndirectedSparseEdge(startVertex, endVertex);
							}

							jungEdge.addUserDatum(ELASTIC_LENGTH_KEY, new Double(elasticEdge
									.getLength()), UserData.SHARED);

							myEdgeMap.put(uiEdge, jungEdge);

							myGraph.addEdge(jungEdge);

							graphChanged = true;
						}
					}

				} else {
					Util.Assert(false, "Could not find Elastic Nodes of edge");
				}
			}
		}
		/*
		 * Remove edges
		 */
		List<PXEdge> edgesToRemove = new ArrayList<PXEdge>();
		for (PXEdge uiEdge : myEdgeMap.keySet()) {
			if (!containsEdge(uiEdge)) {
				edgesToRemove.add(uiEdge);

			}
		}
		for (PXEdge uiEdge : edgesToRemove) {
			AbstractSparseEdge jungEdge = myEdgeMap.get(uiEdge);
			graphChanged = true;
			// have to check if the edge is still there because it might have
			// been removed when the vertice was removed
			if (myGraph.getEdges().contains(jungEdge)) {
				myGraph.removeEdge(jungEdge);
			}
			myEdgeMap.remove(uiEdge);
		}

		// Return whether the graph changed
		return new UpdateGraphResult(graphChanged, addedVertexes);

	}

	public static class UpdateGraphResult {
		private final Collection<ElasticVertex> addedVertices;
		private final boolean graphUpdated;

		public UpdateGraphResult(boolean graphUpdated, Collection<ElasticVertex> addedVertices) {
			super();
			this.graphUpdated = graphUpdated;
			this.addedVertices = addedVertices;
		}

		public Collection<ElasticVertex> getAddedVertices() {
			return addedVertices;
		}

		public boolean isGraphUpdated() {
			return graphUpdated;
		}

	}

}
