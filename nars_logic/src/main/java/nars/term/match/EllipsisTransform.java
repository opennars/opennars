package nars.term.match;

import nars.Op;
import nars.nal.PremiseMatch;
import nars.nal.PremiseRule;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.transform.VariableNormalization;
import nars.term.variable.Variable;

/** ellipsis that transforms one of its elements, which it is required to match within */
public class EllipsisTransform extends EllipsisOneOrMore {

    public final Term from;
    public final Term to;

    public EllipsisTransform(Variable name, Term from, Term to) {
        super(name, ".." + from + '=' + to + "..+");

//        if (from instanceof VarPattern)
//            this.from = new VarPattern(((VarPattern) from).id);
//        else
          this.from = from;
//
//        if (from instanceof VarPattern)
//            this.to = new VarPattern(((VarPattern) to).id);
//        else
          this.to = to;
    }

    @Override
    public Variable normalize(int serial) {
        //handled in a special way elsewhere
        return this;
    }

    @Override
    public Variable clone(Variable v, VariableNormalization normalizer) {
        //normalizes any variable parameter terms of an EllipsisTransform
        PremiseRule.PremiseRuleVariableNormalization vnn = (PremiseRule.PremiseRuleVariableNormalization) normalizer;
        return new EllipsisTransform(v,
                from instanceof Variable ? vnn.applyAfter((Variable)from) : from,
                to instanceof Variable ? vnn.applyAfter((Variable)to) : to);
    }

    public EllipsisMatch collect(Compound y, int a, int b, PremiseMatch subst) {
        if (from.equals(Op.Imdex) && (y.op().isImage())) {

            int rel = y.relation();
            int n = (b-a)+1;
            int i = 0;
            int ab = 0;
            Term[] t = new Term[n];
            Term to = this.to;
            while (i < n)  {
                t[i++] = ((i == rel) ? subst.apply(to) : y.term(ab));
                ab++;
            }
            return new EllipsisMatch(t);

        } else {
            return new EllipsisMatch(
                    y, a, b
            );
        }
    }
}
