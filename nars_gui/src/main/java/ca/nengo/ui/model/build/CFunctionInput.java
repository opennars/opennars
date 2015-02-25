/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "CFunctionInput.java". Description: 
""

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU 
Public License license (the GPL License), in which case the provisions of GPL 
License are applicable  instead of those above. If you wish to allow use of your 
version of this file only under the terms of the GPL License and not to allow 
others to use your version of this file under the MPL, indicate your decision 
by deleting the provisions above and replace  them with the notice and other 
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

package ca.nengo.ui.model.build;

import ca.nengo.math.Function;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.ui.config.ConfigException;
import ca.nengo.ui.config.ConfigResult;
import ca.nengo.ui.config.ConfigSchemaImpl;
import ca.nengo.ui.config.Property;
import ca.nengo.ui.config.descriptors.PFunctionArray;
import ca.nengo.ui.model.node.UIFunctionInput;

public class CFunctionInput extends CNode<FunctionInput> {

	public CFunctionInput() {
		super();
		pName.setDescription("Name of function input");
		pFunctions.setDescription("Defines the function to be used as an input to other components");
	}

	private static final Property pFunctions = new PFunctionArray("Function Generators", 1);

	@Override
	protected FunctionInput createNode(ConfigResult props, String name) throws ConfigException {

		Function[] functions = (Function[]) props.getValue(pFunctions);

		try {
			// setName((String) getProperty(pName));
			return new FunctionInput(name, functions, Units.UNK);
		} catch (StructuralException e) {
			throw new ConfigException(e.getMessage());

		}

	}

	@Override
	public ConfigSchemaImpl getNodeConfigSchema() {
		return new ConfigSchemaImpl(pFunctions);
	}

	public String getTypeName() {
		return UIFunctionInput.typeName;
	}
}
