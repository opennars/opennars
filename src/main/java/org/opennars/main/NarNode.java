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
package org.opennars.main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.opennars.entity.Task;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.main.Shell;
import org.xml.sax.SAXException;

/**
 * @author Patrick Hammer
 */
public class NarNode implements EventObserver  {
    
    /* An extra event for received tasks*/
    public class EventReceivedTask {}
    
    /* The socket the Nar listens from */
    private transient DatagramSocket receiveSocket;
    
    /* Listen port however is not transient and can be used to recover the deserialized instance */
    private int listenPort;
    
    public Nar nar;
    
    /***
     * Create a Nar node that listens for received tasks from other NarNode instances
     * 
     * @param listenPort
     * @throws SocketException
     * @throws UnknownHostException 
     */
    public NarNode(int listenPort) throws SocketException, UnknownHostException, IOException, InstantiationException, 
            InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, 
            ClassNotFoundException, ParseException {
        this(new Nar(),listenPort);
    }
    public NarNode(Nar nar, int listenPort) throws SocketException, UnknownHostException, IOException, InstantiationException, 
            InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, 
            ClassNotFoundException, ParseException {
        super();
        this.nar = nar;
        this.listenPort = listenPort;
        this.receiveSocket = new DatagramSocket(listenPort, InetAddress.getByName("127.0.0.1"));
        nar.event(this, true, Events.TaskAdd.class);
        NarNode THIS = this;
        new Thread() {
            public void run() {
                for(;;) {
                    try {
                        Object ret = THIS.receiveObject();
                        if(ret != null) {
                            if(ret instanceof Task) {
                                nar.memory.event.emit(EventReceivedTask.class, new Object[]{ret});
                                nar.addInput((Task) ret, nar);
                            } else
                            if(ret instanceof String) { //emits IN.class anyway
                                nar.addInput((String) ret);
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(NarNode.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(NarNode.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.start();
    }

    /**
     * Input and derived tasks will be potentially sent
     * 
     * @param event
     * @param args 
     */
    @Override
    public void event(Class event, Object[] args) {
        if(event == Events.TaskAdd.class) {
            Task t = (Task) args[0];
            try {
                sendTask(t);
            } catch (Exception ex) {
                Logger.getLogger(NarNode.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Send tasks that are above priority threshold and contain the optional mustContainTerm
     * 
     * @param t
     * @throws IOException 
     */
    private void sendTask(Task t) throws IOException {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(bStream); 
        oo.writeObject(t);
        oo.close();
        byte[] serializedMessage = bStream.toByteArray();
        for(TargetNar target : targets) {
            if(t.getPriority() > target.threshold) {
                Term term = t.getTerm();
                boolean isCompound = (term instanceof CompoundTerm);
                boolean searchTerm = target.mustContainTerm != null;
                boolean atomicEqualsSearched =     searchTerm && !isCompound && target.mustContainTerm.equals(term);
                boolean compoundContainsSearched = searchTerm &&  isCompound && ((CompoundTerm) term).containsTermRecursively(target.mustContainTerm);
                if(!searchTerm || atomicEqualsSearched || compoundContainsSearched) {
                    DatagramPacket packet = new DatagramPacket(serializedMessage, serializedMessage.length, target.targetAddress, target.targetPort);
                    target.sendSocket.send(packet);
                    //System.out.println("task sent:" + t);
                }
            }
        }
    }
    
    /**
     * Send Narsese that contains the optional mustContainTerm
     *
     * @param input
     * @param target
     * @throws IOException 
     */
    public static void sendNarsese(String input, TargetNar target) throws IOException {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(bStream); 
        oo.writeObject(input);
        oo.close();
        byte[] serializedMessage = bStream.toByteArray();
        boolean searchTerm = target.mustContainTerm != null;
        boolean containsFound = searchTerm && input.contains(target.mustContainTerm.toString());
        if(!searchTerm || containsFound) {
            DatagramPacket packet = new DatagramPacket(serializedMessage, serializedMessage.length, target.targetAddress, target.targetPort);
            target.sendSocket.send(packet);
            //System.out.println("narsese sent:" + input);
        }
    }
    public static void sendNarsese(String input, final String targetIP, final int targetPort, final float taskThreshold, Term mustContainTerm) throws IOException {
        sendNarsese(input, new TargetNar(targetIP, targetPort, taskThreshold, mustContainTerm, true));
    }

    public static class TargetNar {
        
        /**
         * The target Nar node, specifying under which conditions the current Nar node redirects tasks to it.
         * 
         * @param targetIP
         * @param targetPort
         * @param threshold
         * @param mustContainTerm
         * @throws SocketException
         * @throws UnknownHostException 
         */
        public TargetNar(final String targetIP, final int targetPort, final float threshold, Term mustContainTerm, boolean sendInput) throws SocketException, UnknownHostException {
            this.targetAddress = InetAddress.getByName(targetIP);
            this.sendSocket = new DatagramSocket();
            this.threshold = threshold;
            this.targetPort = targetPort;
            this.mustContainTerm = mustContainTerm;
            this.sendInput = sendInput;
        }
        final float threshold;
        final DatagramSocket sendSocket;
        final int targetPort;
        final InetAddress targetAddress;
        final Term mustContainTerm;
        final boolean sendInput;
    }
    
    private List<TargetNar> targets = new ArrayList<>();
    /**
     * Add another target Nar node to redirect tasks to, and under which conditions.
     * 
     * @param targetIP The target Nar node IP
     * @param targetPort The target Nar node port
     * @param taskThreshold The threshold the priority of the task has to have to redirect
     * @param mustContainTerm The optional term that needs to be contained recursively in the task term
     * @throws SocketException
     * @throws UnknownHostException 
     */
    public void addRedirectionTo(final String targetIP, final int targetPort, final float taskThreshold, Term mustContainTerm, boolean sendInput) throws SocketException, UnknownHostException {
        addRedirectionTo(new TargetNar(targetIP, targetPort, taskThreshold, mustContainTerm, sendInput));
    }
    public void addRedirectionTo(TargetNar target) throws SocketException, UnknownHostException {
        targets.add(target);
    }
 
    /***
     * NarNode's receiving a task
     * 
     * @return
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private Object receiveObject() throws IOException, ClassNotFoundException {
        byte[] recBytes = new byte[65535];
        DatagramPacket packet = new DatagramPacket(recBytes, recBytes.length);
        receiveSocket.receive(packet);
        ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(recBytes));
        Object msg = iStream.readObject();
        iStream.close();
        return msg;
    }
}
