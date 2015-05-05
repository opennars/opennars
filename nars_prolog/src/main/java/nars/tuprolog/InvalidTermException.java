/*
 * tuProlog - Copyright (C) 2001-2007 aliCE team at deis.unibo.it
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

/**
 * This exception means that a method has been passed an argument
 * containing an invalid Prolog term.
 */
public class InvalidTermException extends IllegalArgumentException {

    private static final long serialVersionUID = -4416801118548866803L;
    
    /*Castagna 06/2011*/
	public int line = -1;
	public int pos = -1;
	/**/	

    public InvalidTermException(String message) {
        super(message);
    }
    
    /*Castagna 06/2011*/
	public InvalidTermException(String message, int line, int pos) {
		super(message);
		this.line = line;
		this.pos = pos;
	}
	/**/
}
