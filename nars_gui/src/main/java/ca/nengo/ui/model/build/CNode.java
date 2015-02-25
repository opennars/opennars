/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ConstructableNode.java". Description: 
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

import ca.nengo.model.Node;
import ca.nengo.ui.config.*;
import ca.nengo.ui.config.descriptors.PString;

public abstract class CNode<N extends Node> extends AbstractConstructable<N> {
	protected static final Property pName = new PString("Name", "Item identifier", "");

	public CNode() {
		super();
	}

	@Override
	protected final N configureModel(ConfigResult configuredProperties) throws ConfigException {
		String name = (String) configuredProperties.getValue(pName);

		// if (nodeContainer.getNodeModel(name) != null) {
		// throw new ConfigException("A node with the same name already
		// exists");
		// }

		return createNode(configuredProperties, name);
	}

	protected abstract N createNode(ConfigResult configuredProperties, String name)
			throws ConfigException;

	public final ConfigSchema getSchema() {
		ConfigSchema nodeConfigSchema = getNodeConfigSchema();
        if (nodeConfigSchema==null) return null;

        java.util.List<Property> var = nodeConfigSchema.getAdvancedProperties();
        java.util.List<Property> var2 = nodeConfigSchema.getProperties();
        ConfigSchemaImpl newConfigSchema = new ConfigSchemaImpl(var.toArray(new Property[var.size()]), var2.toArray(new Property[var.size()]));

		newConfigSchema.addProperty(pName, 0);

		return newConfigSchema;
	}

	public abstract ConfigSchema getNodeConfigSchema();

}
