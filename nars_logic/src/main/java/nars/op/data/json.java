package nars.op.data;

import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.TermFunction;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.util.io.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by me on 5/18/15.
 */
public class json {

    public static class jsonto extends TermFunction {

        @Override
        public Object function(Operation o) {
            String j = Atom.unquote(o.arg(0));
            Map<String, Object> jj = JSON.toMap(j);
            if (jj==null) return null;

            return termize(jj);
        }


        public static Term termize(Object x) {
            if (x instanceof Map) {
                Map<String, Object> jj = (Map<String, Object>) x;
                List<Term> tt = new ArrayList();

                for (Map.Entry<String,Object> e : jj.entrySet()) {
                    String key = e.getKey();
                    Object data = e.getValue();
                    tt.add(Product.make(Atom.the(key), termize(data)));
                }

                Compound s = SetExt.make(tt);
                return s;
            }
            else if (x instanceof String) {
                return Atom.the((String) x);
            }
            else /*if (x instanceof Number)*/ {
                return Atom.the("\"" + x.toString() + "\"");
            }
        }

    }
    public static class jsonfrom extends TermFunction {

        @Override
        public Object function(Operation o) {
            return Atom.quote(JSON.stringFrom(o.arg(0)));
        }

    }


}
