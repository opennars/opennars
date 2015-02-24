/*
 * Created on 6-Dec-07
 */
package ca.nengo.model.impl;

import ca.nengo.config.Configurable;
import ca.nengo.config.Configuration;
import ca.nengo.config.SingleValuedProperty;
import ca.nengo.config.impl.ConfigurationImpl;
import ca.nengo.config.impl.ListPropertyImpl;
import ca.nengo.model.SimulationMode;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;

import java.util.ArrayList;
import java.util.List;

/**
 * A dummy Configurable class for testing purposes.
 * 
 * TODO: can we get rid of list methods if we don't need listeners?  
 * 
 * @author Bryan Tripp
 */
public class MockConfigurable implements Configurable {
	private int myIntField;
	private float myFloatField;
	private boolean myBooleanField;
	private String myStringField;
	private float[] myFloatArrayField;
	private float[][] myFloatArrayArrayField;
	private SimulationMode mySimulationModeField;
	private Units myUnitsField;
	private Configurable myConfigurableField;
	
	private final String myImmutableField;
	private final List<String> myMultiValuedField;
	private final String[] myFixedCardinalityField;
	
	private ConfigurationImpl myConfiguration;
	
	public MockConfigurable(Configuration immutableProperties) throws StructuralException {
		myImmutableField = ((SingleValuedProperty) immutableProperties.getProperty("immutableField")).getValue().toString();
		
		myIntField = 1;
		myFloatField = 1;
		myBooleanField = true;
		myStringField = "test";
		myFloatArrayField = new float[]{1, 2};
		myFloatArrayArrayField = new float[][]{new float[]{1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1}, new float[]{3, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1}};
		mySimulationModeField = SimulationMode.DEFAULT;
		myUnitsField = Units.UNK;
		myConfigurableField = new MockLittleConfigurable(); 
		
		myMultiValuedField = new ArrayList<String>(10);
		myFixedCardinalityField = new String[]{"test1", "test2"};
		
		myConfiguration = new ConfigurationImpl(this);
		myConfiguration.defineSingleValuedProperty("immutableField", String.class, false);
		
		myConfiguration.defineSingleValuedProperty("intField", Integer.TYPE, true);
		myConfiguration.defineSingleValuedProperty("floatField", Float.TYPE, true);
		myConfiguration.defineSingleValuedProperty("booleanField", Boolean.TYPE, true);
		myConfiguration.defineSingleValuedProperty("stringField", String.class, true);
		myConfiguration.defineSingleValuedProperty("floatArrayField", float[].class, true);
		myConfiguration.defineSingleValuedProperty("floatArrayArrayField", float[][].class, true);
		myConfiguration.defineSingleValuedProperty("simulationModeField", SimulationMode.class, true);
		myConfiguration.defineSingleValuedProperty("unitsField", Units.class, true);
		myConfiguration.defineSingleValuedProperty("configurableField", Configurable.class, true);
		myConfiguration.defineSingleValuedProperty("immutableField", String.class, false);
		
		myConfiguration.defineProperty(ListPropertyImpl.getListProperty(myConfiguration, "multiValuedField", String.class));
		myConfiguration.defineProperty(ListPropertyImpl.getListProperty(myConfiguration, "fixedCardinalityField", String.class));
//		
//		Property fcp = new FixedCardinalityProperty(myConfiguration, "fixedCardinalityField", String.class, true) {
//			@Override
//			public void doSetValue(int index, Object value) throws StructuralException {
//				myFixedCardinalityField[index] = (String) value;
//			}
//
//			@Override
//			public Object doGetValue(int index) throws StructuralException {
//				return myFixedCardinalityField[index];
//			}
//
//			@Override
//			public int getNumValues() {
//				return myFixedCardinalityField.length;
//			}
//		};
//		myConfiguration.defineProperty(fcp);
		
	}
	
	public static Configuration getConstructionTemplate() {
		ConfigurationImpl template = new ConfigurationImpl(null);
		template.defineTemplateProperty("immutableField", String.class, "immutable");
		return template;
	}

	/**
	 * @see ca.nengo.config.Configurable#getConfiguration()
	 */
	public Configuration getConfiguration() {
		return myConfiguration;
	}
	
	public String getImmutableField() {
		return myImmutableField;
	}
	
	public void setIntField(int val) {
		myIntField = val;
	}
	
	public int getIntField() {
		return myIntField;
	}
	
	public void setFloatField(float val) {
		myFloatField = val;
	}
	
	public float getFloatField() {
		return myFloatField;
	}
	
	public void setBooleanField(boolean val) {
		myBooleanField = val;
	}
	
	public boolean getBooleanField() {
		return myBooleanField;
	}

