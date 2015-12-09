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

package nars.task.flow;

import nars.NAR;
import nars.task.Task;
import nars.util.data.buffer.Source;
import nars.util.event.On;

import java.util.function.Consumer;

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


    //void input(NAR n, int numPerFrame);


    default void input(NAR n, int numPerFrame) {
        if (numPerFrame == 0)
            throw new RuntimeException("0 rate");
        /*if (reg!=null)
            throw new RuntimeException("already inputting");*/

        On[] reg = {null};
        Consumer<NAR> inputNext = nn -> {
            int count = 0;
            Task next = null;
            while ((count < numPerFrame) && ((next = get()) != null)) {
                nn.input(next);
                count++;
            }
            if (next == null) {
                reg[0].off();
                reg[0] = null;
            }
        };

        reg[0] = n.memory.eventFrameStart.on(inputNext);

        inputNext.accept(n);//first input
    }
}
