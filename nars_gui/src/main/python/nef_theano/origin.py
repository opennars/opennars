from theano import tensor as TT
from theano.tensor.shared_randomstreams import RandomStreams
import theano
import numpy
import neuron
import collections
import numpy as np

def make_samples(num_samples, dimensions, srng):
    """Generate sample points uniformly distributed within the sphere
    Returns float array of sample points
    
    :param int num_samples: number of num_samples to generate samples for
    :param int dimensions: dimensionality of sphere to generate points in
    :param theano.tensor.shared_randomstreams srng: theano random number generator
    """
    samples = srng.normal((num_samples, dimensions)) # get samples from normal distribution
    # normalize magnitude of sampled points to be of unit length
    norm = TT.sum(samples * samples, axis=[1], keepdims=True) 
    samples = samples / TT.sqrt(norm)

    # generate magnitudes for vectors from uniform distribution
    scale = srng.uniform([num_samples])**(1.0 / dimensions)
    samples = samples.T * scale # scale sample points
    
    return theano.function([],samples)()

class Origin:
    def __init__(self, ensemble, func=None, eval_points=None):
        """The output to a population of neurons (ensemble), performing a transformation (func) on the represented value

        :param Ensemble ensemble: the Ensemble to which this origin is attached
        :param function func: the transformation to perform to the ensemble's represented values to get the output value
        """
        self.ensemble = ensemble
        self.func = func
        self.decoder = self.compute_decoder(eval_points)
        self.dimensions = self.decoder.shape[1]*self.ensemble.array_size
        self.decoded_output = theano.shared(numpy.zeros(self.dimensions).astype('float32'))
    
    def compute_decoder(self, eval_points=None):     
        """Calculate the scaling values to apply to the output to each of the neurons in the attached 
        population such that the weighted summation of their output generates the desired decoded output.
        Decoder values computed as D = (A'A)^-1 A'X_f where A is the matrix of activity values of each 
        neuron over sampled X values, and X_f is the vector of desired f(x) values across sampled points
        
        :param list eval_points: specific set of points to optimize decoders over 
        """
        
        #TODO: have num_samples be more for higher dimensions?  5000 maximum (like Nengo)?
        num_samples=500

        if eval_points == None:  
            # generate sample points from state space randomly to minimize decoder error over in decoder calculation
            srng = RandomStreams(seed=self.ensemble.seed) # theano random number generator
            eval_points = make_samples(num_samples, self.ensemble.dimensions, srng) 
        else: # otherwise reset num_samples, andhow  make sure eval_points is in the right form (rows are input dimensions, columns different samples)
            eval_points = np.array(eval_points)
            if len(eval_points.shape) == 1: eval_points.shape = [1, eval_points.shape[0]]
            num_samples = eval_points.shape[1]

        # compute the target_values at the sampled points (which are the same as the sample points for the 'X' origin)      ?????????? what does this ( ) part mean?
        if self.func is None: # if no function provided, use identity function as default
            target_values = eval_points 
        else: # otherwise calculate target_values using provided function
            # scale all our sample points by ensemble radius, calculate function value, then scale back to unit length
            # this ensures that we accurately capture the shape of the function when the radius is > 1 (think for example func=x**2)
            target_values = numpy.array([self.func(s * self.ensemble.radius) for s in eval_points.T]) / self.ensemble.radius 
            if len(target_values.shape) < 2: target_values.shape = target_values.shape[0], 1
            target_values = target_values.T
        
        # compute the input current for every neuron and every sample point
        J = numpy.dot(self.ensemble.encoders, eval_points)
        J += numpy.array([self.ensemble.bias]).T
        
        # duplicate attached population of neurons into array of ensembles, one ensemble per sample point
        # so in parallel we can calculate the activity of all of the neurons at each sample point 
        neurons = self.ensemble.neurons.__class__((self.ensemble.neurons_num, num_samples), tau_rc=self.ensemble.neurons.tau_rc, tau_ref=self.ensemble.neurons.tau_ref)
        
        # run the neuron model for 1 second, accumulating spikes to get a spike rate
        #  TODO: is this long enough?  Should it be less?  If we do less, we may get a good noise approximation!
        A = neuron.accumulate(J, neurons)
        
        # compute Gamma and Upsilon
        G = numpy.dot(A, A.T)
        U = numpy.dot(A, target_values.T)
        
        #TODO: optimize this so we're not doing the full eigenvalue decomposition
        #TODO: add NxS method for large N?
        
        #TODO: compare below with pinv rcond
        w, v = numpy.linalg.eigh(G) # eigh for symmetric matrices, returns evalues w, and normalized evectors v
        limit = .01 * max(w) # formerly 0.1 * 0.1 * max(w), set threshold 
        for i in range(len(w)):
            if w[i] < limit: w[i] = 0 # if < limit set eval = 0
            else: w[i] = 1.0 / w[i] # prep for upcoming Ginv calculation                                                       
        # w[:, np.core.newaxis] gives transpose of vector, np.multiply is very fast element-wise multiplication
        Ginv = numpy.dot(v, numpy.multiply(w[:, numpy.core.newaxis], v.T)) 
        
        #Ginv=numpy.linalg.pinv(G, rcond=.01)  
        
        # compute decoder - least squares method 
        decoder = numpy.dot(Ginv, U) / (self.ensemble.neurons.dt)
        return decoder.astype('float32')

    def update(self, spikes):
        """The theano computation for converting neuron output into a decoded value
        Returns a dictionary with the decoded output value

        :param array spikes: theano object representing the instantaneous spike raster from the attached population
        """
        # multiply the output by the attached ensemble's radius to put us back in the right range
        return collections.OrderedDict( {self.decoded_output: TT.mul( TT.unbroadcast( TT.dot(spikes,self.decoder).reshape([self.dimensions]), 0), self.ensemble.radius).astype('float32')} ) 
        
