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
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package org.opennars.util.test;

import java.util.LinkedList;
import java.util.List;
import org.opennars.main.NAR;

/**
 *
 * @author me
 */
public class OutputEmptyCondition extends OutputCondition {
    List<String> output = new LinkedList();

    public OutputEmptyCondition(NAR nar) {
        super(nar);
        succeeded = true;
    }

    public String getFalseReason() {
        return "FAIL: output exists but should not: " + output;
    }

    @Override
    public boolean condition(Class channel, Object signal) {
        //any OUT or ERR output is a failure
        if ((channel == OUT.class) || (channel == ERR.class)) {
            output.add(channel.getSimpleName().toString() + ": " + signal.toString());
            succeeded = false;
            return false;
        }
        return false;
    }
    
}