	public void setStringField(String val) {
		myStringField = val;
	}
	
	public String getStringField() {
		return myStringField;
	}

	public void setFloatArrayField(float[] val) {
		myFloatArrayField = val;
	}
	
	public float[] getFloatArrayField() {
		return myFloatArrayField;
	}

	public void setFloatArrayArrayField(float[][] val) {
		myFloatArrayArrayField = val;
	}
	
	public float[][] getFloatArrayArrayField() {
		return myFloatArrayArrayField;
	}

	public void setSimulationModeField(SimulationMode val) {
		mySimulationModeField = val;
	}
	
	public SimulationMode getSimulationModeField() {
		return mySimulationModeField;
	}

	public void setUnitsField(Units val) {
		myUnitsField = val;
	}
	
	public Units getUnitsField() {
		return myUnitsField;
	}

	public void setConfigurableField(Configurable val) {
		myConfigurableField = val;
	}
	
	public Configurable getConfigurableField() {
		return myConfigurableField;
	}

	public List<String> getMultiValuedField() {
		return new ArrayList<String>(myMultiValuedField);
	}
	
	public void setMultiValuedField(int index, String val) {
		myMultiValuedField.set(index, val);
	}
	
	public void addMultiValuedField(String val) {
		myMultiValuedField.add(val);
	}
	
	public void addMultiValuedField(int index, String val) {
		myMultiValuedField.add(index, val);
	}
	
	public void removeMultiValuedField(int index) {
		myMultiValuedField.remove(index);
	}
	
	public String[] getFixedCardinalityField() {
		String[] result = new String[myFixedCardinalityField.length];
		System.arraycopy(myFixedCardinalityField, 0, result, 0, result.length);
		return result;
	}
	
	public void setFixedCardinalityField(int index, String val) {
		myFixedCardinalityField[index] = val;
	}
	
	/**
	 * A simple dummy Configurable for nesting in MockConfigurable. 
	 * 
	 * @author Bryan Tripp
	 */
	public static class MockLittleConfigurable implements Configurable {

		private String myField;
		private ConfigurationImpl myConfiguration;
		
		public MockLittleConfigurable() {
			myField = "test";
			myConfiguration = new ConfigurationImpl(this);
			myConfiguration.defineSingleValuedProperty("field", String.class, true);
		}
		
		public Configuration getConfiguration() {
			return myConfiguration;
		}
		
		public void setField(String value) {
			myField = value;
		}
		
		public String getField() {
			return myField;
		}
		
	}

	/**
	 * A child of MockConfigurable. 
	 * 
	 * @author Bryan Tripp
	 */
	public static class MockChildConfigurable extends MockConfigurable {

		private String myField;
		
		public MockChildConfigurable(Configuration immutableProperties) throws StructuralException {
			super(addParentProperties(immutableProperties));
			myField = "test";
			((ConfigurationImpl) getConfiguration()).defineSingleValuedProperty("field", String.class, true);
		}
		
		private static Configuration addParentProperties(Configuration configuration) {
			((ConfigurationImpl) configuration).defineTemplateProperty("immutableField", String.class, "foo");
			return configuration;
		}
		
		public static Configuration getConstructionTemplate() {
			ConfigurationImpl template = new ConfigurationImpl(null);
			template.defineTemplateProperty("immutableFoo", String.class, "foo");
			return template;
		}
		
		public void setField(String val) {
			myField = val;			
		}
		
		public String getField() {
			return myField;
		}
		
	}
	
//	private static class MultiValuedProperty extends AbstractProperty implements ListProperty {
//		
//		private List<String> myMultiValuedField;
//		
//		public MultiValuedProperty(Configuration configuration, String name, Class c, boolean mutable, List<String> multiValuedField) {
//			super(configuration, name, c, mutable);
//			myMultiValuedField = multiValuedField;
//		}
//
//		public void addValue(Object value) throws StructuralException {
//			myMultiValuedField.add((String) value); 
//		}
//
//		public int getNumValues() {
//			return myMultiValuedField.size();
//		}
//
//		public boolean isFixedCardinality() {
//			return false;
//		}
//
//		public Object getDefaultValue() {
//			return null;
//		}
//
//		public Object getValue(int index) throws StructuralException {
//			return myMultiValuedField.get(index);
//		}
//
//		public void insert(int index, Object value) throws StructuralException {
//			myMultiValuedField.add(index, (String) value);
//		}
//
//		public void remove(int index) throws StructuralException {
//			myMultiValuedField.remove(index);
//		}
//
//		public void setValue(int index, Object value) throws StructuralException {
//			myMultiValuedField.set(index, (String) value);
//		}
//	};
	
	
}