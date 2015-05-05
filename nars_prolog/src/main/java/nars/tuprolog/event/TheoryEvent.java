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
import nars.tuprolog.Theory;

/**
 * This class represents events occurring in theory management. 
 *
 * @since 1.3 
 *
 */
@SuppressWarnings("serial")
public class TheoryEvent extends PrologEvent {

    private Theory oldTh;
    private Theory newTh;
    
    public TheoryEvent(Prolog source, Theory oldth,Theory newth){
        super(source);
            oldTh=oldth;
            newTh=newth;
        }
        
        /**
         * Gets the old theory
         * 
         * @return the old theory
         */
        public Theory getOldTheory(){
            return oldTh;
        }

        /**
         * Gets the new theory
         * 
         * @return the new theory
         */
        public Theory getNewTheory(){
            return newTh;
        }
}
