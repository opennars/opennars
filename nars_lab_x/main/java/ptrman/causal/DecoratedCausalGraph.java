package ptrman.causal;

import java.util.ArrayList;
import java.util.Arrays;

public class DecoratedCausalGraph
{
    public static class Node
    {
        public static class Anotation
        {
            public boolean isOrWasInWorkingSet; // is/was this node in the working set for ttraversal?
            
            // flags used to indicate that the node of the incomming edge must occur before that node
            // flag is not set for elements which are allready in the output
            public boolean[] incommingEdgesRedFlags;
            
            public int incommingEdgesRedFlagsCounter;
            
            public void recountIncommingRedFlags()
            {
                int incommingEdgeI;
                
                incommingEdgesRedFlagsCounter = 0;
                
                for( incommingEdgeI = 0; incommingEdgeI < incommingEdgesRedFlags.length; incommingEdgeI++ )
                {
                    if( incommingEdgesRedFlags[incommingEdgeI] )
                    {
                        incommingEdgesRedFlagsCounter++;
                    }
                }
            }
            
            public int outputIndex = -1;
            
            public boolean isInOutput()
            {
                return outputIndex != -1;
            }
            
            public Anotation clone()
            {
                Anotation cloned;
                
                cloned = new Anotation();
                cloned.outputIndex = outputIndex;
                cloned.incommingEdgesRedFlagsCounter = incommingEdgesRedFlagsCounter;
                cloned.incommingEdgesRedFlags = incommingEdgesRedFlags;
                cloned.incommingEdgesRedFlags = Arrays.copyOf(incommingEdgesRedFlags, incommingEdgesRedFlags.length);
                cloned.isOrWasInWorkingSet = isOrWasInWorkingSet;
                
                return cloned;
            }
        }
        
        public Anotation anotation = new Anotation();
        
        public int[] outgoingEdgeElementIndices;
        public int[] incommingEdgeElementIndices;
        
        
        
        public boolean isRoot()
        {
            return incommingEdgeElementIndices.length == 0;
        }
        
        public Node clone()
        {
            Node cloned;
            
            cloned = new Node();
            
            cloned.outgoingEdgeElementIndices = Arrays.copyOf(outgoingEdgeElementIndices, outgoingEdgeElementIndices.length);
            cloned.incommingEdgeElementIndices = Arrays.copyOf(incommingEdgeElementIndices, outgoingEdgeElementIndices.length);
            cloned.anotation = anotation.clone();
            
            return cloned;
        }
        
    }
    
    public int energy = Integer.MAX_VALUE;
    
    public ArrayList<Node> nodes = new ArrayList<Node>();
    
    public void resetAnnotation()
    {
        int i;
        
        for( i = 0; i < nodes.size(); i++ )
        {
            nodes.get(i).anotation = new Node.Anotation();
            
            int incommingEdgesArrayLength = nodes.get(i).incommingEdgeElementIndices.length;
            nodes.get(i).anotation.incommingEdgesRedFlags = new boolean[incommingEdgesArrayLength];
        }
    }
    
    public void updateEnergy() throws Exception
    {
        /*
         * for each DDNode
         *  * calculate the energy(distance) to all outgoing nodes and add it to energy
         * 
         */
        
        energy = 0;
        
        for( Node iterationNode : nodes )
        {
            int currentNodeIndex;
            
            currentNodeIndex = iterationNode.anotation.outputIndex;
            
            for( int iterationOutgoingElementIndex : iterationNode.outgoingEdgeElementIndices )
            {
                int outputNodeIndex;
                
                outputNodeIndex = nodes.get(iterationOutgoingElementIndex).anotation.outputIndex;
                
                // assert
                if( currentNodeIndex > outputNodeIndex )
                {
                    throw new Exception();
                }
                
                energy += (outputNodeIndex - currentNodeIndex - 1);
            }
        }
    }
    
    public ArrayList<Integer> getRootIndices()
    {
        ArrayList<Integer> result;
        int i;
        
        result = new ArrayList<Integer>();
        
        i = 0;
        for( DecoratedCausalGraph.Node iterationNode : nodes )
        {
            if( iterationNode.isRoot() )
            {
                result.add(new Integer(i));
            }
            
            i++;
        }
        
        return result;
    }
    
    /** does make a deep copy
     * 
     */
    public DecoratedCausalGraph clone()
    {
        DecoratedCausalGraph cloned;
        
        cloned = new DecoratedCausalGraph();
        cloned.energy = energy;
        
        for( int i = 0; i < nodes.size(); i++ )
        {
            Node clonedNode;
            
            clonedNode = nodes.get(i).clone();
            
            cloned.nodes.add(clonedNode);
        }
        
        return cloned;
    }
}
