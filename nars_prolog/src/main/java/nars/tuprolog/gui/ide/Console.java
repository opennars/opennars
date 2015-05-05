package nars.tuprolog.gui.ide;

/**
 * @author  ale
 */
public interface Console
{
    public boolean hasOpenAlternatives();
    public void enableTheoryCommands(boolean flag);
    public void getNextSolution();
    public void acceptSolution();
    public void stopEngine();
    /**
	 * @uml.property  name="goal"
	 */
    public String getGoal();
}