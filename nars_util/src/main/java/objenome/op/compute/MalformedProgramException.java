/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX: genetic programming software for research
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.op.compute;

/**
 * Checked exception that indicates that a program being evaluated, executed or
 * otherwise processed has been found to be incorrectly formed according to the
 * appropriate syntax rules.
 * 
 * @since 2.0
 */
public class MalformedProgramException extends Exception {

	private static final long serialVersionUID = -5736308741313493577L;

	/**
	 * Constructs an exception without a message.
	 */
	public MalformedProgramException() {
	}

	/**
	 * Constructs an exception with the given message.
	 * 
	 * @param message
	 *            a message describing the unexpected behaviour that occurred.
	 */
	public MalformedProgramException(String message) {
		super(message);
	}

}
