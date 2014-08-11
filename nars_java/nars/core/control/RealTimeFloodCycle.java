package nars.core.control;

/**

From: tony_lofthouse@btinternet.com 

Concepts are defined as agents, so can run in parallel. To keep the number of ‘active’ agents manageable I do a few  things; 1) concepts have a latency period and can’t be activated whilst they are latent. 2) The activation threshold is dynamic and is adjusted to keep the number of ‘active’ concepts within a manageable range. 3) Some concepts are inhibited (using negative truth values (0.5) as inhibitor). So each cycle all ‘eligible’ concepts are activated, cycles have a fixed time unit (parameter adjustable, currently 13 cycles/sec (Human alpha wave freq!)),  latency is 8 cycles.  Another difference is that I process more than one task and belief per concept per cycle. My system functions more like a neural net than NARS so has different dynamics.

Forgetting is a separate process which runs in parallel. When memory starts to get low, the process removes low ‘durability’ items. (My attention allocation is different to Pei’s). 

The latency period simulates the recovery period for neurons whereby a neuron cannot fire after previously firing for the specified recovery period.

Each concept records the systemTime when it was last activated. So a concept is latent if the current systemTime(in cycles) minus the last activation time (in cycles) is less then the latency period (e.g. 8 cycles). The latency period is a parameter so can be adjusted. 8 cycles just happen to have given me the best results on my current test data.

Each cycle 'All' concepts that are active and not latent fire. By adjusting the activation threshold this number can be quite small even in a very large network.

In an earlier version of the system I did use this approach with ConceptBags - however, extracting a non-latent concept was not very efficient. I had to TakeOut() then check the latency - this lead to quite a few misses before getting a usable concept.

The agent based approach in place now is much more efficient. Because every concept is an agent it decides whether it needs to fire. Concepts only have the potential to fire if they have received a new task. So again, this limits the number of concepts to fire each cycle.

In summary, each cycle, all new tasks are 'dispatched’’ to the relevant concepts and ‘all’ the concepts that are not latent and have an activation level above the dynamic threshold are fired. There is a final check on each concept so that it only fires once it has processed all of its agent messages (Tasks)
 * 
 */
public class RealTimeFloodCycle {
    
}
