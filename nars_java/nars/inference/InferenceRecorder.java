package nars.inference;

import nars.entity.Concept;
import nars.entity.Task;

public interface InferenceRecorder {

    /**
     * Initialize the window and the file
     */
    public abstract void init();

    /**
     * Show the window
     */
    public abstract void show();

    /**
     * Begin the display
     */
    public abstract void play();

    /**
     * Stop the display
     */
    public abstract void stop();
    
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
    public void preCycle(long clock);

    public void postCycle(long clock);

    /** Added task */
    public void onTaskAdd(Task task, String reason);

    /** Neglected task */
    public void onTaskRemove(Task task, String reason);

}