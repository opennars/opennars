/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.storage;

import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.interfaces.Timable;
import org.opennars.language.Term;
import org.opennars.main.Parameters;
import org.opennars.interfaces.Timable;
import org.opennars.main.Nar;
import java.util.ArrayList; 

public class Buffer extends Bag<Task<Term>,Sentence<Term>> {
    
    Nar nar;
    Parameters narParameters;
    long max_duration;
    static int duration = 100; //buffer duration, TODO make a param
    
    public Buffer(Nar nar, int levels, int capacity, Parameters narParameters) {
        super(levels, capacity, narParameters);
        this.nar = nar;
        this.narParameters = narParameters;
    }
    
    @Override
    public boolean expired(long creationTime) {
        long currentTime = nar.time();
        long delta = currentTime - creationTime;
        long maxDuration = narParameters.DURATION * narParameters.MAX_BUFFER_DURATION_FACTOR;
        return delta > maxDuration;
    }
    
    public void clearExpiredItem(){

        ArrayList<Sentence> expiredKey = new ArrayList<Sentence>();

        nameTable.forEach((key, task) -> {
            if((nar.time() - task.sentence.stamp.getPutInTime()) > duration){
                expiredKey.add(key);
            }
        });

        for (int i = 0; i < expiredKey.size(); i++) {
            Task task = nameTable.get(expiredKey.get(i));
            super.pickOut(task.sentence); //task.getKey()
        }

    }
    
    public Task putIn(Task task){
        task.sentence.stamp.setPutInTime(nar.time());
        return (Task) super.putIn(task);
    } 
    
    @Override
    public Task takeOut(){
        clearExpiredItem();
        return super.takeOut();
    }
    
    //TODO move event bag inference code over here (sequences, implications, equivalences)
}
