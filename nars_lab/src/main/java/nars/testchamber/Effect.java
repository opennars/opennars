package nars.testchamber;


/*
    The result of an action, returned by the game engine to an agent.
*/
public class Effect {
    public final Action action;
    public final String description;
    public final boolean success;
    public final long when;
    
    public Effect(Action a, boolean success, long when, String description) {
        this.when = when;
        action = a;
        this.success = success;
        this.description = description;        
    }

    public Effect(Action a, boolean success, long when) {
        action = a;
        this.when = when;
        this.success = success;
        description = null;
    }

    @Override
    public String toString() {
        String a = action.getClass().getSimpleName() + ' ' + (success ? "SUCCESS" : "FAIL") + " @" + when;
        if (description!=null)
            a += ": " + description;
        return a;
    }
    
    
    
}
