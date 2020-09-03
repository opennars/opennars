/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io;

import java.util.ArrayList;
import org.opennars.entity.Task;
import org.opennars.storage.Buffer;
import org.opennars.storage.Experience_From_Knowledge;
import org.opennars.storage.Experience_From_Narsese;
import org.opennars.storage.Experience_From_Sensorimotor;
import org.opennars.storage.Memory;

/**
 *
 * @author Xiang
 */
public class Distribution {
    
    // The duration for the buffer
    private final int duration = Parameters.MAX_BUFFER_DURATION_FACTOR * Parameters.DURATION_FOR_INTERNAL_BUFFER;
    
    private ArrayList<Buffer> runTable;
        
    protected ReasonerBatch reasoner;
    
    protected Memory memory;
    
    private final Experience_From_Narsese narsese_Experience;
    private final Experience_From_Knowledge knowledge_Experience;
    private final Experience_From_Sensorimotor sensorimotor_Experience ;
    private final Buffer internalBuffer;
    private final Buffer overallBuffer;
    
    public Distribution(ReasonerBatch reasoner, Memory memory){
        
        this.reasoner = reasoner;
        this.memory = memory;
        System.out.println(duration);
        narsese_Experience = new Experience_From_Narsese(memory, duration);
        sensorimotor_Experience = new Experience_From_Sensorimotor(memory, duration);
        knowledge_Experience = new Experience_From_Knowledge(memory, duration);
        
        internalBuffer = new Buffer(memory, duration, "internal");
        overallBuffer = new Buffer(memory, duration, "overall");
        
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
        overallBuffer.init();
        internalBuffer.init();
        narsese_Experience.init();
        sensorimotor_Experience.init();
        knowledge_Experience.init();
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
            memory.getRecorder().append("!!! Perceived: " + task + "\n");
            memory.report(task.getSentence(), true);
            task.getBudget().incPriority((float)0.1);
            narsese_Experience.putIn(task);
        }else{
            memory.getRecorder().append("!!! Neglected: " + task + "\n");
        }
    }
}
