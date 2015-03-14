/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "FnAdvanced.java". Description:
""

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

import ca.nengo.config.PropretiesUtil;
import ca.nengo.config.ui.NewConfigurableDialog;
import ca.nengo.math.Function;

import java.awt.*;

/**
 * TODO
 * 
 * @author TODO
 */
public class FnAdvanced implements ConfigurableFunction {
    private final Class<? extends Function> type;
    private Function myFunction;

    /**
     * @param type TODO
     */
    public FnAdvanced(Class<? extends Function> type) {
        super();
        this.type = type;
    }

    public Function configureFunction(Dialog parent) {
        if (myFunction == null) {
            myFunction = (Function) NewConfigurableDialog.showDialog(parent, type, type);
        } else {
            PropretiesUtil.configure(parent, myFunction);
        }
        return myFunction;
    }

    public Class<? extends Function> getFunctionType() {
        return type;
    }

    public void setFunction(Function function) {
        if (function != null) {
            if (type.isInstance(function)) {
                myFunction = function;
            }
        } else {
            myFunction = null;
        }
    }

    @Override
    public String toString() {
        return '~' + type.getSimpleName();
    }

    public Function getFunction() {
        return myFunction;
    }
}
