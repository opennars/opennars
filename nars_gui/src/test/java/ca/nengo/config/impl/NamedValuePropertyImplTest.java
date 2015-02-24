/*
 * Created on 21-Jan-08
 */
package ca.nengo.config.impl;

import ca.nengo.config.NamedValueProperty;
import ca.nengo.config.ui.ConfigurationTreeCellEditor;
import ca.nengo.config.ui.ConfigurationTreeCellRenderer;
import ca.nengo.config.ui.ConfigurationTreeModel;
import ca.nengo.config.ui.ConfigurationTreePopupListener;
import ca.nengo.model.StructuralException;
import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import ca.nengo.model.nef.NEFEnsembleFactory;
//import ca.nengo.model.nef.impl.NEFEnsembleFactoryImpl;

/**
 * Unit tests for NamedValuePropertyImpl. 
 * 
 * @author Bryan Tripp
 */
public class NamedValuePropertyImplTest extends TestCase {

	private MockObject myObject;
	private ConfigurationImpl myConfiguration;
	
	protected void setUp() throws Exception {
		super.setUp();
		myObject = new MockObject();
		myConfiguration = new ConfigurationImpl(myObject);
		myConfiguration.defineProperty(NamedValuePropertyImpl.getNamedValueProperty(myConfiguration, "A", String.class));
		myConfiguration.defineProperty(NamedValuePropertyImpl.getNamedValueProperty(myConfiguration, "B", MockNamedObject.class));
		myConfiguration.defineProperty(NamedValuePropertyImpl.getNamedValueProperty(myConfiguration, "C", String.class));
		myConfiguration.defineProperty(NamedValuePropertyImpl.getNamedValueProperty(myConfiguration, "D", MockNamedObject.class));
		myConfiguration.defineProperty(NamedValuePropertyImpl.getNamedValueProperty(myConfiguration, "E", String.class));
		assertTrue(NamedValuePropertyImpl.getNamedValueProperty(myConfiguration, "G", String.class) == null);
	}
	
	public NamedValueProperty getProperty(String name) throws StructuralException {
		return (NamedValueProperty) myConfiguration.getProperty(name);
	}

	public void testIsMutable() throws StructuralException {
		assertFalse(myConfiguration.getProperty("A").isMutable());
		assertFalse(myConfiguration.getProperty("B").isMutable());
		assertTrue(myConfiguration.getProperty("C").isMutable());
		assertTrue(myConfiguration.getProperty("D").isMutable());
		assertTrue(myConfiguration.getProperty("E").isMutable());
	}

	public void testGetValue() throws StructuralException {
		assertEquals("1", getProperty("A").getValue("1"));
		assertEquals("1", ((MockNamedObject) getProperty("B").getValue("1")).getValue());
		assertEquals("1", getProperty("C").getValue("1"));
		assertEquals("1", ((MockNamedObject) getProperty("D").getValue("1")).getValue());
		assertEquals("1", getProperty("E").getValue("1"));
	}

	public void testGetValueNames() throws StructuralException {
		List<String> names = null;
		
		names = getProperty("A").getValueNames();
		assertEquals(1, names.size());
		assertEquals("1", names.get(0));
		names = getProperty("B").getValueNames();
		assertEquals(1, names.size());
		assertEquals("1", names.get(0));
		names = getProperty("C").getValueNames();
		assertEquals(1, names.size());
		assertEquals("1", names.get(0));
		names = getProperty("D").getValueNames();
		assertEquals(1, names.size());
		assertEquals("1", names.get(0));
		names = getProperty("E").getValueNames();
		assertEquals(1, names.size());
		assertEquals("1", names.get(0));
	}

	public void testIsNamedAutomatically() throws StructuralException {
		assertFalse(getProperty("A").isNamedAutomatically());
		assertFalse(getProperty("B").isNamedAutomatically());
		assertFalse(getProperty("C").isNamedAutomatically());
		assertTrue(getProperty("D").isNamedAutomatically());
		assertFalse(getProperty("E").isNamedAutomatically());
	}

	public void testRemoveValue() throws StructuralException {
		try {
			getProperty("A").removeValue("1");			
		} catch (StructuralException e) {}
		
		try {
			getProperty("B").removeValue("1");			
		} catch (StructuralException e) {}
		
		try {
			getProperty("C").removeValue("1");			
		} catch (StructuralException e) {}
		
		getProperty("D").removeValue("1");
		assertEquals(0, getProperty("D").getValueNames().size());
		getProperty("E").removeValue("1");
		assertEquals(0, getProperty("E").getValueNames().size());
	}

	public void testSetValueStringObject() throws StructuralException {
		try {
			getProperty("A").setValue("1", "2");
			fail("Should have thrown exception");
		} catch (StructuralException e) {}
		
		try {
			getProperty("B").setValue("1", new MockNamedObject("1", "2"));
			fail("Should have thrown exception");
		} catch (StructuralException e) {}
		
		getProperty("C").setValue("1", "2");
		assertEquals("2", getProperty("C").getValue("1"));
		
		getProperty("D").setValue("1", new MockNamedObject("1", "2"));
		assertEquals("2", ((MockNamedObject) getProperty("D").getValue("1")).getValue());		
		
		getProperty("E").setValue("1", "2");
		assertEquals("2", getProperty("E").getValue("1"));		
	}

