"""This is a test file to test the weight, index_pre, and index_post parameters 
   on the connect function. 
"""

import nef_theano as nef
import math
import numpy as np
import matplotlib.pyplot as plt

net=nef.Network('Weight, Index_Pre, and Index_Post Test')
net.make_input('in', value=math.sin)
net.make('A', 300, 1)
net.make('B', 300, 1)
net.make('C', 400, 2)
net.make('D', 800, 3)
net.make('E', 400, 2)
net.make('F', 400, 2)

net.connect('in', 'A', weight=.5)
net.connect('A', 'B', weight=2)
net.connect('A', 'C', index_post=1)
net.connect('A', 'D')
net.connect('C', 'E', index_pre=1)
net.connect('C', 'F', index_pre=1, index_post=0)

timesteps = 500
Invals = np.zeros((timesteps, 1))
Avals = np.zeros((timesteps, 1))
Bvals = np.zeros((timesteps, 1))
Cvals = np.zeros((timesteps, 2))
Dvals = np.zeros((timesteps, 3))
Evals = np.zeros((timesteps, 2))
Fvals = np.zeros((timesteps, 2))
print "starting simulation"
for i in range(timesteps):
    net.run(0.01)
    Invals[i] = net.nodes['in'].decoded_output.get_value() 
    Avals[i] = net.nodes['A'].origin['X'].decoded_output.get_value() 
    Bvals[i] = net.nodes['B'].origin['X'].decoded_output.get_value()
    Cvals[i] = net.nodes['C'].origin['X'].decoded_output.get_value()
    Dvals[i] = net.nodes['D'].origin['X'].decoded_output.get_value()
    Evals[i] = net.nodes['E'].origin['X'].decoded_output.get_value()
    Fvals[i] = net.nodes['F'].origin['X'].decoded_output.get_value()

plt.ion(); plt.close(); 
plt.subplot(711); plt.title('Input')
plt.plot(Invals)
plt.subplot(712); plt.title('A = Input * .5')
plt.plot(Avals)
plt.subplot(713); plt.title('B = A * 2')
plt.plot(Bvals)
plt.subplot(714); plt.title('C(0) = 0, C(1) = A')
plt.plot(Cvals)
plt.subplot(715); plt.title('D(0:2) = A')
plt.plot(Dvals)
plt.subplot(716); plt.title('E(0:1) = C(1)')
plt.plot(Evals)
plt.subplot(717); plt.title('F(0) = C(1)')
plt.plot(Fvals)
