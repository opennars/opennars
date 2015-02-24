import inspect
import math
import theano
from numbers import Number

class SimpleNode(Node,Probeable):
    """A SimpleNode allows you to put arbitary code as part of a Nengo model.
        
    This object has Origins and Terminations which can be used just like
    any other Nengo component. Arbitrary code can be run every time step,
    making this useful for simulating sensory systems (reading data
    from a file or a webcam, for example), motor systems (writing data to
    a file or driving a robot, for example), or even parts of the brain
    that we don't want a full neural model for (symbolic reasoning or
    declarative memory, for example).
    
    You can have as many origins you like.  The dimensionality
    of the origins are set by the length of the returned vector of floats.  
    
      class SquaringFiveValues(nef.SimpleNode):
          def init(self):
              self.value=0
          def origin_output(self):
              return [self.value]
    
    There is also a special method called tick() that is called once per
    time step.

      class HelloNode(nef.SimpleNode):
          def tick(self):
              print 'Hello world'
              
    The current time can be accessed via ``self.t``.  This value will be the
    time for the beginning of the current time step.  The end of the current
    time step is ``self.t_end``::
    """

    def __init__(self, name):
        """
        :param string name: the name of the created node          
        :param float pstc: the default time constant on the filtered inputs
        :param int dimensions: the number of dimensions of the decoded input signal
        """
        self.name = name
        self.origins = {}
        self.decoded_outputs = {}
        self.dimensions = {} 

        self.init() # initialize internal variables if there are any

        # look at all the defined methods, if any start with 'origin_', make 
        # origins that implement the defined function
        for name, method in inspect.getmembers(self, inspect.ismethod):
            if name.startswith('origin_'):
                self.origins[name[7:]] = method # store method reference

                value = method(0.0) # initial output value = function value with input 0.0
                if isinstance(value, Number): value = [value] # if scalar, make it a list
                self.decoded_outputs[name[7:]] = theano.shared(numpy.float32(value)) # theano internal state defining output value

                # find number of parameters of the projected value
                self.origin_dimensions[name[7:]] = len(value)

    def tick(self):
        """An extra utility function that is called every time step.
        
        Override this to create custom behaviour that isn't necessarily tied
        to a particular input or output.  Often used to write spike data to a file
        or produce some other sort of custom effect.
        """
        pass

    def init(self):
        """Initialize the node.
        
        Override this to initialize any internal variables.  This will
        also be called whenever the simulation is reset
        """        
        pass

    def reset(self):
        """Reset the state of all the filtered inputs and internal variables"""
        self.init()    

    def run(self):
        """Run the simple node

        :param float start: The time to start running 
        :param float end: The time to stop running
        """
        self.tick()    

        for origin in self.origins.keys():
            value = self.origins[origin](self.t)
            # if value is a scalar output, make it a list
            if isinstance(value, Number): value = [value] 
            # cast as float32 for consistency / speed, but _after_ it's been made a list
            self.decoded_outputs[origin].set_value(numpy.float32(value)) 
