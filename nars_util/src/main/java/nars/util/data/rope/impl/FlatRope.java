/*
 *  FlatRope.java
 *  Copyright (C) 2007 Amin Ahmad.
 *
 *  This file is part of Java Ropes.
 *
 *  Java Ropes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Java Ropes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Java Ropes.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Amin Ahmad can be contacted at amin.ahmad@gmail.com or on the web at
 *  www.ahmadsoft.org.
 */
package nars.util.data.rope.impl;

import nars.util.data.rope.Rope;

/**
 * A rope that is directly backed by a data source.
 * 
 * @author Amin Ahmad
 */
public interface FlatRope extends Rope {

	/**
	 * Returns a <code>String</code> representation of a range in this rope.
	 * 
	 * @param offset
	 *            the offset.
	 * @param length
	 *            the length.
	 * @return a <code>String</code> representation of a range in this rope.
	 */
	String toString(int offset, int length);
}
