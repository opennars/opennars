package nars.meter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nars.meter.condition.TaskCondition;
import nars.task.DefaultTask;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.Compound;

import java.io.PrintStream;
import java.util.Map;

/** wrapper class for a Task that holds its explained meaning in zero
 *  or more natural languages. */
public class ExplainableTask extends DefaultTask {

    public final Multimap<String,String> means = HashMultimap.create();

    /** the Task instance that was input to the reasoner */
    public final Task task;


    public ExplainableTask(TaskCondition tc) {
        this(TaskSeed.make(tc.getMemory(), (Compound) tc.term).budget(0,0,0).punctuation(tc.punc)
            .truth(tc.getTruthMean()).time(tc.getCreationTime(),
            Stamp.getOccurrenceTime(tc.getCreationTime(), tc.tense, tc.getMemory()))

        );
    }

    @Deprecated public ExplainableTask(Task<? extends Compound> s) {
        super(s.getTerm(), s.getPunctuation(), s.getTruth(), s.getPriority(), s.getDuration(), s.getQuality());
        this.task = s;
    }

    public ExplainableTask en(String englishMeaning) {
        means.put("en", englishMeaning);
        return this;
    }
    public ExplainableTask es(String spanishMeaning) {
        means.put("es", spanishMeaning);
        return this;
    }
    public ExplainableTask de(String germanMeaning) {
        means.put("de", germanMeaning);
        return this;
    }
    public ExplainableTask cn(String chineseMeaning) {
        means.put("cn", chineseMeaning);
        return this;
    }


    public void printMeaning(PrintStream p) {
        if (means.isEmpty()) return;

        p.println(this);

        for (Map.Entry<String,String> x : means.entries()) {
            p.println("  " + x.getKey() + ": " + x.getValue());
        }
    }
}
