/*
 * Created on 6-Dec-07
 */
package ca.nengo.model.impl;

import ca.nengo.TestUtil;
import ca.nengo.config.Configuration;
import ca.nengo.config.SingleValuedProperty;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.MockConfigurable.MockChildConfigurable;
import junit.framework.TestCase;

import java.util.List;

/**
 * Unit tests for ConfigurationImpl. 
 * 
 * @author Bryan Tripp
 */
public class ConfigurationImplTest extends TestCase {

	private MockConfigurable myConfigurable;
	
	protected void setUp() throws Exception {
		super.setUp();
		myConfigurable = new MockConfigurable(MockConfigurable.getConstructionTemplate());
	}

	public void testGetConfigurable() {
		assertEquals(myConfigurable, myConfigurable.getConfiguration().getConfigurable());
	}

	public void testGetPropertyNames() {
		List<String> names = myConfigurable.getConfiguration().getPropertyNames();
		assertEquals(12, names.size());
		assertTrue(names.contains("intField"));
		assertTrue(names.contains("floatField"));
	}

	public void testIntProperty() throws StructuralException {
		assertEquals(myConfigurable.getIntField(), ((Integer) getSVProperty("intField").getValue()).intValue());
		myConfigurable.setIntField(2);
		assertEquals(2, ((Integer) getSVProperty("intField").getValue()).intValue());
		getSVProperty("intField").setValue(Integer.valueOf(3));
		assertEquals(3, myConfigurable.getIntField());
		
		try {
			getSVProperty("intField").setValue("wrong");
			fail("Should have thrown exception");
		} catch (StructuralException e) {}
	}
	
	public void testFloatProperty() throws StructuralException {
		TestUtil.assertClose(myConfigurable.getFloatField(), ((Float) getSVProperty("floatField").getValue()).floatValue(), .0001f);
		myConfigurable.setFloatField(2);
		TestUtil.assertClose(2, ((Float) getSVProperty("floatField").getValue()).floatValue(), .0001f);
		getSVProperty("floatField").setValue(new Float(3));
		TestUtil.assertClose(3, myConfigurable.getFloatField(), .0001f);
	}
	
	public void testBooleanProperty() throws StructuralException {
		assertEquals(myConfigurable.getBooleanField(), ((Boolean) getSVProperty("booleanField").getValue()).booleanValue());
		myConfigurable.setBooleanField(false);
		assertEquals(false, ((Boolean) getSVProperty("booleanField").getValue()).booleanValue());
		getSVProperty("booleanField").setValue(Boolean.valueOf(true));
		assertEquals(true, myConfigurable.getBooleanField());
	}
	
	//TODO: test remaining fields 
	
	public void testConstruction() throws StructuralException {
		Configuration template = MockConfigurable.getConstructionTemplate();
		((SingleValuedProperty) template.getProperty("immutableField")).setValue("custom");
		MockConfigurable c = new MockConfigurable(template);
		assertEquals("custom", c.getImmutableField());
	}
	
	public void testChild() throws StructuralException {
		MockChildConfigurable c = new MockChildConfigurable(MockChildConfigurable.getConstructionTemplate());
		assertEquals("foo", c.getImmutableField());
		
		((SingleValuedProperty) c.getConfiguration().getProperty("field")).setValue("foo");
		assertEquals("foo", c.getField());
	}
	
	private SingleValuedProperty getSVProperty(String name) throws StructuralException {
		return (SingleValuedProperty) myConfigurable.getConfiguration().getProperty(name);
	}

}
