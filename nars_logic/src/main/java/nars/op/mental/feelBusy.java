/*
 * Copyright (C) 2014 peiwang
 *
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

package nars.op.mental;

import nars.$;
import nars.task.Task;
import nars.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Sense of busy-ness
 */
public class feelBusy extends feel {

    public static final Term business = $.the(feelBusy.class.getSimpleName());

    final static Logger logger = LoggerFactory.getLogger(feelBusy.class);

    /**
     * To get the current value of an internal sensor
     */
    @Override
    public List<Task> apply(Task operation) {
        float busy = nar.memory.emotion.busy();
        logger.info("busy=" + busy + ", terms=" + nar.memory.index.size());
        return feeling(busy, nar.memory, business);
    }

}
