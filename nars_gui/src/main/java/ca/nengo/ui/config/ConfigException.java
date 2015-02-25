/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ConfigException.java". Description:
"Exception to be thrown is if there is an error during Configuration

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

import ca.nengo.ui.lib.UIException;
import ca.nengo.ui.lib.util.UserMessages;

import javax.swing.*;

/**
 * Exception to be thrown is if there is an error during Configuration
 * 
 * @author Shu Wu
 */
public class ConfigException extends UIException {

    private static final long serialVersionUID = 1L;
    private final boolean isRecoverable;

    /**
     * @param message
     *            Message describing the exception
     */
    public ConfigException(String message) {
        this(message, true);
    }

    /**
     * @param message
     *            Message describing the exception
     * @param isRecoverable
     *            if true, the Configurer will try to recover from the exception
     */
    public ConfigException(String message, boolean isRecoverable) {
        super(message);
        this.isRecoverable = isRecoverable;
    }

    @Override
    public void defaultHandleBehavior() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UserMessages.showWarning("Could not complete configuration: "
                        + getMessage());
            }
        });

    }

    /**
     * @return TODO
     */
    public boolean isRecoverable() {
        return isRecoverable;
    }

}
