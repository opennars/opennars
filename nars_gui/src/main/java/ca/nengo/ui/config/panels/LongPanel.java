/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "LongPanel.java". Description:
"Input panel for entering Longs

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

package ca.nengo.ui.config.panels;

import ca.nengo.ui.config.descriptors.PLong;

/**
 * Input panel for entering Longs
 * 
 * @author Shu Wu
 */
public class LongPanel extends PropertyTextPanel {

	private static final int COLUMNS = 10;

    public LongPanel(PLong property) {
        super(property, COLUMNS);
    }

    @Override
    public PLong getDescriptor() {
        return (PLong) super.getDescriptor();
    }

    @Override
    public Long getValue() {
        Long longValue = new Long(getText());
        return longValue;
    }

    @Override
    protected TextError checkValue(String textValue) {
        if (textValue == null || textValue.compareTo("") == 0) {
            return TextError.ValueNotSet;
        }

        try {
            Long value = getValue();

            if (getDescriptor().isCheckRange()) {
                if (value > getDescriptor().getMax()
                        || value < getDescriptor().getMin()) {
                    return TextError.InvalidFormat;
                }
            }

        } catch (NumberFormatException e) {
        	return TextError.InvalidFormat;
        }

        return TextError.NoError;
    }

}