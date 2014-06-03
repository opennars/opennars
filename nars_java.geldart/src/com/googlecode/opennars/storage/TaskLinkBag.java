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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.storage;

import com.googlecode.opennars.entity.TaskLink;
import com.googlecode.opennars.main.Parameters;

/**
 * TaskLinkBag contains links to tasks.
 */
public class TaskLinkBag extends Bag<TaskLink> {

    protected int capacity() {
        return Parameters.TASK_BAG_SIZE;
    }
    
    protected int forgetRate() {
        return Parameters.TASK_DEFAULT_FORGETTING_CYCLE;
    }
}

