/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PassthroughNode.java". Description:
"A Node that passes values through unaltered.

  This can be useful if an input to a Network is actually routed to multiple destinations,
  but you want to handle this connectivity within the Network rather than expose multiple
  terminations"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

/*
 * Created on 24-May-07
 */
package ca.nengo.model.impl;

import ca.nengo.model.*;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.util.MU;
import ca.nengo.util.ScriptGenException;
import ca.nengo.util.VisiblyChanges;
import ca.nengo.util.VisiblyChangesUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * <p>A Node that sends and receives data through sockets via a UDP connection.</p>
 *
 * <p>This can be useful if an input to a Network is actually routed to multiple destinations,
 * but you want to handle this connectivity within the Network rather than expose multiple
 * terminations.</p>
 *
 * @author Bryan Tripp
 */
public class SocketUDPNode implements Node<Node>, Resettable {

	//implementation note: this class doesn't nicely extend AbstractNode

	private static final Logger ourLogger = LogManager.getLogger(SocketUDPNode.class);

	/**
	 * Default name for a termination
	 */
	public static final String TERMINATION = "termination";


	/**
	 * Default name for an origin
	 */
	public static final String ORIGIN = "origin";

	private static final long serialVersionUID = 1L;

	private String myName;
	private int myDimension;
	private Map<String, ObjectTarget<InstantaneousOutput>> myTerminations;
	private BasicSource myOrigin;
	private String myDocumentation;
	private transient ArrayList<VisiblyChanges.Listener> myListeners;

	private int myLocalPort;
	private int myGivenLocalPort;
	private InetAddress myDestAddress;
	private int myDestPort;
	private DatagramSocket mySocket;
	private int mySocketTimeout;
	private boolean myIgnoreTimestamp;
	private PriorityQueue<float[]> mySocketBuffer;
	private NengoUDPPacketComparator myComparator;
	private boolean myIsReceiver;
	private boolean myIsSender;
	private ByteOrder myByteOrder = ByteOrder.BIG_ENDIAN;
	private float myUpdateInterval = 0;
	private float myNextUpdate = 0;

	/**
	 * Constructor for a SocketUDPNode that sends and receives data.
	 *
	 * @param name Node name
	 * @param dimension Dimension of data passing through
	 * @param localPort Port number on the local machine to bind to. Set to 0 to bind to first available port.
	 * @param destAddress Destination address to connect to
	 * @param destPort Destination port to connect to
	 * @param socketTimeout Timeout on socket in milliseconds (socket blocks until timeout expires)
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public SocketUDPNode(String name, int dimension, int recvDimension, int localPort, String destAddress, int destPort,
		                 int socketTimeout, boolean ignoreTimestamp) throws UnknownHostException {
		myName = name;
		myDimension = dimension;
		myTerminations = new HashMap<String, ObjectTarget<InstantaneousOutput>>(10);

		myLocalPort = localPort;
		myGivenLocalPort = localPort; // Stored 
		myDestAddress = InetAddress.getByName(destAddress);
		myDestPort = destPort;
		mySocketTimeout = socketTimeout;
		myIgnoreTimestamp = ignoreTimestamp;
		myComparator = new NengoUDPPacketComparator();
		mySocketBuffer = new PriorityQueue<float[]>(10, myComparator);
		mySocket = null;
		myNextUpdate = 0;
		
		myIsSender = false;
		myIsReceiver = false;
		myOrigin = null;
		
		if (myDestPort > 0)
			myIsSender = true;
		if (myLocalPort > 0) {
			myIsReceiver = true;
			myOrigin = new BasicSource(this, ORIGIN, recvDimension, Units.UNK);
		}

		reset(false);
	}

	/**
	 * Constructor for a SocketUDPNode that acts just like a passthrough node (not receiving or sending
	 * anything over sockets)
	 *
	 * @param name Node name
	 * @param dimension Dimension of data passing through
	 */
	// public SocketUDPNode(String name, int dimension)
	// 		throws UnknownHostException, SocketException {
	// 	this(name, dimension, -1, "", -1, 0, false);
	// }

