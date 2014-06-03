/*
 * TaskLinkBag.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */

package nars.storage;

import nars.entity.TaskLink;
import nars.main_nogui.Parameters;

/**
 * TaskLinkBag contains links to tasks.
 */
public class TaskLinkBag extends Bag<TaskLink> {

    /** Constructor
     * @param memory The reference of memory
     */
    public TaskLinkBag (Memory memory) {
        super(memory);
    }

    /**
     * Get the (constant) capacity of TaskLinkBag
     * @return The capacity of TaskLinkBag
     */
    protected int capacity() {
        return Parameters.TASK_LINK_BAG_SIZE;
    }
    
    /**
     * Get the (adjustable) forget rate of TaskLinkBag
     * @return The forget rate of TaskLinkBag
     */
    protected int forgetRate() {
//        return memory.getMainWindow().forgetTW.value();
        return memory.getTaskForgettingRate().get();
    }
}

