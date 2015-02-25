/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "FnReflective.java". Description:
"Function instances are created through reflection.

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

package ca.nengo.ui.config.descriptors.functions;

import ca.nengo.math.Function;
import ca.nengo.ui.config.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Function instances are created through reflection.
 *
 * @author Shu Wu
 */
public class FnReflective extends AbstractFn {
    private Property[] myProperties;
    private String[] getterNames;

    /**
     * @param functionClass Type of function to construct
     * @param typeName Friendly name of function
     * @param properties A ordered list of properties which map to the function constructor arguments
     * @param getterNames A ordered list of getter function names which map to the constructor arguments
     */
    public FnReflective(
            Class<? extends Function> functionClass,
            String typeName,
            Property[] properties,
            String[] getterNames) {
        super(typeName, functionClass);
        if (properties.length != getterNames.length) {
            throw new IllegalArgumentException("properties and getterNames must be the same length");
        }

        this.myProperties = properties;
        this.getterNames = getterNames;
    }

    @Override
    protected Function createFunction(ConfigResult props) throws ConfigException {
        List<Property> metaProperties = getSchema().getProperties();

        /*
         * Create function using Java reflection, function parameter are
         * configured via the IConfigurable interface
         */
        Class<?> partypes[] = new Class[metaProperties.size()];
        for (int i = 0; i < metaProperties.size(); i++) {
            partypes[i] = metaProperties.get(i).getTypeClass();

        }
        Constructor<?> ct = null;
        try {
            ct = getFunctionType().getConstructor(partypes);

            Object arglist[] = new Object[metaProperties.size()];
            for (int i = 0; i < metaProperties.size(); i++) {
                arglist[i] = props.getValue(metaProperties.get(i).getName());
            }
            Object retobj = null;
            try {
                retobj = ct.newInstance(arglist);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            return (Function) retobj;

        } catch (SecurityException e) {
            e.printStackTrace();
            throw new ConfigException("Could not configure function: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new ConfigException("Could not configure function, no suitable constructor found");
        }
    }

    public ConfigSchema getSchema() {
        if (getFunction() != null) {
            Function func = getFunction();
            for (int i = 0; i < myProperties.length; i++) {
                Property property = myProperties[i];
                String getterName = getterNames[i];
                try {
                    Object result = func.getClass().getMethod(getterName).invoke(func);
                    property.setDefaultValue(result);
                } catch (NoSuchMethodException e) {
                	e.printStackTrace();
                } catch (SecurityException e) {
                	e.printStackTrace();
                } catch (IllegalAccessException e) {
                	e.printStackTrace();
                } catch (IllegalArgumentException e) {
                	e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ConfigSchemaImpl(myProperties);
    }

}
