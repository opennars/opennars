package ptrman.causal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// TODO< remove elements from workingset if it is written out ??? >
public class TrackbackGenerator
{
    static public ArrayList<Integer> generate(Random random, DecoratedCausalGraph causalGraph)
    {
        ArrayList<Integer> result;
        
        // list with all indices of nodes which can be added to the output
        // node could also be linked indirectly
        List<Integer> workingNodeIndices;
        
        result = new ArrayList<>();
        
        causalGraph.resetAnnotation();
        
        // add all root indices
        workingNodeIndices = causalGraph.getRootIndices();
        
        for(;;)
        {
            if( workingNodeIndices.size() == 0 )
            {
                break;
            }
            
            // get and remove random node from workingset
            int currentElementIndexIndex = random.nextInt(workingNodeIndices.size());
            int currentElementIndex = workingNodeIndices.get(currentElementIndexIndex);
            workingNodeIndices.remove(currentElementIndexIndex);

            assert !causalGraph.nodes.get(currentElementIndex).anotation.isInOutput();

            // walk all outgoing edges and remove red flag if its there,
            // if there is a redflag count the redflag counter down,
            // if the counter hits zero, add it to the workingNodes
            int outgoingEdgeI;

            for( outgoingEdgeI = 0; outgoingEdgeI < causalGraph.nodes.get(currentElementIndex).outgoingEdgeElementIndices.length; outgoingEdgeI++ )
            {
                boolean redFlagOfEdge;
                int outgoingNodeIndex;

                outgoingNodeIndex = causalGraph.nodes.get(currentElementIndex).outgoingEdgeElementIndices[outgoingEdgeI];

                int incommingEdgeElementIndicesIndex = getIndexOfElementInArray(causalGraph.nodes.get(outgoingNodeIndex).incommingEdgeElementIndices, currentElementIndex);

                redFlagOfEdge = causalGraph.nodes.get(outgoingNodeIndex).anotation.incommingEdgesRedFlags[incommingEdgeElementIndicesIndex];
                if( redFlagOfEdge )
                {
                    assert causalGraph.nodes.get(outgoingNodeIndex).anotation.incommingEdgesRedFlagsCounter > 0;

                    causalGraph.nodes.get(outgoingNodeIndex).anotation.incommingEdgesRedFlagsCounter--;
                    causalGraph.nodes.get(outgoingNodeIndex).anotation.incommingEdgesRedFlags[incommingEdgeElementIndicesIndex] = false;
                    if(
                        causalGraph.nodes.get(outgoingNodeIndex).anotation.incommingEdgesRedFlagsCounter == 0 &&
                        !causalGraph.nodes.get(outgoingNodeIndex).anotation.isOrWasInWorkingSet
                    )
                    {
                        causalGraph.nodes.get(outgoingNodeIndex).anotation.isOrWasInWorkingSet = true;

                        // add it to the workingNodes
                        workingNodeIndices.add(new Integer(outgoingNodeIndex));
                    }
                }
            }




            int outputIndex = result.size();
            causalGraph.nodes.get(currentElementIndex).anotation.outputIndex = outputIndex;
            result.add(new Integer(currentElementIndex));



            // repeat for all outgoing edges of the current element:
            //    mark all incomming edges of the outgoing nodes as red if they don't point at an element which is allready in the output

            for( outgoingEdgeI = 0; outgoingEdgeI < causalGraph.nodes.get(currentElementIndex).outgoingEdgeElementIndices.length; outgoingEdgeI++ )
            {
                int outgoingNodeIndex;
                int incommingEdgeI;

                outgoingNodeIndex = causalGraph.nodes.get(currentElementIndex).outgoingEdgeElementIndices[outgoingEdgeI];

                for( incommingEdgeI = 0; incommingEdgeI < causalGraph.nodes.get(outgoingNodeIndex).incommingEdgeElementIndices.length; incommingEdgeI++ )
                {
                    int reflectedNodeIndex;

                    reflectedNodeIndex = causalGraph.nodes.get(outgoingNodeIndex).incommingEdgeElementIndices[incommingEdgeI];

                    if( causalGraph.nodes.get(reflectedNodeIndex).anotation.isInOutput() )
                    {
                        continue;
                    }

                    causalGraph.nodes.get(outgoingNodeIndex).anotation.incommingEdgesRedFlags[incommingEdgeI] = true;
                }

                causalGraph.nodes.get(outgoingNodeIndex).anotation.recountIncommingRedFlags();
            }
            
            // now we add outgoing nodes if they don't have any red flaged incomming edges
            // (means that all nodes which point at that node are allready in the output)
            
            for( outgoingEdgeI = 0; outgoingEdgeI < causalGraph.nodes.get(currentElementIndex).outgoingEdgeElementIndices.length; outgoingEdgeI++ )
            {
                int outgoingNodeIndex;
                int incommingEdgeI;

                outgoingNodeIndex = causalGraph.nodes.get(currentElementIndex).outgoingEdgeElementIndices[outgoingEdgeI];
                
                if(
                    causalGraph.nodes.get(outgoingNodeIndex).anotation.incommingEdgesRedFlagsCounter == 0 &&
                    !causalGraph.nodes.get(outgoingNodeIndex).anotation.isOrWasInWorkingSet
                )
                {
                    causalGraph.nodes.get(outgoingNodeIndex).anotation.isOrWasInWorkingSet = true;
                    
                    workingNodeIndices.add(new Integer(outgoingNodeIndex));
                }
            }
            
        }
        
        // we must have the same amount of nodes in the graph and nodes in the result
        assert result.size() == causalGraph.nodes.size();
        
        return result;
    }
    
    static private int getIndexOfElementInArray(int[] array, int element)
    {
        int i;
        
        for( i = 0; i < array.length; i++ )
        {
            if( array[i] == element )
            {
                return i;
            }
        }
        
        throw new RuntimeException();
    }
}
