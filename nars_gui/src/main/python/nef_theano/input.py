from theano import tensor as TT
from numbers import Number
import theano
import numpy

class Input:
    def __init__(self, name, value, zero_after=None):
        """A function input object

        :param string name: name of the function input
        :param value: defines the output decoded_output
        :type value: float or function
        :param float zero_after: time after which to set function output = 0 (s)
        """
        self.name = name
        self.t = 0
        self.function = None
        self.zero_after = zero_after
        self.zeroed = False
        
        if callable(value): # if value parameter is a python function
            self.function = value
            value = self.function(0.0) # initial output value = function value with input 0.0
        if isinstance(value, Number): value = [value] # if scalar, make it a list
        self.decoded_output = theano.shared(numpy.float32(value)) # theano internal state defining output value
    
        # find number of parameters of the projected value
        self.dimensions = len(value)

    def reset(self):
        """Resets the function output state values
        """
        self.zeroed = False

    def tick(self):
        """Move function input forward in time
        """
        if self.zeroed: return

        if self.zero_after is not None and self.t > self.zero_after: # zero output
            self.decoded_output.set_value(numpy.zeros_like(self.decoded_output.get_value()))
            self.zeroed=True

        if self.function is not None: # update output decoded_output
            value = self.function(self.t)
            # if value is a scalar output, make it a list
            if isinstance(value, Number): value = [value] 
            # cast as float32 for consistency / speed, but _after_ it's been made a list
            self.decoded_output.set_value(numpy.float32(value)) 
