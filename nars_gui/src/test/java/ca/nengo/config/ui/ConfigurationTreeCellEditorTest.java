package ca.nengo.config.ui;

import ca.nengo.dynamics.impl.LTISystem;
import ca.nengo.dynamics.impl.SimpleLTISystem;
import junit.framework.TestCase;

/**
 * This is currently a functional test for tree cell resizing on the mac. 
 * 
 * @author Bryan Tripp
 */
public class ConfigurationTreeCellEditorTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testNothing() {		
	}
	
	public static void main(String[] args) {
		NewConfigurableDialog.showDialog(null, LTISystem.class, SimpleLTISystem.class);
	}

}
