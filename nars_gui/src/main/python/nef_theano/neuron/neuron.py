import theano
import numpy
from theano import tensor as TT

def accumulate(input, neuron, time=1.0, init_time=0.05):
    """Take a neuron model, run it for the given amount of time with a fixed input
    Used to generate activity matrix when calculating origin decoders
    Returns the accumulated output over that time            

    :param input: theano function object describing the input
    :param Neuron neuron: population of neurons from which to accumulate data 
    :param float time: length of time to simulate population for (s)
    :param float init_time: run neurons for this long before collecting data to get rid of startup transients (s)
    """
    total = theano.shared(numpy.zeros(neuron.size).astype('float32')) # create internal state variable to keep track of number of spikes
    
    # make the standard neuron update function
    updates = neuron.update(input.astype('float32')) # updates is dictionary of variables returned by neuron.update
    tick = theano.function([], [], updates=updates) # update all internal state variables listed in updates
    
    # make a variant that also includes computing the total output
    updates[total] = total + neuron.output # add another internal variable to change to updates dictionary
    accumulate_spikes = theano.function([], [], updates=updates)#, mode=theano.Mode(optimizer=None, linker='py')) # create theano function that does it all
    
    tick.fn(n_calls = int(init_time / neuron.dt))    # call the standard one a few times to get some startup transients out of the way
    accumulate_spikes.fn(n_calls = int(time / neuron.dt))   # call the accumulator version a bunch of times

    return total.get_value().astype('float32') / time
    
class Neuron:
    def __init__(self, size, dt):
        """Constructor for neuron model superclass
        Subclasses store a set of neurons, and implement an update function
        
        :param int size: number of neurons in this population
        :param float dt: size of timestep taken during update
        """
        self.size = size
        self.dt = dt
        self.output = theano.shared(numpy.zeros(size).astype('float32')) # set up theano internal state variable

    def reset(self):
        self.output.set_value(numpy.zeros(self.size).astype('float32')) # reset internal state variable
