/*
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

package nars.io.in;

import nars.task.Task;
import nars.util.data.buffer.Source;

/**
 *
 * Provides a stream of input tasks
 *
 */
public interface Input extends Source<Task> {

    @Override
    Task get();

    //    default int inputNext(Memory m, int max) {
//        Task t;
//
//        int i =0;
//        while (( t = get() ) != null) {
//            if ((i += m.input(t)) == max) {
//                return i;
//            }
//        }
//        return i;
//    }


}
