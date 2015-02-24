import ensemble
import input
import simplenode

from theano import tensor as TT
import theano
import numpy
import random
import collections

class Network:
    def __init__(self, name, seed=None):
        """Wraps a Nengo network with a set of helper functions for simplifying the creation of Nengo models.

        :param string name: create and wrap a new Network with the given *name*.  
        :param int seed:    random number seed to use for creating ensembles.  This one seed is used only to
                            start the random generation process, so each neural group created will be different.        
        """
        self.name = name
        self.dt = 0.001
        self.run_time = 0.0    
        self.seed = seed        
        self.nodes = {} # all the nodes in the network, indexed by name
        self.theano_tick = None # the function call to run the theano portions of the model one timestep
        self.tick_nodes = [] # the list of nodes who have non-theano code that must be run each timestep
        self.random = random.Random()
        if seed is not None:
            self.random.seed(seed)
          
    #TODO: used for Input now, should be used for SimpleNodes when those get implemented 
    def add(self, node):
        """Add an arbitrary non-theano node to the network 
        
        :param Node node: 
        """
        self.tick_nodes.append(node)        
        self.nodes[node.name]=node

    def compute_transform(self, dim_pre, dim_post, weight=1, index_pre=None, index_post=None):
        """Helper function used by :func:`nef.Network.connect()` to create the 
        *dim_pre* by *dim_post* transform matrix. Values are either 0 or *weight*.  
        *index_pre* and *index_post* are used to determine which values are 
        non-zero, and indicate which dimensions of the pre-synaptic ensemble 
        should be routed to which dimensions of the post-synaptic ensemble.

        :param integer dim_pre: first dimension of transform matrix
        :param integer dim_post: second dimension of transform matrix
        :param float weight: the non-zero value to put into the matrix
        :param index_pre: the indexes of the pre-synaptic dimensions to use
        :type index_pre: list of integers or a single integer
        :param index_post: the indexes of the post-synaptic dimensions to use
        :type index_post: list of integers or a single integer
        :returns: a two-dimensional transform matrix performing the requested routing        
        """
        transform = [[0] * dim_pre for i in range(dim_post)] # create a matrix of zeros

        # default index_pre/post lists set up *weight* value on diagonal of transform
        # if dim_post != dim_pre, then values wrap around when edge hit
        if index_pre is None: index_pre = range(dim_pre) 
        elif isinstance(index_pre, int): index_pre = [index_pre] 
        if index_post is None: index_post = range(dim_post) 
        elif isinstance(index_post, int): index_post = [index_post]

        for i in range(max(len(index_pre), len(index_post))):
            pre = index_pre[i % len(index_pre)]
            post = index_post[i % len(index_post)]
            transform[post][pre] = weight
        return transform
        
    #TODO: encoded_weight - for and encoded weight matrix that is (pre.neurons_num x post.neurons_num)
    def connect(self, pre, post, pstc=0.01, transform=None, weight=1, index_pre=None, index_post=None, 
                        func=None, origin_name=None, decoded_weight_matrix=None):
        """Connect two nodes in the network.
        Note: cannot specify (transform) AND any of (weight, index_pre, index_post) 
              cannot specify (weight_matrix) AND any of (transform, weight, index_pre, index_post, func, origin_name)

        *pre* and *post* can be strings giving the names of the nodes, or they
        can be the nodes themselves (FunctionInputs and NEFEnsembles are
        supported). They can also be actual Origins or Terminations, or any
        combination of the above. 

        If transform is not None, it is used as the transformation matrix for
        the new termination. You can also use *weight*, *index_pre*, and *index_post*
        to define a transformation matrix instead.  *weight* gives the value,
        and *index_pre* and *index_post* identify which dimensions to connect.

        If *func* is not None, a new Origin will be created on the pre-synaptic
        ensemble that will compute the provided function. The name of this origin 
        will taken from the name of the function, or *origin_name*, if provided.  If an
        origin with that name already exists, the existing origin will be used 
        rather than creating a new one.

        :param string pre: Name of the node to connect from.
        :param string post: Name of the node to connect to.
        :param float pstc: post-synaptic time constant for the neurotransmitter/receptor on this connection
        :param transform: The linear transfom matrix to apply across the connection.
                          If *transform* is T and *pre* represents ``x``, then the connection
                          will cause *post* to represent ``Tx``.  Should be an N by M array,
                          where N is the dimensionality of *post* and M is the dimensionality of *pre*.
        :type transform: array of floats                              
        :param index_pre: The indexes of the pre-synaptic dimensions to use.
                          Ignored if *transform* is not None. See :func:`nef.Network.compute_transform()`
        :param float weight: scaling factor for a transformation defined with *index_pre* and *index_post*.
                             Ignored if *transform* is not None. See :func:`nef.Network.compute_transform()`
        :type index_pre: List of integers or a single integer
        :param index_post: The indexes of the post-synaptic dimensions to use.
                           Ignored if *transform* is not None. See :func:`nef.Network.compute_transform()`
        :type index_post: List of integers or a single integer 
        :param function func: function to be computed by this connection.  If None, computes ``f(x)=x``.
                              The function takes a single parameter x which is the current value of
                              the *pre* ensemble, and must return wither a float or an array of floats.
        :param string origin_name: Name of the origin to check for / create to compute the given function.
                                   Ignored if func is None.  If an origin with this name already
                                   exists, the existing origin is used instead of creating a new one.
        :param decoded_weight_matrix: For directly connecting the decoded output of the pre population 
                                to the neurons of the post population, should be (post.neurons_num x pre.dimensions)
        """
        # make sure contradicting things aren't simultaneously specified
        if decoded_weight_matrix is not None:
            assert (transform is None) and (weight == 1) and (index_pre is None) and (index_post is None) and (func is None) and (origin_name is None)
        else: 
            assert not (transform is not None and ((weight != 1) or (index_pre is not None) or (index_post is not None)))
        

        self.theano_tick = None  # reset timer in case the model has been run previously, as adding a new node means we have to rebuild the theano function
                        
        pre = self.nodes[pre] # get pre Node object from node dictionary
        post = self.nodes[post] # get post Node object from node dictionary
    
        if decoded_weight_matrix is None: # if we're doing a decoded connection
            # if pre is an Input object
            if isinstance(pre, input.Input) 
                assert func is None # if pre is an input Node, func must be None
                decoded_output = pre.decoded_output
                dim_pre = pre.dimensions # if Input object, just grab the number of dimensions from it
            #TODO: how to hook up the simple node to the theano business? 
            '''# if pre is a SimpleNode, then origin_name must also be specified 
            if isinstance(pre, simplenode.SimpleNode): 
                assert origin_name is not None
            ''' 
                    

            else:  # this should only be used for ensembles (TODO: maybe reorganize this if statement to check if it is an ensemble?)          
                if func is not None: 
                    if origin_name is None: origin_name = func.__name__ # if no name provided, take name of function being calculated
                    #TODO: better analysis to see if we need to build a new origin (rather than just relying on the name)
                    if origin_name not in pre.origin: # if an origin for this function hasn't already been created
                        pre.add_origin(origin_name, func) # create origin with to perform desired func
                else:                    
                    origin_name = 'X' # otherwise take default identity decoded output from pre population
                decoded_output = pre.origin[origin_name].decoded_output
                dim_pre = pre.origin[origin_name].dimensions # if ensemble, need to get pre dimensions from origin

            # compute transform matrix if not given
            if transform is None:
                transform = self.compute_transform(dim_pre=dim_pre, dim_post=post.dimensions * post.array_size, weight=weight, index_pre=index_pre, index_post=index_post)

            # apply transform matrix, directing pre dimensions to specific post dimensions
            decoded_output = TT.dot(numpy.array(transform), decoded_output)

            # pass in the pre population decoded output function to the post population, connecting them for theano
            post.add_filtered_input(pstc=pstc, decoded_input=decoded_output) 

        else: # if we're doing an encoded connection
            # can't get encoded output from Input or SimpleNode objects
            assert not (isinstance(pre, input.Input) or isinstance(pre, simplenode.SimpleNode)) 

            # get the instantaneous spike raster from the pre population
            neuron_output = pre.neurons.output 
            # the encoded input to the next population is the spikes x weight matrix
            encoded_output = TT.dot(neuron_output, numpy.array(decoded_weight_matrix[0]))
            
            # pass in the pre population encoded output function to the post population, connecting them for theano
            post.add_filtered_input(pstc=pstc, encoded_input=encoded_output)

    def make(self, name, *args, **kwargs): 
        """Create and return an ensemble of neurons. Note that all ensembles are actually arrays of length 1        
        :returns: the newly created ensemble      

        :param string name: name of the ensemble (must be unique)
        :param int seed: random number seed to use.  Will be passed to both random.seed() and ca.nengo.math.PDFTools.setSeed().
                         If this is None and the Network was constructed with a seed parameter, a seed will be randomly generated.
        """
        if 'seed' not in kwargs.keys(): # if no seed provided, get one randomly from the rng
            kwargs['seed'] = self.random.randrange(0x7fffffff)
    
        self.theano_tick=None  # just in case the model has been run previously, as adding a new node means we have to rebuild the theano function
        e = ensemble.Ensemble(*args, **kwargs) 

        self.nodes[name] = e # store created ensemble in node dictionary

    def make_array(self, name, neurons, array_size, dimensions=1, **kwargs): 
        """Generate a network array specifically, for legacy code \ non-theano API compatibility
        """
        return self.make(name=name, neurons=neurons, dimensions=dimensions, array_size=array_size, **kwargs)
    
    def make_input(self, *args, **kwargs): 
        """ # Create an input and add it to the network
        """
        self.add(input.Input(*args, **kwargs))
            
    def make_theano_tick(self):
        """Generate the theano function for running the network simulation
        :returns theano function 
        """
        updates = collections.OrderedDict() # dictionary for all variables and the theano description of how to compute them 

        for node in self.nodes.values(): # for every node in the network
            if hasattr(node, 'update'): # if there is some variable to update 
                updates.update(node.update()) # add it to the list of variables to update every time step

        theano.config.compute_test_value = 'warn' # for debugging
        return theano.function([], [], updates=updates) # create graph and return optimized update function
       
    def run(self, time):
        """Run the simulation. If called twice, the simulation will continue for *time* more seconds.  
        Note that the ensembles are simulated at the dt timestep specified when they are created.
        
        :param float time: the amount of time (in seconds) to run
        """         
        # if theano graph hasn't been calculated yet, retrieve it
        if self.theano_tick is None: self.theano_tick = self.make_theano_tick() 

        for i in range(int(time / self.dt)):
            t = self.run_time + i * self.dt # get current time step
            # run the non-theano nodes
            for node in self.tick_nodes:    
                node.t = t
                node.tick()
            # run the theano nodes
            self.theano_tick()    
           
        self.run_time += time # update run_time variable
