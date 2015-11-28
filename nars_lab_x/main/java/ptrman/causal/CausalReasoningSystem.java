package ptrman.causal;

import java.util.ArrayList;
import java.util.Random;

public class CausalReasoningSystem
{
    public static void main(String[] args)
    {
        InputGraph inputGraph = new InputGraph();
        inputGraph.numberOfNodes = 8;
        inputGraph.connections.add(new InputGraph.Connection(0, 2));
        inputGraph.connections.add(new InputGraph.Connection(1, 2));
        inputGraph.connections.add(new InputGraph.Connection(1, 3));
        inputGraph.connections.add(new InputGraph.Connection(4, 3));
        inputGraph.connections.add(new InputGraph.Connection(5, 3));
        inputGraph.connections.add(new InputGraph.Connection(7, 3));
        inputGraph.connections.add(new InputGraph.Connection(4, 6));
        
        
        /* works
        InputGraph inputGraph = new InputGraph();
        inputGraph.numberOfNodes = 4;
        inputGraph.connections.add(new InputGraph.Connection(0, 3));
        inputGraph.connections.add(new InputGraph.Connection(1, 3));
        inputGraph.connections.add(new InputGraph.Connection(2, 3));
        */
        
        // TODO< cristal example >
        
        DecoratedCausalGraph causalGraph = ConvertInputGraphToCausalGraph.convert(inputGraph);
        
        ArrayList<Integer> result = TrackbackGenerator.generate(new Random(), causalGraph);
        
        int x = 0;
    }
    
}
