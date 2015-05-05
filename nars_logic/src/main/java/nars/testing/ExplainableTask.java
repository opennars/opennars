package nars.testing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nars.testing.condition.TaskCondition;
import nars.nal.Sentence;
import nars.nal.stamp.Stamp;
import nars.nal.Task;

import java.io.PrintStream;
import java.util.Map;

/** wrapper class for a Task that holds its explained meaning in zero
 *  or more natural languages. */
public class ExplainableTask extends Task {

    public final Multimap<String,String> means = HashMultimap.create();

    /** the Task instance that was input to the reasoner */
    public final Task task;

    ExplainableTask(Sentence s) {
        super(s, null);
        this.task = null;
    }

    public ExplainableTask(TaskCondition tc) {
        this(new Sentence(tc.term, tc.punc, tc.getTruthMean(), new Stamp(tc.nar.memory, tc.getCreationTime(), tc.tense)));
    }

    public ExplainableTask(Task t) {
        super(t.sentence, t);
        this.task = t;
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

        p.println(sentence);

        for (Map.Entry<String,String> x : means.entries()) {
            p.println("  " + x.getKey() + ": " + x.getValue());
        }
    }
}
