"""This is a file to test the network array function, both with make_array, 
   and by using the array_size parameter in the network.make command"""

import nef_theano as nef
import numpy as np
import matplotlib.pyplot as plt

net=nef.Network('Array Test', seed=5)
net.make_input('in', [-1,0,0,0,0,1], zero_after=1.0)
net.make_array('A', neurons=200, array_size=3, dimensions=2, neuron_type='lif')
net.make('A2', neurons=200, array_size=2, dimensions=3, neuron_type='lif')
net.make('B', 200, 6, neuron_type='lif')
net.make('B2', 200, dimensions=1, array_size=6, neuron_type='lif')

net.connect('in', 'A', pstc=0.1)
net.connect('in', 'A2', pstc=0.1)
net.connect('in', 'B', pstc=0.1)
net.connect('in', 'B2', pstc=0.1)

timesteps = 500
# setup arrays to store data gathered from sim
Fvals = np.zeros((timesteps, 6))
Avals = np.zeros((timesteps, 6))
A2vals = np.zeros((timesteps, 6))
Bvals = np.zeros((timesteps, 6))
B2vals = np.zeros((timesteps, 6))

print "starting simulation"
for i in range(timesteps):
    net.run(0.005)
    Fvals[i] = net.nodes['in'].decoded_output.get_value() 
    Avals[i] = net.nodes['A'].origin['X'].decoded_output.get_value() 
    A2vals[i] = net.nodes['A2'].origin['X'].decoded_output.get_value() 
    Bvals[i] = net.nodes['B'].origin['X'].decoded_output.get_value() 
    B2vals[i] = net.nodes['B2'].origin['X'].decoded_output.get_value() 

# plot the results
plt.ion(); plt.close(); 
plt.subplot(5,1,1)
plt.plot(Fvals,'x'); plt.title('Input')
plt.subplot(5,1,2)
plt.plot(Avals); plt.title('A, array_size=3, dim=2')
plt.subplot(5,1,3)
plt.plot(A2vals); plt.title('A2, array_size=2, dim=3')
plt.subplot(5,1,4)
plt.plot(Bvals); plt.title('B, array_size=1, dim=6')
plt.subplot(5,1,5)
plt.plot(B2vals); plt.title('B2, array_size=6, dim=1')
