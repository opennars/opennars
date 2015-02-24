"""This is a test file to test the decoded_weight_matrix parameter on addTermination, 
   here we test by creating inhibitory connections.

   Need to test
        - 1) inhibitory to ensemble connection
        - 2) inhibitory to network array connection
"""

import nef_theano as nef
import numpy as np
import matplotlib.pyplot as plt

neurons = 300
dimensions = 1
array_size = 3
inhib_scale = 10

net=nef.Network('WeightMatrix Test')
net.make_input('in1', 1, zero_after=2.5)
net.make_input('in2', [1, .5, 0])
net.make('A', neurons=neurons, dimensions=dimensions, intercept=(.1, 1))
net.make('B', neurons=neurons, dimensions=dimensions) # for test 1
net.make('B2', neurons=neurons, dimensions=dimensions, array_size=array_size) # for test 2 

# setup inhibitory scaling matrix
inhib_scaling_matrix = [[0]*dimensions for i in range(dimensions)]
for i in range(dimensions):
    inhib_scaling_matrix[i][i] = -inhib_scale
# setup inhibitory matrix
inhib_matrix = []
for i in range(dimensions):
    inhib_matrix_part = [[inhib_scaling_matrix[i]] * neurons]
    inhib_matrix.append(inhib_matrix_part[0])

# define our transform and connect up! 
net.connect('in1', 'A')
net.connect('in2', 'B', index_pre=0)
net.connect('in2', 'B2')
net.connect('A', 'B', decoded_weight_matrix=inhib_matrix)
net.connect('A', 'B2', decoded_weight_matrix=inhib_matrix) 

timesteps = 500
In1vals = np.zeros((timesteps, dimensions))
In2vals = np.zeros((timesteps, array_size))
Avals = np.zeros((timesteps, dimensions))
Bvals = np.zeros((timesteps, dimensions))
B2vals = np.zeros((timesteps, dimensions * array_size))
for i in range(timesteps):
    net.run(0.01)
    In1vals[i] = net.nodes['in1'].decoded_output.get_value() 
    In2vals[i] = net.nodes['in2'].decoded_output.get_value() 
    Avals[i] = net.nodes['A'].origin['X'].decoded_output.get_value() 
    Bvals[i] = net.nodes['B'].origin['X'].decoded_output.get_value() 
    B2vals[i] = net.nodes['B2'].origin['X'].decoded_output.get_value() 

plt.ion(); plt.close(); 
plt.subplot(511); plt.title('Input1')
plt.plot(In1vals); 
plt.subplot(512); plt.title('Input2')
plt.plot(In2vals); 
plt.subplot(513); plt.title('A = In1')
plt.plot(Avals)
plt.subplot(514); plt.title('B = In2(0) inhib by A')
plt.plot(Bvals)
plt.subplot(515); plt.title('B2 = In2, network array inhib by A')
plt.plot(B2vals)
