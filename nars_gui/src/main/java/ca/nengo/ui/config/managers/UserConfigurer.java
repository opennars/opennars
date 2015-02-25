/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "UserConfigurer.java". Description:
"Configuration Manager which creates a dialog and let's the user enter
  parameters to used for configuration

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

import ca.nengo.ui.config.ConfigException;
import ca.nengo.ui.config.IConfigurable;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.Util;

import java.awt.*;

/**
 * Configuration Manager which creates a dialog and let's the user enter
 * parameters to used for configuration
 * 
 * @author Shu Wu
 */
public class UserConfigurer extends ConfigManager {

    /**
     * Exception thrown during configuration
     */
    private ConfigException configException;

    /**
     * Lock to be used to communicate cross-thread between this instance and the
     * Configuration Dialog
     */
    private Object configLock;

    /**
     * Parent, if there is one
     */
    protected final Container parent;

    /**
     * @param configurable
     *            Object to be configured
     */
    public UserConfigurer(IConfigurable configurable) {
        super(configurable);
        this.parent = UIEnvironment.getInstance();
    }

    /**
     * @param configurable
     *            Object to be configured
     * @param parent
     *            Frame the user configuration dialog should be attached to
     */
    public UserConfigurer(IConfigurable configurable, Container parent) {
        super(configurable);
        this.parent = parent;
    }

    /**
     * Creates the configuration dialog
     * 
     * @return Created Configuration dialog
     */
    protected ConfigDialog createConfigDialog() {
        if (parent instanceof Frame) {

            return new ConfigDialog(this, (Frame) parent);
        } else if (parent instanceof Dialog) {
            return new ConfigDialog(this, (Dialog) parent);
        } else {
            Util.Assert(false,
                    "Could not create config dialog because parent type if not supported");

        }
        return null;
    }

    /**
     * @param configException
     *            Configuration Exception thrown during configuration, none if
     *            everything went smoothly
     */
    protected void dialogConfigurationFinished(ConfigException configException) {
        this.configException = configException;
        synchronized (configLock) {
            configLock.notifyAll();
            configLock = null;
        }
    }

    @Override
    public synchronized void configureAndWait() throws ConfigException {
        if (configLock == null) {
            configLock = new Object();
        }

        ConfigDialog dialog = createConfigDialog();
        //dialog.setModal(false);   // fixes object creation in OpenJDK
        dialog.setVisible(true);

        /*
         * Block until configuration has completed
         */
        
        
        if (configLock != null) {
            synchronized (configLock) {
                try {
                    if (configLock != null) {
                        configLock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
		
        if (configException != null) {
            throw configException;
        }
        

    }

}
