package nars.johkra;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Johannes Krampf <johkra@gmail.com>
 * Date: 06.02.11
 */
public final class Rule extends Pair<Term,List<Term>> {
    

    public static Rule make(String rule) throws ParseException {
        List<String> flds = Util.split(rule,":-", false);
        Term head = new Term(flds.get(0), null);
        ArrayList<Term> goals = new ArrayList<Term>();

        if (flds.size() == 2) {
            flds = Util.split(flds.get(1),",",true);
            for (String fld : flds) {
                goals.add(new Term(fld, null));
            }
        }
        
        return new Rule(head, goals);
    }

    private Rule(Term head, List<Term> goals) {
        super(head, goals);

    }

    public Term getHead() {
        return a();
    }

    public List<Term> getGoals() {
        return b();
    }

    public void setGoal(Term g) {
        getGoals().clear();
        getGoals().add(g);
    }

    public Rule clone() {
        return new Rule(getHead(), new ArrayList(getGoals()));
    }

    @Override
    public String toString() {
        String goalsString = "";
        for(int i = 0; i < getGoals().size(); i++) {
            if (i != 0) {
                goalsString += ", ";
            }
            goalsString += getGoals().get(i);
        }
        return getHead() + " :- " + goalsString;
    }
}
