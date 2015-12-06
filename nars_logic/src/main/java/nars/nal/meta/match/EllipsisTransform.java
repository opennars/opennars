package nars.nal.meta.match;

import nars.nal.nal4.Image;
import nars.nal.nal4.ShadowProduct;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.term.transform.FindSubst;
import nars.term.transform.VariableNormalization;

/** ellipsis that transforms one of its elements, which it is required to match within */
public class EllipsisTransform extends EllipsisOneOrMore {

    public final Term from;
    public final Term to;

    public EllipsisTransform(Variable name, Term from, Term to) {
        super(name, ".." + from + "=" + to + "..+");

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
    public Variable clone(Variable newVar, VariableNormalization normalizer) {
        throw new RuntimeException("HACK - this is handled by TaskRule.TaskRuleVariableNormalization");
    }

    public ShadowProduct matchRange(Compound x, Compound y, int a, int b, FindSubst subst) {
        if (from.equals(Image.Index) && (y instanceof Image)) {
            Image ii = (Image)y;
            int rel = ii.relationIndex;
            int n = (b-a)+1+1;
            int i = 0;
            int ab = 0;
            Term[] t = new Term[n];
            while (i < n)  {
                if (i == rel) {
                    t[i++] = subst.getXY(to);
                }
                Term yy = y.term(ab);
                subst.match(x.term(ab), yy);
                t[i++] = yy;
                ab++;
            }
            return new ShadowProduct(t);
        }

        return super.matchRange(x, y, a, b, subst);
    }
}
