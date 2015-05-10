/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog;

import nars.nal.term.Term;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class represents a prolog Flag
 */
@SuppressWarnings("serial")
class Flag implements java.io.Serializable {
    
    private String name;
    private Struct valueList;
    private PTerm value;
    private PTerm defaultValue;
    private boolean modifiable;
    private String  libraryName;
    
    /**
     * Builds a Prolog flag
     *
     * @param name is the name of the flag
     * @param valueSet is the Prolog list of the possible values
     * @param defValue is the default value
     * @param modifiable states if the flag is modifiable
     * @param library is the library defining the flag
     */
    public Flag(String name, Struct valueSet, PTerm defValue, boolean modifiable, String library) {
        this.name = name;
        this.valueList = valueSet;
        defaultValue = defValue;
        this.modifiable = modifiable;
        libraryName = library;
        value = defValue;
    }
    
    protected Flag() {}
    
    
    /**
     * Gets a deep copy of the flag
     *
     * @return a copy of the flag
     */
    public Object clone() {
        Flag f = new Flag();
        f.name=name;
        f.valueList=(Struct)valueList.copy(new HashMap<>(),Var.ORIGINAL);
        f.value=PTerm.copyp(value);
        f.defaultValue=PTerm.copyp(defaultValue);
        f.modifiable=modifiable;
        f.libraryName=libraryName;
        return f;
    }


    
    /**
     * Checks if a value is valid according to flag description
     *
     * @param value the possible value of the flag
     * @return flag validity
     */
    public boolean isValidValue(PTerm value) {
        java.util.Iterator<? extends Term> it=valueList.listIterator();

        ArrayList<Var> v1 = new ArrayList(), v2 = new ArrayList();
        long now = System.currentTimeMillis();

        while (it.hasNext()) {
            Term t= it.next();
            if (value.match(t, now, v1, v2)) {
                return true;
            }
        }
        return false;
    }
    
    /**
	 * Gets the name of the flag
	 * @return  the name
	 */
    public String getName() {
        return name;
    }
    
    /**
	 * Gets the list of flag possible values
	 * @return  a Prolog list
	 */
    public Struct getValueList() {
        return valueList;
    }
    
    /**
     * Sets the value of a flag
     *
     * @param value new value of the flag
     * @return true if the value is valid
     */
    public boolean setValue(PTerm value) {
        if (modifiable && isValidValue(value)) {
            this.value = value;
            return true;
        } else {
            return false;
        }
    }
    
    /**
	 * Gets the current value of the flag
	 * @return  flag current value
	 */
    public PTerm getValue() {
        return value;
    }
    
    /**
	 * Checks if the value is modifiable
	 * @return
	 */
    public boolean isModifiable() {
        return modifiable;
    }
    
    /**
	 * Gets the name of the library where the flag has been defined
	 * @return  the library name
	 */
    public String getLibraryName() {
        return libraryName;
    }
    
}