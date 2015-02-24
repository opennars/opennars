/*
 * Created on 15-Nov-07
 */
package ca.nengo.model.impl;

import ca.nengo.model.*;
import ca.nengo.util.ScriptGenException;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Unit tests for AbstractEnsemble. 
 * 
 * @author Bryan Tripp
 */
public class AbstractGroupTest extends TestCase {

	public void testFindCommon1DOrigins() {
		Source one = new BasicSource(null, "2D", 2, Units.UNK);
		Source two = new BasicSource(null, "unique", 1, Units.UNK);
		Source three = new BasicSource(null, "shared1", 1, Units.UNK);
		Source four = new BasicSource(null, "shared2", 1, Units.UNK);
		
		List<Source> shared = new ArrayList<Source>(3);
		shared.add(one);
		shared.add(three);
		shared.add(four);
		
		List<Source> notshared = new ArrayList<Source>(4);
		notshared.add(one);
		notshared.add(three);
		notshared.add(four);
		notshared.add(two);
		
		Node[] nodes = new Node[3];
		nodes[0] = new AbstractNode("a", shared, new ArrayList<Target>(1)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void run(float startTime, float endTime)
					throws SimulationException {}

			@Override
			public void reset(boolean randomize) {}

			public Node[] getChildren() {
				return new Node[0];
			}

			public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
				return "";
			}};		
		nodes[1] = new AbstractNode("b", shared, new ArrayList<Target>(1)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void run(float startTime, float endTime)
					throws SimulationException {}

			@Override
			public void reset(boolean randomize) {}

			public Node[] getChildren() {
				return new Node[0];
			}

			public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
				return "";
			}};		
		nodes[2] = new AbstractNode("c", notshared, new ArrayList<Target>(1)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void run(float startTime, float endTime)
					throws SimulationException {}

			@Override
			public void reset(boolean randomize) {}

			public Node[] getChildren() {
				return new Node[0];
			}

			public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
				return "";
			}};
		
		List<String> origins = AbstractGroup.findCommon1DOrigins(nodes);
		assertEquals(2, origins.size());
		assertTrue(origins.contains(three.getName()));
		assertTrue(origins.contains(four.getName()));
	}

}
