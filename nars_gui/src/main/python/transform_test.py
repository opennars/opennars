"""This is a test file to test the transform parameter on the connect function.
   The transform matrix is post x pre dimensions"""

import nef_theano as nef
import math
import numpy as np
import matplotlib.pyplot as plt

def func(x): 
    return [math.sin(x), -math.sin(x)]

net=nef.Network('Transform Test')
net.make_input('in', value=func)
net.make('A', 300, 3)

# define our transform and connect up! 
transform = [[0,1],[1,0],[1,-1]]
net.connect('in', 'A', transform=transform)

timesteps = 500
Fvals = np.zeros((timesteps,2))
Avals = np.zeros((timesteps,3))
for i in range(timesteps):
    net.run(0.01)
    Fvals[i] = net.nodes['in'].decoded_output.get_value() 
    Avals[i] = net.nodes['A'].origin['X'].decoded_output.get_value() 

plt.ion(); plt.clf(); 
plt.subplot(411); plt.title('Input')
plt.plot(Fvals); plt.legend(['In(0)','In(1)'])
plt.subplot(412); plt.title('A(0) = In(1)')
plt.plot(Avals[:,0])
plt.subplot(413); plt.title('A(1) = In(0)')
plt.plot(Avals[:,1])
plt.subplot(414); plt.title('A(2) = In(0) - In(1)')
plt.plot(Avals[:,2])
