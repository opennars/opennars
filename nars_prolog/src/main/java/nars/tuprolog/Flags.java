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

/**
 * Administrator of flags declared
 * 
 * @author Alex Benini
 */
public class Flags {

    /* flag list */
    private final ArrayList<Flag> flags;

    /**
	 * mediator owner of the manager
	 */
    protected final Prolog mediator;

    Flags(Prolog vm) {
        mediator = vm;
        flags = new ArrayList<>();
    }


    /**
     * Defines a new flag
     */
    public synchronized boolean defineFlag(String name, Struct valueList, PTerm defValue,
            boolean modifiable, String libName) {
        flags.add(new Flag(name, valueList, defValue, modifiable, libName));
        return true;
    }

    public synchronized boolean setFlag(String name, PTerm value) {
        java.util.Iterator<Flag> it = flags.iterator();
        while (it.hasNext()) {
            Flag flag = it.next();
            if (flag.getName().equals(name)) {
                if (flag.isModifiable() && flag.isValidValue(value)) {
                    flag.setValue(value);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public synchronized Struct getPrologFlagList() {
        Struct flist = new Struct();
        java.util.Iterator<Flag> it = flags.iterator();
        while (it.hasNext()) {
            Flag fl = it.next();
            flist = new Struct(new Struct("flag", new Struct(fl.getName()), fl
                    .getValue()), flist);
        }
        return flist;
    }

    public synchronized PTerm getFlag(String name) {
        java.util.Iterator<Flag> it = flags.iterator();
        while (it.hasNext()) {
            Flag fl = it.next();
            if (fl.getName().equals(name)) {
                return fl.getValue();
            }
        }
        return null;
    }

    // restituisce true se esiste un flag di nome name, e tale flag ?
    // modificabile
    public boolean isModifiable(String name) {
        java.util.Iterator<Flag> it = flags.iterator();
        while (it.hasNext()) {
            Flag flag = it.next();
            if (flag.getName().equals(name)) {
                return flag.isModifiable();
            }
        }
        return false;
    }

    // restituisce true se esiste un flag di nome name, e Value ? un valore
    // ammissibile per tale flag
    public boolean isValidValue(String name, PTerm value) {
        java.util.Iterator<Flag> it = flags.iterator();
        while (it.hasNext()) {
            Flag flag = it.next();
            if (flag.getName().equals(name)) {
                return flag.isValidValue(value);
            }
        }
        return false;
    }

}
