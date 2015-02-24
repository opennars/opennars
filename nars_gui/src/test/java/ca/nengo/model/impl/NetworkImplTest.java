/*
 * Created on 24-May-2006
 */
package ca.nengo.model.impl;

import ca.nengo.TestUtil;
import ca.nengo.math.Function;
import ca.nengo.math.impl.ConstantFunction;
import ca.nengo.model.*;
import ca.nengo.model.nef.impl.NEFGroupFactoryImpl;
import ca.nengo.model.nef.impl.NEFGroupImpl;
import ca.nengo.model.neuron.impl.SpikingNeuron;
import ca.nengo.util.*;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetworkImplTest extends TestCase {

	private NetworkImpl myNetwork;

	protected void setUp() throws Exception {
		super.setUp();

		myNetwork = new NetworkImpl();
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.NetworkImpl.getNodes()'
	 */
	public void testGetNodes() throws StructuralException {
		Group a = new MockGroup("a");
		myNetwork.addNode(a);
		myNetwork.addNode(new MockGroup("b"));

		assertEquals(2, myNetwork.getNodes().length);

		try {
			myNetwork.addNode(new MockGroup("a"));
			fail("Should have thrown exception due to duplicate ensemble name");
		} catch (StructuralException e) {
		} // exception is expected

		try {
			myNetwork.removeNode("c");
			fail("Should have thrown exception because named ensemble doesn't exist");
		} catch (StructuralException e) {
		} // exception is expected

		myNetwork.removeNode("b");
		assertEquals(1, myNetwork.getNodes().length);
		assertEquals(a, myNetwork.getNodes()[0]);
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.NetworkImpl.getProjections()'
	 */
	public void testGetProjections() throws StructuralException {
		Source o1 = new ProjectionImplTest.MockSource("o1", 1);
		Source o2 = new ProjectionImplTest.MockSource("o2", 1);
		Target t1 = new ProjectionImplTest.MockTarget("t1", 1);
		Target t2 = new ProjectionImplTest.MockTarget("t2", 1);
		Target t3 = new ProjectionImplTest.MockTarget("t3", 2);

		myNetwork.addProjection(o1, t1);
		myNetwork.addProjection(o1, t2);

		assertEquals(2, myNetwork.getProjections().length);

		try {
			myNetwork.addProjection(o2, t1);
			fail("Should have thrown exception because termination t1 already filled");
		} catch (StructuralException e) {
		} // exception is expected

		try {
			myNetwork.addProjection(o1, t3);
			fail("Should have thrown exception because origin and termination have different dimensions");
		} catch (StructuralException e) {
		} // exception is expected

		myNetwork.removeProjection(t2);
		assertEquals(t1, myNetwork.getProjections()[0].getTermination());
	}
	
	public void testNodeNameChange() throws StructuralException {
		MockGroup e1 = new MockGroup("one");
		myNetwork.addNode(e1);
		
		MockGroup e2 = new MockGroup("two");
		myNetwork.addNode(e2);
		
		assertTrue(myNetwork.getNode("one") != null);
		
		e1.setName("foo");
		assertTrue(myNetwork.getNode("foo") != null);
		try {
			myNetwork.getNode("one");
			fail("Shouldn't exist any more");
		} catch (StructuralException e) {}
		
		try {
			e2.setName("foo");
			fail("Should have thrown exception on duplicate name");
		} catch (StructuralException e) {}
	}
	
	public void testKillNeurons() throws StructuralException
	{
		NEFGroupFactoryImpl ef = new NEFGroupFactoryImpl();
		NEFGroupImpl nef1 = (NEFGroupImpl)ef.make("nef1", 1000, 1);
		NEFGroupImpl nef2 = (NEFGroupImpl)ef.make("nef2", 1000, 1);
		NEFGroupImpl nef3 = (NEFGroupImpl)ef.make("nef3", 1, 1);
		NetworkImpl net = new NetworkImpl();
		
		net.addNode(nef1);
		myNetwork.addNode(net);
		myNetwork.addNode(nef2);
		myNetwork.addNode(nef3);
		
		myNetwork.killNeurons(0.0f,true);
		int numDead = countDeadNeurons(nef1);
		if(numDead != 0)
			fail("Number of dead neurons outside expected range");
		numDead = countDeadNeurons(nef2);
		if(numDead != 0)
			fail("Number of dead neurons outside expected range");
		
		myNetwork.killNeurons(0.5f,true);
		numDead = countDeadNeurons(nef1);
		if(numDead < 400 || numDead > 600)
			fail("Number of dead neurons outside expected range");
		numDead = countDeadNeurons(nef2);
		if(numDead < 400 || numDead > 600)
			fail("Number of dead neurons outside expected range");
		
		myNetwork.killNeurons(1.0f,true);
		numDead = countDeadNeurons(nef1);
		if(numDead != 1000)
			fail("Number of dead neurons outside expected range");
		numDead = countDeadNeurons(nef2);
		if(numDead != 1000)
			fail("Number of dead neurons outside expected range");
		
		numDead = countDeadNeurons(nef3);
		if(numDead != 0)
			fail("Relay protection did not work");
		myNetwork.killNeurons(1.0f,false);
		numDead = countDeadNeurons(nef3);
		if(numDead != 1)
			fail("Number of dead neurons outside expected range");
	}
	private int countDeadNeurons(NEFGroupImpl pop)
	{
		Node[] neurons = pop.getNodes();
		int numDead = 0;
		
		for(int i = 0; i < neurons.length; i++)
		{
			SpikingNeuron n = (SpikingNeuron)neurons[i];
			if(n.getBias() == 0.0f && n.getScale() == 0.0f)
				numDead++;
		}
		
		return numDead;
	}
	
	public void testAddNode() throws StructuralException
	{
		Group a = new MockGroup("a");
		
		try
		{
			myNetwork.getNode("a");
			fail("Node is present in network when it shouldn't be");
		}
		catch(StructuralException se)
		{
		}
			
		
		myNetwork.addNode(a);
		
		if(myNetwork.getNode("a") != a)
			fail("Ensemble not added correctly");
		
		NetworkImpl b = new NetworkImpl();
		b.setName("b");
		myNetwork.addNode(b);
		
		if(myNetwork.getNode("b") != b)
			fail("Network not added correctly");
		
	}
	
	public void testRemoveNode() throws StructuralException, SimulationException
	{
		Group a = new MockGroup("a");
		
		myNetwork.addNode(a);
		if(myNetwork.getNode("a") == null)
			fail("Node not added");
		
		myNetwork.removeNode("a");
		try
		{
			myNetwork.getNode("a");
			fail("Node not removed");
		}
		catch(StructuralException se)
		{
		}
			
		NetworkImpl b = new NetworkImpl();
		b.setName("b");
		myNetwork.addNode(b);
		
		NEFGroupFactoryImpl ef = new NEFGroupFactoryImpl();
		NEFGroupImpl c = (NEFGroupImpl)ef.make("c", 10, 1);
		b.addNode(c);
		b.getSimulator().addProbe("c", "X", true);
		
		b.exposeOrigin(c.getOrigin("X"), "exposed");
		
		if(!b.getExposedOriginName(c.getOrigin("X")).equals("exposed"))
			fail("Origin not exposed correctly");
		
		if(myNetwork.getNode("b") == null)
			fail("Network not added");
		
		myNetwork.removeNode("b");
		
		try
		{
			myNetwork.getNode("b");
			fail("Network not removed");
		}
		catch(StructuralException se)
		{
		}
		
		try
		{
			b.getNode("c");
			fail("Ensemble not recursively removed from network");
		}
		catch(StructuralException se)
		{
		}

		if(b.getSimulator().getProbes().length != 0)
			fail("Probes not removed correctly");
		
		if(b.getExposedOriginName(c.getOrigin("X")) != null)
			fail("Origin not unexposed correctly");
	}
	
	public void testExposeOrigin() throws StructuralException
	{
		NEFGroupFactoryImpl ef = new NEFGroupFactoryImpl();
		NEFGroupImpl a = (NEFGroupImpl)ef.make("a", 10, 1);
		
		myNetwork.addNode(a);
		
		myNetwork.exposeOrigin(a.getOrigin("X"), "test");
		
		try
		{
			myNetwork.getOrigin("test");
		}
		catch(StructuralException se)
		{
			fail("Origin not exposed");
		}
		
		if(myNetwork.getExposedOriginName(a.getOrigin("X")) != "test")
			fail("Origin not exposed with correct name");
		
		myNetwork.removeNode("a");
	}
	
	public void testHideOrigin() throws StructuralException
	{
		NEFGroupFactoryImpl ef = new NEFGroupFactoryImpl();
		NEFGroupImpl a = (NEFGroupImpl)ef.make("a", 10, 1);
		
		myNetwork.addNode(a);
		
		myNetwork.exposeOrigin(a.getOrigin("X"), "test");
		
		myNetwork.hideOrigin("test");
		
		if(myNetwork.getExposedOriginName(a.getOrigin("X")) != null)
			fail("Origin name not removed");
		
		try
		{
			myNetwork.getOrigin("test");
			fail("Origin not removed");
		}
		catch(StructuralException se)
		{
		}
		
		myNetwork.removeNode("a");
	}
	
//	public void testChanged() throws StructuralException, SimulationException
//	{
//		NetworkImpl b = new NetworkImpl();
//		b.setName("b");
//		myNetwork.addNode(b);
//		
//		NEFEnsembleFactoryImpl ef = new NEFEnsembleFactoryImpl();
//		NEFEnsembleImpl a = (NEFEnsembleImpl)ef.make("a", 10, 1);
//		b.addNode(a);
//		
////		b.exposeOrigin(a.getOrigin("X"), "exposed");
////		
////		NEFEnsembleImpl c = (NEFEnsembleImpl)ef.make("c", 10, 1);
////		float[][] tmp = new float[1][1];
////		tmp[0][0] = 1;
////		c.addDecodedTermination("in", tmp, 0.007f, false);
////		myNetwork.addNode(c);
////		
////		myNetwork.addProjection(b.getOrigin("exposed"), c.getTermination("in"));
////		
////		if(myNetwork.getProjections().length != 1)
////			fail("Projection not created properly");
////		
////		b.hideOrigin("exposed");
////		
////		if(myNetwork.getProjections().length != 0)
////			fail("Projection not removed");
////		
////		myNetwork.removeNode("b");
////		myNetwork.removeNode("c");
//		
//		b.getSimulator().addProbe("a", "X", true);
//		myNetwork.collectAllProbes();
//		
//		if(myNetwork.getSimulator().getProbes().length != 1)
//			fail("Probe not added");
//		
//		b.removeNode("a");
//		
//		if(myNetwork.getSimulator().getProbes().length != 0)
//			fail("Probe not removed when node removed");
//		
//		myNetwork.removeNode("b");
//	}
	
	public void testGetNodeTerminations() throws StructuralException
	{
		NetworkImpl net = new NetworkImpl();
		
		if(net.getNodeTerminations().size() != 0)
			fail("Network has terminations when it shouldn't");
		
		NEFGroupFactoryImpl ef = new NEFGroupFactoryImpl();
		NEFGroupImpl a = (NEFGroupImpl)ef.make("a", 10, 1);
		float[][] tmp = new float[1][1];
		tmp[0][0] = 1;
		a.addDecodedTermination("in", tmp, 0.007f, false);
		
		net.addNode(a);
		
		if(net.getNodeTerminations().size() != 1)
			fail("Network hasn't found node termination");
	}
	
	public void testGetNodeOrigins() throws StructuralException
	{
		NetworkImpl net = new NetworkImpl();
		
		if(net.getNodeOrigins().size() != 0)
			fail("Network has origins when it shouldn't");
		
		NEFGroupFactoryImpl ef = new NEFGroupFactoryImpl();
		NEFGroupImpl a = (NEFGroupImpl)ef.make("a", 10, 1);
		
		net.addNode(a);
		
		if(net.getNodeOrigins().size() != a.getOrigins().length)
			fail("Network hasn't found node origin");
		
	}
	
	public void testReset() throws StructuralException, SimulationException
	{
		NetworkImpl net = new NetworkImpl();
		
		NEFGroupFactoryImpl ef = new NEFGroupFactoryImpl();
		NEFGroupImpl a = (NEFGroupImpl)ef.make("a", 10, 1);
		a.addDecodedTermination("input", new float[][]{new float[]{1}}, 0.01f, false);
		
		net.addNode(a);
		
		FunctionInput fin = new FunctionInput("fin", new Function[]{new ConstantFunction(1,0)}, Units.UNK);
		net.addNode(fin);
		
		net.addProjection(fin.getOrigin("origin"), a.getTermination("input"));
		
		Probe p = net.getSimulator().addProbe("a", "rate", true);
		
		net.getSimulator().run(0.0f, 1.0f, 0.001f);
		
		float[][] results1 = p.getData().getValues();
		
		net.reset(false);
		
		net.getSimulator().run(0.0f, 1.0f, 0.001f);
		
		float[][] results2 = p.getData().getValues();
		
		for(int i=0; i < results1.length; i++)
			for(int j=0; j < results1[i].length; j++)
				TestUtil.assertClose(results1[i][j], results2[i][j], 0.0001f);
		
	}
	
	public void testClone() throws StructuralException, CloneNotSupportedException{
		NetworkImpl top = new NetworkImpl();
	    
	    NetworkImpl test1 = new NetworkImpl();
	    test1.setName("test1");
	    top.addNode(test1);
	    
	    NEFGroupFactoryImpl fac = new NEFGroupFactoryImpl();
	    NEFGroupImpl testens = (NEFGroupImpl)fac.make("test", 100, 1);
	    testens.addDecodedTermination("input", new float[][]{new float[]{1}}, 0.01f, false);
	    test1.addNode(testens);
	    
	    test1.exposeTermination(testens.getTermination("input"), "in");
	    
	    NetworkImpl test2 = (NetworkImpl)test1.clone();
	    test2.setName("test2");
	    top.addNode(test2);
	    
	    FunctionInput fin = new FunctionInput("fin", new Function[]{new ConstantFunction(1,0.5f)}, Units.UNK);
	    top.addNode(fin);
	    
	    top.addProjection(fin.getOrigin("origin"), test1.getTermination("in"));
	    top.addProjection(fin.getOrigin("origin"), test2.getTermination("in"));
	    
	    if(test1.getTermination("in") == test2.getTermination("in"))
	    	fail("Exposed terminations did not clone correctly");
	    if(test1.getNode("test") == test2.getNode("test"))
	    	fail("Network nodes did not clone correctly");
	}

	private static class MockGroup implements Group {

		private static final long serialVersionUID = 1L;

		private String myName;
		private transient List<VisiblyMutable.Listener> myListeners;

		public MockGroup(String name) {
			myName = name;
		}

		public String getName() {
			return myName;
		}
		
		public void setName(String name) throws StructuralException {
			VisiblyMutableUtils.nameChanged(this, getName(), name, myListeners);
			myName = name;
		}

		public Node[] getNodes() {
			throw new RuntimeException("not implemented");
		}

//		public void addNeuron(Neuron neuron) {
//			throw new RuntimeException("not implemented");
//		}

//		public void removeNeuron(int index) {
//			throw new RuntimeException("not implemented");
//		}

		public Source[] getOrigins() {
			throw new RuntimeException("not implemented");
		}

		public Target[] getTerminations() {
			throw new RuntimeException("not implemented");
		}

		public void setMode(SimulationMode mode) {
			throw new RuntimeException("not implemented");
		}

		public SimulationMode getMode() {
			throw new RuntimeException("not implemented");
		}

		public void run(float startTime, float endTime)
				throws SimulationException {
			throw new RuntimeException("not implemented");
		}

		public void reset(boolean randomize) {
			throw new RuntimeException("not implemented");
		}

		public Source getOrigin(String name) throws StructuralException {
			throw new RuntimeException("not implemented");
		}

		public Target getTermination(String name)
				throws StructuralException {
			throw new RuntimeException("not implemented");
		}

		public SpikePattern getSpikePattern() {
			throw new RuntimeException("not implemented");
		}

		public void collectSpikes(boolean collect) {
			throw new RuntimeException("not implemented");
		}

		public String getDocumentation() {
			throw new RuntimeException("not implemented");
		}

		public void setDocumentation(String text) {
			throw new RuntimeException("not implemented");
		}

		public boolean isCollectingSpikes() {
			throw new RuntimeException("not implemented");
		}

		public void redefineNodes(Node[] nodes) {
			throw new RuntimeException("not implemented");			
		}

		public void stopProbing(String stateName) {
			throw new RuntimeException("not implemented");			
		}
		
		/**
		 * @see ca.nengo.util.VisiblyMutable#addChangeListener(ca.nengo.util.VisiblyMutable.Listener)
		 */
		public void addChangeListener(Listener listener) {
			if (myListeners == null) {
				myListeners = new ArrayList<Listener>(2);
			}
			myListeners.add(listener);
		}

		/**
		 * @see ca.nengo.util.VisiblyMutable#removeChangeListener(ca.nengo.util.VisiblyMutable.Listener)
		 */
		public void removeChangeListener(Listener listener) {
			myListeners.remove(listener);
		}

		@Override
		public Node clone() throws CloneNotSupportedException {
			return (Node) super.clone();
		}

		public Node[] getChildren() {
			
			return null;
		}

		public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
			// TODO Auto-generated method stub
			return null;
		}
	}

}
