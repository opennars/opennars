/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.operator;

import org.opennars.entity.TruthValue;
import org.opennars.language.Conjunction;

/**
 * ImaginationSpace: A group of operations that when executed in a certain sequence 
 * add up to a certain declarative "picture". This "picture" might be different than the feedback of 
 * the last operation in the sequence: for instance in case of eye movements, each movement 
 * feedback corresponds to a sampling result, where each adds its part of information.
 * @author Patrick
 */
public interface ImaginationSpace {
    //
    public TruthValue AbductionOrComparisonTo(final ImaginationSpace obj, boolean comparison);
    //attaches an imagination space to the conjunction that is constructed
    //by starting with the leftmost element of the conjunction
    //and then gradually moving to the right
    public ImaginationSpace ConstructSpace(Conjunction program);
    //Has to return a new instance, not changing "this"!
    public ImaginationSpace ProgressSpace(Operation op, ImaginationSpace B);
    //Check whether the operation is part of the space:
    public boolean IsOperationInSpace(Operation oper);
}
