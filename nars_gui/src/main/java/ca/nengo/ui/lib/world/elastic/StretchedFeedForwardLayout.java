/**
 * 
 */
package ca.nengo.ui.lib.world.elastic;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.SimpleSparseVertex;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Stretch vertices that are linked to other vertices in the same layer.
 * 
 * @author Wu Yan
 * @version 1.0.1
 */
public class StretchedFeedForwardLayout extends FeedForwardLayout {

	/**
	 * @param g
	 */
	public StretchedFeedForwardLayout(Graph g) {
		super(g);
	}
	
	@Override
	protected LinkedList<LinkedList<Vertex>> sortVertices() {
		LinkedList<LinkedList<Vertex>> sortedVertices = super.sortVertices();
		LinkedList<Vertex> layer = null;

		// indicates whether an actual Vertex was added into new layer
		boolean flag = false;
		LinkedList<Vertex> newLayer = null;
		VoidVertex placeHolder = new VoidVertex();
		
		for (int i = 0; i < sortedVertices.size(); i++) {
			layer = sortedVertices.get(i);
			
			/**
			 *  if a vertex is the predecessor of another vertex, move it and its predecessor vertices into a new layer
			 */
			newLayer = new LinkedList<Vertex>();
			flag = false;
			Vertex v = null;
			// count the number of actual vertices remained in layer
			int remainCounter = 0;
			for (ListIterator<Vertex> liV = layer.listIterator(); liV.hasNext();) {
				v = liV.next();
				if (!(v instanceof VoidVertex))
					remainCounter++;
				if (isPredecessorOfList(v, newLayer) || isPredecessorOfList(v, layer)){
					//add this vertex
					newLayer.add(v);
					// replace current Vertex with a place holder
					liV.set(placeHolder);
					
					flag = true;
					remainCounter--;
				} else {
					// add place holder
					newLayer.add(placeHolder);
				}
			}
			if (flag) {
				sortedVertices.add(i + 1, newLayer);
			}
			
			/** if the whole layer is moved to new layer (e.g. recurrent connections), 
			  * do not create the new layer and skip it 
			  */
			if (remainCounter == 0) {
				sortedVertices.remove(i);
				i++;
			}
		}
		
		return sortedVertices;
	}
	
	private boolean isPredecessorOfList(Vertex v, List<Vertex> list) {
		Vertex vl = null;
		for (Iterator<Vertex> iV = list.iterator(); iV.hasNext();) {
			vl = iV.next();
			if (v.isPredecessorOf(vl))
				return true;
		}
		return false;
	}
	
	/**
	 * VoidVertex serves as place holder in LinkedList
	 * 
	 */
    static class VoidVertex extends SimpleSparseVertex {
	}
}
