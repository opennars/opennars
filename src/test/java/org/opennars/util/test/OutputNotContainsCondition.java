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

import org.opennars.main.Nar;

/**
 *
 * @author me
 */
public class OutputNotContainsCondition extends OutputContainsCondition {

    public OutputNotContainsCondition(final Nar nar, final String containing) {
        super(nar, containing, -1);
        succeeded = true;
    }

    @Override
    public String getFalseReason() {
        return "incorrect output: " + containing;
    }

    @Override
    public boolean condition(final Class channel, final Object signal) {
        if (!succeeded) {
            return false;
        }
        if (cond(channel, signal)) {
            onFailure(channel, signal);
            succeeded = false;
            return false;
        }
        return true;
    }

    public boolean isInverse() {
        return true;
    }

    protected void onFailure(final Class channel, final Object signal) {
    }
    
}
