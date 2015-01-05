package ptrman.causal;

import java.util.ArrayList;
import java.util.Random;

public class EnergyMinimizer
{
    public static class State
    {
        public DecoratedCausalGraph workingGraph; // graph which is being modified
        public DecoratedCausalGraph graphWithMinimalEnergy;
        public int minimalEnergy = Integer.MAX_VALUE;
        public ArrayList<Integer> minimalSequence;
    }
    
    public static void minimize(Random random, int numberOfSteps, State state) throws Exception
    {
        int step;
        
        for( step = 0; step < numberOfSteps; step++ )
        {
            minimizeSingleStep(random, state);
        }
    }
    
    private static void minimizeSingleStep(Random random, State state) throws Exception
    {
        ArrayList<Integer> potentialMinimalSequence;
        
        potentialMinimalSequence = TrackbackGenerator.generate(random, state.workingGraph);
        state.workingGraph.updateEnergy();
        
        if( state.workingGraph.energy < state.minimalEnergy )
        {
            state.minimalEnergy = state.workingGraph.energy;
            state.graphWithMinimalEnergy = state.workingGraph.clone();
            state.minimalSequence = potentialMinimalSequence;
        }
    }
}
