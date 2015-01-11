package nars.johkra;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Johannes Krampf <johkra@gmail.com>
 * Date: 06.02.11
 */
public class Rule {

    private Term head;
    private ArrayList<Term> goals;

    public Rule(String rule) throws ParseException {
        List<String> flds = Util.split(rule, ":-", false);
        head = new Term(flds.get(0), null);
        goals = new ArrayList<Term>();

        if (flds.size() == 2) {
            flds = Util.split(flds.get(1), ",", true);
            for (String fld : flds) {
                goals.add(new Term(fld, null));
            }
        }
    }

    private Rule() {

    }

    public Term getHead() {
        return head;
    }

    public List<Term> getGoals() {
        return goals;
    }

    public void setGoals(ArrayList<Term> goals) {
        this.goals = goals;
    }

    @Override
    public Rule clone() {
        Rule clone = new Rule();
        clone.head = head;
        clone.goals = new ArrayList<Term>(goals);
        return clone;
    }

    @Override
    public String toString() {
        String goalsString = "";
        for (int i = 0; i < goals.size(); i++) {
            if (i != 0) {
                goalsString += ", ";
            }
            goalsString += goals.get(i);
        }
        return head + " :- " + goalsString;
    }
}
