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
import nars.tuprolog.SolveInfo;

/**
 * This class represents events concerning query management.
 * 
 * @since 1.3
 *
 */
@SuppressWarnings("serial")
public class QueryEvent extends PrologEvent {

   private final SolveInfo info;
    
    public QueryEvent(Prolog source, SolveInfo info){
        super(source);
        this.info=info;
    }
    
    /**
     * Gets information about the query result.
     * 
     * @return solve info
     */
    public SolveInfo getSolveInfo(){
        return info;
    }
}
