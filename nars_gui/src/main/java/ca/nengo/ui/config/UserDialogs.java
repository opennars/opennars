/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "UserDialogs.java". Description:
"Creates various dialogs and returns user results

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

import ca.nengo.ui.config.descriptors.PBoolean;
import ca.nengo.ui.config.descriptors.PFloat;
import ca.nengo.ui.config.descriptors.PInt;
import ca.nengo.ui.config.descriptors.PString;
import ca.nengo.ui.config.managers.UserConfigurer;

/**
 * Creates various dialogs and returns user results
 * 
 * @author Shu Wu
 */
public class UserDialogs {

    /**
     * @param dialogName TODO
     * @param defaultValue TODO
     * @return TODO
     * @throws ConfigException TODO
     */
    public static Float showDialogFloat(String dialogName, Float defaultValue)
            throws ConfigException {
        return (Float) showDialog("Config", new PFloat(dialogName, defaultValue));
    }

    /**
     * @param dialogName TODO
     * @param defaultValue TODO
     * @return TODO
     * @throws ConfigException TODO
     */
    public static Boolean showDialogBoolean(String dialogName, Boolean defaultValue)
            throws ConfigException {
        return (Boolean) showDialog("Config", new PBoolean(dialogName, defaultValue));
    }

    /**
     * @param dialogName TODO
     * @param defaultValue TODO
     * @return TODO
     * @throws ConfigException TODO
     */
    public static Integer showDialogInteger(String dialogName, int defaultValue)
            throws ConfigException {
        return (Integer) showDialog("Config", new PInt(dialogName, defaultValue));
    }

    /**
     * @param dialogName TODO
     * @param defaultValue TODO
     * @return TODO
     * @throws ConfigException TODO
     */
    public static String showDialogString(String dialogName, String defaultValue)
            throws ConfigException {
        return (String) showDialog("Config", new PString(dialogName, null, defaultValue));
    }

    /**
     * @param dialogName TODO
     * @param descriptor TODO
     * @return TODO
     * @throws ConfigException TODO
     */
    public static Object showDialog(String dialogName, Property descriptor) throws ConfigException {
        return showDialog(dialogName, new Property[] { descriptor }).getValue(descriptor);
    }

    /**
     * @param dialogName TODO
     * @param descriptors TODO
     * @return TODO
     * @throws ConfigException TODO
     */
    public static ConfigResult showDialog(String dialogName, Property[] descriptors)
            throws ConfigException {
        UserMultiPropDialog dialog = new UserMultiPropDialog(dialogName, descriptors);

        return dialog.configureAndGetResult();

    }
}

/**
 * Creates a configuration dialog from configuration descriptors
 * 
 * @author Shu Wu
 */
class UserMultiPropDialog {
    private final Property[] propertiesSchema;
    private ConfigResult configResults;
    private final String dialogName;

    public UserMultiPropDialog(String dialogName, Property[] configParameters) {
        this.dialogName = dialogName;
        this.propertiesSchema = configParameters;
    }

    public ConfigResult configureAndGetResult() throws ConfigException {

        Configr myConfigurable = new Configr();
        UserConfigurer userConfigurer = new UserConfigurer(myConfigurable);

        userConfigurer.configureAndWait();
        return configResults;
    }

    private class Configr implements IConfigurable {

        public void completeConfiguration(ConfigResult configParameters) throws ConfigException {
            configResults = configParameters;
        }

        public ConfigSchema getSchema() {
            return new ConfigSchemaImpl(propertiesSchema);
        }

        public String getTypeName() {
            return dialogName;
        }

        public void preConfiguration(ConfigResult props) throws ConfigException {
            // do nothing
        }

        public String getDescription() {
            return getTypeName();
        }
    	public String getExtendedDescription() {
    		return null;
    	}
        

    }

}
