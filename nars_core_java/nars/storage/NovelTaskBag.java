/*
 * NovelTaskBag.java
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

import nars.entity.Task;
import nars.main_nogui.Parameters;

/**
 * New tasks that contain new Term.
 */
public class NovelTaskBag extends Bag<Task> {

    /** Constructor
     * @param memory The reference of memory
     */
    public NovelTaskBag(Memory memory) {
        super(memory);
    }
    /**
     * Get the (constant) capacity of NovelTaskBag
     * @return The capacity of NovelTaskBag
     */
    protected int capacity() {
        return Parameters.TASK_BUFFER_SIZE;
    }

    /**
     * Get the (constant) forget rate in NovelTaskBag
     * @return The forget rate in NovelTaskBag
     */
    protected int forgetRate() {
        return Parameters.NEW_TASK_FORGETTING_CYCLE;
    }
}