	public void testSetValueObject() throws StructuralException {
		try {
			getProperty("A").setValue("2");
			fail("Should have thrown exception");
		} catch (StructuralException e) {}
		
		try {
			getProperty("B").setValue(new MockNamedObject("1", "2"));
			fail("Should have thrown exception");
		} catch (StructuralException e) {}
		
		try {
			getProperty("C").setValue("2");
			fail("Should have thrown exception");
		} catch (StructuralException e) {}

		getProperty("D").setValue(new MockNamedObject("1", "2"));
		assertEquals("2", ((MockNamedObject) getProperty("D").getValue("1")).getValue());		
		
		try {
			getProperty("E").setValue("2");
			fail("Should have thrown exception");
		} catch (StructuralException e) {}
	}

	public void testIsFixedCardinality() throws StructuralException {
		assertTrue(getProperty("A").isFixedCardinality());
		assertTrue(getProperty("B").isFixedCardinality());
		assertFalse(getProperty("C").isFixedCardinality());
		assertFalse(getProperty("D").isFixedCardinality());
		assertFalse(getProperty("E").isFixedCardinality());
	}

	private static class MockObject {
		
		private Map<String, String> myA; //explicit accessors immutable  
		private List<MockNamedObject> myB; //explicit accessors array  
		private Map<String, String> myC; //explicit accessors mutable  
		private List<MockNamedObject> myD; //explicit accessors mutable auto-named 
		private Map<String, String> myE; //map accessor
		
		public MockObject() {
			myA = new HashMap<String, String>(10);
			myA.put("1", "1");
			
			myB = new ArrayList<MockNamedObject>(10);
			myB.add(new MockNamedObject("1", "1"));
			
			myC = new HashMap<String, String>(10);
			myC.put("1", "1");
			
			myD = new ArrayList<MockNamedObject>(10);
			myD.add(new MockNamedObject("1", "1"));

			myE = new HashMap<String, String>(10);
			myE.put("1", "1");
		}
		@SuppressWarnings("unused")
		public String getA(String name) {
			return myA.get(name);
		}
		@SuppressWarnings("unused")
		public String[] getANames() {
			return myA.keySet().toArray(new String[0]);
		}
		@SuppressWarnings("unused")
		public MockNamedObject getB(String name) {
			MockNamedObject result = null;
			for (int i = 0; i < myB.size() && result == null; i++) {
				if (myB.get(i).getName().equals(name)) result = myB.get(i);
			}
			return result;
		}
		@SuppressWarnings("unused")
		public MockNamedObject[] getB() {
			return myB.toArray(new MockNamedObject[0]);
		}
		@SuppressWarnings("unused")
		public String getC(String name) {
			return myC.get(name);
		}
		@SuppressWarnings("unused")
		public String[] getCNames() {
			return myC.keySet().toArray(new String[0]);
		}
		@SuppressWarnings("unused")
		public void setC(String name, String value) {
			myC.put(name, value);
		}
		@SuppressWarnings("unused")
		public void removeC(String name) {
			myC.remove(name);
		}
		@SuppressWarnings("unused")
		public MockNamedObject getD(String name) {
			MockNamedObject result = null;
			
			for (int i = 0; i < myD.size() && result == null; i++) {
				if (myD.get(i).getName().equals(name)) result = myD.get(i);
			}
			
			return result;
		}
		@SuppressWarnings("unused")
		public MockNamedObject[] getD() {			
			return myD.toArray(new MockNamedObject[0]);
		}
		@SuppressWarnings("unused")
		public void setD(MockNamedObject value) {
			removeD(value.getName());
			myD.add(value);
		}
		public void removeD(String name) {
			for (int i = 0; i < myD.size(); i++) {
				if (myD.get(i).getName().equals(name)) myD.remove(i);
			}
		}
		@SuppressWarnings("unused")
		public Map<String, String> getE() {
			return myE;
		}
		
	}
	
	private static class MockNamedObject {
		
		private String myName;
		private String myValue;
		
		public MockNamedObject(String name, String value) {
			myName = name;
			myValue = value;
		}
		
		public String getName() {
			return myName;
		}
		
		public String getValue() {
			return myValue;
		}
	}
	
	public static void main(String[] args) {
		try {
			JFrame frame = new JFrame("Tree Test");
			Object configurable = new MockObject();
			
			ConfigurationTreeModel model = new ConfigurationTreeModel(configurable); 
			JTree tree = new JTree(model);
			tree.setEditable(true); 
			tree.setCellEditor(new ConfigurationTreeCellEditor(tree));
			tree.addMouseListener(new ConfigurationTreePopupListener(tree, model));
			ConfigurationTreeCellRenderer cellRenderer = new ConfigurationTreeCellRenderer();
			tree.setCellRenderer(cellRenderer);
			
			ToolTipManager.sharedInstance().registerComponent(tree);
			
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);
			
			frame.pack();
			frame.setVisible(true);
			
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent arg0) {
					System.exit(0);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
