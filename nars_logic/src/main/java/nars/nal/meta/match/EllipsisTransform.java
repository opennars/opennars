package nars.nal.meta.match;

import nars.nal.nal4.Image;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.transform.FindSubst;
import nars.term.transform.VariableNormalization;
import nars.term.variable.Variable;

/** ellipsis that transforms one of its elements, which it is required to match within */
public class EllipsisTransform extends EllipsisOneOrMore {

    public Term from;
    public Term to;

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
    public Variable clone(Variable v, VariableNormalization normalizer) {
        //normalizes any variable parameter terms of an EllipsisTransform
        return new EllipsisTransform(v,
                from instanceof Variable ? normalizer.apply((Variable)from) : from,
                to instanceof Variable ? normalizer.apply((Variable)to) : to);
    }

    public ArrayEllipsisMatch collect(Compound y, int a, int b, FindSubst subst) {
        if (from.equals(Image.Index) && (y instanceof Image)) {
            Image ii = (Image)y;
            int rel = ii.relationIndex;
            int n = (b-a)+1;
            int i = 0;
            int ab = 0;
            Term[] t = new Term[n];
            while (i < n)  {
                if (i == rel) {
                    t[i++] = subst.resolve(to);
                }
                else {
                    t[i++] = y.term(ab);
                }
                ab++;
            }
            return new ArrayEllipsisMatch(t);
        }

        return new ArrayEllipsisMatch(
                y, a, b
        );
    }
}
