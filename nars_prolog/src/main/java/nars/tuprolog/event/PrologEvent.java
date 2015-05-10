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
package nars.tuprolog.event;

import nars.tuprolog.Prolog;

/**
 * Base class for engine event  
 *
 * @since 1.3
 *
 */
@SuppressWarnings("serial")
public abstract class PrologEvent extends java.util.EventObject {

    private final long when;
    
    public PrologEvent(Prolog source){
        super(source);
        when = System.currentTimeMillis();
    }
    
    /**
     * Gets the VM time of  event occurrence.
     * @return time in millis
     */
    public long getTime(){
        return when;
    }
}
