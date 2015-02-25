/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "AbstractFn.java". Description:
"@author User"

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
import ca.nengo.ui.config.ConfigException;
import ca.nengo.ui.config.ConfigResult;
import ca.nengo.ui.config.IConfigurable;
import ca.nengo.ui.config.managers.UserConfigurer;
import ca.nengo.ui.lib.util.UserMessages;

import java.awt.*;

/**
 * Describes how to configure a function through the IConfigurable interface.
 * 
 * @author Shu Wu
 */
public abstract class AbstractFn implements IConfigurable, ConfigurableFunction {

    private UserConfigurer configurer;

    /**
     * Function to be created
     */
    private Function function;

    private final Class<? extends Function> functionType;

    /**
     * What the type of function to be created is called
     */
    private final String typeName;

    /**
     * @param typeName
     * @param functionType
     */
    public AbstractFn(String typeName, Class<? extends Function> functionType) {
        super();

        this.typeName = typeName;
        this.functionType = functionType;
    }

    protected abstract Function createFunction(ConfigResult props) throws ConfigException;

    /*
     * (non-Javadoc)
     * 
     * @see ca.nengo.ui.configurable.IConfigurable#completeConfiguration(ca.nengo.ui.configurable.ConfigParam)
     *      Creates the function through reflection of its constructor and
     *      passing the user parameters to it
     */
    public void completeConfiguration(ConfigResult props) throws ConfigException {
        try {
            Function function = createFunction(props);
            setFunction(function);
        } catch (Exception e) {
            throw new ConfigException("Error creating function");
        }
    }

    public Function configureFunction(Dialog parent) {
        if (parent != null) {

            if (configurer == null) {
                configurer = new UserConfigurer(this, parent);
            }
            try {
                configurer.configureAndWait();
                return getFunction();
            } catch (ConfigException e) {
                e.defaultHandleBehavior();
            }
        } else {
            UserMessages.showError("Could not attach properties dialog");
        }
        return null;
    }

    public String getDescription() {
        return getTypeName();
    }

    /**
     * @return The function created
     */
    public Function getFunction() {
        return function;
    }

    public Class<? extends Function> getFunctionType() {
        return functionType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.nengo.ui.configurable.IConfigurable#getTypeName()
     */
    public String getTypeName() {
        return typeName;
    }

    public void preConfiguration(ConfigResult props) throws ConfigException {
        // do nothing
    }

    /**
     * @param function
     *            function wrapper
     */
    public final void setFunction(Function function) {
        if (function != null) {
            if (!getFunctionType().isInstance(function)) {
                throw new IllegalArgumentException("Unexpected function type");
            } else {
                this.function = function;
            }
        } else {
            this.function = null;
        }
    }

    @Override
    public String toString() {
        return getTypeName();
    }

	public String getExtendedDescription() {
		return null;
	}


}