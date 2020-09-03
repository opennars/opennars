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
package org.opennars.plugin.perception;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.interfaces.Timable;
import org.opennars.interfaces.pub.Reasoner;
import org.opennars.io.Narsese;
import org.opennars.io.Symbols;
import org.opennars.io.Texts;
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.Events;
import org.opennars.io.events.Events.CycleEnd;
import org.opennars.io.events.Events.ResetEnd;
import org.opennars.language.*;
import org.opennars.main.Nar;
import org.opennars.operator.Operator;
import org.opennars.io.Narsese;
import org.opennars.io.Parser;
import org.opennars.entity.Task;
import org.opennars.interfaces.Timable;

/**
 *
 * @author Patrick
 */

public class NarseseChannel extends SensoryChannel  {
 
    Nar nar;
    public NarseseChannel(Nar nar){
        super(nar);
        this.nar = nar;
    }
 
    @Override
    public Nar addInput(final Task t, final Timable time){ return nar; } //this channel can't receive re-routed tasks

    Task task = null;
    public void putIn(Nar nar, String text) throws Parser.InvalidInputException
    {
     final Parser narsese = new Narsese(nar);
     this.task = narsese.parseTask(text);
    }

    public Task takeOut() //todo use channel method
    {
     return this.task;
    }

}
