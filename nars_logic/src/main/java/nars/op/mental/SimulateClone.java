package nars.op.mental;

/**
 * (^simulateClone, cloneID, "some input", numCycles) clones the NAR, runs it
 * for finite time, collecting its output asynchronously as inputs of the form:
 * <(*, cloneID, [aTaskItOutputted] ) --> outputTask>. :|: or maybe produces the
 * complete result, upon finishing, as a sequence <(&/, outputTask1,
 * outputTask2) --> cloneID>. :|:
 * 
 * ^believe is irreversible but a cloned belief system would allow to see what
 * would happen
 * 
 * hypothesize
 * 
 * it could even make a low-resolution / low-fidelity clone, removing weak
 * confidence and low priority items like its dreaming making a model of itself
 */
public class SimulateClone {

	// TODO

	// depends on a working NAR.clone(), NAR.clone(resolution,filter,...)

}
