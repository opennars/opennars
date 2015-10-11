package ca.nengo.math;

import ca.nengo.sim.model.Node;
import ca.nengo.sim.model.Projection;

import java.util.ArrayList;
import java.util.Set;

public interface NetworkPartitioner {
	
	void initialize(Node[] nodes, Projection[] projections, int numPartitions);
	
	ArrayList<Set<Node>> getPartitions();
	int[] getPartitionsAsIntArray();
}
