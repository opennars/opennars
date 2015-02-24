"""This test file is for checking the eval_points parameter added to the ensemble and origin constructors.
   An ensemble can be created with a set of default eval_points for every origin to use, or an origin can 
   be called with a specific set of eval_points to use for optimization. 
   
   This tests:
        -1) creating origin w/ eval_points
        -2) creating ensemble w/ eval_points
        -3) creating ensemble w/ eval_points, creating origin w/ eval_points
"""

import nef_theano as nef; reload(nef)
import math
import numpy as np
import matplotlib.pyplot as plt

# create the list of evaluation points
eval_points = np.arange(-1, 0, .5)

net=nef.Network('EvalPoints Test')
net.make_input('in', value=math.sin)

net.make('A1', neurons=300, dimensions=1) # for test 1
net.make('A2', neurons=300, dimensions=1, eval_points=eval_points) # for test 2
net.make('A3', neurons=300, dimensions=1, eval_points=eval_points) # for test 3

net.make('B', neurons=100, dimensions=1)
net.make('C', neurons=100, dimensions=1)
net.make('D', neurons=100, dimensions=1)

# function for testing evaluation points
def pow(x):
    return [xval**2 for xval in x]

# create origins with eval_points
net.nodes['A1'].add_origin('pow', func=pow, eval_points=eval_points) # for test 1
net.nodes['A3'].add_origin('pow', func=pow, eval_points=eval_points) # for test 3

net.connect('in', 'A1')
net.connect('in', 'A2')
net.connect('in', 'A3')
net.connect('A1', 'B', origin_name='pow') # for test 1
net.connect('A2', 'C', func=pow) # for test 2
net.connect('A3', 'D', origin_name='pow') # for test 3

timesteps = 500
# setup arrays to store data gathered from sim
Fvals = np.zeros((timesteps,1))
A1vals = np.zeros((timesteps,1))
A2vals = np.zeros((timesteps,1))
A3vals = np.zeros((timesteps,1))

print "starting simulation"
for i in range(timesteps):
    net.run(0.01)
    Fvals[i] = net.nodes['in'].decoded_output.get_value() 
    A1vals[i] = net.nodes['A1'].origin['pow'].decoded_output.get_value() 
    A2vals[i] = net.nodes['A2'].origin['pow'].decoded_output.get_value() 
    A3vals[i] = net.nodes['A3'].origin['pow'].decoded_output.get_value() 

# plot the results
plt.ion(); plt.clf(); 
plt.subplot(411); plt.title('Input')
plt.plot(Fvals)
plt.subplot(412); plt.title('A1')
plt.plot(A1vals)
plt.subplot(413); plt.title('A2')
plt.plot(A2vals)
plt.subplot(414); plt.title('A3')
plt.plot(A3vals)
