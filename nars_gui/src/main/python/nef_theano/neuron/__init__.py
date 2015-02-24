from lif import LIFNeuron
from lif_rate import LIFRateNeuron

from neuron import accumulate

# a lookup table for the various neuron types available
names={'lif':LIFNeuron,
       'lif-rate':LIFRateNeuron,
      }
