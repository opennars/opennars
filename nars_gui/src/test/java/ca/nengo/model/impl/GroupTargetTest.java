/**
 * 
 */
package ca.nengo.model.impl;

import ca.nengo.TestUtil;
import ca.nengo.model.NTarget;
import ca.nengo.model.StructuralException;
import junit.framework.TestCase;

/**
 * Unit tests for EnsembleTermination. 
 * 
 * @author Bryan Tripp
 */
public class GroupTargetTest extends TestCase {

	private static float ourTau = .005f;
	private static float ourTolerance = 1e-5f;
	
	private NTarget[] myNodeTargets;
	private GroupTarget myEnsembleTermination;
	
	/**
	 * @param arg0
	 */
	public GroupTargetTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		myNodeTargets = new NTarget[10];
		for (int i = 0; i < myNodeTargets.length; i++) {
			myNodeTargets[i] = new LinearExponentialTarget(null, ""+i, new float[]{1}, ourTau);
		}
		
		myEnsembleTermination = new GroupTarget(null, "test", myNodeTargets);
	}

	/**
	 * Test method for {@link GroupTarget#setModulatory(boolean)}.
	 */
	public void testSetModulatory() {
		assertFalse(myEnsembleTermination.getModulatory());
		myNodeTargets[0].setModulatory(true);
		assertFalse(myEnsembleTermination.getModulatory());
		
		myEnsembleTermination.setModulatory(true);
		assertTrue(myEnsembleTermination.getModulatory());
		assertTrue(myNodeTargets[0].getModulatory());
		assertTrue(myNodeTargets[1].getModulatory());
	}

	/**
	 * Test method for {@link GroupTarget#setTau(float)}.
	 * @throws StructuralException 
	 */
	public void testSetTau() throws StructuralException {
		TestUtil.assertClose(ourTau, myEnsembleTermination.getTau(), ourTolerance);
		myNodeTargets[0].setTau(ourTau*2);
		assertTrue(myEnsembleTermination.getTau() > ourTau*1.01f);
		
		myEnsembleTermination.setTau(ourTau*2);
		TestUtil.assertClose(ourTau*2, myEnsembleTermination.getTau(), ourTolerance);
		TestUtil.assertClose(ourTau*2, myNodeTargets[0].getTau(), ourTolerance);
		TestUtil.assertClose(ourTau*2, myNodeTargets[0].getTau(), ourTolerance);
	}

}
