/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "IConfigurable.java". Description:
"Describes a object which can be configured by a IConfigurationManager

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

/**
 * Describes a object which can be configured by a IConfigurationManager
 * 
 * @author Shu Wu
 */
public interface IConfigurable {

    /**
     * Called when configuration parameters have been set
     * 
     * @param props A set of properties
     * @throws ConfigException Exception thrown if there is an error during
     *             pre-configuration.
     */
    public void completeConfiguration(ConfigResult props) throws ConfigException;

    /**
     * Called before full configuration to initialize and find errors.
     * 
     * @param props A set of properties
     * @throws ConfigException Exception thrown if there is an error during
     *             pre-configuration.
     */
    public void preConfiguration(ConfigResult props) throws ConfigException;

    /**
     * @return An array of objects which describe what needs to be configured in
     *         this object
     */
    public ConfigSchema getSchema();

    /**
     * @return Name given to this type of object
     */
    public String getTypeName();

    /**
     * @return Name given to this type of object
     */
    public String getDescription();
    
    /** 
     * @return an html-formatted extended description/instructions of 
     * what will be created 
     */
    public String getExtendedDescription();

}
