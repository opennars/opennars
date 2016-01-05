/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX
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
package objenome.solver.evolve.event;

import objenome.solver.evolve.Organism;
import objenome.solver.evolve.OrganismOperator;

/**
 * The root class for operator events.
 */
public abstract class OperatorEvent implements Event {

	/**
	 * The <code>Operator</code> that fired the event.
	 */
	private final OrganismOperator operator;

	/**
	 * The array of individuals undergoing the operator.
	 */
	private final Organism[] parents;

	/**
	 * Constructs an <code>OperatorEvent</code>.
	 * 
	 * @param operator
	 *            the operator that fired the event.
	 * @param parents
	 *            the array of individuals undergoing the operator.
	 */
	public OperatorEvent(OrganismOperator operator, Organism[] parents) {
		this.operator = operator;
		this.parents = parents;
	}

	/**
	 * Returns the operator that fired the event.
	 * 
	 * @return the operator that fired the event.
	 */
	public OrganismOperator getOperator() {
		return operator;
	}

	/**
	 * Returns the array of individuals undergoing the operator.
	 * 
	 * @return the array of individuals undergoing the operator.
	 */
	public Organism[] getParents() {
		return parents;
	}

	/**
	 * Default event to signal the start of an operator.
	 */
	public static class StartOperator extends OperatorEvent {

		/**
		 * Constructs a <code>StartOperator</code>.
		 * 
		 * @param operator
		 *            the operator that fired the event.
		 * @param parents
		 *            the array of individuals undergoing the operator.
		 */
		public StartOperator(OrganismOperator operator, Organism[] parents) {
			super(operator, parents);
		}

	}

	/**
	 * Default event to signal the end of an operator.
	 */
	public static class EndOperator extends OperatorEvent {

		/**
		 * The array of individuals produced by the operator.
		 */
		private Organism[] children;

		/**
		 * Constructs a <code>EndOperator</code>.
		 * 
		 * @param operator
		 *            the operator that fired the event.
		 * @param parents
		 *            the array of individuals undergoing the operator.
		 */
		public EndOperator(OrganismOperator operator, Organism[] parents) {
			super(operator, parents);
		}

		/**
		 * Returns the array of individuals produced by the operator.
		 * 
		 * @return the array of individuals produced by the operator.
		 */
		public Organism[] getChildren() {
			return children;
		}

		/**
		 * Sets the array of individuals produced by the operator.
		 * 
		 * @param children
		 *            the array of individuals produced by the operator.
		 */
		public void setChildren(Organism[] children) {
			this.children = children;
		}

	}

}
