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
package org.opennars.io.events;

import org.opennars.main.Nar;

/**
 */
public abstract class EventHandler implements EventEmitter.EventObserver {
    protected final EventEmitter source;
    protected boolean active = false;
    private final Class[] events;

    public EventHandler(final Nar n, final boolean active, final Class... events) {
        this(n.memory.event, active, events);
    }
    
    public EventHandler(final EventEmitter source, final boolean active, final Class... events) {
        this.source = source;
        this.events = events;
        setActive(active);
    }

    public void setActive(final boolean b) {
        if (this.active == b) return;
        
        this.active = b;
        source.set(this, b, events);
    }

    public boolean isActive() {
        return active;
    }
}
