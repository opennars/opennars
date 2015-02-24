"""This is a test file to test the radius parameter of ensembles
   Need to test the radius both on identity, linear, and non-linear 
   projections. It's affects 3 places, the termination, scaling input,
   the origin, scaling output, and when computing decoders, to scale 
   the function being computed so that it has the proper shape inside
   unit length.
"""

import nef_theano as nef
import math
import numpy as np
import matplotlib.pyplot as plt

def sin3(x):
    return math.sin(x) * 3
def pow(x):
    return [xval**2 for xval in x]
def mult(x):
    return [xval*2 for xval in x]

net=nef.Network('Encoder Test')
net.make_input('in', value=sin3)
net.make('A', 1000, 1, radius=5)
net.make('B', 300, 1, radius=.5)
net.make('C', 1000, 1, radius=10)
net.make('D', 300, 1, radius=6)

net.connect('in', 'A')
net.connect('A', 'B')
net.connect('A', 'C', func=pow)
net.connect('A', 'D', func=mult)

timesteps = 500
Fvals = np.zeros((timesteps,1))
Avals = np.zeros((timesteps,1))
Bvals = np.zeros((timesteps,1))
Cvals = np.zeros((timesteps,1))
Dvals = np.zeros((timesteps,1))
print "starting simulation"
for i in range(timesteps):
    net.run(0.01)
    Fvals[i] = net.nodes['in'].decoded_output.get_value() 
    Avals[i] = net.nodes['A'].origin['X'].decoded_output.get_value() 
    Bvals[i] = net.nodes['B'].origin['X'].decoded_output.get_value()
    Cvals[i] = net.nodes['C'].origin['X'].decoded_output.get_value()
    Dvals[i] = net.nodes['D'].origin['X'].decoded_output.get_value()

plt.ion(); plt.clf(); plt.hold(1);
plt.plot(Fvals)
plt.plot(Avals)
plt.plot(Bvals)
plt.plot(Cvals)
plt.plot(Dvals)
plt.legend(['Input','A','B','C'])
