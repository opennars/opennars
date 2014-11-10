package nars.plugin.mental;

import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.Plugin;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.io.Symbols;
import nars.language.Term;
import nars.operator.Operation;

/**
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 * @param task The task processed
 */
public class MinimalInternalExperience implements Plugin {

    public Observer obs;
    
    @Override public boolean setEnabled(NAR n, boolean enabled) {
        Parameters.INTERNAL_EXPERIENCE_FULL=false;
        Memory memory = n.memory;
        
        Parameters.INTERNAL_EXPERIENCE=enabled;
        
        if(obs==null) {
            obs=new Observer() {
                @Override public void event(Class event, Object[] a) {

                    if (event!=Events.ConceptDirectProcessedTask.class)
                        return;

                    Task task = (Task)a[0];                

                    Term content = task.getContent();

                    // to prevent infinite recursions
                    if (content instanceof Operation/* ||  Memory.randomNumber.nextDouble()>Parameters.INTERNAL_EXPERIENCE_PROBABILITY*/)
                        return;

                    Sentence sentence = task.sentence;
                    TruthValue truth = new TruthValue(1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);

                    Stamp stamp = task.sentence.stamp.clone();
                    stamp.setOccurrenceTime(memory.time());

                    Sentence j = new Sentence(sentence.toTerm(memory), Symbols.JUDGMENT_MARK, truth, stamp);
                    BudgetValue newbudget=new BudgetValue(
                            Parameters.DEFAULT_JUDGMENT_CONFIDENCE*Parameters.INTERNAL_EXPERIENCE_PRIORITY_MUL,
                            Parameters.DEFAULT_JUDGMENT_PRIORITY*Parameters.INTERNAL_EXPERIENCE_DURABILITY_MUL, 
                            BudgetFunctions.truthToQuality(truth));

                    Task newTask = new Task(j, (BudgetValue) newbudget, 
                            Parameters.INTERNAL_EXPERIENCE_FULL ? null : task);

                    memory.addNewTask(newTask, "Remembered Action (Minimal Internal Experience)");

                }
            };
        }
        
        memory.event.set(obs, enabled, Events.ConceptDirectProcessedTask.class);
        return true;
    }
    
}
