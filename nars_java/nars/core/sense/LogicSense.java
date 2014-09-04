package nars.core.sense;

import java.io.Serializable;
import nars.core.Memory;
import nars.util.meter.data.DataSet;
import nars.util.meter.sensor.EventValueSensor;
import nars.util.meter.sensor.HitPeriodTracker;

/**
 *
 * @author me
 */


public class LogicSense extends AbstractSense implements Serializable {
    
    //public final Sensor CONCEPT_FIRE;
    
    public final EventValueSensor TASK_IMMEDIATE_PROCESS_PRIORITY;
    public final HitPeriodTracker TASK_IMMEDIATE_PROCESS;
    
    public final EventValueSensor TASKLINK_FIRE;
    public final EventValueSensor TASKTERMLINK_REASON; //both duration and count
    
    public final EventValueSensor OUTPUT_TASK;
    
    public final EventValueSensor CONCEPT_NEW;
    
    
    //public final ThreadBlockTimeTracker CYCLE_BLOCK_TIME;
    private long conceptNum;
    private double conceptPriorityMean;
    //private Object conceptPrioritySum;
    private long conceptBeliefsSum;
    private long conceptQuestionsSum;               

    public LogicSense() {
        super();
        
        add(TASK_IMMEDIATE_PROCESS = new HitPeriodTracker("task.immediate_process"));
        add(TASK_IMMEDIATE_PROCESS_PRIORITY = new EventValueSensor("task.immediate_process.priority"));

                
        //add(CONCEPT_FIRE = new DefaultEventSensor("concept.fire"));
        add(TASKLINK_FIRE = new EventValueSensor("tasklink.fire"));
        TASKLINK_FIRE.setSampleWindow(32);
        
        add(OUTPUT_TASK = new EventValueSensor("output.task"));
        OUTPUT_TASK.setSampleWindow(32);

        add(CONCEPT_NEW = new EventValueSensor("concept.new"));
        CONCEPT_NEW.setSampleWindow(32);
        
        add(TASKTERMLINK_REASON = new EventValueSensor("tasklink.reason"));
        TASKTERMLINK_REASON.setSampleWindow(32);
    }
    
    @Override
    public void sense(Memory memory) {
        put("concepts.count", conceptNum);
        put("concepts.priority.mean", conceptPriorityMean);        
        put("concepts.beliefs.sum", conceptBeliefsSum);
        put("concepts.questions.sum", conceptQuestionsSum);
        
        put("memory.noveltasks.total", memory.novelTasks.size());
        //put("memory.newtasks.total", memory.newTasks.size()); //redundant with output.tasks below

        //TODO move to EmotionState
        put("emotion.happy", memory.emotion.happy());
        put("emotion.busy", memory.emotion.busy());

        
        {
            DataSet fire = TASKLINK_FIRE.get();
            //DataSet reason = TASKLINK_REASON.get();
            put("reason.fire.tasklink.priority.mean", fire.mean());
            put("reason.fire.tasklinks.delta", TASKLINK_FIRE.getDeltaHits());
            
            //only makes sense as a mean, since it occurs multiple times during a cycle
            put("reason.tasktermlink.priority.mean", TASKTERMLINK_REASON.get().mean());
            
            put("reason.tasktermlinks", TASKTERMLINK_REASON.getHits());
        }
        {
            put("output.tasks", OUTPUT_TASK.getHits());
            put("output.tasks.budget.mean", OUTPUT_TASK.get().mean());
        }
        {
            put("concept.new", CONCEPT_NEW.getHits());
            put("concept.new.complexity.mean", CONCEPT_NEW.get().mean());
        }        
    }

    public void setConceptBeliefsSum(long conceptBeliefsSum) {
        this.conceptBeliefsSum = conceptBeliefsSum;
    }

    public void setConceptNum(long conceptNum) {
        this.conceptNum = conceptNum;
    }

    public void setConceptPriorityMean(double conceptPriorityMean) {
        this.conceptPriorityMean = conceptPriorityMean;
    }

//    public void setConceptPrioritySum(double conceptPrioritySum) {
//        this.conceptPrioritySum = conceptPrioritySum;
//    }

    public void setConceptQuestionsSum(long conceptQuestionsSum) {
        this.conceptQuestionsSum = conceptQuestionsSum;
    }
    
    
}
