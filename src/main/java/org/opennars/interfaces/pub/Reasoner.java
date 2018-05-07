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
package org.opennars.interfaces.pub;

import org.opennars.entity.Concept;
import org.opennars.interfaces.*;
import org.opennars.io.Narsese;
import org.opennars.io.events.AnswerHandler;

/**
 * Implementations implement a full Non-axiomatic-reasoner
 */
public interface Reasoner extends
    HasSensoryChannels,
    Resetable,
    StringNarsese,
    TaskConsumer,
    Eventable,
    HasPlugins,
    Multistepable
{
    Reasoner ask(final String termString, final AnswerHandler answered) throws Narsese.InvalidInputException;
    Reasoner askNow(final String termString, final AnswerHandler answered) throws Narsese.InvalidInputException;

    Concept concept(final String concept) throws Narsese.InvalidInputException;
    void run();

    /**
     * return the current time from the clock
     *
     * @return The current time
     */
    long time();

    boolean isRunning();
    long getMinCyclePeriodMS();
    void setThreadYield(final boolean b);
}
