"""This is a test file to test the func parameter on the connect method"""

import nef_theano as nef
import numpy as np
import matplotlib.pyplot as plt
import math

#TODO: this doesn't actually work, all output is the same
net=nef.Network('Function Test')
net.make_input('in', value=math.sin)
net.make('A', neurons=500, dimensions=1)
net.make('B', neurons=500, dimensions=3)

# function example for testing
def square(x):
    return [-x[0]*x[0], -x[0], x[0]]

net.connect('in', 'A')
net.connect('A', 'B', func=square, pstc=0.1)

timesteps = 500
# setup arrays to store data gathered from sim
Fvals = np.zeros((timesteps, 1))
Avals = np.zeros((timesteps, 1))
Bvals = np.zeros((timesteps, 3))

print "starting simulation"
for i in range(timesteps):
    net.run(0.01)
    Fvals[i] = net.nodes['in'].decoded_output.get_value() 
    Avals[i] = net.nodes['A'].origin['X'].decoded_output.get_value() 
    Bvals[i] = net.nodes['B'].accumulators[0.1].decoded_input.get_value()
    #Bvals[i] = net.nodes['B'].origin['X'].decoded_output.get_value() 

# plot the results
plt.ion(); plt.clf(); plt.hold(1);
plt.plot(Fvals)
plt.plot(Avals)
plt.plot(Bvals)
plt.legend(['Input','A','B0','B1','B2'])
