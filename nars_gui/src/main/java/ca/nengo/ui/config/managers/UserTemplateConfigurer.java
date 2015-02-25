/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "UserTemplateConfigurer.java". Description:
"A lot like UserConfigurer, except it allows the user to use templates to save
  and re-use values

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

import ca.nengo.ui.config.IConfigurable;
import ca.nengo.ui.lib.util.Util;

import java.awt.*;

/**
 * A lot like UserConfigurer, except it allows the user to use templates to save
 * and re-use values
 * 
 * @author Shu Wu
 */
public class UserTemplateConfigurer extends UserConfigurer {
    /**
     * Name of the default template
     */
    public static final String DEFAULT_TEMPLATE_NAME = "default";

    /**
     * TODO
     */
    public static final String PREFERRED_TEMPLATE_NAME = "last_used";

    private boolean isTemplateEditable;

    /**
     * @param configurable TODO
     */
    public UserTemplateConfigurer(IConfigurable configurable) {
        super(configurable);
        init(true);
    }

    /**
     * @param configurable TODO
     * @param parent TODO
     */
    public UserTemplateConfigurer(IConfigurable configurable, Container parent) {
        super(configurable, parent);
        init(true);
    }

    /**
     * @param configurable TODO
     * @param parent TODO
     * @param isTemplateEditable TODO
     */
    public UserTemplateConfigurer(IConfigurable configurable, Container parent,
            boolean isTemplateEditable) {
        super(configurable, parent);
        init(isTemplateEditable);
    }

    private void init(boolean isTemplateEditable) {
        this.isTemplateEditable = isTemplateEditable;
    }

    @Override
    protected ConfigDialog createConfigDialog() {
        if (parent instanceof Frame) {

            return new ConfigTemplateDialog(this, (Frame) parent);
        } else if (parent instanceof Dialog) {
            return new ConfigTemplateDialog(this, (Dialog) parent);
        } else {
            Util
            .Assert(false,
                    "Could not create config dialog because parent type if not supported");

        }
        return null;

    }

    /**
     * @return TODO
     */
    public boolean isTemplateEditable() {
        return isTemplateEditable;
    }

}
