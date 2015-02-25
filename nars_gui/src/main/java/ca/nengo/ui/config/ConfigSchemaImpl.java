/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ConfigSchemaImpl.java". Description:
"Default implementation of a IConfigSchema

  @author Shu Wu"

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

package ca.nengo.ui.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of a IConfigSchema
 * 
 * @author Shu Wu
 */
public class ConfigSchemaImpl implements ConfigSchema {
    private final List<Property> advancedProperties;
    private final List<Property> properties;

    /**
     * Default constructor, no property descriptors
     */
    public ConfigSchemaImpl() {
        this(new Property[] {}, new Property[] {});
    }

    /**
     * @param property TODO
     */
    public ConfigSchemaImpl(Property property) {
        this(new Property[] { property }, new Property[] {});
    }

    /**
     * @param properties TODO
     */
    public ConfigSchemaImpl(Property[] properties) {
        this(properties, new Property[] {});
    }

    /**
     * @param properties TODO
     * @param advancedProperties TODO
     */
    public ConfigSchemaImpl(Property[] properties,
            Property[] advancedProperties) {
        super();
        this.properties = new ArrayList<Property>(properties.length);
        Collections.addAll(this.properties, properties);

        this.advancedProperties = new ArrayList<Property>(properties.length);
        Collections.addAll(this.advancedProperties, advancedProperties);
    }

    /**
     * @param propDesc
     *            Property Descriptor
     * @param position
     *            Location to insert into the property list
     */
    public void addProperty(Property propDesc, int position) {
        properties.add(position, propDesc);
    }

    public List<Property> getAdvancedProperties() {
        return advancedProperties;
    }

    public List<Property> getProperties() {
        return properties;
    }

}
