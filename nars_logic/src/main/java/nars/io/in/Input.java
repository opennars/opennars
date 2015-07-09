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

import nars.Memory;
import nars.nal.nal8.ImmediateOperation;
import nars.task.Task;
import nars.util.data.buffer.Source;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 *
 * Provides a stream of input tasks
 *
 */
public interface Input extends Source<Task> {

    @Override
    abstract public Task get();

    /**
     *
     * called when a NAR forcibly removes the inputs, allowing
     * this input to close any connections or free resources
     */
    default public void stop() {

    }

    /** supplies consumer with tasks until get() returns null */
    default int inputAll(Memory m) {
        Task t;

        int i =0;
        while (( t = get() ) != null) {
            i += m.input(t);
        }
        return i;
    }
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


    /** an input that generates tasks in batches, which are stored in a buffer */
    abstract public static class BufferedInput implements Input , Consumer<Task> {

        final Deque<Task> queue = new ArrayDeque();

        protected int accept(ImmediateOperation o) {
            accept(o.newTask());
            return 1;
        }

        protected int accept(Iterator<Task> tasks) {
            if (tasks == null) return 0;
            int count = 0;
            while (tasks.hasNext()) {
                queue.add(tasks.next());
                count++;
            }
            return count;
        }

        @Override
        public void accept(Task task) {
            queue.add(task);
        }

        @Override
        public Task get() {
            if (!queue.isEmpty()) {
                return queue.removeFirst();
            }
            return null;
        }

        @Override
        public void stop() {
            queue.clear();
        }
    }
}
