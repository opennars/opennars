/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ConfigManager.java". Description:
"Configuration Manager used to configure IConfigurable objects

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

package ca.nengo.ui.config.managers;

import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.config.*;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.util.Util;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.io.*;

/**
 * Configuration Manager used to configure IConfigurable objects
 * 
 * @author Shu Wu
 */
public abstract class ConfigManager {
    /**
     * Name of directory where to store saved configuration
     */
    static final String SAVED_CONFIG_DIR = AbstractNengo.USER_FILE_DIR + "/Config";
    
    static final String DEV_DIST_DIR = "dist-files/" + AbstractNengo.USER_FILE_DIR + "/Config";

    /**
     * Creates a saved objects folder if it isn't already there
     * 
     * @return The Saved Objects folder
     */
    private static File getSavedObjectsFolder() {
        File file = new File(SAVED_CONFIG_DIR);
        if (!file.exists()) {
            file.mkdir();
            
            // If we are building from source, find the dev config file
            // directory and copy it to the directory it's expecting to find config files
            File devConfigDir = new File(DEV_DIST_DIR);
            if (devConfigDir.exists()) {
            	File[] devConfigFiles = devConfigDir.listFiles();
            	for (File devConfigFile : devConfigFiles) {
            		File newConfigFile = new File(SAVED_CONFIG_DIR + '/' + devConfigFile.getName());
            		try {
            			Util.copyFile(devConfigFile, newConfigFile);
            		} catch (IOException e) {
            			System.out.println(e.getMessage());
            		}
            	}
            }
        }
        return file;
    }

    /**
     * TODO
     * 
     * @author TODO
     */
    public enum ConfigMode {
        /**
         * TODO
         */
        STANDARD,

        /**
         * TODO
         */
        TEMPLATE_NOT_CHOOSABLE,

        /**
         * TODO
         */
        TEMPLATE_CHOOSABLE
    }

    /**
     * @param prop TODO
     * @param typeName TODO
     * @param parent TODO
     * @return TODO
     * @throws ConfigException TODO
     */
    public static Object configure(Property prop, String typeName, Container parent)
            throws ConfigException {

        ConfigResult properties = configure(new ConfigSchemaImpl(prop), typeName, null, parent,
                ConfigMode.TEMPLATE_NOT_CHOOSABLE);
        return properties.getValue(prop);
    }

    /**
     * Convenient function to automatically wrap the PropertyDescriptors with a
     * default Config schema
     * @param schema TODO
     * @param typeName TODO
     * @param parent TODO
     * @param configMode TODO
     * @return TODO
     * @throws ConfigException TODO
     */
    public static ConfigResult configure(Property[] schema, String typeName, Container parent,
            ConfigMode configMode) throws ConfigException {
        return configure(new ConfigSchemaImpl(schema), typeName, null, parent, configMode);
    }

    /**
     * @param schema TODO
     * @param typeName TODO
     * @param description TODO
     * @param parent TODO
     * @param configMode TODO
     * @return TODO
     * @throws ConfigException TODO
     */
    public static ConfigResult configure(ConfigSchema schema, String typeName, String description,
            Container parent, ConfigMode configMode) throws ConfigException {
        if (description == null) {
            description = typeName;
        }
        Configureable configurable = new Configureable(schema, typeName, description);

        UserConfigurer configurer;

        if (configMode == ConfigMode.STANDARD) {
            configurer = new UserConfigurer(configurable, parent);
        } else if (configMode == ConfigMode.TEMPLATE_NOT_CHOOSABLE) {
            configurer = new UserTemplateConfigurer(configurable, parent, false);
        } else if (configMode == ConfigMode.TEMPLATE_CHOOSABLE) {
            configurer = new UserTemplateConfigurer(configurable, parent, true);
        } else {
            throw new IllegalArgumentException("Unsupported config mode");
        }

        configurer.configureAndWait();

        return configurable.getProperties();
    }

    /**
     * @returns the file name prefix given per class
     */
    protected static String getFileNamePrefix(IConfigurable obj) {

        return obj.getTypeName() + "_Props_";

    }

    /**
     * Object to be configured
     */
    private final IConfigurable configurable;

    /**
     * Set of attributes that will be set during configuration
     */
    private MutableAttributeSet properties;

    /**
     * @param configurable
     *            Object to be configured
     */
    public ConfigManager(IConfigurable configurable) {
        super();
        properties = new SimpleAttributeSet();
        this.configurable = configurable;

    }

