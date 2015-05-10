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
import java.io.Serializable;

/**
 * This class defines a tuProlog operate, in terms of a name,
 * a type, and a  priority.
 *
 */
@SuppressWarnings("serial")
final public class Operator implements Serializable {
    
    /**
	 * operate name
	 */
    public final String name;
    
    /**
	 * type(xf,yf,fx,fy,xfx,xfy,yfy,yfx
	 */
    public final String type;
    
    /**
	 * priority
	 */
    public final int prio;
    
/*Castagna 06/2011*/public/**/ Operator(String name_,String type_,int prio_) {
        name = name_;
        type = type_;
        prio = prio_;
    }

    //hashcode and equality consistent with String name
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Operator) {
            return ((Operator)obj).name.equals(name);
        }
        else if (obj instanceof String) {
            return obj.equals(name);
        }
        return false;
    }

}