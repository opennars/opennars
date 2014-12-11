package ptrman.causal;

import java.util.ArrayList;

public class InputGraph
{
    public static class Connection
    {
        public int sourceIndex;
        public int destinationIndex;
        
        public Connection(int sourceIndex, int destinationIndex)
        {
            this.sourceIndex = sourceIndex;
            this.destinationIndex = destinationIndex;
        }
    }
    
    public ArrayList<Connection> connections = new ArrayList<InputGraph.Connection>();
    
    public int numberOfNodes;
}
