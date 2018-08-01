package org.opennars.core;

/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
                if(a == 2) {
                    System.out.println("success");
                    break;
                }
            }
        }
        assert(true);
    }
}
