/*
 * Created on Nov 5, 2003
 * 
 * Copyright (C)aliCE team at deis.unibo.it
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
 *
 */
package prolog;

import nars.tuprolog.Library;
import nars.tuprolog.Term;

import java.util.HashMap;

public class HashLibrary extends Library {
	private static final long serialVersionUID = 1L;

	private HashMap<String, Term> dict;
		
	public boolean hashtable_0(){
		dict = new HashMap<>();
		return true;
	} 
	
	public boolean put_data_2(Term key, Term object){
		dict.put(key.toString(),object);
		return true;
	}
	
	public boolean get_data_2(Term key, Term res){
		Term result = dict.get(key.toString());
		return unify(res,result);
	}
	
	public boolean remove_data_1(Term key){
		dict.remove(key.toString());
		return true;
	}
}
