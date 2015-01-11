package nars.johkra;

import java.util.HashMap;

/**
 * User: Johannes Krampf <johkra@gmail.com>
 * Date: 07.02.11
 */
public final class Goal {
    private Rule rule;
    private Goal parent;
    private HashMap<String, Term> env;
    private int inx;

    public Goal(Rule rule, Goal parent) {
        this.rule = rule;
        this.parent = parent;
        this.env = new HashMap<String, Term>();
        this.inx = 0;
    }

    public Goal(Rule rule, Goal parent, HashMap<String, Term> env) {
        this.rule = rule;
        this.parent = parent;
        this.env = env;
        this.inx = 0;
    }

    private Goal() {
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Goal getParent() {
        return parent;
    }

    public void setParent(Goal parent) {
        this.parent = parent;
    }

    public HashMap<String, Term> getEnv() {
        return env;
    }

    public int getInx() {
        return inx;
    }

    public void setInx(int inx) {
        this.inx = inx;
    }

    public Goal clone() {
        Goal clone = new Goal();
        clone.rule = rule.clone();
        clone.parent = parent;
        clone.env = new HashMap<String, Term>(env);
        clone.inx = inx;
        return clone;
    }

    @Override
    public String toString() {
        return "Goal{" + rule +
                "; parent=" + (parent != null) +
                ", inx=" + inx +
                ", env=" + env +
                '}';
    }
}
