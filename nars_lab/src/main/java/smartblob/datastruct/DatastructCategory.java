/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.datastruct;

public enum DatastructCategory{
	
	/** A point, NsNode, or any single thing, including if it has an attached scalar or bit value */
	node,

	/** anything between 2 or more nodes as long as its a constant number of nodes.
	Neural nets and undirected graph are common examples.
	*/
	edge,
	
	/** Example: bayesNode weight */
	powersetedge

}
