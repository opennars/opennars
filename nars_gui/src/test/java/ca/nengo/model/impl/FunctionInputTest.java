/*
 * Created on 24-Jul-2006
 */
package ca.nengo.model.impl;

import ca.nengo.math.Function;
import ca.nengo.math.impl.ConstantFunction;
import ca.nengo.model.*;
import junit.framework.TestCase;

public class FunctionInputTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.model.impl.FunctionInput.getName()'
	 */
	public void testGetName() throws StructuralException {
		String name = "test";
		FunctionInput input = new FunctionInput(name, new Function[]{new ConstantFunction(1, 1f)}, Units.UNK);
		assertEquals(name, input.getName());
	}

	/*
	 * Test method for 'ca.nengo.model.impl.FunctionInput.getDimensions()'
	 */
	public void testGetDimensions() throws StructuralException {
		FunctionInput input = new FunctionInput("test", new Function[]{new ConstantFunction(1, 1f)}, Units.UNK);
		assertEquals(1, input.getOrigin(FunctionInput.ORIGIN_NAME).getDimensions());

		input = new FunctionInput("test", new Function[]{new ConstantFunction(1, 1f), new ConstantFunction(1, 1f)}, Units.UNK);
		assertEquals(2, input.getOrigin(FunctionInput.ORIGIN_NAME).getDimensions());		
		
		try {
			input = new FunctionInput("test", new Function[]{new ConstantFunction(2, 1f)}, Units.UNK);
			fail("Should have thrown exception due to 2-D function");
		} catch (Exception e) {} //exception is expected
	}

	/*
	 * Test method for 'ca.nengo.model.impl.FunctionInput.getValues()'
	 */
	public void testGetValues() throws StructuralException, SimulationException {
		FunctionInput input = new FunctionInput("test", new Function[]{new ConstantFunction(1, 1f), new ConstantFunction(1, 2f)}, Units.UNK);
		Source source = input.getOrigin(FunctionInput.ORIGIN_NAME);
		assertEquals(2, source.get().getDimension());
		assertEquals(2, ((RealOutput) source.get()).getValues().length);
		
		input.run(0f, 1f);
		assertEquals(2, source.get().getDimension());
		assertEquals(2, ((RealOutput) source.get()).getValues().length);
		float value = ((RealOutput) source.get()).getValues()[0];
		assertTrue(value > .9f);
		value = ((RealOutput) source.get()).getValues()[1];
		assertTrue(value > 1.9f);
	}

	/*
	 * Test method for 'ca.nengo.model.impl.FunctionInput.getMode()'
	 */
	public void testGetMode() throws StructuralException {
		FunctionInput input = new FunctionInput("test", new Function[]{new ConstantFunction(1, 1f)}, Units.UNK);
		assertEquals(SimulationMode.DEFAULT, input.getMode());
	}

	/*
	 * Test method for 'ca.nengo.model.impl.FunctionInput.getHistory(String)'
	 */
	public void testGetHistory() throws StructuralException, SimulationException {
		FunctionInput input = new FunctionInput("test", new Function[]{new ConstantFunction(1, 1f), new ConstantFunction(1, 2f)}, Units.UNK);
		assertEquals(1, input.listStates().size());
		assertTrue(input.listStates().get(FunctionInput.STATE_NAME) != null);
		
		assertEquals(1, input.getHistory(FunctionInput.STATE_NAME).getValues().length);
		assertTrue(input.getHistory(FunctionInput.STATE_NAME).getValues()[0][0] > .5f);
		
		input.run(0f, 1f);
		
		assertEquals(1, input.getHistory(FunctionInput.STATE_NAME).getValues().length);
		assertTrue(input.getHistory(FunctionInput.STATE_NAME).getValues()[0][0] > .5f);
	}

}
