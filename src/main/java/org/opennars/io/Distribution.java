/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io;

import java.util.ArrayList;
import org.opennars.entity.Task;
import org.opennars.storage.Buffer;
import org.opennars.storage.InternalExperience;
import org.opennars.storage.Memory;
import org.opennars.main.Nar;
import org.opennars.plugin.perception.NarseseChannel;

/**
 *
 * @author Xiang
 */
public class Distribution {
    
    // The duration for the buffer
    private final int duration = 100; //Parameters.MAX_BUFFER_DURATION_FACTOR * Parameters.DURATION_FOR_INTERNAL_BUFFER;
    
    private ArrayList<Buffer> runTable;
        
    protected Nar reasoner;

    private final InternalExperience internalExperience;
    private final Buffer internalBuffer;
    private final Buffer overallBuffer;
    private final NarseseChannel narseseChannel = new NarseseChannel();
    
    public Distribution(Nar reasoner){
        
        this.reasoner = reasoner;
        System.out.println(duration);
        int levels = 10;
        int capacity = 50;
        internalBuffer = new InternalExperience(nar, levels, capacity, reasoner.narParameters);
        overallBuffer = new Buffer(nar, levels, capacity, reasoner.narParameters);
        initRunTable();
    }
    
    public Buffer getInternalBuffer(){
        return internalBuffer;
    }
    
    public Buffer getOverallBuffer(){
        return overallBuffer;
    }
    
    public void initRunTable(){
        runTable = new ArrayList<Buffer>();
        runTable.add(internalBuffer);
        runTable.add(sensorimotor_Experience);
        runTable.add(knowledge_Experience);
        runTable.add(narsese_Experience);
    }
    
    public void init(){
        internalBuffer = new InternalExperience(nar, levels, capacity, reasoner.narParameters);
        overallBuffer = new Buffer(nar, levels, capacity, reasoner.narParameters);
    }
    
    public void distributeWorkCycle(){
        
        for (int i = 0; i < runTable.size(); i++) {
                
            Task task = runTable.get(i).takeOut();
            
            if(task != null){
                overallBuffer.putIn(task);
            }
        }
    }
    
    public void inputNarseseTask(Task task){
                
        if(task.getBudget().aboveThreshold()){
            //memory.getRecorder().append("!!! Perceived: " + task + "\n");
            //memory.report(task.getSentence(), true);
            //task.getBudget().incPriority((float)0.1);
            narseseChannel.putIn(task);
            overallBuffer.putIn(narseseChannel.retrieve());
        }else{
            memory.getRecorder().append("!!! Neglected: " + task + "\n");
        }
    }
}