    /**
     * Configures the IConfigurable object and waits until the configuration
     * finishes
     */
    protected abstract void configureAndWait() throws ConfigException;

    /**
     * @param name
     *            filename prefix
     */
    protected void deletePropertiesFile(String name) {
        File file = new File(getSavedObjectsFolder(), getFileNamePrefix(configurable) + name);

        System.gc();
        if (file.exists()) {
            boolean val = file.delete();
            if (val == false) {
                UserMessages.showError("Could not delete file");
            }

        }
    }

    /**
     * @return Object to be configured
     */
    protected IConfigurable getConfigurable() {
        return configurable;
    }

    /**
     * @return Set of properties to be set during the configuration process
     */
    protected MutableAttributeSet getProperties() {
        return properties;
    }

    /**
     * @param name
     *            Name of property
     * @return Value of property
     */
    protected Object getProperty(String name) {
        return getProperties().getAttribute(name);
    }

    /**
     * @return List of fileNames which point to saved configuration files
     */
    protected String[] getPropertyFiles() {
        File file = getSavedObjectsFolder();
        /*
         * Gets a list of property files
         */
        String[] files = file.list(new ConfigFilesFilter(configurable));

        /*
         * Return the file names without the prefix
         */
        String[] files0 = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            files0[i] = files[i].substring(getFileNamePrefix(configurable).length(), files[i]
                    .length());
        }
        return files0;

    }

    /**
     * @param name
     *            Name of the properties set to be loaded
     */
    protected void loadPropertiesFromFile(String name) {

        FileInputStream f_in;

        try {
            f_in = new FileInputStream(SAVED_CONFIG_DIR + '/' + getFileNamePrefix(configurable)
                    + name);

            ObjectInputStream obj_in = new ObjectInputStream(f_in);

            Object obj;

            obj = obj_in.readObject();

            if (obj == null) {
                UserMessages.showError("Could not load file: " + name);
            } else {

                this.properties = (MutableAttributeSet) obj;
            }
            obj_in.close();

        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found exception");
        } catch (InvalidClassException e) {
            System.out.println("Invalid class exception");
        } catch (NotSerializableException e) {
            Util.debugMsg("Loading properties not serializable: " + e.getMessage());
        } catch (IOException e) {
            Util.debugMsg("IO Error serializing properties: " + e.getMessage());
        }
    }

    /**
     * @param name
     *            name of the properties set to be saved
     */
    protected void savePropertiesFile(String name) {

        // Write to disk with FileOutputStream
        FileOutputStream f_out;
        try {
            File objectsFolder = getSavedObjectsFolder();
            File file = new File(objectsFolder, getFileNamePrefix(configurable) + name);

            if (file.exists()) {
                Util.debugMsg("Replaced existing file: " + file.getName());
            }
            f_out = new FileOutputStream(file);

            ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
            obj_out.writeObject(properties);
            obj_out.close();

            f_out.close();

        } catch (NotSerializableException e) {
            Util.debugMsg("Did not save settings to template because " + e.getMessage());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void setProperty(String name, Object value) {
        getProperties().addAttribute(name, value);
    }

}

/**
 * Filters files needed by ConfigManager
 * 
 * @author Shu
 */
class ConfigFilesFilter implements FilenameFilter {
    final IConfigurable parent;

    public ConfigFilesFilter(IConfigurable parent) {
        super();
        this.parent = parent;
    }

    public boolean accept(File file, String name) {

        if (name.startsWith(ConfigManager.getFileNamePrefix(parent))) {
            return true;
        } else {
            return false;
        }

    }
}

class Configureable implements IConfigurable {

    private ConfigResult properties;

    private final ConfigSchema schema;
    private final String typeName;
    private final String description;

    public Configureable(ConfigSchema configSchema, String typeName, String description) {
        super();
        this.schema = configSchema;
        this.typeName = typeName;
        this.description = description;
    }

    public void completeConfiguration(ConfigResult props) throws ConfigException {
        properties = props;
    }

    public ConfigSchema getSchema() {
        return schema;
    }

    public ConfigResult getProperties() {
        return properties;
    }

    public String getTypeName() {
        return typeName;
    }

    public void preConfiguration(ConfigResult props) throws ConfigException {
        // do nothing
    }

    public String getDescription() {
        return description;
    }
	public String getExtendedDescription() {
		return null;
	}

}