	/**
	 * Constructor for a SocketUDPNode that only receives data.
	 *
	 * @param name Node name
	 * @param dimension Dimension of data passing through
	 * @param localPort Port number on the local machine to bind to. Set to 0 to bind to first available port.
	 * @param socketTimeout Timeout on socket in milliseconds (socket blocks until timeout expires)
	 */
	public SocketUDPNode(String name, int dimension, int recvDimension, int localPort, int socketTimeout)
			throws UnknownHostException {
		this(name, dimension, recvDimension, localPort, "", -1, Math.max(socketTimeout, 1), false);
	}

	/**
	 * Constructor for a SocketUDPNode that only receives data, with an option to ignore the timestamp on
	 * incoming packets.
	 *
	 * @param name Node name
	 * @param dimension Dimension of data passing through
	 * @param localPort Port number on the local machine to bind to. Set to 0 to bind to first available port.
	 * @param ignoreTimestamp Set to true to ignore timestamps on incoming packets.
	 * @param socketTimeout Timeout on socket in milliseconds (socket blocks until timeout expires)
	 */
	public SocketUDPNode(String name, int dimension, int recvDimension, int localPort, int socketTimeout,
		                 boolean ignoreTimestamp) throws UnknownHostException {
		this(name, dimension, recvDimension, localPort, "", -1, Math.max(socketTimeout, 1), ignoreTimestamp);
	}

	/**
	 * Constructor for a SocketUDPNode that only sends data.
	 *
	 * @param name Node name
	 * @param dimension Dimension of data passing through
	 * @param destAddress Destination address to connect to
	 * @param destPort Destination port to connect to
	 */
	public SocketUDPNode(String name, int dimension, String destAddress, int destPort)
			throws UnknownHostException {
		this(name, dimension, 0, -1, destAddress, destPort, -1, false);
	}

	public void initialize() throws SimulationException{
		if (mySocket != null)
			// Socket has already been initialized, don't try to reinitialize it.
			return;
		try{
			if (myLocalPort > 0)
				// Create a socket if localPort > 0 (i.e. we want to receive data from somewhere)
				// or if destPort > 0 (i.e. we want to send data to somewhere - we still need a socket to send stuff)
				mySocket = new DatagramSocket(myLocalPort);
			else {
				// If localPort is not defined (defaults to -1), then create socket on first available port
				// and set localPort to the port the socket has connected to.
				mySocket = new DatagramSocket();
				myLocalPort = mySocket.getLocalPort();
			}
			if (mySocketTimeout > 0)
				mySocket.setSoTimeout(mySocketTimeout);
		}
		catch( Exception e ) {
			throw new SimulationException(e);
		}
	}

	/**
	 * @see ca.nengo.model.Node#name()
	 */
	public String name() {
		return myName;
	}

	/**
	 * @param name The new name
	 */
	public void setName(String name) throws StructuralException {
		VisiblyChangesUtils.nameChanged(this, name(), name, myListeners);
		myName = name;
	}

	public int getDimension(){
		return myDimension;
	}

	public void setDimension(int dimension) {
		myDimension = dimension;
	}

	public int getLocalPort(){
		return myLocalPort;
	}

	public InetAddress getDestInetAddress(){
		return myDestAddress;
	}

	public int getDestPort(){
		return myDestPort;
	}

	public int getSocketTimeout(){
		return mySocketTimeout;
	}

	public boolean getIgnoreTimestamp(){
		return myIgnoreTimestamp;
	}

	public boolean isSender(){
		return myIsSender;
	}

	public boolean isReceiver(){
		return myIsReceiver;
	}
	
	public void setByteOrder(ByteOrder byteOrder){
		myByteOrder = byteOrder;
	}
	
