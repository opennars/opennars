package nars.inference;

import nars.entity.Concept;
import nars.entity.Task;

public interface InferenceRecorder {


    public boolean isActive();

    /**
     * Add new text to display
     *
     * @param s The line to be displayed
     */
    public void append(String s);

    /** when a concept is instantiated */
    public void onConceptNew(Concept concept);

    /** called at the beginning of each inference clock cycle */
    public void onCycleStart(long clock);

    /** called at the end of each inference clock cycle */
    public void onCycleEnd(long clock);

    /** Added task */
    public void onTaskAdd(Task task, String reason);

    /** Neglected task */
    public void onTaskRemove(Task task, String reason);

}