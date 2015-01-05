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
package nars.prolog;

/**
 * This exceptions means that a not valid tuProlog theory has been specified
 *
 * @see Theory
 *
 */
@SuppressWarnings("serial")
public class InvalidTheoryException extends PrologException {
    
    public int line = -1;
    public int pos = -1;
    /*Castagna 06/2011*/	
	public int clause = -1;
	/**/
    
    public InvalidTheoryException() {}
    
    public InvalidTheoryException(int line, int pos) {
        this.line = line;
        this.pos = pos;
    }

    public InvalidTheoryException(String message) {
        super(message);
    }
    
    /*Castagna 06/2011*/
	public InvalidTheoryException(String message, int clause, int line, int pos)
	{
		super(message);
		this.clause = clause;
		this.line = line;
		this.pos = pos;
	}
	/**/

        
        
}