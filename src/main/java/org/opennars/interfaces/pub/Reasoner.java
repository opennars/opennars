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
    SensoryChannelConsumer,
    Resettable,
    NarseseConsumer,
    InputFileConsumer,
    TaskConsumer<Reasoner>,
    Eventable,
    Pluggable,
    Multistepable,
    Timable
{
    /**
     * ask reasoner a eternal question
     * @param termString narsese string with term of question
     * @param answered handler which will be called when answered
     * @return reasoner which processes the question
     * @throws Narsese.InvalidInputException
     */
    Reasoner ask(final String termString, final AnswerHandler answered) throws Narsese.InvalidInputException;

    /**
     * ask reasoner a now question
     * @param termString narsese string with term of question
     * @param answered handler which will be called when answered
     * @return reasoner which processes the question
     * @throws Narsese.InvalidInputException
     */
    Reasoner askNow(final String termString, final AnswerHandler answered) throws Narsese.InvalidInputException;

    /**
     * returns the concept by name/term or creates it if it doesn't exist
     * @param concept the name/term of the concept
     * @return queried or created concept
     * @throws Narsese.InvalidInputException
     */
    Concept concept(final String concept) throws Narsese.InvalidInputException;

    /**
     * Main loop executed by the Thread.  Should not be called directly.
     */
    void run();

    /**
     * return the current time from the clock
     *
     * @return The current time
     */
    long time();

    /**
     * is the reasoner running?
     * @return is it running
     */
    boolean isRunning();

    /**
     * returns the minimum delay of a cycle in milliseconds
     * @return minimum cycle delay period
     */
    long getMinCyclePeriodMS();

    /**
     * When b is true, Nar will call Thread.yield each run() iteration that minCyclePeriodMS==0 (no delay).
     *  This is for improving program responsiveness when Nar is run with no delay.
     * @param b Nar will call Thread.yield each run()
     */
    void setThreadYield(final boolean b);
}