	public void setByteOrder(String byteOrder){
		if (byteOrder.toLowerCase() == "little")
			myByteOrder = ByteOrder.LITTLE_ENDIAN;
		else if (byteOrder.toLowerCase() == "big")
			myByteOrder = ByteOrder.BIG_ENDIAN;
	}
	
	public void setUpdateInterval(float interval){
		myUpdateInterval = interval;
	}

	/**
	 * @see ca.nengo.model.Node#getSource(java.lang.String)
	 */
	public NSource getSource(String name) throws StructuralException {
		if (myOrigin != null && myOrigin.getName().equals(name)) {
			return myOrigin;
		} else {
			throw new StructuralException("Unknown origin: " + name);
		}
	}

	/**
	 * @see ca.nengo.model.Node#getSources()
	 */
	public NSource[] getSources() {
		if (myOrigin != null)
			return new NSource[]{myOrigin};
		else
			return new NSource[0];
	}

	public NTarget addTermination(String name, float[][] transform)
			throws StructuralException {
		for (NTarget t : getTargets()) {
			if (t.getName().equals(name))
				throw new StructuralException("This node already contains a termination named " + name);
		}

		ObjectTarget result = new ObjectTarget(this, name, transform);
		myTerminations.put(name, result);
		return result;
	}

	/**
	 * @see ca.nengo.model.Node#getTarget(java.lang.String)
	 */
	public NTarget getTarget(String name) throws StructuralException {
		if (myTerminations.containsKey(name)) {
			return myTerminations.get(name);
		} else {
			throw new StructuralException("Unknown termination: " + name);
		}
	}

	/**
	 * @see ca.nengo.model.Node#getTargets()
	 */
	public NTarget[] getTargets() {
        Collection<ObjectTarget<InstantaneousOutput>> var = myTerminations.values();
        return var.toArray(new ObjectTarget[var.size()]);
	}

