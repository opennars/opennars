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
package org.opennars.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.opennars.entity.Task;
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.OutputHandler.IN;
import org.opennars.main.NarNode;
import org.opennars.main.NarNode.TargetNar;
import org.xml.sax.SAXException;

/**
 * Test for NarNode functionality
 */
public class NarNodeTest {
    static Integer a = 0;
    @Test
    public void testNarToNar() throws UnknownHostException, IOException, SocketException, InstantiationException, InvocationTargetException, 
            NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException, InterruptedException {
        int nar1port = 64001;
        int nar2port = 64002;
        String localIP = "127.0.0.1";
        NarNode nar1 = new NarNode(nar1port);
        NarNode nar2 = new NarNode(nar2port);
        TargetNar nar2_connection = new TargetNar(localIP, nar2port, 0.5f, null, true);
        nar1.addRedirectionTo(nar2_connection);
        nar2.nar.event(new EventEmitter.EventObserver() {
            @Override
            public void event(Class event, Object[] args) {
                if(event == NarNode.EventReceivedTask.class || event == IN.class) {
                    Task task = (Task) args[0];
                    System.out.println("received task event triggered in nar2: " + task);
                    synchronized(a) {
                        a++;
                    }
                }
            }
        }, true, NarNode.EventReceivedTask.class, IN.class);
        System.out.println("High priority task occurred in nar1");
        NarNode.sendNarsese("<{task1} --> [great]>.", nar2_connection);
        nar1.nar.addInput("<{task1} --> [great]>.");
        while(true) {
            synchronized(a) {
                if(a >= 2) {
                    System.out.println("success");
                    break;
                }
            }
        }
        assert(true);
        nar1.nar.stop();
        nar2.nar.stop();
    }
}
