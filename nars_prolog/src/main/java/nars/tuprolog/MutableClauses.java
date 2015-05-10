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

import java.util.HashMap;
import java.util.Iterator;

/**
 * Customized HashMap for storing clauses in the TheoryManager
 *
 * @author ivar.orstavik@hist.no
 *
 * Reviewed by Paolo Contessi
 */
@SuppressWarnings("serial")
public class MutableClauses extends HashMap<String,ClauseIndex> implements Clauses {


	@Override
	public void addFirst(String key, Clause d) {
		ClauseIndex family = get(key);
		if (family == null)
			put(key, family = new ClauseIndex());
		family.addFirst(d);
	}

	@Override
	public void addLast(String key, Clause d) {
		ClauseIndex family = get(key);
		if (family == null)
			put(key, family = new ClauseIndex());
		family.addLast(d);
	}

	@Override
	public Iterator<Clause> getPredicates(final PTerm headt) {
		ClauseIndex family = get(((Struct) headt).getPredicateIndicator());
		if (family == null){
			return null;
		}
		return family.get(headt);
	}

	@Override
	public Iterator<Clause> getPredicates(final String key){
		ClauseIndex family = get(key);
		if(family == null){
			return null;
		}
		return family.iterator();
	}

        @Override
	public Iterator<Clause> iterator() {
		return new CompleteIterator(this);
	}

	public static class CompleteIterator implements Iterator<Clause> {
		final Iterator<ClauseIndex> values;
		Iterator<Clause> workingList;
		//private boolean busy = false;

		public CompleteIterator(final MutableClauses clauseDatabase) {
			values = clauseDatabase.values().iterator();
		}

		@Override
		public boolean hasNext() {
			while (true) {
				if (workingList != null && workingList.hasNext())
					return true;
				if (values.hasNext()) {
					workingList = values.next().iterator();
					continue;
				}
				return false;
			}
		}

		@Override
		public synchronized Clause next() {
			if (workingList.hasNext())
				return workingList.next();
			else return null;
		}

		@Override
		public void remove() {
			workingList.remove();
		}
	}

	@Override
	public boolean containsKey(String key) {
		return super.containsKey(key);
	}

	@Override
	public ClauseIndex get(String ctxID) {
		return super.get(ctxID);
	}

	@Override
	public ClauseIndex remove(String ctxID) {
		return super.remove(ctxID);
	}

	@Override
	public ClauseIndex put(String key, ClauseIndex value) {
		return super.put(key, value);
	}
}