	/**
	 * @see ca.nengo.model.Node#run(float, float)
	 */
	public void run(float startTime, float endTime) throws SimulationException {
		// TODO: Thread this thing, so that it doesn't block if the blocking receive calls
		//       are called before the send calls. (waiting for a receive before a send causes 
		//       a deadlock situation.
		if (isSender() && (startTime + myUpdateInterval / 2.0) >= myNextUpdate) {
			if (mySocket == null)
				// If for some rule the socket hasn't been initialized, then initialize it.
				initialize();
			if (myTerminations.isEmpty())
				throw new SimulationException("SocketUDPNode is sender, but has no terminations to get data from.");
			else {
				// TODO: Test with spiking outputs?
				float[] values = new float[myDimension];
				Iterator<ObjectTarget<InstantaneousOutput>> it = myTerminations.values().iterator();
				while (it.hasNext()) {
					ObjectTarget<InstantaneousOutput> termination = it.next();
					InstantaneousOutput io = termination.get();
					if (io instanceof RealSource) {
						values = MU.sum(values, ((RealSource) io).getValues());
					} else if (io instanceof SpikeOutput) {
						boolean[] spikes = ((SpikeOutput) io).getValues();
						for (int i = 0; i < spikes.length; i++) {
							if (spikes[i]) {
	                            values[i] += 1f/(endTime - startTime);
	                        }
						}
					} else if (io == null) {
						throw new SimulationException("Null input to Termination " + termination.getName());
					} else {
						throw new SimulationException("Output type unknown: " + io.getClass().getName());
					}
				}
				// Send values over the socket.
				// Datagram format:
				// - bytes 1-4: Timestamp (float)
				// - bytes 4-(myDim+1)*4: values[i] (float)
				ByteBuffer buffer = ByteBuffer.allocate((myDimension + 1) * 4);
				buffer.order(myByteOrder);
				buffer.putFloat((float)((startTime + endTime + myUpdateInterval) / 2.0));
				for(int i = 0; i < myDimension; i++)
					buffer.putFloat(values[i]);
				byte[] bufArray = buffer.array();
				DatagramPacket packet = new DatagramPacket(bufArray, bufArray.length, myDestAddress, myDestPort);
				try {
					mySocket.send(packet);
				}
				catch (IOException e) {
					// TODO: Handle this better
					throw new SimulationException(e);
				}
			}
		}
		if (isReceiver()) {
			float[] values = new float[myOrigin.getDimensions()];
			float[] tempValues = new float[myOrigin.getDimensions()+1];

			// Check items in priority queue to see if there is anything that is good to go (i.e. timestamp is within 
			// startTime and endTime.
			boolean foundItem = false;
			boolean foundFutureItem = false;
			int i = 0;
			while (!mySocketBuffer.isEmpty()) {
				tempValues = mySocketBuffer.peek();
				foundItem = (tempValues[0] >= startTime && tempValues[0] <= endTime) ||  myIgnoreTimestamp;
				foundFutureItem = tempValues[0] > endTime;
				if (foundItem)
					mySocketBuffer.remove();
				else
					break;
			}
			if (foundItem) {
				// If an item was found in the queue (i.e. message with future timestamp was received before), use this
				// data instead of waiting on socket for new data.
				for (i = 0; i < myOrigin.getDimensions(); i++)
					values[i] = tempValues[i+1];
			}
			else if (foundFutureItem || startTime < myNextUpdate) {
				// Buffer contained item in the future, so skip waiting for a new packet to arrive, and hurry 
				// the heck up.
				values = ((RealOutputImpl) myOrigin.get()).getValues().clone();
			}
			else {
				// If no items were found in queue, wait on socket for new data.
				try {
					byte[] bufArray = new byte[(myOrigin.getDimensions() + 1) * 4];
					DatagramPacket packet = new DatagramPacket(bufArray, bufArray.length);
					
					while (true) {
						mySocket.receive(packet);
						
						ByteBuffer buffer = ByteBuffer.wrap(bufArray);
						buffer.order(myByteOrder);
						for (i = 0; i < myOrigin.getDimensions() + 1; i++) {
							tempValues[i] = buffer.getFloat();
						}
						
						// Check for timestamp for validity (i.e. within the start and end of this run call).
						if ((tempValues[0] >= startTime && tempValues[0] <= endTime) || myIgnoreTimestamp || tempValues[0] < 0) {
							// Valid timestamp encountered; or have been instructed to ignore timestamps. 
							// No further actions required, just break out of while loop.
							System.arraycopy(tempValues, 1, values, 0, myOrigin.getDimensions());
							break;
						}
						else if (tempValues[0] > endTime) {
							// Future timestamp encountered, place into priority queue, use previous origin value as
							// current value, and then out of while loop.
							// Note: we break out of the while loop because receiving future timestamps means this 
							//       system is (potentially) running slow.
							mySocketBuffer.add(tempValues.clone());
							values = ((RealOutputImpl) myOrigin.get()).getValues().clone();
							break;
						}
						// Past timestamp encountered. Just ignore it, and wait for another packet.
					}
				}
				catch (SocketTimeoutException e){
					// If a timeout occurs, don't really do anything, just keep the origin at the previous value.
					values = ((RealOutputImpl) myOrigin.get()).getValues().clone();
				}
				catch (Exception e){
					// TODO: Handle this better
					throw new SimulationException(e);
				}
			}
			myOrigin.accept(new RealOutputImpl(values, Units.UNK, endTime));
		}
		if (startTime >= myNextUpdate)
			myNextUpdate += myUpdateInterval;
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		float time = 0;
		myNextUpdate = 0;
		if (myOrigin != null) {
			//try {
				if (myOrigin.get() != null) {
	                myOrigin.get().getTime();
	            }
			/*} catch (SimulationException e) {
				ourLogger.warn("Exception getting time from existing output during reset", e);
			}*/
			myOrigin.accept(new RealOutputImpl(new float[myOrigin.getDimensions()], Units.UNK, time));
			myOrigin.reset(randomize);
		}
		mySocketBuffer.clear();
	}

