package nars.operator.mental;

/**
 * (^simulateClone, cloneID, "some input", numCycles)  
 * clones the NAR, runs it for finite time, collecting its output asynchronously as inputs of the form:
 *    <(*, cloneID, [aTaskItOutputted] ) --> outputTask>. :|:
 * or maybe produces the complete result, upon finishing, as a sequence
 *    <(&/, outputTask1, outputTask2) --> cloneID>. :|:
 */
public class SimulateClone {
 
    //TODO
    
    //depends on a working NAR.clone()
    
}
