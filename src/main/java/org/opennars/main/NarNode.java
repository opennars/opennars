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
        String wat = t.toString();
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
        byte[] recBytes = new byte[1000000];
        DatagramPacket packet = new DatagramPacket(recBytes, recBytes.length);
        receiveSocket.receive(packet);
        ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(recBytes));
        Object msg = iStream.readObject();
        iStream.close();
        return msg;
    }

    /**
     * logging
     *
     */
    static void log(String message) {
        // l for log
        System.out.println("[l]: " + message);
    }


    /**
     * An example with one NarNode sending a task to another NarNode
     * 
     * @param args
     * @throws SocketException
     * @throws UnknownHostException
     * @throws IOException
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws SocketException, UnknownHostException, IOException, 
            InterruptedException, InstantiationException, InvocationTargetException, ParserConfigurationException, 
            NoSuchMethodException, SAXException, ClassNotFoundException, IllegalAccessException, ParseException {
        if((args.length-3) % 5 != 0) { //args length check, it has to be 3+5*k, with k in N0
            System.out.println("expected arguments: file cycles listenPort targetIP1 targetPort1 prioThres1 mustContainTerm1 sendInput1 ... targetIPN targetPortN prioThresN mustContainTermN sendInputN");
            System.out.println("Here, since file and cycles are not always used, they can be null too, example: null null 64001 127.0.0.1 64002 0.5 null True");
            System.exit(0);
        }
        int nar1port = Integer.parseInt(args[2]);


        log("creating Reasoner...");

        Nar nar = new Nar();

        log("creating NarNode...");

        NarNode nar1 = new NarNode(nar, nar1port);


        List<TargetNar> redirections = new ArrayList<TargetNar>();
        for(int i=3; i<args.length; i+=5) {
            Term T = args[i+3].equals("null") ? null : new Term(args[i+3]);
            redirections.add(new TargetNar(args[i], Integer.parseInt(args[i+1]), Float.parseFloat(args[i+2]), T, Boolean.parseBoolean(args[i+4])));
        }
        for(TargetNar target : redirections) {
            nar1.addRedirectionTo(target);
        }
        
        log("running Shell...");

        new Shell(nar1.nar).run(new String[]{args[0], args[1]});
    }


}