	/**
	 * @see ca.nengo.model.SimulationMode.ModeConfigurable#getMode()
	 */
	public SimulationMode getMode() {
		return SimulationMode.DEFAULT;
	}

	/**
	 * Does nothing (only DEFAULT mode is supported).
	 *
	 * @see ca.nengo.model.SimulationMode.ModeConfigurable#setMode(ca.nengo.model.SimulationMode)
	 */
	public void setMode(SimulationMode mode) {
	}

	/**
	 * @see ca.nengo.model.Node#getDocumentation()
	 */
	public String getDocumentation() {
		return myDocumentation;
	}

	/**
	 * @see ca.nengo.model.Node#setDocumentation(java.lang.String)
	 */
	public void setDocumentation(String text) {
		myDocumentation = text;
	}

	/**
	 * @see ca.nengo.util.VisiblyChanges#addChangeListener(ca.nengo.util.VisiblyChanges.Listener)
	 */
	public void addChangeListener(Listener listener) {
		if (myListeners == null) {
			myListeners = new ArrayList<Listener>(2);
		}
		myListeners.add(listener);
	}

	/**
	 * @see ca.nengo.util.VisiblyChanges#removeChangeListener(ca.nengo.util.VisiblyChanges.Listener)
	 */
	public void removeChangeListener(Listener listener) {
		myListeners.remove(listener);
	}

	@Override
	public Node clone() throws CloneNotSupportedException {
		if (mySocket != null) {
			// Cannot clone a SocketUDPNode (because you cannot bind to a socket already bound to)
			throw new CloneNotSupportedException("SocketUDPNode can only be cloned if it is not already bound to a socket.");
		}
		else {
			try {
				SocketUDPNode result = (SocketUDPNode) super.clone();
				
				if (myOrigin != null)
					result.myOrigin = myOrigin.clone(result);
				result.myTerminations = new HashMap(10);
				for (ObjectTarget oldTerm : myTerminations.values()) {
					ObjectTarget newTerm = oldTerm.clone(result);
					result.myTerminations.put(newTerm.getName(), newTerm);
				}
	
				result.myListeners = new ArrayList<Listener>(2);
				
				// Note: Cloning a SocketUDPNode is weird, because it copies all of the pre-existing socket values
				//       like destination address, port, etc.
				result.myComparator = new NengoUDPPacketComparator();
				result.myDestAddress = InetAddress.getByName(myDestAddress.getHostAddress());
				result.myDestPort = myDestPort;
				result.myDimension = myDimension;
				result.myDocumentation = myDocumentation;
				result.myGivenLocalPort = myGivenLocalPort;
				result.myIgnoreTimestamp = myIgnoreTimestamp;
				result.myLocalPort = myLocalPort;
				result.mySocketBuffer = new PriorityQueue<float[]>(10, myComparator);
				result.mySocketTimeout = mySocketTimeout;
	
				return result;
			}
			catch (UnknownHostException e){
				throw new CloneNotSupportedException("SocketUDPNode clone error: " + e.getMessage());			
			}
		}
	}


	public Node[] getChildren() {
		return new Node[0];
	}

	public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
		return "";
	}

	public void close(){
		// Close the socket when object is finalized.
		if (mySocket != null){
			mySocket.close();
			mySocket = null;
		}
		// Restore myLocalPort value to the value originally provided by the user. 
		// myLocalPort value is overwritten during initialize() function call, and this is so that
		// when the socket node is re-initialized after closure, it uses the same settings.
		myLocalPort = myGivenLocalPort;
		reset(false);
	}

    public void releaseMemory() {
	}	
	
	private static class NengoUDPPacketComparator implements Comparator<float[]> {
		public int compare(float[] o1, float[] o2) {
			return o1[0] < o2[0] ? -1 : o1[0] == o2[0] ? 0 : 1; 
		}
	}
}
