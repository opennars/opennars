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

import org.opennars.entity.Task;
import org.opennars.interfaces.Timable;
import org.opennars.language.Term;
import org.opennars.main.MiscFlags;
import org.opennars.storage.Memory;

import java.util.List;

/**
 *  A class used as a template for Operator definition.
 */
public class NullOperator extends Operator {

    
    public NullOperator() {
        this("^sample");
    }
    
    public NullOperator(final String name) {
        super(name);
    }

    /** called from Operator */
    @Override 
    protected List<Task> execute(final Operation operation, final Term[] args, final Memory memory, final Timable time) {
        if (MiscFlags.DEBUG) {
            memory.emit(getClass(), args);
        }
        return null;
    }

}

